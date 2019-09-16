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
import java.util.List;
import java.util.UUID;

import org.killbill.billing.ObjectType;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentApiException;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PaymentTransaction;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.TestUtils;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.core.PluginCustomField;
import org.killbill.billing.plugin.api.payment.PluginPaymentMethodPlugin;
import org.killbill.billing.plugin.qualpay.client.PGApi;
import org.killbill.billing.util.api.CustomFieldApiException;
import org.killbill.billing.util.customfield.CustomField;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.client.api.CustomerVaultApi;
import io.swagger.client.model.AddBillingCardRequest;
import io.swagger.client.model.AddCustomerRequest;
import io.swagger.client.model.BillingCard;
import io.swagger.client.model.CustomerVault;
import io.swagger.client.model.GetBillingResponse;
import io.swagger.client.model.UpdateBillingCardRequest;
import qpPlatform.ApiClient;
import qpPlatform.ApiException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class TestQualpayPaymentPluginApi extends TestBase {

    @Test(groups = "slow")
    public void testVerifyAddPaymentMethod() throws PaymentPluginApiException, ApiException {
        final UUID kbAccountId = account.getId();

        assertEquals(qualpayPaymentPluginApi.getPaymentMethods(kbAccountId, false, ImmutableList.<PluginProperty>of(), context).size(), 0);

        final String customerId = createQualpayCustomerWithCreditCard();

        final List<PaymentMethodInfoPlugin> paymentMethods = qualpayPaymentPluginApi.getPaymentMethods(kbAccountId, true, ImmutableList.<PluginProperty>of(), context);
        assertEquals(paymentMethods.size(), 1);
        assertEquals(paymentMethods.get(0).getAccountId(), kbAccountId);
        assertNotNull(paymentMethods.get(0).getExternalPaymentMethodId());

        // Verify the Qualpay id
        final ApiClient apiClient = qualpayPaymentPluginApi.buildApiClient(context, true);
        final CustomerVaultApi customerVaultApi = new CustomerVaultApi(apiClient);
        final GetBillingResponse billingCards = customerVaultApi.getBillingCards(customerId, null);
        assertEquals(billingCards.getData().getBillingCards().size(), 1);
        final BillingCard billingCard = billingCards.getData().getBillingCards().get(0);
        assertEquals(billingCard.getCardId(), paymentMethods.get(0).getExternalPaymentMethodId());
    }

    @Test(groups = "slow")
    public void testVerifySyncOfPaymentMethodsAdd() throws PaymentPluginApiException, ApiException, CustomFieldApiException {
        final UUID kbAccountId = account.getId();

        final AddCustomerRequest addCustomerRequest = new AddCustomerRequest();
        addCustomerRequest.setAutoGenerateCustomerId(true);
        addCustomerRequest.setCustomerFirmName("Kill Bill");
        final AddBillingCardRequest billingCardRequest = new AddBillingCardRequest();
        billingCardRequest.setBillingFirmName("Kill Bill");
        billingCardRequest.setBillingZip("94402");
        billingCardRequest.setCvv2("152");
        billingCardRequest.setExpDate("0420");
        billingCardRequest.setCardNumber("4111111111111111");
        addCustomerRequest.setBillingCards(ImmutableList.of(billingCardRequest));
        final ApiClient apiClient = qualpayPaymentPluginApi.buildApiClient(context, true);
        final CustomerVaultApi customerVaultApi = new CustomerVaultApi(apiClient);
        final CustomerVault customerVault = customerVaultApi.addCustomer(addCustomerRequest).getData();

        final String customerId = customerVault.getCustomerId();
        final CustomField customField = new PluginCustomField(kbAccountId,
                                                              ObjectType.ACCOUNT,
                                                              "QUALPAY_CUSTOMER_ID",
                                                              customerId,
                                                              UUID.randomUUID(),
                                                              clock.getUTCNow(),
                                                              clock.getUTCNow());
        customFieldUserApi.addCustomFields(ImmutableList.of(customField), context);

        assertEquals(qualpayPaymentPluginApi.getPaymentMethods(kbAccountId, false, ImmutableList.<PluginProperty>of(), context).size(), 0);

        // Sync Qualpay <-> Kill Bill
        final List<PaymentMethodInfoPlugin> paymentMethodInfoPlugins = qualpayPaymentPluginApi.getPaymentMethods(kbAccountId, true, ImmutableList.<PluginProperty>of(), context);
        assertEquals(paymentMethodInfoPlugins.size(), 1);
        assertEquals(paymentMethodInfoPlugins.get(0).getExternalPaymentMethodId(), customerVault.getBillingCards().get(0).getCardId());
    }

    @Test(groups = "slow")
    public void testVerifySyncOfPaymentMethodsUpdate() throws PaymentPluginApiException, ApiException {
        final UUID kbAccountId = account.getId();
        final String customerId = createQualpayCustomerWithCreditCard();
        final List<PaymentMethodInfoPlugin> paymentMethods = qualpayPaymentPluginApi.getPaymentMethods(kbAccountId, true, ImmutableList.<PluginProperty>of(), context);

        // Verify update path
        PaymentMethodPlugin paymentMethodDetail = qualpayPaymentPluginApi.getPaymentMethodDetail(kbAccountId,
                                                                                                 paymentMethods.get(0).getPaymentMethodId(),
                                                                                                 ImmutableList.of(),
                                                                                                 context);
        assertNull(PluginProperties.findPluginPropertyValue("billing_country_code", paymentMethodDetail.getProperties()));

        // Update metadata in Qualpay
        final UpdateBillingCardRequest updateBillingCardRequest = new UpdateBillingCardRequest();
        updateBillingCardRequest.setCardId(PluginProperties.findPluginPropertyValue("id", paymentMethodDetail.getProperties()));
        updateBillingCardRequest.setBillingFirstName(PluginProperties.findPluginPropertyValue("billing_first_name", paymentMethodDetail.getProperties()));
        updateBillingCardRequest.setBillingLastName(PluginProperties.findPluginPropertyValue("billing_last_name", paymentMethodDetail.getProperties()));
        updateBillingCardRequest.setBillingZip(PluginProperties.findPluginPropertyValue("billing_zip", paymentMethodDetail.getProperties()));
        updateBillingCardRequest.setBillingCountryCode("840");
        final ApiClient apiClient = qualpayPaymentPluginApi.buildApiClient(context, true);
        final CustomerVaultApi customerVaultApi = new CustomerVaultApi(apiClient);
        customerVaultApi.updateBillingCard(customerId, updateBillingCardRequest);

        // Sync Qualpay <-> Kill Bill
        qualpayPaymentPluginApi.getPaymentMethods(kbAccountId, true, ImmutableList.<PluginProperty>of(), context);
        paymentMethodDetail = qualpayPaymentPluginApi.getPaymentMethodDetail(kbAccountId,
                                                                             paymentMethods.get(0).getPaymentMethodId(),
                                                                             ImmutableList.of(),
                                                                             context);
        assertEquals(PluginProperties.toMap(paymentMethodDetail.getProperties()).get("billing_country_code"), "840");
    }

    @Test(groups = "slow")
    public void testSuccessfulAuthCapture() throws PaymentPluginApiException, ApiException, PaymentApiException {
        final UUID kbPaymentMethodId = createQualpayCustomerWithCreditCardAndReturnKBPaymentMethodId();

        final Payment payment = TestUtils.buildPayment(account.getId(), account.getPaymentMethodId(), account.getCurrency(), killbillApi);
        final PaymentTransaction authorizationTransaction = TestUtils.buildPaymentTransaction(payment, TransactionType.AUTHORIZE, BigDecimal.TEN, payment.getCurrency());
        final PaymentTransaction captureTransaction = TestUtils.buildPaymentTransaction(payment, TransactionType.CAPTURE, BigDecimal.TEN, payment.getCurrency());

        final PaymentTransactionInfoPlugin authorizationInfoPlugin = qualpayPaymentPluginApi.authorizePayment(account.getId(),
                                                                                                              payment.getId(),
                                                                                                              authorizationTransaction.getId(),
                                                                                                              kbPaymentMethodId, authorizationTransaction.getAmount(),
                                                                                                              authorizationTransaction.getCurrency(),
                                                                                                              ImmutableList.of(),
                                                                                                              context);
        TestUtils.updatePaymentTransaction(authorizationTransaction, authorizationInfoPlugin);
        verifyPaymentTransactionInfoPlugin(payment, authorizationTransaction, authorizationInfoPlugin, PaymentPluginStatus.PROCESSED);

        final PaymentTransactionInfoPlugin captureInfoPlugin = qualpayPaymentPluginApi.capturePayment(account.getId(),
                                                                                                      payment.getId(),
                                                                                                      captureTransaction.getId(),
                                                                                                      kbPaymentMethodId, captureTransaction.getAmount(),
                                                                                                      captureTransaction.getCurrency(),
                                                                                                      ImmutableList.of(),
                                                                                                      context);
        TestUtils.updatePaymentTransaction(captureTransaction, captureInfoPlugin);
        verifyPaymentTransactionInfoPlugin(payment, captureTransaction, captureInfoPlugin, PaymentPluginStatus.PROCESSED);
    }

    @Test(groups = "slow")
    public void testSuccessfulAuthVoid() throws PaymentPluginApiException, PaymentApiException {
        final UUID kbPaymentMethodId = createQualpayCustomerWithCreditCardAndReturnKBPaymentMethodId();

        final Payment payment = TestUtils.buildPayment(account.getId(), account.getPaymentMethodId(), account.getCurrency(), killbillApi);
        final PaymentTransaction authorizationTransaction = TestUtils.buildPaymentTransaction(payment, TransactionType.AUTHORIZE, BigDecimal.TEN, payment.getCurrency());
        final PaymentTransaction voidTransaction = TestUtils.buildPaymentTransaction(payment, TransactionType.VOID, BigDecimal.TEN, payment.getCurrency());

        final PaymentTransactionInfoPlugin authorizationInfoPlugin = qualpayPaymentPluginApi.authorizePayment(account.getId(),
                                                                                                              payment.getId(),
                                                                                                              authorizationTransaction.getId(),
                                                                                                              kbPaymentMethodId, authorizationTransaction.getAmount(),
                                                                                                              authorizationTransaction.getCurrency(),
                                                                                                              ImmutableList.of(),
                                                                                                              context);
        TestUtils.updatePaymentTransaction(authorizationTransaction, authorizationInfoPlugin);
        verifyPaymentTransactionInfoPlugin(payment, authorizationTransaction, authorizationInfoPlugin, PaymentPluginStatus.PROCESSED);

        final PaymentTransactionInfoPlugin voidInfoPlugin = qualpayPaymentPluginApi.voidPayment(account.getId(),
                                                                                                payment.getId(),
                                                                                                voidTransaction.getId(),
                                                                                                kbPaymentMethodId, ImmutableList.of(),
                                                                                                context);
        TestUtils.updatePaymentTransaction(voidTransaction, voidInfoPlugin);
        verifyPaymentTransactionInfoPlugin(payment, voidTransaction, voidInfoPlugin, PaymentPluginStatus.PROCESSED);
    }

    @Test(groups = "slow")
    public void testSuccessfulPurchaseRefund() throws PaymentPluginApiException, PaymentApiException {
        final UUID kbPaymentMethodId = createQualpayCustomerWithCreditCardAndReturnKBPaymentMethodId();

        final Payment payment = TestUtils.buildPayment(account.getId(), account.getPaymentMethodId(), account.getCurrency(), killbillApi);
        final PaymentTransaction purchaseTransaction = TestUtils.buildPaymentTransaction(payment, TransactionType.PURCHASE, BigDecimal.TEN, payment.getCurrency());
        final PaymentTransaction refundTransaction = TestUtils.buildPaymentTransaction(payment, TransactionType.REFUND, BigDecimal.TEN, payment.getCurrency());

        final PaymentTransactionInfoPlugin purchaseInfoPlugin = qualpayPaymentPluginApi.purchasePayment(account.getId(),
                                                                                                        payment.getId(),
                                                                                                        purchaseTransaction.getId(),
                                                                                                        kbPaymentMethodId,
                                                                                                        purchaseTransaction.getAmount(),
                                                                                                        purchaseTransaction.getCurrency(),
                                                                                                        ImmutableList.of(),
                                                                                                        context);
        TestUtils.updatePaymentTransaction(purchaseTransaction, purchaseInfoPlugin);
        verifyPaymentTransactionInfoPlugin(payment, purchaseTransaction, purchaseInfoPlugin, PaymentPluginStatus.PROCESSED);

        final PaymentTransactionInfoPlugin refundInfoPlugin = qualpayPaymentPluginApi.refundPayment(account.getId(),
                                                                                                    payment.getId(),
                                                                                                    refundTransaction.getId(),
                                                                                                    kbPaymentMethodId,
                                                                                                    refundTransaction.getAmount(),
                                                                                                    refundTransaction.getCurrency(),
                                                                                                    ImmutableList.of(),
                                                                                                    context);
        TestUtils.updatePaymentTransaction(refundTransaction, refundInfoPlugin);
        verifyPaymentTransactionInfoPlugin(payment, refundTransaction, refundInfoPlugin, PaymentPluginStatus.PROCESSED);
    }

    @Test(groups = "slow")
    public void testSuccessfulPurchaseMultiplePartialRefunds() throws PaymentPluginApiException, PaymentApiException {
        final UUID kbPaymentMethodId = createQualpayCustomerWithCreditCardAndReturnKBPaymentMethodId();

        final Payment payment = TestUtils.buildPayment(account.getId(), account.getPaymentMethodId(), account.getCurrency(), killbillApi);
        final PaymentTransaction purchaseTransaction = TestUtils.buildPaymentTransaction(payment, TransactionType.PURCHASE, BigDecimal.TEN, payment.getCurrency());
        final PaymentTransaction refundTransaction1 = TestUtils.buildPaymentTransaction(payment, TransactionType.REFUND, new BigDecimal("1"), payment.getCurrency());
        final PaymentTransaction refundTransaction2 = TestUtils.buildPaymentTransaction(payment, TransactionType.REFUND, new BigDecimal("2"), payment.getCurrency());
        final PaymentTransaction refundTransaction3 = TestUtils.buildPaymentTransaction(payment, TransactionType.REFUND, new BigDecimal("3"), payment.getCurrency());

        final PaymentTransactionInfoPlugin purchaseInfoPlugin = qualpayPaymentPluginApi.purchasePayment(account.getId(),
                                                                                                        payment.getId(),
                                                                                                        purchaseTransaction.getId(),
                                                                                                        kbPaymentMethodId,
                                                                                                        purchaseTransaction.getAmount(),
                                                                                                        purchaseTransaction.getCurrency(),
                                                                                                        ImmutableList.of(),
                                                                                                        context);
        TestUtils.updatePaymentTransaction(purchaseTransaction, purchaseInfoPlugin);
        verifyPaymentTransactionInfoPlugin(payment, purchaseTransaction, purchaseInfoPlugin, PaymentPluginStatus.PROCESSED);

        final List<PaymentTransactionInfoPlugin> paymentTransactionInfoPlugin1 = qualpayPaymentPluginApi.getPaymentInfo(account.getId(),
                                                                                                                        payment.getId(),
                                                                                                                        ImmutableList.of(),
                                                                                                                        context);
        assertEquals(paymentTransactionInfoPlugin1.size(), 1);

        final PaymentTransactionInfoPlugin refundInfoPlugin1 = qualpayPaymentPluginApi.refundPayment(account.getId(),
                                                                                                     payment.getId(),
                                                                                                     refundTransaction1.getId(),
                                                                                                     kbPaymentMethodId,
                                                                                                     refundTransaction1.getAmount(),
                                                                                                     refundTransaction1.getCurrency(),
                                                                                                     ImmutableList.of(),
                                                                                                     context);
        TestUtils.updatePaymentTransaction(refundTransaction1, refundInfoPlugin1);
        verifyPaymentTransactionInfoPlugin(payment, refundTransaction1, refundInfoPlugin1, PaymentPluginStatus.PROCESSED);

        final List<PaymentTransactionInfoPlugin> paymentTransactionInfoPlugin2 = qualpayPaymentPluginApi.getPaymentInfo(account.getId(),
                                                                                                                        payment.getId(),
                                                                                                                        ImmutableList.of(),
                                                                                                                        context);
        assertEquals(paymentTransactionInfoPlugin2.size(), 2);

        final PaymentTransactionInfoPlugin refundInfoPlugin2 = qualpayPaymentPluginApi.refundPayment(account.getId(),
                                                                                                     payment.getId(),
                                                                                                     refundTransaction2.getId(),
                                                                                                     kbPaymentMethodId,
                                                                                                     refundTransaction2.getAmount(),
                                                                                                     refundTransaction2.getCurrency(),
                                                                                                     ImmutableList.of(),
                                                                                                     context);
        TestUtils.updatePaymentTransaction(refundTransaction2, refundInfoPlugin2);
        verifyPaymentTransactionInfoPlugin(payment, refundTransaction2, refundInfoPlugin2, PaymentPluginStatus.PROCESSED);

        final List<PaymentTransactionInfoPlugin> paymentTransactionInfoPlugin3 = qualpayPaymentPluginApi.getPaymentInfo(account.getId(),
                                                                                                                        payment.getId(),
                                                                                                                        ImmutableList.of(),
                                                                                                                        context);
        assertEquals(paymentTransactionInfoPlugin3.size(), 3);

        final PaymentTransactionInfoPlugin refundInfoPlugin3 = qualpayPaymentPluginApi.refundPayment(account.getId(),
                                                                                                     payment.getId(),
                                                                                                     refundTransaction3.getId(),
                                                                                                     kbPaymentMethodId,
                                                                                                     refundTransaction3.getAmount(),
                                                                                                     refundTransaction3.getCurrency(),
                                                                                                     ImmutableList.of(),
                                                                                                     context);
        TestUtils.updatePaymentTransaction(refundTransaction3, refundInfoPlugin3);
        verifyPaymentTransactionInfoPlugin(payment, refundTransaction3, refundInfoPlugin3, PaymentPluginStatus.PROCESSED);

        final List<PaymentTransactionInfoPlugin> paymentTransactionInfoPlugin4 = qualpayPaymentPluginApi.getPaymentInfo(account.getId(),
                                                                                                                        payment.getId(),
                                                                                                                        ImmutableList.of(),
                                                                                                                        context);
        assertEquals(paymentTransactionInfoPlugin4.size(), 4);
    }

    @Test(groups = "slow")
    public void testVerifyAddPaymentMethodPurchaseNoVault() throws PaymentPluginApiException, ApiException, PaymentApiException {
        // Directly tokenize the card
        final String cardId = tokenizeCreditCard();

        final UUID kbAccountId = account.getId();
        assertEquals(qualpayPaymentPluginApi.getPaymentMethods(kbAccountId, false, ImmutableList.<PluginProperty>of(), context).size(), 0);

        // Add the payment method
        final UUID kbPaymentMethodId = UUID.randomUUID();
        qualpayPaymentPluginApi.addPaymentMethod(kbAccountId,
                                                 kbPaymentMethodId,
                                                 new PluginPaymentMethodPlugin(kbPaymentMethodId, null, false, ImmutableList.<PluginProperty>of()),
                                                 false,
                                                 PluginProperties.buildPluginProperties(ImmutableMap.of("card_id", cardId)),
                                                 context);
        final List<PaymentMethodInfoPlugin> paymentMethods = qualpayPaymentPluginApi.getPaymentMethods(kbAccountId, false, ImmutableList.<PluginProperty>of(), context);
        assertEquals(paymentMethods.size(), 1);
        assertEquals(paymentMethods.get(0).getAccountId(), kbAccountId);
        assertEquals(paymentMethods.get(0).getExternalPaymentMethodId(), cardId);

        // Verify the card id can be used
        final Payment payment = TestUtils.buildPayment(account.getId(), account.getPaymentMethodId(), account.getCurrency(), killbillApi);
        final PaymentTransaction purchaseTransaction = TestUtils.buildPaymentTransaction(payment, TransactionType.PURCHASE, BigDecimal.TEN, payment.getCurrency());
        final PaymentTransactionInfoPlugin purchaseInfoPlugin = qualpayPaymentPluginApi.purchasePayment(account.getId(),
                                                                                                        payment.getId(),
                                                                                                        purchaseTransaction.getId(),
                                                                                                        paymentMethods.get(0).getPaymentMethodId(),
                                                                                                        purchaseTransaction.getAmount(),
                                                                                                        purchaseTransaction.getCurrency(),
                                                                                                        ImmutableList.of(),
                                                                                                        context);
        TestUtils.updatePaymentTransaction(purchaseTransaction, purchaseInfoPlugin);
        verifyPaymentTransactionInfoPlugin(payment, purchaseTransaction, purchaseInfoPlugin, PaymentPluginStatus.PROCESSED);
    }

    private void verifyPaymentTransactionInfoPlugin(final Payment payment,
                                                    final PaymentTransaction paymentTransaction,
                                                    final PaymentTransactionInfoPlugin paymentTransactionInfoPlugin,
                                                    final PaymentPluginStatus expectedPaymentPluginStatus) {
        assertEquals(paymentTransactionInfoPlugin.getKbPaymentId(), payment.getId());
        assertEquals(paymentTransactionInfoPlugin.getKbTransactionPaymentId(), paymentTransaction.getId());
        assertEquals(paymentTransactionInfoPlugin.getTransactionType(), paymentTransaction.getTransactionType());
        if (TransactionType.VOID.equals(paymentTransaction.getTransactionType())) {
            assertNull(paymentTransactionInfoPlugin.getAmount());
            assertNull(paymentTransactionInfoPlugin.getCurrency());
        } else {
            assertEquals(paymentTransactionInfoPlugin.getAmount().compareTo(paymentTransaction.getAmount()), 0);
            assertEquals(paymentTransactionInfoPlugin.getCurrency(), paymentTransaction.getCurrency());
        }
        assertNotNull(paymentTransactionInfoPlugin.getCreatedDate());
        assertNotNull(paymentTransactionInfoPlugin.getEffectiveDate());

        if ("skip_gw".equals(paymentTransactionInfoPlugin.getGatewayError()) ||
            "true".equals(PluginProperties.findPluginPropertyValue("skipGw", paymentTransactionInfoPlugin.getProperties()))) {
            assertNull(paymentTransactionInfoPlugin.getGatewayErrorCode());
            assertEquals(paymentTransactionInfoPlugin.getStatus(), PaymentPluginStatus.PROCESSED);
        } else {
            assertEquals(paymentTransactionInfoPlugin.getGatewayErrorCode(), "000");
            assertEquals(paymentTransactionInfoPlugin.getStatus(), expectedPaymentPluginStatus);

            assertNotNull(paymentTransactionInfoPlugin.getGatewayError());
            if (expectedPaymentPluginStatus == PaymentPluginStatus.PROCESSED) {
                assertNotNull(paymentTransactionInfoPlugin.getFirstPaymentReferenceId());
                if (paymentTransaction.getTransactionType() == TransactionType.AUTHORIZE || paymentTransaction.getTransactionType() == TransactionType.PURCHASE) {
                    assertNotNull(paymentTransactionInfoPlugin.getSecondPaymentReferenceId());
                }
            }
        }
    }

    private UUID createQualpayCustomerWithCreditCardAndReturnKBPaymentMethodId() throws PaymentPluginApiException {
        final UUID kbAccountId = account.getId();
        createQualpayCustomerWithCreditCard();
        final List<PaymentMethodInfoPlugin> paymentMethods = qualpayPaymentPluginApi.getPaymentMethods(kbAccountId, true, ImmutableList.<PluginProperty>of(), context);
        assertEquals(paymentMethods.size(), 1);
        return paymentMethods.get(0).getPaymentMethodId();
    }

    private String createQualpayCustomerWithCreditCard() throws PaymentPluginApiException {
        final UUID kbAccountId = account.getId();
        final UUID kbPaymentMethodId = UUID.randomUUID();

        final ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
        builder.put("card_number", "4111111111111111");
        builder.put("exp_date", "0420");
        builder.put("cvv2", "152");
        builder.put("billing_first_name", "John");
        builder.put("billing_last_name", "Doe");
        builder.put("billing_zip", "94402");

        qualpayPaymentPluginApi.addPaymentMethod(kbAccountId,
                                                 kbPaymentMethodId,
                                                 new PluginPaymentMethodPlugin(kbPaymentMethodId, null, false, ImmutableList.<PluginProperty>of()),
                                                 false,
                                                 PluginProperties.buildPluginProperties(builder.build()),
                                                 context);

        return customFieldUserApi.getCustomFieldsForAccountType(kbAccountId, ObjectType.ACCOUNT, context).get(0).getFieldValue();
    }

    private String tokenizeCreditCard() throws ApiException {
        final AddBillingCardRequest billingCardsItem = new AddBillingCardRequest();
        billingCardsItem.setCardNumber("4111111111111111");
        billingCardsItem.setExpDate("0420");
        billingCardsItem.setCvv2("152");
        billingCardsItem.setBillingFirstName("John");
        billingCardsItem.setBillingLastName("Doe");
        billingCardsItem.setBillingZip("94402");
        billingCardsItem.setMerchantId(qualpayPaymentPluginApi.getMerchantId(context));

        final ApiClient apiClient = qualpayPaymentPluginApi.buildApiClient(context, true);
        final PGApi pgApi = new PGApi(apiClient);
        return pgApi.tokenize(billingCardsItem).getCardId();
    }
}
