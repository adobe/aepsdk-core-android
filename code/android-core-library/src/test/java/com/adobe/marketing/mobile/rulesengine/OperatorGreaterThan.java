/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
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
 ******************************************************************************/

package com.adobe.marketing.mobile.rulesengine;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OperatorGreaterThan {

	private ConditionEvaluator defaultEvaluator = new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT);

	/* **************************************************************************
	 *  Operand - Contains
	 **************************************************************************/
	@Test
	public void test_relationalOperator_Int_vs_Int() {
		// setup
		final Operand op1 = new OperandLiteral(66);
		final Operand op2 = new OperandLiteral(55);

		// test
		final RulesResult result1 = new ComparisonExpression(op1, "greaterThan", op2).evaluate(defaultContext());
		final RulesResult result2 = new ComparisonExpression(op1, "greaterEqual", op2).evaluate(defaultContext());
		final RulesResult result3 = new ComparisonExpression(op2, "lessThan", op1).evaluate(defaultContext());
		final RulesResult result4 = new ComparisonExpression(op2, "lessEqual", op1).evaluate(defaultContext());

		// test failure
		final RulesResult result5 = new ComparisonExpression(op2, "greaterThan", op1).evaluate(defaultContext());
		final RulesResult result6 = new ComparisonExpression(op2, "greaterEqual", op1).evaluate(defaultContext());
		final RulesResult result7 = new ComparisonExpression(op1, "lessThan", op2).evaluate(defaultContext());
		final RulesResult result8 = new ComparisonExpression(op1, "lessEqual", op2).evaluate(defaultContext());

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
		final Operand op1 = new OperandLiteral(66);

		// test
		final RulesResult result1 = new ComparisonExpression(op1, "greaterThan", op1).evaluate(defaultContext());
		final RulesResult result2 = new ComparisonExpression(op1, "greaterEqual", op1).evaluate(defaultContext());
		final RulesResult result3 = new ComparisonExpression(op1, "lessThan", op1).evaluate(defaultContext());
		final RulesResult result4 = new ComparisonExpression(op1, "lessEqual", op1).evaluate(defaultContext());

		// verify
		assertFalse(result1.isSuccess());
		assertTrue(result2.isSuccess());
		assertFalse(result3.isSuccess());
		assertTrue(result4.isSuccess());
	}

	@Test
	public void test_relationalOperator_Int_vs_Float() {
		// setup
		final Operand intOp = new OperandLiteral(66);
		final Operand floatOp = new OperandLiteral(55.55);

		// test
		final RulesResult result = new ComparisonExpression(intOp, "greaterThan", floatOp).evaluate(defaultContext());

		// verify
		assertTrue(result.isSuccess());
	}

	@Test
	public void test_relationalOperator_Int_vs_Long() {
		// setup
		final Operand intOp = new OperandLiteral(66);
		final Operand longtOp = new OperandLiteral(22222222222.233);

		// test
		final RulesResult result = new ComparisonExpression(longtOp, "greaterThan", intOp).evaluate(defaultContext());

		// verify
		assertTrue(result.isSuccess());
	}


	/* **************************************************************************
	 *  Private methods
	 **************************************************************************/
	private Context defaultContext() {
		return new Context(new FakeTokenFinder(new HashMap<>()), defaultEvaluator, null);
	}

}
