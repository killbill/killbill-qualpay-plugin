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

import java.util.List;

import com.google.gson.annotations.SerializedName;

// Model missing from the official client
public class PGApiTransactionRequest {

    @SerializedName("merchant_id")
    private Long merchantId = null;

    @SerializedName("customer_id")
    private String customerId = null;

    @SerializedName("amt_tran")
    private Double amtTran = null;

    @SerializedName("tokenize")
    private Boolean tokenize = null;

    @SerializedName("card_id")
    private String cardId = null;

    @SerializedName("avs_zip")
    private String avsZip = null;

    @SerializedName("tran_currency")
    private String tranCurrency = null;

    @SerializedName("line_items")
    private List<PGApiLineItem> lineItems = null;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(final Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final String customerId) {
        this.customerId = customerId;
    }

    public Double getAmtTran() {
        return amtTran;
    }

    public void setAmtTran(final Double amtTran) {
        this.amtTran = amtTran;
    }

    public Boolean getTokenize() {
        return tokenize;
    }

    public void setTokenize(final Boolean tokenize) {
        this.tokenize = tokenize;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(final String cardId) {
        this.cardId = cardId;
    }

    public String getAvsZip() {
        return avsZip;
    }

    public void setAvsZip(final String avsZip) {
        this.avsZip = avsZip;
    }

    public String getTranCurrency() {
        return tranCurrency;
    }

    public void setTranCurrency(final String tranCurrency) {
        this.tranCurrency = tranCurrency;
    }

    public List<PGApiLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(final List<PGApiLineItem> lineItems) {
        this.lineItems = lineItems;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PGApiTransactionRequest{");
        sb.append("merchantId=").append(merchantId);
        sb.append(", customerId='").append(customerId).append('\'');
        sb.append(", amtTran=").append(amtTran);
        sb.append(", tokenize=").append(tokenize);
        sb.append(", cardId='").append(cardId).append('\'');
        sb.append(", avsZip='").append(avsZip).append('\'');
        sb.append(", tranCurrency='").append(tranCurrency).append('\'');
        sb.append(", lineItems=").append(lineItems);
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

        final PGApiTransactionRequest that = (PGApiTransactionRequest) o;

        if (merchantId != null ? !merchantId.equals(that.merchantId) : that.merchantId != null) {
            return false;
        }
        if (customerId != null ? !customerId.equals(that.customerId) : that.customerId != null) {
            return false;
        }
        if (amtTran != null ? !amtTran.equals(that.amtTran) : that.amtTran != null) {
            return false;
        }
        if (tokenize != null ? !tokenize.equals(that.tokenize) : that.tokenize != null) {
            return false;
        }
        if (cardId != null ? !cardId.equals(that.cardId) : that.cardId != null) {
            return false;
        }
        if (avsZip != null ? !avsZip.equals(that.avsZip) : that.avsZip != null) {
            return false;
        }
        if (tranCurrency != null ? !tranCurrency.equals(that.tranCurrency) : that.tranCurrency != null) {
            return false;
        }
        return lineItems != null ? lineItems.equals(that.lineItems) : that.lineItems == null;
    }

    @Override
    public int hashCode() {
        int result = merchantId != null ? merchantId.hashCode() : 0;
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
        result = 31 * result + (amtTran != null ? amtTran.hashCode() : 0);
        result = 31 * result + (tokenize != null ? tokenize.hashCode() : 0);
        result = 31 * result + (cardId != null ? cardId.hashCode() : 0);
        result = 31 * result + (avsZip != null ? avsZip.hashCode() : 0);
        result = 31 * result + (tranCurrency != null ? tranCurrency.hashCode() : 0);
        result = 31 * result + (lineItems != null ? lineItems.hashCode() : 0);
        return result;
    }
}
