# DataQueuing

## Overview

`DataQueuing` is a service that provides access to FIFO queues. This service is particularly useful when used in conjunction with a `PersistentHitQueue`.

## Declaration

##### `public interface DataQueuing {}`

## Usage

The following code snippet shows how to create a `DataQueue` and add a `DataEntity` to the queue.

### Java

```java
// Create a `DataQueue`
DataQueue dataqueue = ServiceProvider.getInstance().getDataQueueService().getDataQueue("name");

// Create a `DataEntity`
DataEntity dataEntity = new DataEntity("myData");

// Add entity to `dataQueue`
dataqueue.add(dataEntity);
```

### Kotlin

```kotlin
// Create a `DataQueue`
val dataqueue = ServiceProvider.getInstance().dataQueueService.getDataQueue("name")

// Create a `DataEntity`
val dataEntity = DataEntity("myData")

// Add entity to `dataQueue`
dataqueue.add(dataEntity)
```

## APIs

For a full list of APIs provided by the `DataQueuing` service, see [`DataQueue.java`](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/DataQueue.java).

## Further Reading

Additional types such as [`DataQueue`](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/DataQueue.java), [`DataEntity`](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/DataEntity.java), and [`PersistentHitQueue`](https://github.com/adobe/aepsdk-core-android/blob/staging/code/core/src/main/java/com/adobe/marketing/mobile/services/PersistentHitQueue.java) are useful when using the `DataQueuing` service.
