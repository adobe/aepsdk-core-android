-keep class com.adobe.marketing.mobile.* {
    <init>(...);
}

# covers all extensions that use these classes, keeps the class and constructor, other members are obfuscated
-keep class * extends com.adobe.marketing.mobile.Extension {
   <init>(...);
}
-keep class * extends com.adobe.marketing.mobile.ExtensionListener {
   <init>(...);
}