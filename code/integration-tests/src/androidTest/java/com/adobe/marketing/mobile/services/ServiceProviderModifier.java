package com.adobe.marketing.mobile.services;

public class ServiceProviderModifier {
    public static void resetApp() {
        ServiceProvider.getInstance().resetAppInstance();
    }
}
