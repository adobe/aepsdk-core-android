# DataStoring

## Overview

The `DataStoring` service provides shared functionality to read and write values to local storage.

## Declaration

##### `public interface DataStoring {}`

## Usage

The following code snippet shows how to create a `NamedCollection` object and set/get a key-value pair.

### Java

```java
NamedCollection namedCollection = ServiceProvider.getInstance().getDataStoreService().getNamedCollection("name");
namedCollection.setString("key", "value");
String value = namedCollection.getString("key", "default_value");

```

### Kotlin

```kotlin
val namedCollection =
            ServiceProvider.getInstance().dataStoreService.getNamedCollection("name")
namedCollection.setString("key", "value")
val value = namedCollection.getString("key", "default_value")
```

## APIs

For a full list of APIs provided by the `DataStoring` service see [DataStoring.java](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/DataStoring.java) and [NamedCollection.java](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/NamedCollection.java).
