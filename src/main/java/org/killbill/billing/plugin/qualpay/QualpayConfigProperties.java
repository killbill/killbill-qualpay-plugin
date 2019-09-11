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

import java.util.Properties;

import com.google.common.base.Ascii;
import com.google.common.base.MoreObjects;

public class QualpayConfigProperties {

    private static final String PROPERTY_PREFIX = "org.killbill.billing.plugin.qualpay.";

    private static final String DEFAULT_CONNECTION_TIMEOUT = "30000";
    private static final String DEFAULT_READ_TIMEOUT = "60000";

    private final String region;
    private final String apiKey;
    private final String merchantId;
    private final String baseUrl;
    private final String connectionTimeout;
    private final String readTimeout;
    private final String chargeDescription;
    private final String kbUsername;
    private final String kbPassword;

    public QualpayConfigProperties(final Properties properties, final String region) {
        this.region = region;
        this.apiKey = properties.getProperty(PROPERTY_PREFIX + "apiKey");
        this.merchantId = properties.getProperty(PROPERTY_PREFIX + "merchantId");
        this.baseUrl = MoreObjects.firstNonNull(properties.getProperty(PROPERTY_PREFIX + "baseUrl"), "https://api-test.qualpay.com");
        this.connectionTimeout = properties.getProperty(PROPERTY_PREFIX + "connectionTimeout", DEFAULT_CONNECTION_TIMEOUT);
        this.readTimeout = properties.getProperty(PROPERTY_PREFIX + "readTimeout", DEFAULT_READ_TIMEOUT);
        this.chargeDescription = Ascii.truncate(MoreObjects.firstNonNull(properties.getProperty(PROPERTY_PREFIX + "chargeDescription"), "Kill Bill charge"), 22, "...");
        this.kbUsername = properties.getProperty(MoreObjects.firstNonNull(properties.getProperty(PROPERTY_PREFIX + "kbUsername"), "admin"));
        this.kbPassword = properties.getProperty(MoreObjects.firstNonNull(properties.getProperty(PROPERTY_PREFIX + "kbPassword"), "password"));
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getConnectionTimeout() {
        return connectionTimeout;
    }

    public String getReadTimeout() {
        return readTimeout;
    }

    public String getChargeDescription() {
        return chargeDescription;
    }

    public String getKbUsername() {
        return kbUsername;
    }

    public String getKbPassword() {
        return kbPassword;
    }
}
