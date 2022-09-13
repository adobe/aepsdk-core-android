package com.adobe.marketing.mobile.internal.context;

@FunctionalInterface
public interface SimpleCallback<T> {
    void call(T t);
}
