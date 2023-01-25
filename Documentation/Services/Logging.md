# Logging

## Overview

The `Logging` service provides shared functionality to write messages to the console.

## Declaration

##### `public interface Logging {}`

## Usage

While you can access the `Logging` service directly via the `ServiceProvider`, it is recommended to use the wrapper class, `Log`. This class can be used to read and write messages to the console.

### Java

```java
Log.debug("label1", "label2", "My log message");
```

### Kotlin

```kotlin
Log.debug("label1", "label2", "My log message")
```

## APIs

For a full list of APIs provided by the `Logging` service, see [Logging.java](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/Logging.java) and for a complete list of APIs provided by the wrapper class, see [Log.java](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/phone/java/com/adobe/marketing/mobile/services/Log.java).
