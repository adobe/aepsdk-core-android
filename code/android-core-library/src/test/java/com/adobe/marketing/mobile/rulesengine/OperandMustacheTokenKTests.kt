/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.rulesengine

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class OperandMustacheTokenKTests {
    private val defaultEvaluator: ConditionEvaluator =
        ConditionEvaluator(ConditionEvaluator.Option.DEFAULT)

    @Before
    fun setup() {
    }

    @Test
    fun testPassingJavaTypesInKotlin() {
        // setup
        val tokenString = OperandMustacheToken(
            "{{Hero}}",
            String::class.java
        )
        val tokenInteger = OperandMustacheToken(
            "{{integerToken}}",
            Number::class.java
        )
        val tokenBoolean = OperandMustacheToken(
            "{{booleanToken}}",
            java.lang.Boolean::class.java
        )
        val tokenDouble = OperandMustacheToken(
            "{{doubleToken}}",
            Number::class.java
        )
        val tokenFloat = OperandMustacheToken(
            "{{floatToken}}",
            Number::class.java
        )

        // test
        val result1 = tokenString.resolve(defaultContext(defaultEvaluator))
        val result2 = tokenInteger.resolve(defaultContext(defaultEvaluator))
        val result3 = tokenBoolean.resolve(defaultContext(defaultEvaluator))
        val result4 = tokenDouble.resolve(defaultContext(defaultEvaluator))
        val result5 = tokenFloat.resolve(defaultContext(defaultEvaluator))

        // verify
        Assert.assertEquals("Soldier", result1)
        Assert.assertEquals(33, result2)
        Assert.assertEquals(false, result3)
        Assert.assertEquals(3.3, result4)
        Assert.assertEquals(3.3f, result5)
    }

    /* **************************************************************************
	 *  Private methods
	 **************************************************************************/
    private fun defaultContext(conditionEvaluator: ConditionEvaluator): Context {
        val context = HashMap<String, Any>()
        context["Beer"] = "Corona"
        context["Hero"] = "Soldier"
        context["Soda"] = "Pepsi"
        context["answer"] = "Corona extra"
        context["integerToken"] = 33
        context["doubleToken"] = 3.3
        context["floatToken"] = 3.3f
        context["booleanToken"] = false
        return Context(FakeTokenFinder(context), conditionEvaluator, FakeTransformer.create())
    }
}
