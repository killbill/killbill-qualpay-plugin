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

/*
 * This file is generated by jOOQ.
 */
package org.killbill.billing.plugin.qualpay.dao.gen;


import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayPaymentMethods;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayResponses;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.records.QualpayPaymentMethodsRecord;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.records.QualpayResponsesRecord;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * killbill.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<QualpayPaymentMethodsRecord> KEY_QUALPAY_PAYMENT_METHODS_PRIMARY = Internal.createUniqueKey(QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS, DSL.name("KEY_qualpay_payment_methods_PRIMARY"), new TableField[] { QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.RECORD_ID }, true);
    public static final UniqueKey<QualpayPaymentMethodsRecord> KEY_QUALPAY_PAYMENT_METHODS_QUALPAY_PAYMENT_METHODS_KB_PAYMENT_ID = Internal.createUniqueKey(QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS, DSL.name("KEY_qualpay_payment_methods_qualpay_payment_methods_kb_payment_id"), new TableField[] { QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID }, true);
    public static final UniqueKey<QualpayPaymentMethodsRecord> KEY_QUALPAY_PAYMENT_METHODS_RECORD_ID = Internal.createUniqueKey(QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS, DSL.name("KEY_qualpay_payment_methods_record_id"), new TableField[] { QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.RECORD_ID }, true);
    public static final UniqueKey<QualpayResponsesRecord> KEY_QUALPAY_RESPONSES_PRIMARY = Internal.createUniqueKey(QualpayResponses.QUALPAY_RESPONSES, DSL.name("KEY_qualpay_responses_PRIMARY"), new TableField[] { QualpayResponses.QUALPAY_RESPONSES.RECORD_ID }, true);
    public static final UniqueKey<QualpayResponsesRecord> KEY_QUALPAY_RESPONSES_RECORD_ID = Internal.createUniqueKey(QualpayResponses.QUALPAY_RESPONSES, DSL.name("KEY_qualpay_responses_record_id"), new TableField[] { QualpayResponses.QUALPAY_RESPONSES.RECORD_ID }, true);
}
