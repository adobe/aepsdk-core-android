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

    @Test
    fun `test getValue returns basic value for BASIC`() {
        assertEquals(PushTemplateType.BASIC.value, "basic")
    }

    @Test
    fun `test getValue returns car value for CAROUSEL`() {
        assertEquals(PushTemplateType.CAROUSEL.value, "car")
    }

    @Test
    fun `test getValue returns input value for INPUT_BOX`() {
        assertEquals(PushTemplateType.INPUT_BOX.value, "input")
    }

    @Test
    fun `test getValue returns unknown value for UNKNOWN`() {
        assertEquals(PushTemplateType.UNKNOWN.value, "unknown")
    }
}