# Caching

## Overview

`Caching` describes a service that temporarily holds data and supports read and write operations. The default implementation of the `Caching` is provided by the `FileCacheService`, which cached items on the disk.

## Declaration

##### `public interface CacheService {}`

## Usage

The following code snippet shows how to set/get a `CacheEntry` to the local cache.

### Java

```java
CacheEntry cacheEntry = new CacheEntry(inputStream, CacheExpiry.never(), metadata);

ServiceProvider.getInstance().getCacheService().set("cache_name", "key", cacheEntry);
```

### Kotlin

```kotlin
val cacheEntry = CacheEntry(inputStream, CacheExpiry.never(), metadata)

ServiceProvider.getInstance().cacheService.set("cache_name", "key", cacheEntry)
```

## APIs

For a full list of APIs provided by the `CacheService`, see [CacheService.java](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/caching/CacheService.java).
