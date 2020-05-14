/*
    Copyright 2020 Exclamation Labs

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.exclamationlabs.connid.zoom;

import com.exclamationlabs.connid.zoom.util.ZoomFaultProcessor;
import com.exclamationlabs.connid.zoom.util.ZoomJWTAuthenticator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectionBrokenException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorSecurityException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ZoomConnection {

    private static final Log LOG = Log.getLog(ZoomConnection.class);

    private static ZoomFaultProcessor faultProcessor;
    private static GsonBuilder gsonBuilder;

    private ZoomJWTAuthenticator authenticator;
    private HttpClient stubClient; // for unit testing
    private String accessToken;

    static {
        gsonBuilder = new GsonBuilder();
    }

    private ZoomConfiguration configuration;

    public ZoomConnection(ZoomConfiguration inputConfiguration, ZoomJWTAuthenticator authenticatorIn) {
        authenticator = authenticatorIn;
        configuration = inputConfiguration;
        faultProcessor = new ZoomFaultProcessor(gsonBuilder);
    }

    void init() throws ConnectorSecurityException {
        accessToken = authenticator.authenticate(configuration);
    }

    void dispose() {
        faultProcessor = null;
        configuration = null;
    }

    void test() {
        try {
            HttpGet request = new HttpGet(configuration.getServiceUrl() + "/accounts/me/settings");
            prepareHeaders(request);
            executeRequest(request, null);
        } catch (Exception e) {
            throw new ConnectorException("Self-identification for Zoom connection user failed.", e);
        }
    }

    public HttpGet createGetRequest(String restUri) {
        HttpGet request = new HttpGet(getZoomUrl() + restUri);
        prepareHeaders(request);
        return request;
    }

    public HttpDelete createDeleteRequest(String restUri) {
        HttpDelete request = new HttpDelete(getZoomUrl() + restUri);
        prepareHeaders(request);
        return request;
    }

    public HttpPost createPostRequest(String restUri, Object requestBody) {
        HttpPost request = new HttpPost(getZoomUrl() + restUri);
        prepareHeaders(request);
        setupJsonRequestBody(request, requestBody);
        return request;
    }

    public HttpPatch createPatchRequest(String restUri, Object requestBody) {
        HttpPatch request = new HttpPatch(getZoomUrl() + restUri);
        prepareHeaders(request);
        if (requestBody != null) {
            setupJsonRequestBody(request, requestBody);
        }
        return request;
    }

    private String getZoomUrl() {
        return configuration.getServiceUrl();
    }

    private void prepareHeaders(HttpRequestBase request) {
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    }

    private HttpClient createClient() {
        return stubClient != null ? stubClient : HttpClients.createDefault();
    }

    public <T>T executeRequest(HttpRequestBase request, Class<T> returnType) {
        if (gsonBuilder == null || faultProcessor == null || configuration == null) {
            throw new ConnectionBrokenException("Connection invalidated or disposed, request cannot " +
                    "be performed.  Gsonbuilder: " + gsonBuilder + "; faultProcessor: " +
                    faultProcessor + "; configuration: " + configuration);
        }

        HttpClient client = createClient();
        HttpResponse response;
        T result = null;

        try {

            LOG.info("Request details: {0} to {1}", request.getMethod(),
                    request.getURI());
            response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            LOG.info("request status code is {0}", statusCode);

            if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
                LOG.info("request execution failed; status code is {0}", statusCode);
                faultProcessor.process(response);
            }

            if (returnType != null && statusCode != HttpStatus.SC_NOT_FOUND) {
                String rawJson = EntityUtils.toString(response.getEntity(), Charsets.UTF_8.name());
                LOG.info("Received {0} response for {1} {2}, raw JSON: {3}", statusCode,
                        request.getMethod(), request.getURI(), rawJson);

                Gson gson = gsonBuilder.create();
                result = gson.fromJson(rawJson, returnType);
            }

        } catch (ClientProtocolException e) {
            throw new ConnectorException(
                    "Unexpected ClientProtocolException occurred while attempting Zoom call: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ConnectorException(
                    "Unexpected IOException occurred while attempting Zoom call: " + e.getMessage(), e);

        }

        return result;
    }

    private void setupJsonRequestBody(HttpEntityEnclosingRequestBase request, Object requestBody) {

        Gson gson = gsonBuilder.create();
        String json = gson.toJson(requestBody);
        LOG.info("JSON formatted request for {0}: {1}", requestBody.getClass().getName(), json);
        try {
            StringEntity stringEntity = new StringEntity(json);
            request.setEntity(stringEntity);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Request body encoding failed for data: " + json, e);
        }
    }

    public void setStubClient(HttpClient stubClient) {
        this.stubClient = stubClient;
    }

    public ZoomConfiguration getConfiguration() {
        return configuration;
    }
}
