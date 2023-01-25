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
import org.junit.Test;

public class OperatorGreaterThanTests {

    private final ConditionEvaluator defaultEvaluator = new ConditionEvaluator();

    /* **************************************************************************
     *  Operand - Contains
     **************************************************************************/
    @Test
    public void test_relationalOperator_Int_vs_Int() {
        // setup
        final Operand<Integer> op1 = new OperandLiteral<>(66);
        final Operand<Integer> op2 = new OperandLiteral<>(55);

        // test
        final RulesResult result1 =
                new ComparisonExpression<>(op1, "greaterThan", op2).evaluate(defaultContext());
        final RulesResult result2 =
                new ComparisonExpression<>(op1, "greaterEqual", op2).evaluate(defaultContext());
        final RulesResult result3 =
                new ComparisonExpression<>(op2, "lessThan", op1).evaluate(defaultContext());
        final RulesResult result4 =
                new ComparisonExpression<>(op2, "lessEqual", op1).evaluate(defaultContext());

        // test failure
        final RulesResult result5 =
                new ComparisonExpression<>(op2, "greaterThan", op1).evaluate(defaultContext());
        final RulesResult result6 =
                new ComparisonExpression<>(op2, "greaterEqual", op1).evaluate(defaultContext());
        final RulesResult result7 =
                new ComparisonExpression<>(op1, "lessThan", op2).evaluate(defaultContext());
        final RulesResult result8 =
                new ComparisonExpression<>(op1, "lessEqual", op2).evaluate(defaultContext());

        // verify
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertTrue(result3.isSuccess());
        assertTrue(result4.isSuccess());

        // verify failure
        assertFalse(result5.isSuccess());
        assertFalse(result6.isSuccess());
        assertFalse(result7.isSuccess());
        assertFalse(result8.isSuccess());
    }

    @Test
    public void test_relationalOperator_SameInt() {
        // setup
        final Operand<Integer> op1 = new OperandLiteral<>(66);

        // test
        final RulesResult result1 =
                new ComparisonExpression<>(op1, "greaterThan", op1).evaluate(defaultContext());
        final RulesResult result2 =
                new ComparisonExpression<>(op1, "greaterEqual", op1).evaluate(defaultContext());
        final RulesResult result3 =
                new ComparisonExpression<>(op1, "lessThan", op1).evaluate(defaultContext());
        final RulesResult result4 =
                new ComparisonExpression<>(op1, "lessEqual", op1).evaluate(defaultContext());

        // verify
        assertFalse(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertFalse(result3.isSuccess());
        assertTrue(result4.isSuccess());
    }

    @Test
    public void test_relationalOperator_Int_vs_Float() {
        // setup
        final Operand<Integer> intOp = new OperandLiteral<>(66);
        final Operand<Float> floatOp = new OperandLiteral<>(55.55f);

        // test
        final RulesResult result =
                new ComparisonExpression<>(intOp, "greaterThan", floatOp)
                        .evaluate(defaultContext());

        // verify
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_relationalOperator_Int_vs_Long() {
        // setup
        final Operand<Integer> intOp = new OperandLiteral<>(66);
        final Operand<Long> longOp = new OperandLiteral<>(22222222222233L);

        // test
        final RulesResult result =
                new ComparisonExpression<>(longOp, "greaterThan", intOp).evaluate(defaultContext());

        // verify
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_relationalOperator_NonNumberOperands() {
        // setup
        final Operand<String> op1 = new OperandLiteral<>("66a");
        final Operand<Integer> op2 = new OperandLiteral<>(66);

        // test
        final RulesResult result1 =
                new ComparisonExpression<>(op1, "greaterThan", op2).evaluate(defaultContext());
        final RulesResult result2 =
                new ComparisonExpression<>(op1, "greaterEqual", op2).evaluate(defaultContext());
        final RulesResult result3 =
                new ComparisonExpression<>(op1, "lessThan", op2).evaluate(defaultContext());
        final RulesResult result4 =
                new ComparisonExpression<>(op1, "lessEqual", op2).evaluate(defaultContext());

        // verify
        assertFalse(result1.isSuccess());
        assertFalse(result2.isSuccess());
        assertFalse(result3.isSuccess());
        assertFalse(result4.isSuccess());
    }

    /* **************************************************************************
     *  Private methods
     **************************************************************************/
    private Context defaultContext() {
        return new Context(new FakeTokenFinder(new HashMap<>()), defaultEvaluator, null);
    }
}
