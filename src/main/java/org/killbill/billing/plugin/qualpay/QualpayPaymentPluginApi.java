/*
 * Copyright 2014-2019 Groupon, Inc
 * Copyright 2014-2019 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.qualpay;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.money.CurrencyUnit;
import org.joda.time.DateTime;
import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillLogService;
import org.killbill.billing.payment.api.PaymentApiException;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.GatewayNotification;
import org.killbill.billing.payment.plugin.api.HostedPaymentPageFormDescriptor;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.core.PluginCustomField;
import org.killbill.billing.plugin.api.payment.PluginPaymentMethodInfoPlugin;
import org.killbill.billing.plugin.api.payment.PluginPaymentPluginApi;
import org.killbill.billing.plugin.qualpay.client.PGApi;
import org.killbill.billing.plugin.qualpay.client.PGApiCaptureRequest;
import org.killbill.billing.plugin.qualpay.client.PGApiLineItem;
import org.killbill.billing.plugin.qualpay.client.PGApiRefundRequest;
import org.killbill.billing.plugin.qualpay.client.PGApiTransactionRequest;
import org.killbill.billing.plugin.qualpay.client.PGApiVoidRequest;
import org.killbill.billing.plugin.qualpay.dao.QualpayDao;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayPaymentMethods;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayResponses;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.records.QualpayPaymentMethodsRecord;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.records.QualpayResponsesRecord;
import org.killbill.billing.util.api.CustomFieldApiException;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.customfield.CustomField;
import org.killbill.clock.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import io.swagger.client.api.CustomerVaultApi;
import io.swagger.client.model.AddBillingCardRequest;
import io.swagger.client.model.AddCustomerRequest;
import io.swagger.client.model.BillingCard;
import io.swagger.client.model.CustomerResponse;
import io.swagger.client.model.CustomerVault;
import io.swagger.client.model.DeleteBillingCardRequest;
import io.swagger.client.model.GatewayResponse;
import io.swagger.client.model.GetBillingCardsResponse;
import io.swagger.client.model.GetBillingResponse;
import qpPlatform.ApiClient;
import qpPlatform.ApiException;
import qpPlatform.Configuration;

public class QualpayPaymentPluginApi extends PluginPaymentPluginApi<QualpayResponsesRecord, QualpayResponses, QualpayPaymentMethodsRecord, QualpayPaymentMethods> {

    private static final Logger logger = LoggerFactory.getLogger(QualpayPaymentPluginApi.class);

    public static final String PROPERTY_OVERRIDDEN_TRANSACTION_STATUS = "overriddenTransactionStatus";

    private final QualpayConfigPropertiesConfigurationHandler qualpayConfigPropertiesConfigurationHandler;
    private final QualpayDao dao;

    public QualpayPaymentPluginApi(final QualpayConfigPropertiesConfigurationHandler qualpayConfigPropertiesConfigurationHandler,
                                   final OSGIKillbillAPI killbillAPI,
                                   final OSGIConfigPropertiesService configProperties,
                                   final OSGIKillbillLogService logService,
                                   final Clock clock,
                                   final QualpayDao dao) {
        super(killbillAPI, configProperties, logService, clock, dao);
        this.qualpayConfigPropertiesConfigurationHandler = qualpayConfigPropertiesConfigurationHandler;
        this.dao = dao;
    }

    @Override
    protected PaymentTransactionInfoPlugin buildPaymentTransactionInfoPlugin(final QualpayResponsesRecord record) {
        return QualpayPaymentTransactionInfoPlugin.build(record);
    }

    @Override
    protected PaymentMethodPlugin buildPaymentMethodPlugin(final QualpayPaymentMethodsRecord record) {
        return QualpayPaymentMethodPlugin.build(record);
    }

    @Override
    protected PaymentMethodInfoPlugin buildPaymentMethodInfoPlugin(final QualpayPaymentMethodsRecord record) {
        return new PluginPaymentMethodInfoPlugin(UUID.fromString(record.getKbAccountId()),
                                                 UUID.fromString(record.getKbPaymentMethodId()),
                                                 false,
                                                 record.getQualpayId());
    }

    @Override
    public void addPaymentMethod(final UUID kbAccountId,
                                 final UUID kbPaymentMethodId,
                                 final PaymentMethodPlugin paymentMethodProps,
                                 final boolean setDefault,
                                 final Iterable<PluginProperty> properties,
                                 final CallContext context) throws PaymentPluginApiException {
        final String qualpayCustomerIdMaybeNull = getCustomerIdNoException(kbAccountId, context);
        final String cardIdMaybeNull = PluginProperties.findPluginPropertyValue("card_id", properties);

        final String qualpayId;
        if (qualpayCustomerIdMaybeNull != null && paymentMethodProps.getExternalPaymentMethodId() != null) {
            // The customer and payment method already exist (sync code path), we just need to update our tables
            qualpayId = paymentMethodProps.getExternalPaymentMethodId();
        } else if (qualpayCustomerIdMaybeNull == null && paymentMethodProps.getExternalPaymentMethodId() != null) {
            // Invalid sync path
            throw new PaymentPluginApiException("USER", "Specified Qualpay card id but missing QUALPAY_CUSTOMER_ID custom field");
        } else if (cardIdMaybeNull != null) {
            // Card was tokenized via the Payment Gateway API - we will simply store the card locally
            qualpayId = cardIdMaybeNull;
        } else {
            // We need to create a new payment method, either on a new customer or on an existing one (for testing or for companies with a tokenization proxy)
            final ApiClient apiClient = buildApiClient(context, true);
            final CustomerVaultApi customerVaultApi = new CustomerVaultApi(apiClient);

            final AddBillingCardRequest billingCardsItem = new AddBillingCardRequest();
            billingCardsItem.setCardNumber(PluginProperties.findPluginPropertyValue("card_number", properties));
            billingCardsItem.setExpDate(PluginProperties.findPluginPropertyValue("exp_date", properties));
            billingCardsItem.setCvv2(PluginProperties.findPluginPropertyValue("cvv2", properties));
            billingCardsItem.setBillingFirstName(PluginProperties.findPluginPropertyValue("billing_first_name", properties));
            billingCardsItem.setBillingLastName(PluginProperties.findPluginPropertyValue("billing_last_name", properties));
            billingCardsItem.setBillingFirmName(PluginProperties.findPluginPropertyValue("billing_firm_name", properties));
            billingCardsItem.setBillingZip(PluginProperties.findPluginPropertyValue("billing_zip", properties));

            try {
                if (qualpayCustomerIdMaybeNull == null) {
                    // Create customer and payment method
                    final AddCustomerRequest addCustomerRequest = new AddCustomerRequest();
                    addCustomerRequest.setAutoGenerateCustomerId(true);
                    addCustomerRequest.addBillingCardsItem(billingCardsItem);
                    final String customerFirstName = PluginProperties.findPluginPropertyValue("customer_first_name", properties);
                    addCustomerRequest.setCustomerFirstName(customerFirstName != null ? customerFirstName : billingCardsItem.getBillingFirstName());
                    final String customerLastName = PluginProperties.findPluginPropertyValue("customer_last_name", properties);
                    addCustomerRequest.setCustomerLastName(customerLastName != null ? customerLastName : billingCardsItem.getBillingLastName());
                    final String customerFirmName = PluginProperties.findPluginPropertyValue("customer_firm_name", properties);
                    addCustomerRequest.setCustomerFirmName(customerFirmName != null ? customerFirmName : billingCardsItem.getBillingFirmName());
                    final CustomerVault customerVault = customerVaultApi.addCustomer(addCustomerRequest).getData();
                    // TODO Guaranteed it's the last one?
                    final BillingCard createdBillingCard = customerVault.getBillingCards().get(customerVault.getBillingCards().size() - 1);
                    qualpayId = createdBillingCard.getCardId();

                    // Add the magic Custom Field
                    final PluginCustomField customField = new PluginCustomField(kbAccountId,
                                                                                ObjectType.ACCOUNT,
                                                                                "QUALPAY_CUSTOMER_ID",
                                                                                customerVault.getCustomerId(),
                                                                                clock.getUTCNow());
                    try {
                        final QualpayConfigProperties qualpayConfigProperties = qualpayConfigPropertiesConfigurationHandler.getConfigurable(context.getTenantId());
                        killbillAPI.getSecurityApi().login(qualpayConfigProperties.getKbUsername(), qualpayConfigProperties.getKbPassword());
                        killbillAPI.getCustomFieldUserApi().addCustomFields(ImmutableList.<CustomField>of(customField), context);
                    } finally {
                        killbillAPI.getSecurityApi().logout();
                    }
                } else {
                    // Add payment method to existing customer
                    final CustomerResponse customerResponse = customerVaultApi.addBillingCard(qualpayCustomerIdMaybeNull, billingCardsItem);
                    // TODO Guaranteed it's the last one?
                    qualpayId = customerResponse.getData().getBillingCards().get(customerResponse.getData().getBillingCards().size() - 1).getCardId();
                }
            } catch (final ApiException e) {
                throw new PaymentPluginApiException("Error connecting to Qualpay: " + e.getResponseBody(), e);
            } catch (final CustomFieldApiException e) {
                throw new PaymentPluginApiException("Error adding custom field", e);
            }
        }

        final Map<String, Object> additionalDataMap = PluginProperties.toMap(properties);
        final DateTime utcNow = clock.getUTCNow();
        try {
            dao.addPaymentMethod(kbAccountId, kbPaymentMethodId, additionalDataMap, qualpayId, utcNow, context.getTenantId());
        } catch (final SQLException e) {
            throw new PaymentPluginApiException("Unable to add payment method", e);
        }
    }

    @Override
    protected String getPaymentMethodId(final QualpayPaymentMethodsRecord record) {
        return record.getKbPaymentMethodId();
    }

    @Override
    public void deletePaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        final QualpayPaymentMethodsRecord qualPayPaymentMethodsRecord;
        try {
            qualPayPaymentMethodsRecord = dao.getPaymentMethod(kbPaymentMethodId, context.getTenantId());
        } catch (final SQLException e) {
            throw new PaymentPluginApiException("Unable to retrieve payment method", e);
        }

        final DeleteBillingCardRequest deleteBillingCardRequest = new DeleteBillingCardRequest();
        deleteBillingCardRequest.setCardId(qualPayPaymentMethodsRecord.getQualpayId());
        deleteBillingCardRequest.setMerchantId(getMerchantId(context));

        final String qualpayCustomerId = getCustomerId(kbAccountId, context);

        final ApiClient apiClient = buildApiClient(context, true);
        final CustomerVaultApi customerVaultApi = new CustomerVaultApi(apiClient);
        try {
            // Delete the card in the Vault
            customerVaultApi.deleteBillingCard(qualpayCustomerId, deleteBillingCardRequest);
        } catch (final ApiException e) {
            throw new PaymentPluginApiException("Error connecting to Qualpay", e);
        }

        // Delete our local copy
        super.deletePaymentMethod(kbAccountId, kbPaymentMethodId, properties, context);
    }

    @Override
    public List<PaymentMethodInfoPlugin> getPaymentMethods(final UUID kbAccountId, final boolean refreshFromGateway, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        // If refreshFromGateway isn't set, simply read our tables
        if (!refreshFromGateway) {
            return super.getPaymentMethods(kbAccountId, refreshFromGateway, properties, context);
        }

        // Retrieve our currently known payment methods
        final Map<String, QualpayPaymentMethodsRecord> existingPaymentMethodByQualpayId = new HashMap<String, QualpayPaymentMethodsRecord>();
        try {
            final List<QualpayPaymentMethodsRecord> existingQualpayPaymentMethodRecords = dao.getPaymentMethods(kbAccountId, context.getTenantId());
            for (final QualpayPaymentMethodsRecord existingQualpayPaymentMethodRecord : existingQualpayPaymentMethodRecords) {
                existingPaymentMethodByQualpayId.put(existingQualpayPaymentMethodRecord.getQualpayId(), existingQualpayPaymentMethodRecord);
            }
        } catch (final SQLException e) {
            throw new PaymentPluginApiException("Unable to retrieve existing payment methods", e);
        }

        // To retrieve all payment methods in Qualpay, retrieve the Qualpay customer id (custom field on the account)
        final String qualpayCustomerId = getCustomerId(kbAccountId, context);

        // Sync Qualpay payment methods (source of truth)
        final ApiClient apiClient = buildApiClient(context, true);
        final CustomerVaultApi customerVaultApi = new CustomerVaultApi(apiClient);
        try {
            final GetBillingResponse billingResponse = customerVaultApi.getBillingCards(qualpayCustomerId, getMerchantId(context));
            final GetBillingCardsResponse billingCardsResponse = billingResponse.getData();
            syncPaymentMethods(kbAccountId, billingCardsResponse.getBillingCards(), existingPaymentMethodByQualpayId, context);
        } catch (final ApiException e) {
            throw new PaymentPluginApiException("Error connecting to Qualpay", e);
        } catch (final PaymentApiException e) {
            throw new PaymentPluginApiException("Error creating payment method", e);
        } catch (final SQLException e) {
            throw new PaymentPluginApiException("Error creating payment method", e);
        }

        for (final QualpayPaymentMethodsRecord qualpayPaymentMethodsRecord : existingPaymentMethodByQualpayId.values()) {
            logger.info("Deactivating local Qualpay payment method {} - not found in Qualpay", qualpayPaymentMethodsRecord.getQualpayId());
            super.deletePaymentMethod(kbAccountId, UUID.fromString(qualpayPaymentMethodsRecord.getKbPaymentMethodId()), properties, context);
        }

        // Refresh the state
        return super.getPaymentMethods(kbAccountId, false, properties, context);
    }

    private void syncPaymentMethods(final UUID kbAccountId,
                                    final Iterable<BillingCard> billingCards,
                                    final Map<String, QualpayPaymentMethodsRecord> existingPaymentMethodByQualpayId,
                                    final CallContext context) throws PaymentApiException, SQLException {
        for (final BillingCard billingCard : billingCards) {
            final Map<String, Object> additionalDataMap = QualpayPluginProperties.toAdditionalDataMap(billingCard);

            final QualpayPaymentMethodsRecord existingPaymentMethodRecord = existingPaymentMethodByQualpayId.remove(billingCard.getCardId());
            if (existingPaymentMethodRecord == null) {
                // We don't know about it yet, create it
                logger.info("Creating new local Qualpay payment method {}", billingCard.getCardId());
                final List<PluginProperty> properties = PluginProperties.buildPluginProperties(additionalDataMap);
                final PaymentMethodPlugin paymentMethodInfo = new QualpayPaymentMethodPlugin(null,
                                                                                             billingCard.getCardId(),
                                                                                             properties);
                killbillAPI.getPaymentApi().addPaymentMethod(getAccount(kbAccountId, context),
                                                             billingCard.getCardId(),
                                                             QualpayActivator.PLUGIN_NAME,
                                                             false,
                                                             paymentMethodInfo,
                                                             ImmutableList.<PluginProperty>of(),
                                                             context);
            } else {
                logger.info("Updating existing local Qualpay payment method {}", billingCard);
                dao.updatePaymentMethod(UUID.fromString(existingPaymentMethodRecord.getKbPaymentMethodId()),
                                        additionalDataMap,
                                        billingCard.getCardId(),
                                        clock.getUTCNow(),
                                        context.getTenantId());
            }
        }
    }

    @Override
    public PaymentTransactionInfoPlugin authorizePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return executeInitialTransaction(TransactionType.AUTHORIZE, kbAccountId, kbPaymentId, kbTransactionId, kbPaymentMethodId, amount, currency, properties, context);
    }

    @Override
    public PaymentTransactionInfoPlugin capturePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return executeFollowUpTransaction(TransactionType.CAPTURE,
                                          new TransactionExecutor<GatewayResponse>() {
                                              @Override
                                              public GatewayResponse execute(final Account account, final QualpayPaymentMethodsRecord paymentMethodsRecord, final QualpayResponsesRecord previousResponse) throws ApiException {
                                                  final ApiClient apiClient = buildApiClient(context, false);
                                                  final PGApi pgApi = new PGApi(apiClient);

                                                  final Map additionalData = QualpayDao.fromAdditionalData(previousResponse.getAdditionalData());
                                                  final String pgId = (String) additionalData.get("id");

                                                  final PGApiCaptureRequest captureRequest = new PGApiRefundRequest();
                                                  captureRequest.setMerchantId(getMerchantId(context));
                                                  captureRequest.setAmtTran(amount.doubleValue());

                                                  return pgApi.capture(pgId, captureRequest);
                                              }
                                          },
                                          kbAccountId,
                                          kbPaymentId,
                                          kbTransactionId,
                                          kbPaymentMethodId,
                                          amount,
                                          currency,
                                          properties,
                                          context);
    }

    @Override
    public PaymentTransactionInfoPlugin purchasePayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return executeInitialTransaction(TransactionType.PURCHASE, kbAccountId, kbPaymentId, kbTransactionId, kbPaymentMethodId, amount, currency, properties, context);
    }

    @Override
    public PaymentTransactionInfoPlugin voidPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return executeFollowUpTransaction(TransactionType.VOID,
                                          new TransactionExecutor<GatewayResponse>() {
                                              @Override
                                              public GatewayResponse execute(final Account account, final QualpayPaymentMethodsRecord paymentMethodsRecord, final QualpayResponsesRecord previousResponse) throws ApiException {
                                                  final ApiClient apiClient = buildApiClient(context, false);
                                                  final PGApi pgApi = new PGApi(apiClient);

                                                  final Map additionalData = QualpayDao.fromAdditionalData(previousResponse.getAdditionalData());
                                                  final String pgId = (String) additionalData.get("id");

                                                  final PGApiVoidRequest voidRequest = new PGApiVoidRequest();
                                                  voidRequest.setMerchantId(getMerchantId(context));

                                                  return pgApi.voidTx(pgId, voidRequest);
                                              }
                                          },
                                          kbAccountId,
                                          kbPaymentId,
                                          kbTransactionId,
                                          kbPaymentMethodId,
                                          null,
                                          null,
                                          properties,
                                          context);
    }

    @Override
    public PaymentTransactionInfoPlugin creditPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        throw new PaymentPluginApiException("INTERNAL", "#creditPayment not yet implemented, please contact support@killbill.io");
    }

    @Override
    public PaymentTransactionInfoPlugin refundPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return executeFollowUpTransaction(TransactionType.REFUND,
                                          new TransactionExecutor<GatewayResponse>() {
                                              @Override
                                              public GatewayResponse execute(final Account account, final QualpayPaymentMethodsRecord paymentMethodsRecord, final QualpayResponsesRecord previousResponse) throws ApiException {
                                                  final ApiClient apiClient = buildApiClient(context, false);
                                                  final PGApi pgApi = new PGApi(apiClient);

                                                  final Map additionalData = QualpayDao.fromAdditionalData(previousResponse.getAdditionalData());
                                                  final String pgId = (String) additionalData.get("id");

                                                  final PGApiRefundRequest refundRequest = new PGApiRefundRequest();
                                                  refundRequest.setMerchantId(getMerchantId(context));
                                                  refundRequest.setAmtTran(amount.doubleValue());

                                                  return pgApi.refund(pgId, refundRequest);
                                              }
                                          },
                                          kbAccountId,
                                          kbPaymentId,
                                          kbTransactionId,
                                          kbPaymentMethodId,
                                          amount,
                                          currency,
                                          properties,
                                          context);
    }

    @VisibleForTesting
    ApiClient buildApiClient(final TenantContext context, final boolean platform) {
        final QualpayConfigProperties qualpayConfigProperties = qualpayConfigPropertiesConfigurationHandler.getConfigurable(context.getTenantId());

        final ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setUsername(qualpayConfigProperties.getApiKey());
        apiClient.setBasePath(qualpayConfigProperties.getBaseUrl() + (platform ? "/platform" : ""));
        apiClient.setConnectTimeout(Integer.parseInt(qualpayConfigProperties.getConnectionTimeout()));
        apiClient.setReadTimeout(Integer.parseInt(qualpayConfigProperties.getReadTimeout()));
        apiClient.setUserAgent("KillBill/1.0");

        return apiClient;
    }

    @Override
    public HostedPaymentPageFormDescriptor buildFormDescriptor(final UUID kbAccountId, final Iterable<PluginProperty> customFields, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        throw new PaymentPluginApiException("INTERNAL", "#buildFormDescriptor not yet implemented, please contact support@killbill.io");
    }

    @Override
    public GatewayNotification processNotification(final String notification, final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        throw new PaymentPluginApiException("INTERNAL", "#processNotification not yet implemented, please contact support@killbill.io");
    }

    private abstract static class TransactionExecutor<T> {

        public T execute(final Account account, final QualpayPaymentMethodsRecord paymentMethodsRecord) throws ApiException, SQLException {
            throw new UnsupportedOperationException();

        }

        public T execute(final Account account, final QualpayPaymentMethodsRecord paymentMethodsRecord, final QualpayResponsesRecord previousResponse) throws ApiException {
            throw new UnsupportedOperationException();
        }
    }

    private PaymentTransactionInfoPlugin executeInitialTransaction(final TransactionType transactionType,
                                                                   final UUID kbAccountId,
                                                                   final UUID kbPaymentId,
                                                                   final UUID kbTransactionId,
                                                                   final UUID kbPaymentMethodId,
                                                                   final BigDecimal amount,
                                                                   final Currency currency,
                                                                   final Iterable<PluginProperty> properties,
                                                                   final CallContext context) throws PaymentPluginApiException {
        return executeInitialTransaction(transactionType,
                                         new TransactionExecutor<GatewayResponse>() {
                                             @Override
                                             public GatewayResponse execute(final Account account, final QualpayPaymentMethodsRecord paymentMethodsRecord) throws ApiException, SQLException {
                                                 final ApiClient apiClient = buildApiClient(context, false);
                                                 final PGApi pgApi = new PGApi(apiClient);

                                                 final PGApiTransactionRequest pgApiTransactionRequest = new PGApiTransactionRequest();
                                                 pgApiTransactionRequest.setMerchantId(getMerchantId(context));
                                                 pgApiTransactionRequest.setAmtTran(amount.doubleValue());
                                                 pgApiTransactionRequest.setTranCurrency(CurrencyUnit.of(currency.toString()).getNumeric3Code());

                                                 final QualpayPaymentMethodsRecord paymentMethod = dao.getPaymentMethod(kbPaymentMethodId, context.getTenantId());
                                                 pgApiTransactionRequest.setCardId(paymentMethod.getQualpayId());

                                                 final List<PGApiLineItem> lineItems = new ArrayList<PGApiLineItem>(1);
                                                 final PGApiLineItem lineItem = new PGApiLineItem();
                                                 lineItem.setQuantity(1);
                                                 lineItem.setDescription(qualpayConfigPropertiesConfigurationHandler.getConfigurable(context.getTenantId()).getChargeDescription());
                                                 lineItem.setUnitOfMeasure("each");
                                                 lineItem.setProductCode(kbTransactionId.toString());
                                                 lineItem.setDebitCardInt("D");
                                                 lineItem.setUnitCost(pgApiTransactionRequest.getAmtTran());
                                                 lineItems.add(lineItem);
                                                 pgApiTransactionRequest.setLineItems(lineItems);

                                                 logger.debug("Creating Qualpay transaction: {}", pgApiTransactionRequest);
                                                 switch (transactionType) {
                                                     case AUTHORIZE:
                                                         return pgApi.authorize(pgApiTransactionRequest);
                                                     case PURCHASE:
                                                         return pgApi.sale(pgApiTransactionRequest);
                                                     default:
                                                         throw new UnsupportedOperationException(transactionType.toString());
                                                 }
                                             }
                                         },
                                         kbAccountId,
                                         kbPaymentId,
                                         kbTransactionId,
                                         kbPaymentMethodId,
                                         amount,
                                         currency,
                                         properties,
                                         context);
    }

    private PaymentTransactionInfoPlugin executeInitialTransaction(final TransactionType transactionType,
                                                                   final TransactionExecutor<GatewayResponse> transactionExecutor,
                                                                   final UUID kbAccountId,
                                                                   final UUID kbPaymentId,
                                                                   final UUID kbTransactionId,
                                                                   final UUID kbPaymentMethodId,
                                                                   final BigDecimal amount,
                                                                   final Currency currency,
                                                                   final Iterable<PluginProperty> properties,
                                                                   final TenantContext context) throws PaymentPluginApiException {
        final Account account = getAccount(kbAccountId, context);
        final QualpayPaymentMethodsRecord nonNullPaymentMethodsRecord = getQualpayPaymentMethodsRecord(kbPaymentMethodId, context);
        final DateTime utcNow = clock.getUTCNow();

        final GatewayResponse response;
        if (shouldSkipQualpay(properties)) {
            throw new UnsupportedOperationException("skip_gw=true not yet implemented, please contact support@killbill.io");
        } else {
            try {
                response = transactionExecutor.execute(account, nonNullPaymentMethodsRecord);
            } catch (final ApiException e) {
                throw new PaymentPluginApiException("Error connecting to Qualpay", e);
            } catch (final SQLException e) {
                throw new PaymentPluginApiException("Unable to submit payment, we encountered a database error", e);
            }
        }

        try {
            final QualpayResponsesRecord responsesRecord = dao.addResponse(kbAccountId, kbPaymentId, kbTransactionId, transactionType, amount, currency, response, utcNow, context.getTenantId());
            return QualpayPaymentTransactionInfoPlugin.build(responsesRecord);
        } catch (final SQLException e) {
            throw new PaymentPluginApiException("Payment went through, but we encountered a database error. Payment details: " + response.toString(), e);
        }
    }

    private PaymentTransactionInfoPlugin executeFollowUpTransaction(final TransactionType transactionType,
                                                                    final TransactionExecutor<GatewayResponse> transactionExecutor,
                                                                    final UUID kbAccountId,
                                                                    final UUID kbPaymentId,
                                                                    final UUID kbTransactionId,
                                                                    final UUID kbPaymentMethodId,
                                                                    @Nullable final BigDecimal amount,
                                                                    @Nullable final Currency currency,
                                                                    final Iterable<PluginProperty> properties,
                                                                    final TenantContext context) throws PaymentPluginApiException {
        final Account account = getAccount(kbAccountId, context);
        final QualpayPaymentMethodsRecord nonNullPaymentMethodsRecord = getQualpayPaymentMethodsRecord(kbPaymentMethodId, context);

        final QualpayResponsesRecord previousResponse;
        try {
            previousResponse = dao.getSuccessfulAuthorizationResponse(kbPaymentId, context.getTenantId());
            if (previousResponse == null) {
                throw new PaymentPluginApiException(null, "Unable to retrieve previous payment response for kbTransactionId " + kbTransactionId);
            }
        } catch (final SQLException e) {
            throw new PaymentPluginApiException("Unable to retrieve previous payment response for kbTransactionId " + kbTransactionId, e);
        }

        final DateTime utcNow = clock.getUTCNow();

        final GatewayResponse response;
        if (shouldSkipQualpay(properties)) {
            throw new UnsupportedOperationException("skip_gw=true not yet implemented, please contact support@killbill.io");
        } else {
            try {
                response = transactionExecutor.execute(account, nonNullPaymentMethodsRecord, previousResponse);
            } catch (final ApiException e) {
                throw new PaymentPluginApiException("Error connecting to Qualpay", e);
            }
        }

        try {
            final QualpayResponsesRecord responsesRecord = dao.addResponse(kbAccountId, kbPaymentId, kbTransactionId, transactionType, amount, currency, response, utcNow, context.getTenantId());
            return QualpayPaymentTransactionInfoPlugin.build(responsesRecord);
        } catch (final SQLException e) {
            throw new PaymentPluginApiException("Payment went through, but we encountered a database error. Payment details: " + (response.toString()), e);
        }
    }

    @VisibleForTesting
    Long getMerchantId(final TenantContext context) {
        final QualpayConfigProperties qualpayConfigProperties = qualpayConfigPropertiesConfigurationHandler.getConfigurable(context.getTenantId());
        return Long.valueOf(MoreObjects.firstNonNull(qualpayConfigProperties.getMerchantId(), "0"));
    }

    private String getCustomerId(final UUID kbAccountId, final CallContext context) throws PaymentPluginApiException {
        final String qualpayCustomerId = getCustomerIdNoException(kbAccountId, context);
        if (qualpayCustomerId == null) {
            throw new PaymentPluginApiException("INTERNAL", "Missing QUALPAY_CUSTOMER_ID custom field");
        }
        return qualpayCustomerId;
    }

    private String getCustomerIdNoException(final UUID kbAccountId, final CallContext context) {
        final List<CustomField> customFields = killbillAPI.getCustomFieldUserApi().getCustomFieldsForAccountType(kbAccountId, ObjectType.ACCOUNT, context);
        String qualpayCustomerId = null;
        for (final CustomField customField : customFields) {
            if ("QUALPAY_CUSTOMER_ID".equals(customField.getFieldName())) {
                qualpayCustomerId = customField.getFieldValue();
                break;
            }
        }
        return qualpayCustomerId;
    }

    private QualpayPaymentMethodsRecord getQualpayPaymentMethodsRecord(@Nullable final UUID kbPaymentMethodId, final TenantContext context) throws PaymentPluginApiException {
        QualpayPaymentMethodsRecord paymentMethodsRecord = null;

        if (kbPaymentMethodId != null) {
            try {
                paymentMethodsRecord = dao.getPaymentMethod(kbPaymentMethodId, context.getTenantId());
            } catch (final SQLException e) {
                throw new PaymentPluginApiException("Failed to retrieve payment method", e);
            }
        }

        return MoreObjects.firstNonNull(paymentMethodsRecord, emptyRecord(kbPaymentMethodId));
    }

    private QualpayPaymentMethodsRecord emptyRecord(@Nullable final UUID kbPaymentMethodId) {
        final QualpayPaymentMethodsRecord record = new QualpayPaymentMethodsRecord();
        if (kbPaymentMethodId != null) {
            record.setKbPaymentMethodId(kbPaymentMethodId.toString());
        }
        return record;
    }

    private boolean shouldSkipQualpay(final Iterable<PluginProperty> properties) {
        return "true".equals(PluginProperties.findPluginPropertyValue("skipGw", properties)) || "true".equals(PluginProperties.findPluginPropertyValue("skip_gw", properties));
    }
}
