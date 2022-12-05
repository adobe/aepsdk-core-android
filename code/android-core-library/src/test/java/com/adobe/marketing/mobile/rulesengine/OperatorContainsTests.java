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

import static org.junit.Assert.*;

import java.util.HashMap;
import org.junit.Test;

public class OperatorContainsTests {

    private final ConditionEvaluator defaultEvaluator =
            new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT);
    private final ConditionEvaluator caseSensitiveEvaluator =
            new ConditionEvaluator(ConditionEvaluator.Option.CASE_INSENSITIVE);

    /* **************************************************************************
     *  Operand - Contains
     **************************************************************************/
    @Test
    public void test_operator_contains() {
        // setup
        final Operand<String> op1 =
                new OperandLiteral<>("This is a big sentence that contains, contains.");
        final Operand<String> op2 = new OperandLiteral<>(", contains.");

        // test
        final ComparisonExpression<String, String> expression =
                new ComparisonExpression<>(op1, "contains", op2);
        final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));

        // verify
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_operator_contains_notHappy() {
        // setup
        final Operand<String> op1 =
                new OperandLiteral<>("This is a big sentence that contains, contains.");
        final Operand<String> op2 = new OperandLiteral<>("this isn't present");

        // test
        final ComparisonExpression<String, String> expression =
                new ComparisonExpression<>(op1, "contains", op2);
        final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));

        // verify
        assertFalse(result.isSuccess());
        assertEquals(
                "Condition not matched for operation \"contains\"", result.getFailureMessage());
    }

    /* **************************************************************************
     *  Operand - Not Contains
     **************************************************************************/
    @Test
    public void test_operator_notContains() {
        // setup
        final Operand<String> op1 = new OperandLiteral<>("This doesn't have the secret word.");
        final Operand<String> op2 = new OperandLiteral<>("contains");

        // test
        final ComparisonExpression<String, String> expression =
                new ComparisonExpression<>(op1, "notContains", op2);
        final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));

        // verify
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_operator_notContains_notHappy() {
        // setup
        final Operand<String> op1 =
                new OperandLiteral<>("This is a big sentence that contains, contains.");
        final Operand<String> op2 = new OperandLiteral<>("contains");

        // test
        final ComparisonExpression<String, String> expression =
                new ComparisonExpression<>(op1, "notContains", op2);
        final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));

        // verify
        assertFalse(result.isSuccess());
        assertEquals(
                "Condition not matched for operation \"notContains\"", result.getFailureMessage());
    }

    @Test
    public void test_operator_contain_caseSensitive() {
        // setup
        final Operand<String> op1 =
                new OperandLiteral<>("This is a big sentence that contains, contains.");
        final Operand<String> op2 = new OperandLiteral<>(", CONTAINS.");

        // test
        final ComparisonExpression<String, String> expression =
                new ComparisonExpression<>(op1, "contains", op2);
        final RulesResult result = expression.evaluate(defaultContext(caseSensitiveEvaluator));

        // verify
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_operator_contain_nonStringOperands() {
        // Always returns false
        // setup
        final Operand<String> op1 = new OperandLiteral<>("11");
        final Operand<Integer> op2 = new OperandLiteral<>(1);

        // test
        final ComparisonExpression<String, Integer> expression =
                new ComparisonExpression<>(op1, "contains", op2);
        final RulesResult result = expression.evaluate(defaultContext(caseSensitiveEvaluator));

        // verify
        assertFalse(result.isSuccess());
        assertEquals(
                "Condition not matched for operation \"contains\"", result.getFailureMessage());
    }

    @Test
    public void test_operator_nonContain_nonStringOperands() {
        // Always returns true
        // setup
        final Operand<String> op1 = new OperandLiteral<>("11");
        final Operand<Integer> op2 = new OperandLiteral<>(1);

        // test
        final ComparisonExpression<String, Integer> expression =
                new ComparisonExpression<>(op1, "notContains", op2);
        final RulesResult result = expression.evaluate(defaultContext(caseSensitiveEvaluator));

        // verify
        assertTrue(result.isSuccess());
    }

    @Test
    public void testComparisonExpression_InvalidArguments() {
        // test
        ComparisonExpression<String, String> expression =
                new ComparisonExpression<>(null, "contains", new OperandLiteral<>("One"));
        ComparisonExpression<String, String> expression1 =
                new ComparisonExpression<>(new OperandLiteral<>("One"), "contains", null);
        ComparisonExpression<String, String> expression2 =
                new ComparisonExpression<>(
                        new OperandLiteral<>("One"), null, new OperandLiteral<>("One"));

        assertFalse(expression.evaluate(defaultContext(caseSensitiveEvaluator)).isSuccess());
        assertFalse(expression1.evaluate(defaultContext(caseSensitiveEvaluator)).isSuccess());
        assertFalse(expression2.evaluate(defaultContext(caseSensitiveEvaluator)).isSuccess());
    }

    @Test
    public void test_unknown_operator() {
        // Always returns true
        // setup
        final Operand<String> op1 = new OperandLiteral<>("11");
        final Operand<Integer> op2 = new OperandLiteral<>(1);

        // test
        final ComparisonExpression<String, Integer> expression =
                new ComparisonExpression<>(op1, "invalid", op2);
        final RulesResult result = expression.evaluate(defaultContext(caseSensitiveEvaluator));

        // verify
        assertFalse(result.isSuccess());
    }

    /* **************************************************************************
     *  Private methods
     **************************************************************************/

    private Context defaultContext(final ConditionEvaluator conditionEvaluator) {
        return new Context(new FakeTokenFinder(new HashMap<>()), conditionEvaluator, null);
    }
}
