/*
 * This file is generated by jOOQ.
 */
package org.killbill.billing.plugin.qualpay.dao.gen;


import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayPaymentMethods;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayResponses;


/**
 * A class modelling indexes of tables in killbill.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index QUALPAY_PAYMENT_METHODS_QUALPAY_PAYMENT_METHODS_QUALPAY_ID = Internal.createIndex(DSL.name("qualpay_payment_methods_qualpay_id"), QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS, new OrderField[] { QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.QUALPAY_ID }, false);
    public static final Index QUALPAY_RESPONSES_QUALPAY_RESPONSES_KB_PAYMENT_ID = Internal.createIndex(DSL.name("qualpay_responses_kb_payment_id"), QualpayResponses.QUALPAY_RESPONSES, new OrderField[] { QualpayResponses.QUALPAY_RESPONSES.KB_PAYMENT_ID }, false);
    public static final Index QUALPAY_RESPONSES_QUALPAY_RESPONSES_KB_PAYMENT_TRANSACTION_ID = Internal.createIndex(DSL.name("qualpay_responses_kb_payment_transaction_id"), QualpayResponses.QUALPAY_RESPONSES, new OrderField[] { QualpayResponses.QUALPAY_RESPONSES.KB_PAYMENT_TRANSACTION_ID }, false);
    public static final Index QUALPAY_RESPONSES_QUALPAY_RESPONSES_QUALPAY_ID = Internal.createIndex(DSL.name("qualpay_responses_qualpay_id"), QualpayResponses.QUALPAY_RESPONSES, new OrderField[] { QualpayResponses.QUALPAY_RESPONSES.QUALPAY_ID }, false);
}
