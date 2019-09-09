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

import java.util.List;
import java.util.UUID;

import org.killbill.billing.ObjectType;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.core.PluginCustomField;
import org.killbill.billing.plugin.api.payment.PluginPaymentMethodPlugin;
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
        final ApiClient apiClient = qualpayPaymentPluginApi.buildApiClient(context);
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
        final ApiClient apiClient = qualpayPaymentPluginApi.buildApiClient(context);
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
        final ApiClient apiClient = qualpayPaymentPluginApi.buildApiClient(context);
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
}
