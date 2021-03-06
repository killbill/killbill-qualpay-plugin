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
package org.killbill.billing.plugin.qualpay.dao.gen.tables.records;


import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.killbill.billing.plugin.qualpay.dao.gen.tables.QualpayPaymentMethods;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class QualpayPaymentMethodsRecord extends UpdatableRecordImpl<QualpayPaymentMethodsRecord> implements Record9<ULong, String, String, String, Short, String, Timestamp, Timestamp, String> {

    private static final long serialVersionUID = 1758215017;

    /**
     * Setter for <code>killbill.qualpay_payment_methods.record_id</code>.
     */
    public void setRecordId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>killbill.qualpay_payment_methods.record_id</code>.
     */
    public ULong getRecordId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>killbill.qualpay_payment_methods.kb_account_id</code>.
     */
    public void setKbAccountId(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>killbill.qualpay_payment_methods.kb_account_id</code>.
     */
    public String getKbAccountId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>killbill.qualpay_payment_methods.kb_payment_method_id</code>.
     */
    public void setKbPaymentMethodId(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>killbill.qualpay_payment_methods.kb_payment_method_id</code>.
     */
    public String getKbPaymentMethodId() {
        return (String) get(2);
    }

    /**
     * Setter for <code>killbill.qualpay_payment_methods.qualpay_id</code>.
     */
    public void setQualpayId(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>killbill.qualpay_payment_methods.qualpay_id</code>.
     */
    public String getQualpayId() {
        return (String) get(3);
    }

    /**
     * Setter for <code>killbill.qualpay_payment_methods.is_deleted</code>.
     */
    public void setIsDeleted(Short value) {
        set(4, value);
    }

    /**
     * Getter for <code>killbill.qualpay_payment_methods.is_deleted</code>.
     */
    public Short getIsDeleted() {
        return (Short) get(4);
    }

    /**
     * Setter for <code>killbill.qualpay_payment_methods.additional_data</code>.
     */
    public void setAdditionalData(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>killbill.qualpay_payment_methods.additional_data</code>.
     */
    public String getAdditionalData() {
        return (String) get(5);
    }

    /**
     * Setter for <code>killbill.qualpay_payment_methods.created_date</code>.
     */
    public void setCreatedDate(Timestamp value) {
        set(6, value);
    }

    /**
     * Getter for <code>killbill.qualpay_payment_methods.created_date</code>.
     */
    public Timestamp getCreatedDate() {
        return (Timestamp) get(6);
    }

    /**
     * Setter for <code>killbill.qualpay_payment_methods.updated_date</code>.
     */
    public void setUpdatedDate(Timestamp value) {
        set(7, value);
    }

    /**
     * Getter for <code>killbill.qualpay_payment_methods.updated_date</code>.
     */
    public Timestamp getUpdatedDate() {
        return (Timestamp) get(7);
    }

    /**
     * Setter for <code>killbill.qualpay_payment_methods.kb_tenant_id</code>.
     */
    public void setKbTenantId(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>killbill.qualpay_payment_methods.kb_tenant_id</code>.
     */
    public String getKbTenantId() {
        return (String) get(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<ULong> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<ULong, String, String, String, Short, String, Timestamp, Timestamp, String> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<ULong, String, String, String, Short, String, Timestamp, Timestamp, String> valuesRow() {
        return (Row9) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<ULong> field1() {
        return QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.RECORD_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.KB_ACCOUNT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.QUALPAY_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Short> field5() {
        return QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.IS_DELETED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.ADDITIONAL_DATA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field7() {
        return QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.CREATED_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field8() {
        return QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.UPDATED_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field9() {
        return QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS.KB_TENANT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ULong value1() {
        return getRecordId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getKbAccountId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getKbPaymentMethodId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getQualpayId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Short value5() {
        return getIsDeleted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getAdditionalData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value7() {
        return getCreatedDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value8() {
        return getUpdatedDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value9() {
        return getKbTenantId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QualpayPaymentMethodsRecord value1(ULong value) {
        setRecordId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QualpayPaymentMethodsRecord value2(String value) {
        setKbAccountId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QualpayPaymentMethodsRecord value3(String value) {
        setKbPaymentMethodId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QualpayPaymentMethodsRecord value4(String value) {
        setQualpayId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QualpayPaymentMethodsRecord value5(Short value) {
        setIsDeleted(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QualpayPaymentMethodsRecord value6(String value) {
        setAdditionalData(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QualpayPaymentMethodsRecord value7(Timestamp value) {
        setCreatedDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QualpayPaymentMethodsRecord value8(Timestamp value) {
        setUpdatedDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QualpayPaymentMethodsRecord value9(String value) {
        setKbTenantId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QualpayPaymentMethodsRecord values(ULong value1, String value2, String value3, String value4, Short value5, String value6, Timestamp value7, Timestamp value8, String value9) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached QualpayPaymentMethodsRecord
     */
    public QualpayPaymentMethodsRecord() {
        super(QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS);
    }

    /**
     * Create a detached, initialised QualpayPaymentMethodsRecord
     */
    public QualpayPaymentMethodsRecord(ULong recordId, String kbAccountId, String kbPaymentMethodId, String qualpayId, Short isDeleted, String additionalData, Timestamp createdDate, Timestamp updatedDate, String kbTenantId) {
        super(QualpayPaymentMethods.QUALPAY_PAYMENT_METHODS);

        set(0, recordId);
        set(1, kbAccountId);
        set(2, kbPaymentMethodId);
        set(3, qualpayId);
        set(4, isDeleted);
        set(5, additionalData);
        set(6, createdDate);
        set(7, updatedDate);
        set(8, kbTenantId);
    }
}
