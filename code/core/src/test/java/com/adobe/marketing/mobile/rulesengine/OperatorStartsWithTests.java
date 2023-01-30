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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class OperatorStartsWithTests {

    private final ConditionEvaluator defaultEvaluator = new ConditionEvaluator();
    private final ConditionEvaluator caseInSensitiveEvaluator =
            new ConditionEvaluator(ConditionEvaluator.Option.CASE_INSENSITIVE);

    @Before
    public void setup() {}

    /* **************************************************************************
     *  Operand - StartsWith
     **************************************************************************/

    @Test
    public void testComparisonExpression_StartsWith_Happy() {
        // setup
        final Operand<String> key = new OperandLiteral<>("mat");
        final Operand<String> op1 = new OperandLiteral<>("mat");
        final Operand<String> op2 = new OperandLiteral<>("match");
        final Operand<String> op3 =
                new OperandLiteral<>("matchme since this sentence starts with match");

        // test when both strings match
        ComparisonExpression<?, ?> expression = new ComparisonExpression<>(op1, "startsWith", key);
        RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
        assertTrue(result.isSuccess());

        // test when string starts with given key
        expression = new ComparisonExpression<>(op2, "startsWith", key);
        result = expression.evaluate(defaultContext(defaultEvaluator));
        assertTrue(result.isSuccess());

        // test when string is a sentence that starts with given key
        expression = new ComparisonExpression<>(op3, "startsWith", key);
        result = expression.evaluate(defaultContext(defaultEvaluator));
        assertTrue(result.isSuccess());
    }

    @Test
    public void testComparisonExpression_StartsWith_UnHappyCases() {
        // setup
        final Operand<String> key = new OperandLiteral<>("300");
        final Operand<Boolean> booleanOperand = new OperandLiteral<>(false);
        final Operand<Integer> numericOperand = new OperandLiteral<>(300);
        final Operand<String> invalidStringOperand = new OperandLiteral<>("3300");

        // test when comparison string is a boolean
        ComparisonExpression<?, ?> expression =
                new ComparisonExpression<>(booleanOperand, "startsWith", key);
        RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
        assertFalse(result.isSuccess());

        // test when comparison string is a numeric
        expression = new ComparisonExpression<>(numericOperand, "startsWith", key);
        result = expression.evaluate(defaultContext(defaultEvaluator));
        assertFalse(result.isSuccess());

        // test when comparison string doesnt not start with the given key
        expression = new ComparisonExpression<>(invalidStringOperand, "startsWith", key);
        result = expression.evaluate(defaultContext(defaultEvaluator));
        assertFalse(result.isSuccess());
    }

    @Test
    public void testComparisonExpression_StartsWith_CaseSensitive() {
        // setup
        final Operand<String> key = new OperandLiteral<>("MatchMe");
        final Operand<String> caseMatch = new OperandLiteral<>("MatchMe I think it will");
        final Operand<String> caseNotMatch = new OperandLiteral<>("matchme i Think nothing");

        // test default evaluator when case matches
        ComparisonExpression<?, ?> expression =
                new ComparisonExpression<>(caseMatch, "startsWith", key);
        RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
        assertTrue(result.isSuccess());

        // test default evaluator when case does not match
        expression = new ComparisonExpression<>(caseNotMatch, "startsWith", key);
        result = expression.evaluate(defaultContext(defaultEvaluator));
        assertFalse(result.isSuccess());

        // test case-insensitive evaluator when case does not match
        expression = new ComparisonExpression<>(caseNotMatch, "startsWith", key);
        result = expression.evaluate(defaultContext(caseInSensitiveEvaluator));
        assertTrue(result.isSuccess());
    }

    /* **************************************************************************
     *  Operand - EndsWith
     **************************************************************************/

    @Test
    public void testComparisonExpression_EndsWith_Happy() {
        // setup
        final Operand<String> key = new OperandLiteral<>("tch");
        final Operand<String> op1 = new OperandLiteral<>("tch");
        final Operand<String> op2 = new OperandLiteral<>("match");
        final Operand<String> op3 =
                new OperandLiteral<>("matchme since this sentence starts with match");

        // test when both strings match
        ComparisonExpression<?, ?> expression = new ComparisonExpression<>(op1, "endsWith", key);
        RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
        assertTrue(result.isSuccess());

        // test when string starts with given key
        expression = new ComparisonExpression<>(op2, "endsWith", key);
        result = expression.evaluate(defaultContext(defaultEvaluator));
        assertTrue(result.isSuccess());

        // test when string is a sentence that starts with given key
        expression = new ComparisonExpression<>(op3, "endsWith", key);
        result = expression.evaluate(defaultContext(defaultEvaluator));
        assertTrue(result.isSuccess());
    }

    @Test
    public void testComparisonExpression_EndsWith_UnHappyCases() {
        // setup
        final Operand<String> key = new OperandLiteral<>("300");
        final Operand<Boolean> booleanOperand = new OperandLiteral<>(false);
        final Operand<Integer> numericOperand = new OperandLiteral<>(300);
        final Operand<String> invalidStringOperand = new OperandLiteral<>("2330");

        // test when comparison string is a boolean
        ComparisonExpression<?, ?> expression =
                new ComparisonExpression<>(booleanOperand, "endsWith", key);
        RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
        assertFalse(result.isSuccess());

        // test when comparison string is a numeric
        expression = new ComparisonExpression<>(numericOperand, "endsWith", key);
        result = expression.evaluate(defaultContext(defaultEvaluator));
        assertFalse(result.isSuccess());

        // test when comparison string doesnt not start with the given key
        expression = new ComparisonExpression<>(invalidStringOperand, "endsWith", key);
        result = expression.evaluate(defaultContext(defaultEvaluator));
        assertFalse(result.isSuccess());
    }

    @Test
    public void testComparisonExpression_EndsWith_CaseInSensitive() {
        // setup
        final Operand<String> key = new OperandLiteral<>("nothing");
        final Operand<String> caseMatch = new OperandLiteral<>("it is nothing");
        final Operand<String> caseNotMatch = new OperandLiteral<>("it is NOTHING");

        // test default evaluator when case matches
        ComparisonExpression<?, ?> expression =
                new ComparisonExpression<>(caseMatch, "endsWith", key);
        RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
        assertTrue(result.isSuccess());

        // test default evaluator when case does not match
        expression = new ComparisonExpression<>(caseNotMatch, "endsWith", key);
        result = expression.evaluate(defaultContext(defaultEvaluator));
        assertFalse(result.isSuccess());

        // test case-insensitive evaluator when case does not match
        expression = new ComparisonExpression<>(caseNotMatch, "endsWith", key);
        result = expression.evaluate(defaultContext(caseInSensitiveEvaluator));
        assertTrue(result.isSuccess());
    }

    /* **************************************************************************
     *  Private methods
     * *************************************************************************/

    private Context defaultContext(final ConditionEvaluator conditionEvaluator) {
        return new Context(new FakeTokenFinder(new HashMap<>()), conditionEvaluator, null);
    }
}
