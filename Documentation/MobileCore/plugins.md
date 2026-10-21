# Plugins

> Plugins are available for **Android only**, starting with Mobile Core **3.10.0**.

Plugins are optional modules that add a specific capability to an Adobe Experience Platform Mobile SDK extension. For example, the Adobe Journey Optimizer extension uses the Live Updates plugin to display Android Live Updates, and the UI Builder plugin to display Adobe Journey Optimizer push templates.

You add plugins to Mobile Core when your app starts, before you register extensions. When an extension needs a capability, it uses the matching plugin that your app registered. If your app does not use a capability, you do not need to include or register its plugin.

## Plugins and extensions

Extensions and plugins are both modules that you add to your app, but they serve different purposes.

|  | Extensions | Plugins |
| --- | --- | --- |
| **Purpose** | Provide the features of an Adobe solution or service, such as Analytics, Edge Network, or Adobe Journey Optimizer. | Provide a specific capability to an extension, such as rendering push templates. |
| **Registration** | Registered with the `MobileCore.initialize` or `MobileCore.registerExtensions` api. | Registered with the [`MobileCore.addPlugins`](./api-reference.md#registering-plugins) api. |
| **How they work** | First-class components of the SDK. Each extension supports a set of related features and provides public APIs that your app uses to interact with the extension. | Additional, optional features that are developed separately from extensions, so your app can include them only if needed. |
| **Requirements** | Depend only on their core dependencies and must be registered with Mobile Core. | Used by extensions, so they must be added before the extensions that use them are registered. |
| **When not added** | The features of the extension are not available. | The features of the plugin are not available; the extension that needs the capability logs a warning and continues without it. |

## Plugin contracts

Mobile Core defines the plugin contracts in the `com.adobe.marketing.mobile.plugin` package:

* [`IAepPlugin`](../../code/core/src/main/java/com/adobe/marketing/mobile/plugin/IAepPlugin.kt) - the marker interface every plugin implements. It carries no methods of its own; it only identifies a type as a plugin that can be registered with and resolved by `MobileCore`.
* [`ILiveupdatePlugin`](../../code/core/src/main/java/com/adobe/marketing/mobile/plugin/ILiveupdatePlugin.kt) - the contract for handling a Live Update push. Implemented by the `LiveUpdatePlugin` class shipped in the `liveupdates` artifact.
* [`IUiTemplatePlugin`](../../code/core/src/main/java/com/adobe/marketing/mobile/plugin/IUiTemplatePlugin.kt) - the contract for rendering an out-of-the-box push-template notification. Implemented by the `NotificationBuilderPlugin` class shipped in the `notificationbuilder` artifact.

Core only owns the contracts and the registry. The concrete plugin implementations, their dependencies, and their minimum versions are documented with the extension that uses them; see [Mobile Core plugins](https://developer.adobe.com/client-sdks/documentation/mobile-core/plugins/) on the public SDK documentation site for the Live Updates and UI Builder plugins.

## Add plugins to your app

Register your plugins with the `MobileCore.addPlugins` api in the `onCreate` method of your `Application` class, before you initialize the SDK. This ensures that plugins are available when extensions start processing, including when your app is started by a push notification.

You can register one or more plugins in a single call. Registering the same plugin instance more than once has no effect.

### Java

```java
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.messaging.liveupdate.LiveUpdatePlugin;
import com.adobe.marketing.mobile.notificationbuilder.NotificationBuilderPlugin;
...
import android.app.Application;
...
public class MainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MobileCore.addPlugins(
            new NotificationBuilderPlugin(),
            new LiveUpdatePlugin(new MyLiveUpdateStyleProvider())
        );

        MobileCore.initialize(this, "ENVIRONMENT_ID");
    }
}
```

### Kotlin

```kotlin
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.messaging.liveupdate.LiveUpdatePlugin
import com.adobe.marketing.mobile.notificationbuilder.NotificationBuilderPlugin
...
import android.app.Application
...
class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileCore.addPlugins(
            NotificationBuilderPlugin(),
            LiveUpdatePlugin(MyLiveUpdateStyleProvider())
        )

        MobileCore.initialize(this, "ENVIRONMENT_ID")
    }
}
```

In the examples above, `MyLiveUpdateStyleProvider` is a class in your app that implements `ILiveUpdateStyleProvider` and defines how Live Update notifications are styled.

## How extensions resolve plugins

Extensions look up the plugin they need with the `MobileCore.getPlugin` api, passing the plugin contract type. Apps typically do not call this api; it is used internally by extensions such as Adobe Journey Optimizer.

```kotlin
val plugin = MobileCore.getPlugin(ILiveupdatePlugin::class.java)
```

If no plugin is registered for the requested type, `getPlugin` returns `null` and the calling extension is responsible for falling back gracefully, for example by logging a warning and skipping the capability.

## Next steps

* [Mobile Core API reference](./api-reference.md#registering-plugins)
* [Mobile Core plugins](https://developer.adobe.com/client-sdks/documentation/mobile-core/plugins/) on the public SDK documentation site
