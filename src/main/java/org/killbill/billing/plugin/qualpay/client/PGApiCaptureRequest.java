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

// Model missing from the official client
public class PGApiCaptureRequest {

    @SerializedName("merchant_id")
    private Long merchantId = null;

    @SerializedName("amt_tran")
    private Double amtTran = null;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(final Long merchantId) {
        this.merchantId = merchantId;
    }

    public Double getAmtTran() {
        return amtTran;
    }

    public void setAmtTran(final Double amtTran) {
        this.amtTran = amtTran;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PGApiCaptureRequest{");
        sb.append("merchantId=").append(merchantId);
        sb.append(", amtTran=").append(amtTran);
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

        final PGApiCaptureRequest that = (PGApiCaptureRequest) o;

        if (merchantId != null ? !merchantId.equals(that.merchantId) : that.merchantId != null) {
            return false;
        }
        return amtTran != null ? amtTran.equals(that.amtTran) : that.amtTran == null;
    }

    @Override
    public int hashCode() {
        int result = merchantId != null ? merchantId.hashCode() : 0;
        result = 31 * result + (amtTran != null ? amtTran.hashCode() : 0);
        return result;
    }
}
