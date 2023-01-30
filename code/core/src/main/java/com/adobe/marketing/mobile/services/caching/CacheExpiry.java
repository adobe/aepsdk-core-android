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

package com.adobe.marketing.mobile.services.caching;

import java.util.Date;

/** Represents the expiry of a cached item. */
public class CacheExpiry {

    /** The date beyond which the cache item is deemed expired and invalid. */
    private final Date expiration;

    private CacheExpiry(final Date expiration) {
        this.expiration = expiration;
    }

    public final Date getExpiration() {
        return expiration;
    }

    /**
     * Creates a {@code CacheExpiry} with {@code CacheExpiry.expiration} after {@code
     * durationInMillis} from now.
     *
     * @param durationInMillis the milliseconds after current time that the {@code expiration}
     *     should be set to
     * @return {@code CacheExpiry} with {@code expiration} after {@code durationInMillis} from now.
     */
    public static CacheExpiry after(final long durationInMillis) {
        return new CacheExpiry(new Date(System.currentTimeMillis() + durationInMillis));
    }

    /**
     * Creates a {@code CacheExpiry} with {@code expiration} at the date provided.
     *
     * @param date that that the {@code CacheExpiry.expiration} should be set to
     * @return {@code CacheExpiry} with {@code expiration} at the date provided.
     */
    public static CacheExpiry at(final Date date) {
        return new CacheExpiry(date);
    }

    /**
     * Creates a {@code CacheExpiry} with no {@code expiration} date.
     *
     * @return {@code CacheExpiry} with no {@code expiration}.
     */
    public static CacheExpiry never() {
        return new CacheExpiry(null);
    }

    /**
     * Evaluates whether the {@code CacheExpiry.expiration} has elapsed.
     *
     * @return true if the {@code CacheExpiry.expiration} is before current time.
     */
    public boolean isExpired() {
        return expiration != null && System.currentTimeMillis() >= expiration.getTime();
    }
}
