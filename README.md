# Adobe Experience Platform Core SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/core.svg?logo=android&logoColor=white&label=core)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/core)
[![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/identity.svg?logo=android&logoColor=white&label=identity)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/identity)
[![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/signal.svg?logo=android&logoColor=white&label=signal)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/signal)
[![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/lifecycle.svg?logo=android&logoColor=white&label=lifecycle)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/lifecycle)

## About this project

The `MobileCore` and `Identity` extensions represent the foundation of the Adobe Experience Platform SDK. Every app using the SDK must include them. These modules contain a common set of functionality and services which are required by all SDK extensions.

`MobileCore` contains the implementation of the Event Hub. The Event Hub is the mechanism used for delivering events between the app and the SDK. The Event Hub is also used for sharing data between extensions and provides several reusable implementations needed for platform support, including networking, disk access, and database management.

`Identity` implements the integration with Adobe Experience Platform Identity services.

`Signal` represents the Adobe Experience Platform SDK's `Signal` extension that allows marketers to send a "signal" to their apps to send data to external destinations or to open URLs. 

`Lifecycle` represents the Adobe Experience Platform SDK's `Lifecycle` extension that helps collect application Lifecycle metrics, such as application install or upgrade information, application launch and session information, device information, and any additional context data provided by the application developer. 

## Requirements

- Android API 19 (Kitkat) and newer.

## Installing the AEP SDK for Android

Installation via [Maven](https://maven.apache.org/) & [Gradle](https://gradle.org/) is the easiest and recommended way to get the AEP SDK into your Android app.  In your `build.gradle` file, include the latest version of following dependencies:

> The following 2.x.x should be replaced with the latest version numbers.

```gradle
implementation 'com.adobe.marketing.mobile:core:2.x.x'
implementation 'com.adobe.marketing.mobile:identity:2.x.x'
implementation 'com.adobe.marketing.mobile:signal:2.x.x'
implementation 'com.adobe.marketing.mobile:lifecycle:2.x.x'
```

## Development

***Open the project*** To open and run the project, open the `code/build.gradle` file in Android Studio

***Run demo application*** Once you open the project in Android Studio (see above), select the `testapp` or `testapp-kotlin` runnable and your favorite emulator and run the program.

## Related Projects

| Project                                                      | Description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [AEP SDK Sample App for Android](https://github.com/adobe/aepsdk-sample-app-android) | Contains Android sample app for the AEP SDK. |
| [AEP Edge SDK for Android](https://github.com/adobe/aepsdk-edge-android) | Provides support to the Experience Platform Edge for the AEP SDK.
 |
 | [AEP Assurance SDK for Android](https://github.com/adobe/aepsdk-assurance-android) |  Provides support to inspect and validate AEP SDK by integrating with Adobe Experience Platform Assurance. 
 |

## Documentation

Additional documentation for usage and SDK architecture can be found under the [Documentation](Documentation) directory.

## Contributing

Contributions are welcomed! Read the [Contributing Guide](./.github/CONTRIBUTING.md) for more information.

## Licensing

This project is licensed under the Apache V2 License. See [LICENSE](LICENSE) for more information.

