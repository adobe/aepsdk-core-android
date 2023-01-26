# Services

## Contents

- [Overview](#overview)
- [`ServiceProvider`](#serviceprovider)
- [Implementing a Service](#implementing-a-service)
- [Overriding a Service](#overriding-a-service)
- [Provided Services](#provided-services)

## Overview

The AEP SDK contains a set of services. These services provide shared functionality throughout the SDK that can be shared by extensions. For example, services provide shared functionality for networking, logging, caching, and more.

A public `interface` defines each service; this allows customers to override services if they see fit. For example, here is the definition of the `Logging` service responsible for supplying shared logging functionality to all extensions.

```java
public interface Logging {
    void trace(String tag, String message);

    void debug(String tag, String message);

    void warning(String tag, String message);

    void error(String tag, String message);
}
```

The `Logging` service above defines a simple interface for logging messages.

## `ServiceProvider`

The SDK provides a shared `ServicesProvider`, responsible for maintaining the current set of provided services and any potential service overrides.

## Accessing Services

Some services provide wrapper classes. For example, the `Log` class is a wrapper around the `LoggingService`. However, in some cases, a wrapper class may not exist, and one might need to access a service directly from the ServiceProvider. For example, the below code shows how to access `CacheService`.

### Java

```java
CacheService cacheService =
    ServiceProvider.getInstance().getCacheService();
```

### Kotlin

```kotlin
val cacheService = ServiceProvider.getInstance().cacheService
```

## Implementing a Service

This example will show how one would implement their own `Logging` service throughout the SDK.

First, one must implement a type that conforms to the `Logging` interface, as defined above. We will do this by defining a logging service that only prints out messages with a log level of `Error`.

### Java

```java
class ErrorLogger implements Logging {
	@Override
	public void trace(String tag, String message) {}

	@Override
	public void debug(String tag, String message) {}

	@Override
	public void warning(String tag, String message) {}

	@Override
	public void error(String tag, String message) {
		Log.e("ErrorLogger", message);
	}
}
```

### Kotlin

```kotlin
internal class ErrorLogger : Logging {
    override fun trace(tag: String, message: String) {}
    override fun debug(tag: String, message: String) {}
    override fun warning(tag: String, message: String) {}
    override fun error(tag: String, message: String) {
        Log.e("ErrorLogger", message)
    }
}
```

In the code snippet above, we have a class that implements `Logging` and provides simple implementation for the single required API.

## Overriding a Service

As we saw above, implementing the `Logging` interface was quite simple, but how do we get the entire SDK to take advantage of this new service in place of the default implementation?

We can do this by setting the `loggingService` on the shared `ServiceProvider`, used by the entire SDK.

> For the Android SDK to use overridden services, Services overriding should be done before the SDK is initialized.

### Java

```java
ServiceProvider.getInstance().setLoggingService(new ErrorLogger());
```

### Kotlin

```kotlin
ServiceProvider.getInstance().loggingService = ErrorLogger()
```

If one wishes to revert to the `loggingService` default implementation, you can set the `loggingService` to nil.

### Java

```java
ServiceProvider.getInstance().setLoggingService(null);
```

### Kotlin

```kotlin
ServiceProvider.getInstance().loggingService = null
```

> Note: Use caution when overriding services. Changes to behavior for a given service can have unintended consequences throughout the SDK.

## Provided Services

- [`DeviceInforming`](./DeviceInforming.md)
- [`DataStoring`](./DataStoring.md)
- [`Networking`](./Networking.md)
- [`DataQueuing`](./DataQueuing.md)
- [`Caching`](./Caching.md)
- [`Logging`](./Logging.md)
