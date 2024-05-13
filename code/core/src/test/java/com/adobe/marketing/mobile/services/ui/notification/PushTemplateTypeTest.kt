package com.adobe.marketing.mobile.services.ui.notification

import org.junit.Test
import kotlin.test.assertEquals


class PushTemplateTypeTest {

    @Test
    fun `test fromString returns BASIC value`() {
        val templateType = PushTemplateType.fromString("basic")
        assertEquals(templateType, PushTemplateType.BASIC)
    }

    @Test
    fun `test fromString returns CAROUSEL value`() {
        val templateType = PushTemplateType.fromString("car")
        assertEquals(templateType, PushTemplateType.CAROUSEL)
    }

    @Test
    fun `test fromString returns INPUT_BOX value`() {
        val templateType = PushTemplateType.fromString("input")
        assertEquals(templateType, PushTemplateType.INPUT_BOX)
    }

    @Test
    fun `test fromString returns UNKNOWN value`() {
        val templateType = PushTemplateType.fromString("templateType")
        assertEquals(templateType, PushTemplateType.UNKNOWN)
    }
}