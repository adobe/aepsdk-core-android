/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 *//*


package com.adobe.testapp;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.InvalidInitException;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.lifecycle.Lifecycle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class TestApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		MobileCore.setApplication(this);
		MobileCore.setLogLevel(com.adobe.marketing.mobile.LoggingMode.VERBOSE);
		MobileCore.configureWithAppID("launch-EN8fc4e9cda45e4514b075f0cf5e249742-development");
		//		MobileCore.configureWithFileInAssets("test/ADBMobileConfig.json");

		try {
			Identity.registerExtension();
			Lifecycle.registerExtension();
			Signal.registerExtension();
			Analytics.registerExtension();

		} catch (InvalidInitException e) {
			e.printStackTrace();
		}

		MobileCore.start(new AdobeCallback() {

			@Override
			public void call(Object value) {
				MobileCore.collectPii(new HashMap<String, String>() {
					{
						put("triggerKey", "collectPIIIOS");
						put("cusFirstName", "aa");
						put("cusLastName", "bb");
						put("cusEmail", "aa.bb@gmail.com");
					}
				});
			}
		});

		//				new Thread(new Runnable() {
		//					@Override
		//					public void run() {
		//						while(true){
		//							try {
		//								Thread.sleep(10);
		//							} catch (InterruptedException e) {
		//								e.printStackTrace();
		//							}
		//							ServiceProvider.getInstance().getNetworkService().connectAsync(new NetworkRequest("https://google.com", HttpMethod.GET, null, null, 3, 3), new NetworkCallback() {
		//								@Override
		//								public void call(HttpConnecting connection) {
		//									if(connection == null) {
		//										MobileCore.log(LoggingMode.ERROR, "Test Network", "Failed!!!!!");
		//									} else{
		//										MobileCore.log(LoggingMode.DEBUG, "Test Network", "Success!!!!!");
		//									}
		//								}
		//							});
		//						}
		//					}
		//				}).start();

//		try {
//
//
//			PackageInfo packageInfo = this.getApplicationContext().getPackageManager().getPackageInfo(
//										  this.getApplicationContext().getPackageName(), 0);
//			Method method = packageInfo.getClass().getDeclaredMethod("getLongVersionCode");
//			Long reflectVer = (Long)method.invoke(packageInfo);
//			Log.d("X", "********" + (reflectVer & 0x00000000ffffffff));
//
//			long version = this.getApplicationContext().getPackageManager().getPackageInfo(
//							   this.getApplicationContext().getPackageName(), 0).getLongVersionCode();
//			int versionCodeMajor = (int)(version >> 32);
//			int versionCode = (int)version;
//			Log.d("X", "-----" + this.getApplicationContext().getPackageManager().getPackageInfo(
//					  this.getApplicationContext().getPackageName(), 0).versionCode);
//			Log.d("X", "!!!!!!!!" + (version >> 32));
//			Log.d("X", "++++++" + (version & 0x00000000ffffffff));
//
//		} catch (PackageManager.NameNotFoundException | NoSuchMethodException | IllegalAccessException |
//					 InvocationTargetException e) {
//			e.printStackTrace();
//		}



	}
}
*/
