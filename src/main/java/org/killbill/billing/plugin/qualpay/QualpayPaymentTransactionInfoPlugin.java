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
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.payment.PluginPaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.qualpay.dao.QualpayDao;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.records.QualpayResponsesRecord;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class QualpayPaymentTransactionInfoPlugin extends PluginPaymentTransactionInfoPlugin {

    // Kill Bill limits the field size to 32
    private static final int ERROR_CODE_MAX_LENGTH = 32;

    // See https://www.qualpay.com/developer/api/reference#api-response-codes
    private static final ImmutableSet<String> PLUGIN_FAILURE_CODES = new Builder<String>().add("100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112", "114", "115", "116", "117", "118", "119", "404", "405").build();
    private static final ImmutableSet<String> PAYMENT_FAILURE_CODES = new Builder<String>().add("113", "401", "402", "403").build();

    private final QualpayResponsesRecord qualpayResponseRecord;

    public static QualpayPaymentTransactionInfoPlugin build(final QualpayResponsesRecord qualpayResponsesRecord) {
        final Map additionalData = QualpayDao.fromAdditionalData(qualpayResponsesRecord.getAdditionalData());
        final String firstPaymentReferenceId = (String) additionalData.get("id");
        final String secondPaymentReferenceId = (String) additionalData.get("auth_code");

        final DateTime responseDate = new DateTime(qualpayResponsesRecord.getCreatedDate(), DateTimeZone.UTC);

        return new QualpayPaymentTransactionInfoPlugin(qualpayResponsesRecord,
                                                       UUID.fromString(qualpayResponsesRecord.getKbPaymentId()),
                                                       UUID.fromString(qualpayResponsesRecord.getKbPaymentTransactionId()),
                                                       TransactionType.valueOf(qualpayResponsesRecord.getTransactionType()),
                                                       qualpayResponsesRecord.getAmount(),
                                                       Strings.isNullOrEmpty(qualpayResponsesRecord.getCurrency()) ? null : Currency.valueOf(qualpayResponsesRecord.getCurrency()),
                                                       getPaymentPluginStatus(additionalData),
                                                       getGatewayError(additionalData),
                                                       truncate(getGatewayErrorCode(additionalData)),
                                                       firstPaymentReferenceId,
                                                       secondPaymentReferenceId,
                                                       responseDate,
                                                       responseDate,
                                                       PluginProperties.buildPluginProperties(additionalData));
    }

    private static PaymentPluginStatus getPaymentPluginStatus(final Map additionalData) {
        final String overriddenTransactionStatus = (String) additionalData.get(QualpayPaymentPluginApi.PROPERTY_OVERRIDDEN_TRANSACTION_STATUS);
        if (overriddenTransactionStatus != null) {
            return PaymentPluginStatus.valueOf(overriddenTransactionStatus);
        }

        final String code = (String) additionalData.get("rcode");
        if ("000".equals(code)) {
            return PaymentPluginStatus.PROCESSED;
        } else if (PLUGIN_FAILURE_CODES.contains(code)) {
            return PaymentPluginStatus.CANCELED;
        } else if (PAYMENT_FAILURE_CODES.contains(code)) {
            return PaymentPluginStatus.ERROR;
        } else {
            return PaymentPluginStatus.UNDEFINED;
        }
    }

    private static String getGatewayError(final Map additionalData) {
        return (String) additionalData.get("rmsg");
    }

    private static String getGatewayErrorCode(final Map additionalData) {
        return (String) additionalData.get("rcode");
    }

    private static String truncate(@Nullable final String string) {
        if (string == null) {
            return null;
        } else if (string.length() <= ERROR_CODE_MAX_LENGTH) {
            return string;
        } else {
            return string.substring(0, ERROR_CODE_MAX_LENGTH);
        }
    }

    public QualpayPaymentTransactionInfoPlugin(final QualpayResponsesRecord qualpayResponsesRecord,
                                               final UUID kbPaymentId,
                                               final UUID kbTransactionPaymentPaymentId,
                                               final TransactionType transactionType,
                                               final BigDecimal amount,
                                               final Currency currency,
                                               final PaymentPluginStatus pluginStatus,
                                               final String gatewayError,
                                               final String gatewayErrorCode,
                                               final String firstPaymentReferenceId,
                                               final String secondPaymentReferenceId,
                                               final DateTime createdDate,
                                               final DateTime effectiveDate,
                                               final List<PluginProperty> properties) {
        super(kbPaymentId,
              kbTransactionPaymentPaymentId,
              transactionType,
              amount,
              currency,
              pluginStatus,
              gatewayError,
              gatewayErrorCode,
              firstPaymentReferenceId,
              secondPaymentReferenceId,
              createdDate,
              effectiveDate,
              properties);
        this.qualpayResponseRecord = qualpayResponsesRecord;
    }

    public QualpayResponsesRecord getQualpayResponseRecord() {
        return qualpayResponseRecord;
    }
}
