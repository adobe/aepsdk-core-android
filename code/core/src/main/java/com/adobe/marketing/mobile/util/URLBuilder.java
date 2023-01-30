/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util;

import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.services.Log;
import java.net.URL;
import java.util.Map;

/** A class providing a better way to construct a url. */
public class URLBuilder {

    public enum EncodeType {
        NONE(1),
        ENCODE(2);

        public final int id;

        EncodeType(final int identifier) {
            id = identifier;
        }
    }

    private boolean sslEnabled = true;
    private String path;
    private String server;
    private String query;

    /** constructor */
    public URLBuilder() {
        this.path = "";
        this.query = "";
        this.server = "";
    }

    /**
     * set whether SSL is enabled
     *
     * @param sslEnabled the boolean flag to indicated whether SSL is enabled
     * @return this
     */
    public URLBuilder enableSSL(final boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
        return this;
    }

    /**
     * set the server address
     *
     * @param server server address
     * @return this
     */
    public URLBuilder setServer(final String server) {
        this.server = server;
        return this;
    }

    /**
     * add path to the url, should not include '/' in the string
     *
     * @param newPath path string without '/'
     * @return this
     */
    public URLBuilder addPath(final String newPath) {
        if (newPath == null || newPath.length() == 0) {
            return this;
        }

        this.path = this.path + "/" + UrlUtils.urlEncode(newPath);
        return this;
    }

    /**
     * add multiple query parameters
     *
     * @param parameters the map containing query parameters
     * @return this
     */
    public URLBuilder addQueryParameters(final Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            this.addQueryParameter(entry.getKey(), entry.getValue());
        }

        return this;
    }

    /**
     * add one query parameter with key/value pair, both key and value will be encoded
     *
     * @param key the key of the query parameter
     * @param value the value of the query parameter
     * @return this
     */
    public URLBuilder addQueryParameter(final String key, final String value) {
        if (StringUtils.isNullOrEmpty(key) || StringUtils.isNullOrEmpty(value)) {
            return this;
        }

        return this.addQuery(
                UrlUtils.urlEncode(key) + "=" + UrlUtils.urlEncode(value), EncodeType.NONE);
    }

    /**
     * add a whole string as a query in the url, the string will be encoded
     *
     * @param newQuery the query string to be added to the url
     * @return this
     */
    public URLBuilder addQuery(final String newQuery) {
        return this.addQuery(newQuery, EncodeType.ENCODE);
    }

    /**
     * add a whole string as a query in the url
     *
     * @param newQuery the query string to be added to the url
     * @param encodeType encode type to be used to encode the query
     * @return this
     */
    public URLBuilder addQuery(final String newQuery, final EncodeType encodeType) {
        if (newQuery == null || newQuery.length() == 0) {
            return this;
        }

        String encodedQuery =
                encodeType == EncodeType.ENCODE ? UrlUtils.urlEncode(newQuery) : newQuery;

        if (this.query == null || this.query.length() == 0) {
            this.query = encodedQuery;
        } else {
            this.query = this.query + "&" + encodedQuery;
        }

        return this;
    }

    /**
     * build the url string based on all the data provided before
     *
     * @return the url string
     */
    public String build() {
        if (StringUtils.isNullOrEmpty(this.server)) {
            Log.error(
                    "URLBuilder",
                    "Failed to generate the URL for (server:%s,  path:%s, query:%s)",
                    this.server,
                    this.path,
                    this.query);
            return null;
        }

        boolean hasQuery = this.query != null && this.query.length() > 0;
        final String urlString =
                String.format(
                        "%s://%s%s%s%s",
                        this.sslEnabled ? "https" : "http",
                        this.server,
                        this.path,
                        hasQuery ? "?" : "",
                        this.query);

        try {
            new URL(urlString).toURI();
        } catch (Exception e) {
            Log.error(
                    CoreConstants.LOG_TAG,
                    "URLBuilder",
                    "Failed to generate the URL for (server:%s,  path:%s, query:%s) (%s)",
                    this.server,
                    this.path,
                    this.query,
                    e);
            return null;
        }

        return urlString;
    }
}
