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

import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.services.caching.CacheService;
import com.adobe.marketing.mobile.services.internal.caching.FileCacheService;
import com.adobe.marketing.mobile.services.internal.context.App;
import com.adobe.marketing.mobile.services.ui.AndroidUIService;
import com.adobe.marketing.mobile.services.ui.UIService;
import com.adobe.marketing.mobile.services.ui.URIHandler;

/** Maintains the current set of provided services and any potential service overrides */
public class ServiceProvider {

    private static class ServiceProviderSingleton {

        private static final ServiceProvider INSTANCE = new ServiceProvider();
    }

    /**
     * Singleton method to get the instance of ServiceProvider
     *
     * @return the {@link ServiceProvider} singleton
     */
    public static ServiceProvider getInstance() {
        return ServiceProviderSingleton.INSTANCE;
    }

    private DeviceInfoService defaultDeviceInfoService;
    private DeviceInforming overrideDeviceInfoService;
    private NetworkService defaultNetworkService;
    private Networking overrideNetworkService;
    private DataQueuing dataQueueService;
    private DataStoring defaultDataStoreService;
    private UIService defaultUIService;
    private MessagingDelegate messageDelegate;
    private Logging defaultLoggingService;
    private Logging overrideLoggingService;
    private CacheService defaultCacheService;
    private AppContextService defaultAppContextService;
    private AppContextService overrideAppContextService;

    private ServiceProvider() {
        defaultNetworkService = new NetworkService();
        defaultDeviceInfoService = new DeviceInfoService();
        dataQueueService = new DataQueueService();
        defaultDataStoreService = new LocalDataStoreService();
        defaultUIService = new AndroidUIService();
        messageDelegate = null;
        defaultLoggingService = new AndroidLoggingService();
        defaultCacheService = new FileCacheService();
    }

    /**
     * Returns the current {@link Logging}
     *
     * @return the current {@link Logging}
     */
    public Logging getLoggingService() {
        return overrideLoggingService != null ? overrideLoggingService : defaultLoggingService;
    }

    /**
     * Overrides the {@link Logging} service.
     *
     * @param loggingService the new {@link Logging} service which will override the default {@link
     *     Logging} service
     */
    public void setLoggingService(final Logging loggingService) {
        overrideLoggingService = loggingService;
    }

    /**
     * Gets the {@link DataStoring} service
     *
     * @return the {@link DataStoring} service
     */
    public DataStoring getDataStoreService() {
        return defaultDataStoreService;
    }

    /**
     * Gets the {@link DeviceInforming} service
     *
     * @return the {@link DeviceInforming} service
     */
    public DeviceInforming getDeviceInfoService() {
        return overrideDeviceInfoService != null
                ? overrideDeviceInfoService
                : defaultDeviceInfoService;
    }

    /**
     * For testing purpose. Overrides the default {@link DeviceInforming} service
     *
     * @param deviceInfoService new {@link DeviceInforming} service
     */
    @VisibleForTesting
    void setDeviceInfoService(final DeviceInforming deviceInfoService) {
        overrideDeviceInfoService = deviceInfoService;
    }

    /**
     * Gets the current {@link Networking} service.
     *
     * @return the override {@link Networking} service if it has been provided, otherwise the
     *     default {@link Networking} service is returned
     */
    public Networking getNetworkService() {
        return overrideNetworkService != null ? overrideNetworkService : defaultNetworkService;
    }

    /**
     * Overrides the {@link Networking} service.
     *
     * @param networkService the new {@link Networking} service which will override the default
     *     {@link Networking} service
     */
    public void setNetworkService(final Networking networkService) {
        overrideNetworkService = networkService;
    }

    /**
     * Gets the {@link DataQueuing} service
     *
     * @return the {@link DataQueuing} service
     */
    public DataQueuing getDataQueueService() {
        return dataQueueService;
    }

    /**
     * Gets the {@link UIService} service
     *
     * @return the {@link UIService} service
     */
    public UIService getUIService() {
        return defaultUIService;
    }

    /**
     * Gets the {@link CacheService} service
     *
     * @return the {@link UIService} service
     */
    public CacheService getCacheService() {
        return defaultCacheService;
    }

    /**
     * Gets the {@link AppContextService} service
     *
     * @return the {@link AppContextService} service
     */
    public AppContextService getAppContextService() {
        return overrideAppContextService != null ? overrideAppContextService : App.INSTANCE;
    }

    /**
     * For testing purpose. Overrides the default {@link AppContextService} service
     *
     * @param appContextService new {@link AppContextService} service
     */
    @VisibleForTesting
    void setAppContextService(final AppContextService appContextService) {
        overrideAppContextService = appContextService;
    }

    /**
     * Gets the custom {@link MessagingDelegate}.
     *
     * @return the custom {@code MessagingDelegate} if set, null otherwise
     */
    public MessagingDelegate getMessageDelegate() {
        return this.messageDelegate;
    }

    /**
     * Sets a custom {@link MessagingDelegate}.
     *
     * @param messageDelegate the custom {@link MessagingDelegate} to message visibility updates
     */
    public void setMessageDelegate(final MessagingDelegate messageDelegate) {
        this.messageDelegate = messageDelegate;
    }

    /**
     * Provides an {@link URIHandler} to decide the destination of the given URI
     *
     * @param uriHandler An {@link URIHandler} instance used to decide the Android link's
     *     destination
     */
    public void setURIHandler(final URIHandler uriHandler) {
        this.getUIService().setURIHandler(uriHandler);
    }

    /**
     * Reset the {@code ServiceProvider} to its default state. Any previously set services are reset
     * to their default state.
     */
    void resetServices() {
        defaultDeviceInfoService = new DeviceInfoService();
        defaultNetworkService = new NetworkService();
        dataQueueService = new DataQueueService();
        defaultDataStoreService = new LocalDataStoreService();
        defaultLoggingService = new AndroidLoggingService();
        defaultUIService = new AndroidUIService();
        defaultCacheService = new FileCacheService();

        overrideDeviceInfoService = null;
        overrideNetworkService = null;
        overrideAppContextService = null;
        messageDelegate = null;
    }
}
