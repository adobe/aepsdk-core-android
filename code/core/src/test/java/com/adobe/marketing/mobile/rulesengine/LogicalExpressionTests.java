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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

public class LogicalExpressionTests {

    private final ComparisonExpression<String, String> expressionTrue =
            new ComparisonExpression<>(
                    new OperandLiteral<>("One"), "equals", new OperandLiteral<>("One"));
    private final ComparisonExpression<String, String> expressionFalse =
            new ComparisonExpression<>(
                    new OperandLiteral<>("One"), "notEquals", new OperandLiteral<>("One"));

    @Test
    public void testLogicalExpression_NullOperands() {
        // setup
        List<Evaluable> operands = new ArrayList<>();
        operands.add(expressionTrue);
        operands.add(expressionTrue);
        operands.add(null);

        // test
        RulesResult result = new LogicalExpression(operands, "and").evaluate(defaultContext());
        assertTrue(result.isSuccess());

        // setup again
        operands.add(expressionFalse);

        // test
        RulesResult result2 = new LogicalExpression(operands, "and").evaluate(defaultContext());
        assertFalse(result2.isSuccess());
        assertEquals("AND operation returned false.", result2.getFailureMessage());
    }

    @Test
    public void testLogicalExpression_AND() {
        // setup
        List<Evaluable> operands = new ArrayList<>();
        operands.add(expressionTrue);
        operands.add(expressionTrue);

        // test
        RulesResult result = new LogicalExpression(operands, "and").evaluate(defaultContext());
        assertTrue(result.isSuccess());

        // setup again
        operands.add(expressionFalse);

        // test
        RulesResult result2 = new LogicalExpression(operands, "and").evaluate(defaultContext());
        assertFalse(result2.isSuccess());
        assertEquals("AND operation returned false.", result2.getFailureMessage());
    }

    @Test
    public void testLogicalExpression_OR() {
        // setup
        List<Evaluable> operands = new ArrayList<>();
        operands.add(expressionFalse);
        operands.add(expressionFalse);

        // test
        RulesResult result = new LogicalExpression(operands, "or").evaluate(defaultContext());
        assertFalse(result.isSuccess());
        assertEquals("OR operation returned false.", result.getFailureMessage());

        // setup again
        operands.add(expressionTrue);

        // test
        RulesResult result2 = new LogicalExpression(operands, "or").evaluate(defaultContext());
        assertTrue(result2.isSuccess());
    }

    @Test
    public void testLogicalExpression_UnknownOperator() {
        // setup
        List<Evaluable> operands = new ArrayList<>();
        operands.add(expressionTrue);
        operands.add(expressionTrue);

        // test
        RulesResult result = new LogicalExpression(operands, "equal").evaluate(defaultContext());
        assertFalse(result.isSuccess());
        assertEquals("Unknown conjunction operator - equal.", result.getFailureMessage());
    }

    private Context defaultContext() {
        return new Context(
                new FakeTokenFinder(new HashMap<>()),
                new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT),
                null);
    }
}
