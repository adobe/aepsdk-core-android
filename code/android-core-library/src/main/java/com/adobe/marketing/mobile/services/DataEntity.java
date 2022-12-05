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

package com.adobe.marketing.mobile.services;

import java.util.Date;
import java.util.UUID;

/**
 * Data Model class for entities stored in {@link com.adobe.marketing.mobile.services.DataQueuing}
 */
public final class DataEntity {

    private final String uniqueIdentifier;
    private final Date timestamp;
    private final String data;

    /**
     * Generates a new {@link DataEntity}
     *
     * @param data a {@link String} to be stored in database entity.
     */
    public DataEntity(final String data) {
        this(UUID.randomUUID().toString(), new Date(), data);
    }

    /**
     * Generates a new {@link DataEntity}
     *
     * @param data a {@link String} to be stored in database entity.
     * @param uniqueIdentifier unique {@link String} value.
     * @param timestamp instance of {@link Date} for retrieving {@link Date#getTime()}.
     */
    public DataEntity(final String uniqueIdentifier, final Date timestamp, final String data) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.timestamp = timestamp;
        this.data = data;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return ("DataEntity{"
                + "uniqueIdentifier='"
                + uniqueIdentifier
                + '\''
                + ", timeStamp="
                + timestamp
                + ", data="
                + data
                + '}');
    }
}
