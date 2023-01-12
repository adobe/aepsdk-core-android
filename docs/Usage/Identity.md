# Identity API Usage

This document lists the APIs provided by Identity extension, along with sample code snippets on how to properly use the APIs.

For more in-depth information about the Identity extension, visit the [official SDK documentation on Identity](https://developer.adobe.com/client-sdks/documentation/mobile-core/identity/).


#### Registering Identity extension

`Identity.EXTENSION` represents a reference to the `IdentityExtension` class that can be registered with `MobileCore` via its `registerExtensions` api.

##### Java

```java
import com.adobe.marketing.mobile.Identity;

MobileCore.registerExtensions(Arrays.asList(Identity.EXTENSION, ...), new AdobeCallback<Object>() {
    // handle callback
});
```

##### Kotlin

```kotlin
import com.adobe.marketing.mobile.Identity

MobileCore.registerExtensions(Arrays.asList(Identity.EXTENSION, ...)){
    // handle callback
}
```


#### Getting Identity extension version

##### Java

```java
final String identityExtensionVersion = Identity.extensionVersion();
```

##### Kotlin

```kotlin
val identityExtensionVersion: String = Identity.extensionVersion()
```

#### Append visitor data to a URL

##### Java

```java
Identity.appendVisitorInfoForURL("https://example.com", new AdobeCallback<String>() {    
    @Override    
    public void call(String urlWithAdobeVisitorInfo) {        
        //handle callback    
    }
});
```

##### Kotlin

```kotlin
Identity.appendVisitorInfoForURL("https://example.com"){ urlWithAdobeVisitorInfo ->
    // handle callback
}
```


#### Get URL Variables

##### Java

```java
Identity.getUrlVariables(new AdobeCallback<String>() {    
    @Override    
    public void call(String stringWithAdobeVisitorInfo) {        
        //handle callaback    
    }
});
```

##### Kotlin

```kotlin
Identity.getUrlVariables{ stringWithAdobeVisitorInfo ->
    // handle callback
}
```


#### Get Identifiers

##### Java

```java
Identity.getIdentifiers(new AdobeCallback<List<VisitorID>>() {    
    @Override    
    public void call(List<VisitorID> idList) {        
         // handle callback
    }

});
```

##### Kotlin

```kotlin
Identity.getIdentifiers{ idList ->
    // handle callback
}
```


#### Get Experience Cloud ID

##### Java

```java
Identity.getExperienceCloudId(new AdobeCallback<String>() {    
    @Override    
    public void call(String experienceCloudId) {        
         //handle callback
    }
});
```

##### Kotlin

```kotlin
Identity.getExperienceCloudId{ experienceCloudId ->
    //handle callback
}
```


#### Sync Identifier

##### Java

```java
Identity.syncIdentifier("idType", "idValue", VisitorID.AuthenticationState.AUTHENTICATED);
```

##### Kotlin

```kotlin
Identity.syncIdentifier("idType", "idValue", VisitorID.AuthenticationState.AUTHENTICATED)
```


#### Sync Identifiers

##### Java

```java
final Map<String, String> identifiers = new HashMap<>();
identifiers.put("idType1", "idValue1");
identifiers.put("idType2", "idValue2");
identifiers.put("idType3", "idValue3");
Identity.syncIdentifiers(identifiers);
```

##### Kotlin

```kotlin
val identifiers: Map<String, String?> = mapOf(
    "idType1" to "idValue1",
    "idType2" to "idValue2",
    "idType3" to "idValue3"
)
Identity.syncIdentifiers(identifiers);
```


#### Sync Identifiers with Authentication State

##### Java

```java
final Map<String, String> identifiers = new HashMap<>();
identifiers.put("idType1", "idValue1");
identifiers.put("idType2", "idValue2");
identifiers.put("idType3", "idValue3");
Identity.syncIdentifiers(identifiers, VisitorID.AuthenticationState.AUTHENTICATED);
```

##### Kotlin

```kotlin
val identifiers: Map<String, String?> = mapOf(
    "idType1" to "idValue1",
    "idType2" to "idValue2",
    "idType3" to "idValue3"
)
Identity.syncIdentifiers(identifiers, VisitorID.AuthenticationState.AUTHENTICATED);
```
