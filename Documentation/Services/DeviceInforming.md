# DeviceInforming

## Overview

The `DeviceInforming` service lets you access critical pieces of information related to the user's device, such as carrier name, device name, locale, and more. The `DeviceInforming` service can be accessed directly from the `ServiceProvider`.

## Declaration

##### `public interface DeviceInforming {}`

## Usage

The following code snippet shows how to read the `SystemInfoService` and how to invoke the API to retrieve the user's active locale.

### Java

```java
Locale local = ServiceProvider.getInstance().getDeviceInfoService().getActiveLocale();
```

### Kotlin

```kotlin
val local = ServiceProvider.getInstance().deviceInfoService.activeLocale
```

## APIs

For a full list of APIs provided by the `DeviceInforming` service, see [`DeviceInforming.java`](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/DeviceInforming.java).
