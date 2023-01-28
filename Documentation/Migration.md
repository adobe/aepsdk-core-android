# Migrate an existing Android app to AEP Android SDK 2.0

This document describes how existing Android SDK 1.x customers can upgrade to the Android SDK 2.0 without introducing any breaking changes. We have made upgrading seamless, which will ensure that APIs between SDK 1.x and 2.0 are compatible.

## Upgrading

### Gradle

To upgrade, update the Gradle dependency references to `Android SDK 2.0` in the Android app's `build.gradle`:

> **Warning**
> Using dynamic dependency versions is not recommended for production apps. Refer to this [page](./MobileCore/gradle-dependencies.md) for managing gradle dependencies.

```diff
- implementation 'com.adobe.marketing.mobile:sdk-core:1.+'

+ implementation 'com.adobe.marketing.mobile:core:2.+'
+ implementation 'com.adobe.marketing.mobile:identity:2.+'
+ implementation 'com.adobe.marketing.mobile:lifecycle:2.+'
+ implementation 'com.adobe.marketing.mobile:signal:2.+'
```

Then `sync project with the gradle files` and build the project.

## Sample Apps

To see more examples of integrating with the Android SDK 2.0, head over to the sample apps.

[View Samples](https://github.com/adobe/aepsdk-sample-app-android)

## Next Steps

- Get familiar with the various APIs offered by the Android SDK 2.0 by checking out the [Mobile Core API reference](./MobileCore/api-reference.md)
- To leverage shared services offered by the Android SDK, check out the [Services documentation](./Services/README.md).
- To build an extension on top of the Android SDK, check out the [Building Extensions documentation](./EventHub/BuildingExtensions.md).
- Verify an SDK implementation with [Assurance](./Debugging.md).
