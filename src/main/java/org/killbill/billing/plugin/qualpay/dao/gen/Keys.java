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


import javax.annotation.Generated;

import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;
import org.jooq.types.ULong;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayPaymentMethods;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayResponses;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.records.QualpayPaymentMethodsRecord;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.records.QualpayResponsesRecord;


/**
 * A class modelling foreign key relationships between tables of the <code>killbill</code> 
 * schema
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<QualpayPaymentMethodsRecord, ULong> IDENTITY_QUALPAY_PAYMENT_METHODS = Identities0.IDENTITY_QUALPAY_PAYMENT_METHODS;
    public static final Identity<QualpayResponsesRecord, ULong> IDENTITY_QUALPAY_RESPONSES = Identities0.IDENTITY_QUALPAY_RESPONSES;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<QualpayPaymentMethodsRecord> KEY_QUALPAY_PAYMENT_METHODS_PRIMARY = UniqueKeys0.KEY_QUALPAY_PAYMENT_METHODS_PRIMARY;
    public static final UniqueKey<QualpayPaymentMethodsRecord> KEY_QUALPAY_PAYMENT_METHODS_RECORD_ID = UniqueKeys0.KEY_QUALPAY_PAYMENT_METHODS_RECORD_ID;
    public static final UniqueKey<QualpayPaymentMethodsRecord> KEY_QUALPAY_PAYMENT_METHODS_QUALPAY_PAYMENT_METHODS_KB_PAYMENT_ID = UniqueKeys0.KEY_QUALPAY_PAYMENT_METHODS_QUALPAY_PAYMENT_METHODS_KB_PAYMENT_ID;
    public static final UniqueKey<QualpayResponsesRecord> KEY_QUALPAY_RESPONSES_PRIMARY = UniqueKeys0.KEY_QUALPAY_RESPONSES_PRIMARY;
    public static final UniqueKey<QualpayResponsesRecord> KEY_QUALPAY_RESPONSES_RECORD_ID = UniqueKeys0.KEY_QUALPAY_RESPONSES_RECORD_ID;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 extends AbstractKeys {
        public static Identity<QualpayPaymentMethodsRecord, ULong> IDENTITY_QUALPAY_PAYMENT_METHODS = createIdentity(QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS, QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.RECORD_ID);
        public static Identity<QualpayResponsesRecord, ULong> IDENTITY_QUALPAY_RESPONSES = createIdentity(QualpayResponses.QUALPAY_RESPONSES, QualpayResponses.QUALPAY_RESPONSES.RECORD_ID);
    }

    private static class UniqueKeys0 extends AbstractKeys {
        public static final UniqueKey<QualpayPaymentMethodsRecord> KEY_QUALPAY_PAYMENT_METHODS_PRIMARY = createUniqueKey(QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS, "KEY_qualpay_payment_methods_PRIMARY", QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.RECORD_ID);
        public static final UniqueKey<QualpayPaymentMethodsRecord> KEY_QUALPAY_PAYMENT_METHODS_RECORD_ID = createUniqueKey(QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS, "KEY_qualpay_payment_methods_record_id", QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.RECORD_ID);
        public static final UniqueKey<QualpayPaymentMethodsRecord> KEY_QUALPAY_PAYMENT_METHODS_QUALPAY_PAYMENT_METHODS_KB_PAYMENT_ID = createUniqueKey(QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS, "KEY_qualpay_payment_methods_qualpay_payment_methods_kb_payment_id", QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID);
        public static final UniqueKey<QualpayResponsesRecord> KEY_QUALPAY_RESPONSES_PRIMARY = createUniqueKey(QualpayResponses.QUALPAY_RESPONSES, "KEY_qualpay_responses_PRIMARY", QualpayResponses.QUALPAY_RESPONSES.RECORD_ID);
        public static final UniqueKey<QualpayResponsesRecord> KEY_QUALPAY_RESPONSES_RECORD_ID = createUniqueKey(QualpayResponses.QUALPAY_RESPONSES, "KEY_qualpay_responses_record_id", QualpayResponses.QUALPAY_RESPONSES.RECORD_ID);
    }
}
