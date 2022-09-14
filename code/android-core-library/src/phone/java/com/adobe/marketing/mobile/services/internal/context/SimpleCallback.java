package com.adobe.marketing.mobile.services.internal.context;

@FunctionalInterface
public interface SimpleCallback<T> {
    void call(T t);
}
