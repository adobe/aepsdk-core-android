package com.adobe.marketing.mobile;


import android.content.Context;
import android.content.Intent;

interface BroadcastHandler {

	/**
	 * Handle the Android system broadcast. This may marshall the data from the Intent, and convert it into an event data {@code Map<String, Object>}
	 * and call the corresponding API to dispatch the data in an {@link Event}.
	 *
	 * @param context Context as received from the Android Broadcast receiver.
	 * @param intent  Intent as received from the Android Broadcast receiver.
	 */
	void handleBroadcast(Context context, Intent intent);
}
