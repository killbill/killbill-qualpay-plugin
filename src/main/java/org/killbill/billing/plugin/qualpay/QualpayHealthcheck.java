/*
 * Copyright 2020-2022 Equinix, Inc
 * Copyright 2014-2022 The Billing Project, LLC
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

import java.util.Map;

import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.tenant.api.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.api.CustomerVaultApi;
import qpPlatform.ApiClient;
import qpPlatform.Configuration;

public class QualpayHealthcheck implements Healthcheck {

    private static final Logger logger = LoggerFactory.getLogger(QualpayHealthcheck.class);

    private final QualpayConfigPropertiesConfigurationHandler qualpayConfigPropertiesConfigurationHandler;

    public QualpayHealthcheck(final QualpayConfigPropertiesConfigurationHandler qualpayConfigPropertiesConfigurationHandler) {
        this.qualpayConfigPropertiesConfigurationHandler = qualpayConfigPropertiesConfigurationHandler;
    }

    @Override
    public HealthStatus getHealthStatus(final Tenant tenant, final Map properties) {
        if (tenant == null) {
            // The plugin is running
            return HealthStatus.healthy("Qualpay OK");
        } else {
            // Specifying the tenant lets you also validate the tenant configuration
            final QualpayConfigProperties qualpayConfigProperties = qualpayConfigPropertiesConfigurationHandler.getConfigurable(tenant.getId());
            return pingQualpay(tenant, qualpayConfigProperties, true);
        }
    }

    private HealthStatus pingQualpay(final Tenant tenant, final QualpayConfigProperties qualpayConfigProperties, final boolean platform) {
        try {
            final ApiClient apiClient = Configuration.getDefaultApiClient();
            apiClient.setUsername(qualpayConfigProperties.getApiKey());
            apiClient.setBasePath(qualpayConfigProperties.getBaseUrl() + (platform ? "/platform" : ""));
            apiClient.setConnectTimeout(Integer.parseInt(qualpayConfigProperties.getConnectionTimeout()));
            apiClient.setReadTimeout(Integer.parseInt(qualpayConfigProperties.getReadTimeout()));
            apiClient.setUserAgent("KillBill/1.0");
            
            final CustomerVaultApi customerVaultApi = new CustomerVaultApi(apiClient); //TODO this may not be sufficient, improve this
            return HealthStatus.healthy("Qualpay OK");
        } catch (final Throwable e) {
            logger.warn("Healthcheck error", e);
            return HealthStatus.unHealthy("Qualpay error: " + e.getMessage());
        }
    }
}
