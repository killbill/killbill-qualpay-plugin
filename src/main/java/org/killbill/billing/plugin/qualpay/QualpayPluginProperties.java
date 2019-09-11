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

import java.util.HashMap;
import java.util.Map;

import io.swagger.client.model.BillingCard;
import io.swagger.client.model.GatewayResponse;

public abstract class QualpayPluginProperties {

    public static Map<String, Object> toAdditionalDataMap(final BillingCard billingCard) {
        final Map<String, Object> additionalDataMap = new HashMap<String, Object>();

        additionalDataMap.put("card_number", billingCard.getCardNumber());
        additionalDataMap.put("exp_date", billingCard.getExpDate());
        additionalDataMap.put("card_type", billingCard.getCardType());
        additionalDataMap.put("verified_date", billingCard.getVerifiedDate());
        // Don't store name / address details (GDPR)
        additionalDataMap.put("billing_city", billingCard.getBillingCity());
        additionalDataMap.put("billing_state", billingCard.getBillingState());
        additionalDataMap.put("billing_zip", billingCard.getBillingZip());
        additionalDataMap.put("billing_zip4", billingCard.getBillingZip4());
        additionalDataMap.put("billing_country", billingCard.getBillingCountry());
        additionalDataMap.put("billing_country_code", billingCard.getBillingCountryCode());

        additionalDataMap.put("id", billingCard.getCardId());

        return additionalDataMap;
    }

    public static Map<String, Object> toAdditionalDataMap(final GatewayResponse gatewayResponse) {
        final Map<String, Object> additionalDataMap = new HashMap<String, Object>();

        additionalDataMap.put("id", gatewayResponse.getPgId());
        additionalDataMap.put("rcode", gatewayResponse.getRcode());
        additionalDataMap.put("rmsg", gatewayResponse.getRmsg());
        additionalDataMap.put("auth_code", gatewayResponse.getAuthCode());

        return additionalDataMap;
    }
}
