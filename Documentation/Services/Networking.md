# Networking

## Overview

The `Networking` service provides shared functionality to make asynchronous network requests and handle their responses.

## Declaration

##### `public interface Networking {}`

## Usage

The following code snippet details how to make a simple network request and handle the response.

### Java

```java
ServiceProvider.getInstance().getNetworkService().connectAsync(request, connection -> {
			// handle response connection
		});
```

### Kotlin

```kotlin
ServiceProvider.getInstance().networkService.connectAsync(
  request
) { connection: HttpConnecting? ->
            // handle response connection
}
```

## APIs

For a full list of APIs provided by the `Networking` service, see [Networking.java](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/Networking.java).

## Further Reading

Additional types such as [`NetworkRequest`](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/NetworkRequest.java) and [`HttpConnecting`](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/HttpConnecting.java) are required to send and handle network requests.
