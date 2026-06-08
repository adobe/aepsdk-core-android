/*
  Copyright 2025 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.TimeZone;

/**
 * An immutable, caller-supplied set of profile attributes to sync to the user's Adobe Experience
 * Platform profile via {@link MobileCore#updateProfileAttributes(ProfileAttributes)}.
 *
 * <p>Instances are created with the {@link Builder}:
 *
 * <pre>
 * ProfileAttributes attributes =
 *         new ProfileAttributes.Builder()
 *                 .setTimeZone(TimeZone.getDefault())
 *                 .build();
 * MobileCore.updateProfileAttributes(attributes);
 * </pre>
 */
public final class ProfileAttributes {

    private final TimeZone timeZone;

    private ProfileAttributes(@NonNull final Builder builder) {
        this.timeZone = builder.timeZone;
    }

    /**
     * Returns the {@link TimeZone} to sync, or {@code null} if none was set.
     *
     * @return the {@link TimeZone}, or {@code null}
     */
    @Nullable public TimeZone getTimeZone() {
        return timeZone;
    }

    /** Fluent builder for {@link ProfileAttributes}. */
    public static final class Builder {

        private TimeZone timeZone;

        /** Creates a new builder with no attributes set. */
        public Builder() {}

        /**
         * Sets the {@link TimeZone} to sync to the user's profile.
         *
         * <p>The timezone's IANA identifier ({@link TimeZone#getID()}, for example {@code
         * "America/New_York"}) is what gets sent to the Edge Network. Typically {@code
         * TimeZone.getDefault()} to sync the device's current timezone.
         *
         * @param timeZone the {@link TimeZone} to sync
         * @return this builder, for chaining
         */
        @NonNull public Builder setTimeZone(@Nullable final TimeZone timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        /**
         * Builds an immutable {@link ProfileAttributes} from the values set on this builder.
         *
         * @return the built {@link ProfileAttributes}
         */
        @NonNull public ProfileAttributes build() {
            return new ProfileAttributes(this);
        }
    }
}
