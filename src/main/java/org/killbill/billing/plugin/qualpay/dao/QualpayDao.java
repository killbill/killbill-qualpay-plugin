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

package org.killbill.billing.plugin.qualpay.dao;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.jooq.impl.DSL;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.dao.payment.PluginPaymentDao;
import org.killbill.billing.plugin.qualpay.QualpayPluginProperties;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayPaymentMethods;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayResponses;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.records.QualpayPaymentMethodsRecord;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.records.QualpayResponsesRecord;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.ImmutableMap;

import static org.killbill.billing.plugin.qualpay.dao.gen.Tables.QUALPAY_PAYMENT_METHODS;
import static org.killbill.billing.plugin.qualpay.dao.gen.Tables.QUALPAY_RESPONSES;

public class QualpayDao extends PluginPaymentDao<QualpayResponsesRecord, QualpayResponses, QualpayPaymentMethodsRecord, QualpayPaymentMethods> {

    public QualpayDao(final DataSource dataSource) throws SQLException {
        super(QUALPAY_RESPONSES, QUALPAY_PAYMENT_METHODS, dataSource);
        // Save space in the database
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
    }

    // Payment methods

    public void addPaymentMethod(final UUID kbAccountId,
                                 final UUID kbPaymentMethodId,
                                 final Map<String, Object> additionalDataMap,
                                 final String qualpayId,
                                 final DateTime utcNow,
                                 final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(),
                new WithConnectionCallback<QualpayResponsesRecord>() {
                    @Override
                    public QualpayResponsesRecord withConnection(final Connection conn) throws SQLException {
                        DSL.using(conn, dialect, settings)
                           .insertInto(QUALPAY_PAYMENT_METHODS,
                                       QUALPAY_PAYMENT_METHODS.KB_ACCOUNT_ID,
                                       QUALPAY_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID,
                                       QUALPAY_PAYMENT_METHODS.QUALPAY_ID,
                                       QUALPAY_PAYMENT_METHODS.IS_DELETED,
                                       QUALPAY_PAYMENT_METHODS.ADDITIONAL_DATA,
                                       QUALPAY_PAYMENT_METHODS.CREATED_DATE,
                                       QUALPAY_PAYMENT_METHODS.UPDATED_DATE,
                                       QUALPAY_PAYMENT_METHODS.KB_TENANT_ID)
                           .values(kbAccountId.toString(),
                                   kbPaymentMethodId.toString(),
                                   qualpayId,
                                   (short) FALSE,
                                   asString(additionalDataMap),
                                   toTimestamp(utcNow),
                                   toTimestamp(utcNow),
                                   kbTenantId.toString()
                                   )
                           .execute();

                        return null;
                    }
                });
    }

    public void updatePaymentMethod(final UUID kbPaymentMethodId,
                                    final Map<String, Object> additionalDataMap,
                                    final String QualpayId,
                                    final DateTime utcNow,
                                    final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(),
                new WithConnectionCallback<QualpayResponsesRecord>() {
                    @Override
                    public QualpayResponsesRecord withConnection(final Connection conn) throws SQLException {
                        DSL.using(conn, dialect, settings)
                           .update(QUALPAY_PAYMENT_METHODS)
                           .set(QUALPAY_PAYMENT_METHODS.ADDITIONAL_DATA, asString(additionalDataMap))
                           .set(QUALPAY_PAYMENT_METHODS.UPDATED_DATE, toTimestamp(utcNow))
                           .where(QUALPAY_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID.equal(kbPaymentMethodId.toString()))
                           .and(QUALPAY_PAYMENT_METHODS.QUALPAY_ID.equal(QualpayId))
                           .and(QUALPAY_PAYMENT_METHODS.KB_TENANT_ID.equal(kbTenantId.toString()))
                           .execute();
                        return null;
                    }
                });
    }

    public static Map fromAdditionalData(@Nullable final String additionalData) {
        if (additionalData == null) {
            return ImmutableMap.of();
        }

        try {
            return objectMapper.readValue(additionalData, Map.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
