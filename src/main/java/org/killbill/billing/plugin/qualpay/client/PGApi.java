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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import io.swagger.client.model.AddBillingCardRequest;
import io.swagger.client.model.GatewayResponse;
import qpPlatform.ApiClient;
import qpPlatform.ApiException;
import qpPlatform.ApiResponse;
import qpPlatform.Pair;

// API missing from the official client
public class PGApi {

    private final ApiClient apiClient;

    public PGApi(final ApiClient apiClient) {
        this.apiClient = apiClient;
        this.apiClient.setBasePath(apiClient.getBasePath().replace("/platform", ""));
    }

    public GatewayResponse tokenize(final AddBillingCardRequest body) throws ApiException {
        return createPGTransactionWithHttpInfo("/pg/tokenize", body);
    }

    public GatewayResponse authorize(final PGApiTransactionRequest body) throws ApiException {
        return createPGTransactionWithHttpInfo("/pg/auth", body);
    }

    public GatewayResponse capture(final String pgIdOrig,
                                   final PGApiCaptureRequest body) throws ApiException {
        return createPGTransactionWithHttpInfo("/pg/capture/{pg_id_orig}".replaceAll("\\{" + "pg_id_orig" + "\\}", apiClient.escapeString(pgIdOrig)),
                                               body);
    }

    public GatewayResponse sale(final PGApiTransactionRequest body) throws ApiException {
        return createPGTransactionWithHttpInfo("/pg/sale", body);
    }

    public GatewayResponse refund(final String pgIdOrig,
                                  final PGApiRefundRequest body) throws ApiException {
        return createPGTransactionWithHttpInfo("/pg/refund/{pg_id_orig}".replaceAll("\\{" + "pg_id_orig" + "\\}", apiClient.escapeString(pgIdOrig)),
                                               body);
    }

    public GatewayResponse voidTx(final String pgIdOrig,
                                  final PGApiVoidRequest body) throws ApiException {
        return createPGTransactionWithHttpInfo("/pg/void/{pg_id_orig}".replaceAll("\\{" + "pg_id_orig" + "\\}", apiClient.escapeString(pgIdOrig)),
                                               body);
    }

    private GatewayResponse createPGTransactionWithHttpInfo(final String path, final Object body) throws ApiException {
        final com.squareup.okhttp.Call call = createPGTransactionCall(path, body);
        final Type localVarReturnType = new TypeToken<GatewayResponse>() {}.getType();
        final ApiResponse<GatewayResponse> resp = apiClient.execute(call, localVarReturnType);
        return resp.getData();
    }

    private com.squareup.okhttp.Call createPGTransactionCall(final String path,
                                                             final Object body) throws ApiException {
        final List<Pair> localVarQueryParams = new ArrayList<Pair>();
        final List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();

        final Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        final Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
                "application/json"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        final String[] localVarAuthNames = {"basicAuth"};
        return apiClient.buildCall(path,
                                   "POST",
                                   localVarQueryParams,
                                   localVarCollectionQueryParams,
                                   body,
                                   localVarHeaderParams,
                                   localVarFormParams,
                                   localVarAuthNames,
                                   null);
    }
}
