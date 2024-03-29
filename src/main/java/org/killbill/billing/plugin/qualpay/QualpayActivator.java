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

import java.util.Hashtable;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.osgi.libs.killbill.KillbillActivatorBase;
import org.killbill.billing.payment.plugin.api.PaymentPluginApi;
import org.killbill.billing.plugin.api.notification.PluginConfigurationEventHandler;
import org.killbill.billing.plugin.core.config.PluginEnvironmentConfig;
import org.killbill.billing.plugin.core.resources.jooby.PluginApp;
import org.killbill.billing.plugin.core.resources.jooby.PluginAppBuilder;
import org.killbill.billing.plugin.qualpay.dao.QualpayDao;
import org.osgi.framework.BundleContext;

public class QualpayActivator extends KillbillActivatorBase {

    public static final String PLUGIN_NAME = "killbill-qualpay";

    private QualpayConfigPropertiesConfigurationHandler qualpayConfigPropertiesConfigurationHandler;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

        final QualpayDao qualpayDao = new QualpayDao(dataSource.getDataSource());

        final String region = PluginEnvironmentConfig.getRegion(configProperties.getProperties());
        qualpayConfigPropertiesConfigurationHandler = new QualpayConfigPropertiesConfigurationHandler(PLUGIN_NAME, killbillAPI, region);

        final QualpayConfigProperties qualpayConfigProperties = qualpayConfigPropertiesConfigurationHandler.createConfigurable(configProperties.getProperties());
        qualpayConfigPropertiesConfigurationHandler.setDefaultConfigurable(qualpayConfigProperties);

        // Register the payment plugin
        final PaymentPluginApi pluginApi = new QualpayPaymentPluginApi(qualpayConfigPropertiesConfigurationHandler,
                                                                       killbillAPI,
                                                                       configProperties,
                                                                       clock.getClock(),
                                                                       qualpayDao);
        registerPaymentPluginApi(context, pluginApi);

        // Expose a healthcheck, so other plugins can check on the plugin status
        final Healthcheck healthcheck = new QualpayHealthcheck(qualpayConfigPropertiesConfigurationHandler);
        registerHealthcheck(context, healthcheck);

        // Register a servlet
        final PluginApp pluginApp = new PluginAppBuilder(PLUGIN_NAME, killbillAPI, dataSource, super.clock, configProperties)
                .withRouteClass(QualpayHealthcheckServlet.class).withService(healthcheck)
                .withService(qualpayConfigPropertiesConfigurationHandler)
                .build();
        final HttpServlet httpServlet = PluginApp.createServlet(pluginApp);
        registerServlet(context, httpServlet);

        registerHandlers();
    }

    private void registerHandlers() {
        final PluginConfigurationEventHandler handler = new PluginConfigurationEventHandler(qualpayConfigPropertiesConfigurationHandler);
        dispatcher.registerEventHandlers(handler);
    }

    private void registerServlet(final BundleContext context, final Servlet servlet) {
        final Hashtable<String, String> props = new Hashtable<>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, Servlet.class, servlet, props);
    }

    private void registerPaymentPluginApi(final BundleContext context, final PaymentPluginApi api) {
        final Hashtable<String, String> props = new Hashtable<>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, PaymentPluginApi.class, api, props);
    }

    private void registerHealthcheck(final BundleContext context, final Healthcheck healthcheck) {
        final Hashtable<String, String> props = new Hashtable<>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, Healthcheck.class, healthcheck, props);
    }
}
