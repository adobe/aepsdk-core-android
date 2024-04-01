# Adobe Experience Platform Core SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/sdk-bom.svg?logo=android&logoColor=white&label=sdk-bom)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/sdk-bom)

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

## Getting started

Refer to the [getting started](./Documentation/MobileCore/getting-started.md) guide for setting up and using the SDK with your application.

## Development

***Open the project*** To open and run the project, open the `code/build.gradle.kts` file in Android Studio

***Run demo application*** Once you open the project in Android Studio (see above), select the `testapp` runnable and your favorite emulator and run the program.

## Documentation

Additional documentation about API usage and SDK architecture can be found under the [Documentation](./Documentation) directory.

## Related Projects

| Project | Maven | Github |
|---|---|---|
| [Profile](https://developer.adobe.com/client-sdks/documentation/profile/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/userprofile.svg?logo=android&logoColor=white&label=userprofile&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/userprofile) | [Link](https://github.com/adobe/aepsdk-userprofile-android) |
| [Adobe Experience Platform Edge Network](https://developer.adobe.com/client-sdks/documentation/edge-network/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/edge.svg?logo=android&logoColor=white&label=edge&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/edge) | [Link](https://github.com/adobe/aepsdk-edge-android) |
| [Identity for Edge Network](https://developer.adobe.com/client-sdks/documentation/identity-for-edge-network/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/edgeidentity.svg?logo=android&logoColor=white&label=edgeidentity&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/edgeidentity) | [Link](https://github.com/adobe/aepsdk-edgeidentity-android) |
| [Consent for Edge Network](https://developer.adobe.com/client-sdks/documentation/consent-for-edge-network/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/edgeconsent.svg?logo=android&logoColor=white&label=edgeconsent&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/edgeconsent) | [Link](https://github.com/adobe/aepsdk-edgeconsent-android) |
| [Media for Edge Network](https://developer.adobe.com/client-sdks/documentation/media-for-edge-network/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/edgemedia.svg?logo=android&logoColor=white&label=edgemedia&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/edgemedia) | [Link](https://github.com/adobe/aepsdk-edgemedia-android) |
| [Edge Bridge](https://developer.adobe.com/client-sdks/documentation/adobe-analytics/migrate-to-edge-network/) | ![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/edgebridge.svg?logo=android&logoColor=white&label=edgebridge&style=flat-square) | [Link](https://github.com/adobe/aepsdk-edgebridge-android) |
| [Adobe Experience Platform Assurance](https://developer.adobe.com/client-sdks/documentation/platform-assurance-sdk/) | ![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/assurance.svg?logo=android&logoColor=white&label=assurance&style=flat-square) | [Link](https://github.com/adobe/aepsdk-assurance-android)
| [Places Service](https://developer.adobe.com/client-sdks/documentation/places/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/places.svg?logo=android&logoColor=white&label=places&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/places) | [Link](https://github.com/adobe/aepsdk-places-android) |
| [Adobe Analytics](https://developer.adobe.com/client-sdks/documentation/adobe-analytics/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/analytics.svg?logo=android&logoColor=white&label=analytics&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/analytics) | [Link](https://github.com/adobe/aepsdk-analytics-android) |
| [Adobe Streaming Media for Edge Network](https://developer.adobe.com/client-sdks/documentation/media-for-edge-network/) | ![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/edgemedia.svg?logo=android&logoColor=white&label=edgemedia&style=flat-square) | [Link](https://github.com/adobe/aepsdk-edgemedia-android) |
| [Adobe Analytics - Media Analytics for Audio & Video](https://developer.adobe.com/client-sdks/documentation/adobe-media-analytics/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/media.svg?logo=android&logoColor=white&label=media&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/media) | [Link](https://github.com/adobe/aepsdk-media-android) |
| [Adobe Audience Manager](https://developer.adobe.com/client-sdks/documentation/adobe-audience-manager/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/audience.svg?logo=android&logoColor=white&label=audience&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/audience) | [Link](https://github.com/adobe/aepsdk-audience-android) |
| [Adobe Journey Optimizer](https://developer.adobe.com/client-sdks/documentation/adobe-journey-optimizer/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/messaging.svg?logo=android&logoColor=white&label=messaging&style=flat-square)](#) | [Link](https://github.com/adobe/aepsdk-messaging-android) |
| [Adobe Journey Optimizer - Decisioning](https://developer.adobe.com/client-sdks/documentation/adobe-journey-optimizer-decisioning/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/optimize.svg?logo=android&logoColor=white&label=optimize&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/optimize) | [Link](https://github.com/adobe/aepsdk-optimize-android) |
| [Adobe Target](https://developer.adobe.com/client-sdks/documentation/adobe-target/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/target.svg?logo=android&logoColor=white&label=target&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/target) | [Link](https://github.com/adobe/aepsdk-target-android) |
| [Adobe Campaign Standard](https://developer.adobe.com/client-sdks/documentation/adobe-campaign-standard/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/campaign.svg?logo=android&logoColor=white&label=campaign&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/campaign) | [Link](https://github.com/adobe/aepsdk-campaign-android) |
| [Adobe Campaign Classic](https://developer.adobe.com/client-sdks/documentation/adobe-campaign-classic/) | [![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/campaignclassic.svg?logo=android&logoColor=white&label=campaignclassic&style=flat-square)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/campaignclassic) | [Link](https://github.com/adobe/aepsdk-campaignclassic-android) |
| AEP SDK Sample App for Android | - | [Link](https://github.com/adobe/aepsdk-sample-app-android) |

## Contributing

Contributions are welcomed! Read the [Contributing Guide](./.github/CONTRIBUTING.md) for more information.

## Licensing

This project is licensed under the Apache V2 License. See [LICENSE](./LICENSE) for more information.

