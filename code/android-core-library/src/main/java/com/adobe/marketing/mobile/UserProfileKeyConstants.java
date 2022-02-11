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

package com.adobe.marketing.mobile;

/**
 * Contains {@link String} constants to be used as keys with the {@code UserProfile} module.
 */
@SuppressWarnings("unused")
public class UserProfileKeyConstants {

	/**
	 * Key to store user's first name.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String FIRST_NAME = "first_name";

	/**
	 * Key to store user's last name.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String LAST_NAME = "last_name";

	/**
	 * Key to store user's email address.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String EMAIL = "email";

	/**
	 * Key to store user's date of birth
	 * <p>
	 * Expected value type: {@link String} with format “YYYY-MM-DD”
	 */
	public static final String DATE_OF_BIRTH = "date_of_birth";

	/**
	 * Key to store user's country.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String COUNTRY = "country";

	/**
	 * Key to store user's language.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String LANGUAGE = "language";

	/**
	 * Key to store user's time zone.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String TIME_ZONE = "time_zone";

	/**
	 * Key to store user's home city.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String HOME_CITY = "home_city";

	/**
	 * Key to store user's current location.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String CURRENT_LOCATION = "current_location";

	/**
	 * Key to store user's bio.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String BIO = "bio";

	/**
	 * Key to store user's gender.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String GENDER = "gender";

	/**
	 * Key to store user's phone number.
	 * <p>
	 * Expected value type: {@link String} (of digits)
	 */
	public static final String PHONE = "phone";

	/**
	 * Key to store user's email subscription status.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String EMAIL_SUBSCRIBE = "email_subscribe";

	/**
	 * Key to store user's push subscription status.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String PUSH_SUBSCRIBE = "push_subscribe";

	/**
	 * Key to store date at which the user first used the app.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String DATE_OF_FIRST_SESSION = "date_of_first_session";

	/**
	 * Key to store date at which the user last used the app.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String DATE_OF_LAST_SESSION = "date_of_last_session";

	/**
	 * Key to store the url of image to be associated with user profile.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String IMAGE_URL = "image_url";

	/**
	 * Key to store the array of objects with app id and token string.
	 * <p>
	 * Expected value type: {@link java.util.ArrayList} containing {@link java.util.HashMap}{@code <String, String>}
	 * with {@code appID} as keys and {@code tokenString} as values
	 */
	public static final String PUSH_TOKENS = "push_tokens";

	/**
	 * Key to store user id.
	 * <p>
	 * Expected value type: {@link String}
	 */
	public static final String USER_ID = "user_id";

	/**
	 * Key to store hash containing any of id ({@link String}), likes ({@link java.util.ArrayList} of {@code String}s),
	 * num_friends ({@code int})
	 * <p>
	 * Expected value type: {@code String} / {@code int} / {@code List<String>}
	 */
	public static final String FACEBOOK_ID_HASH = "facebook_id";

	/**
	 * Key to store hash containing any of id ({@code int}), screen name ({@link String}, Twitter handle),
	 * follower count ({@code int}), friend count ({@code int}), status count ({@code int})
	 * <p>
	 * Expected value type: {@code String} / {@code int} / {@code HashMap<String, String>}
	 */
	public static final String TWITTER_ID_HASH = "twitter_id";

	/**
	 * Key to store date at which the user last received any Adobe Campaign communications.
	 * <p>
	 * Expected value type: {@link java.util.Date}
	 */
	public static final String DATE_OF_LAST_RECEIVED_ANY_CAMPAIGN = "last_received_any_campaign";

	/**
	 * Key to store date at which the user last received an email from Adobe Campaign.
	 * <p>
	 * Expected value type: {@link java.util.Date}
	 */
	public static final String DATE_OF_LAST_RECEIVED_EMAIL_CAMPAIGN = "last_received_email_campaign";

	/**
	 * Key to store date at which the user last received a push message from Adobe Campaign.
	 * <p>
	 * Expected value type: {@link java.util.Date}
	 */
	public static final String DATE_OF_LAST_RECEIVED_PUSH_CAMPAIGN = "last_received_push_campaign";

	private UserProfileKeyConstants() {}
}
