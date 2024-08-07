package com.adobe.marketing.mobile.internal

import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.internal.eventhub.Tenant

abstract class TenantAwareExtension(extensionApi: ExtensionApi, tenant: Tenant) :
    Extension(extensionApi)
