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

import android.app.Activity;
import android.content.Context;

import com.adobe.marketing.mobile.services.ui.AndroidUIService;
import com.adobe.marketing.mobile.services.ui.FullscreenMessageDelegate;
import com.adobe.marketing.mobile.services.ui.UIService;
import com.adobe.marketing.mobile.services.ui.URIHandler;

import java.lang.ref.WeakReference;

/**
 * Maintains the current set of provided services and any potential service overrides
 */
public class ServiceProvider {

	private static class ServiceProviderSingleton {
		private static final ServiceProvider INSTANCE = new ServiceProvider();
	}

	/**
	 * Singleton method to get the instance of ServiceProvider
	 * @return the {@link ServiceProvider} singleton
	 */
	public static ServiceProvider getInstance() {
		return ServiceProviderSingleton.INSTANCE;
	}
	private volatile WeakReference<Activity> currentActivity;
	private volatile WeakReference<Context> applicationContext;

	private DeviceInfoService defaultDeviceInfoService;
	private DeviceInforming overrideDeviceInfoService;
	private NetworkService defaultNetworkService;
	private Networking overrideNetworkService;
	private DataQueueService dataQueueService;
	private DataStoring defaultDataStoreService;
	private UIService defaultUIService;
	private FullscreenMessageDelegate messageDelegate;

	private ServiceProvider() {
		defaultNetworkService = new NetworkService();
		defaultDeviceInfoService = new DeviceInfoService();
		dataQueueService = new DataQueueService();
		defaultDataStoreService = new LocalDataStoreService();
		defaultUIService = new AndroidUIService();
		messageDelegate = null;
	}

	/**
	 * Sets the {@link Context} of the application
	 * @param applicationContext android application {@link Context}
	 */
	public void setContext(final Context applicationContext) {
		this.applicationContext = new WeakReference<>(applicationContext);
	}

	/**
	 * Sets the current {@link Activity}
	 * @param activity the current {@link Activity}
	 */
	public void setCurrentActivity(final Activity activity) {
		this.currentActivity = new WeakReference<>(activity);
	}

	/**
	 * Returns the {@code Context} of the application
	 *
	 * @return the {@code Context} of the application
	 */
	Context getApplicationContext() {
		return this.applicationContext != null ? this.applicationContext.get() : null;
	}

	/**
	 * Returns the current {@code Activity}
	 *
	 * @return the current {@code Activity}
	 */
	Activity getCurrentActivity() {
		return this.currentActivity != null ? this.currentActivity.get() : null;
	}

	/**
	 * Gets the {@link DataStoring} service
	 * @return the {@link DataStoring} service
	 */
	public DataStoring getDataStoreService() {
		return defaultDataStoreService;
	}

	/**
	 * Gets the {@link DeviceInforming} service
	 * @return the {@link DeviceInforming} service
	 */
	public DeviceInforming getDeviceInfoService() {
		return overrideDeviceInfoService != null ? overrideDeviceInfoService : defaultDeviceInfoService;
	}

	/**
	 * For testing purpose. Overrides the default {@link DeviceInforming} service
	 * @param deviceInfoService new {@link DeviceInforming} service
	 */
	protected void setDeviceInfoService(DeviceInforming deviceInfoService) {
		overrideDeviceInfoService = deviceInfoService;
	}

	/**
	 * Gets the current {@link Networking} service.
	 * @return the override {@link Networking} service if it has been provided, otherwise the default
	 * {@link Networking} service is returned
	 */
	public Networking getNetworkService() {
		return overrideNetworkService != null ? overrideNetworkService : defaultNetworkService;
	}

	/**
	 * Overrides the {@link Networking} service.
	 * @param networkService the new {@link Networking} service which will override the default  {@link Networking} service
	 */
	public void setNetworkService(Networking networkService) {
		overrideNetworkService = networkService;
	}
	/**
	 * Gets the {@link DataQueuing} service
	 * @return the {@link DataQueuing} service
	 */
	public DataQueuing getDataQueueService() {
		return dataQueueService;
	}

	public UIService getUIService() {
		return defaultUIService;
	}

	/**
	 * Gets the custom {@link FullscreenMessageDelegate}.
	 * @return the custom {@code FullscreenMessageDelegate} if set, null otherwise
	 */
	public FullscreenMessageDelegate getMessageDelegate() {
		return this.messageDelegate;
	}

	/**
	 * Sets a custom {@link FullscreenMessageDelegate}.
	 * @param messageDelegate the custom {@code FullscreenMessageDelegate} to use for handling the display of Messaging extension in-app messages
	 */
	public void setMessageDelegate(final FullscreenMessageDelegate messageDelegate) {
		this.messageDelegate = messageDelegate;
	}

	/**
	 * Provides an {@link URIHandler} to decide the destination of the given URI
	 * @param uriHandler An {@link URIHandler} instance used to decide the Android link's destination
	 */
	public void setURIHandler(URIHandler uriHandler){
		this.getUIService().setURIHandler(uriHandler);
	}

	/**
	 * Reset the {@code ServiceProvider} to its default state.
	 * Any previously set services are reset to their default state.
	 */
	protected void reset() {
		defaultDeviceInfoService = new DeviceInfoService();
		defaultNetworkService = new NetworkService();
		dataQueueService = new DataQueueService();
		defaultDataStoreService = new LocalDataStoreService();

		overrideDeviceInfoService = null;
		overrideNetworkService = null;
		messageDelegate = null;
	}
}
