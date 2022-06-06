package com.adobe.marketing.mobile.test.utility

internal fun readTestResources(filePath: String): String? {
    return object {}.javaClass.classLoader.getResource(filePath)?.readText()
}
