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

package com.adobe.marketing.mobile.rulesengine;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import org.junit.Test;

public class OperandFunctionTests {

    private final ConditionEvaluator defaultEvaluator =
            new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT);

    @Test
    public void test_OperandFunction() {
        // setup
        final Operand<String> stringOperand = new OperandLiteral<>("bossbaby");
        final OperandFunction<String> functionOperator =
                new OperandFunction<>(
                        (Object... args) -> {
                            StringBuilder builder = new StringBuilder();
                            for (Object each : args) {
                                builder.append(each.toString());
                            }
                            return builder.toString();
                        },
                        "boss",
                        "baby");

        // test
        final RulesResult result =
                new ComparisonExpression<>(stringOperand, "equals", functionOperator)
                        .evaluate(defaultContext(defaultEvaluator));

        // verify
        assertTrue(result.isSuccess());
    }

    /* **************************************************************************
     *  Private methods
     **************************************************************************/
    private Context defaultContext(final ConditionEvaluator conditionEvaluator) {
        HashMap<String, Object> context = new HashMap<>();
        context.put("Beer", "Corona");
        context.put("Hero", "Soldier");
        context.put("Soda", "Pepsi");
        context.put("answer", "Corona extra");
        context.put("integerToken", 33);
        context.put("booleanToken", false);
        return new Context(
                new FakeTokenFinder(context), conditionEvaluator, FakeTransformer.create());
    }
}
