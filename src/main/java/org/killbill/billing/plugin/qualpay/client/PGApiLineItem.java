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

package org.killbill.billing.plugin.qualpay.client;

import com.google.gson.annotations.SerializedName;
import io.swagger.client.model.LineItem;

// Model missing from the official client
public class PGApiLineItem extends LineItem {

    @SerializedName("unit_of_measure")
    private String unitOfMeasure = null;

    @SerializedName("product_code")
    private String productCode = null;

    @SerializedName("debit_credit_int")
    private String debitCardInt = null;

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(final String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(final String productCode) {
        this.productCode = productCode;
    }

    public String getDebitCardInt() {
        return debitCardInt;
    }

    public void setDebitCardInt(final String debitCardInt) {
        this.debitCardInt = debitCardInt;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PGApiLineItem{");
        sb.append("unitOfMeasure='").append(unitOfMeasure).append('\'');
        sb.append(", productCode='").append(productCode).append('\'');
        sb.append(", debitCardInt='").append(debitCardInt).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final PGApiLineItem that = (PGApiLineItem) o;

        if (unitOfMeasure != null ? !unitOfMeasure.equals(that.unitOfMeasure) : that.unitOfMeasure != null) {
            return false;
        }
        if (productCode != null ? !productCode.equals(that.productCode) : that.productCode != null) {
            return false;
        }
        return debitCardInt != null ? debitCardInt.equals(that.debitCardInt) : that.debitCardInt == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (unitOfMeasure != null ? unitOfMeasure.hashCode() : 0);
        result = 31 * result + (productCode != null ? productCode.hashCode() : 0);
        result = 31 * result + (debitCardInt != null ? debitCardInt.hashCode() : 0);
        return result;
    }
}
