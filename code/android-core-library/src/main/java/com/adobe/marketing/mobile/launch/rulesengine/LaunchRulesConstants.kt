/* ************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2022 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

package com.adobe.marketing.mobile.launch.rulesengine

internal object LaunchRulesConstants {
    const val LOG_MODULE_PREFIX = "Launch Rules Engine"
    const val DATA_STORE_PREFIX = "com.adobe.module.rulesengine"

    internal object Transform {
        const val URL_ENCODING_FUNCTION = "urlenc"
        const val TRANSFORM_TO_INT = "int"
        const val TRANSFORM_TO_DOUBLE = "double"
        const val TRANSFORM_TO_STRING = "string"
        const val TRANSFORM_TO_BOOL = "bool"
    }
}