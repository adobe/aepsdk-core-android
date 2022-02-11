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
import static org.junit.Assert.*;

public class OperatorContainsTests {

	private ConditionEvaluator defaultEvaluator = new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT);
	private ConditionEvaluator caseSensitiveEvaluator = new ConditionEvaluator(ConditionEvaluator.Option.CASE_INSENSITIVE);

	/* **************************************************************************
	 *  Operand - Contains
	 **************************************************************************/
	@Test
	public void test_operator_contains() {
		// setup
		final Operand op1 = new OperandLiteral("This is a big sentence that contains, contains.");
		final Operand op2 = new OperandLiteral(", contains.");

		// test
		final ComparisonExpression expression = new ComparisonExpression(op1, "contains", op2);
		final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));

		// verify
		assertTrue(result.isSuccess());
	}

	@Test
	public void test_operator_contains_notHappy() {
		// setup
		final Operand op1 = new OperandLiteral("This is a big sentence that contains, contains.");
		final Operand op2 = new OperandLiteral("this isn't present");

		// test
		final ComparisonExpression expression = new ComparisonExpression(op1, "contains", op2);
		final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));

		// verify
		assertFalse(result.isSuccess());
		assertEquals("Condition not matched for operation \"contains\"", result.getFailureMessage());
	}

	/* **************************************************************************
	 *  Operand - Not Contains
	 **************************************************************************/
	@Test
	public void test_operator_notContains() {
		// setup
		final Operand op1 = new OperandLiteral("This doesn't have the secret word.");
		final Operand op2 = new OperandLiteral("contains");

		// test
		final ComparisonExpression expression = new ComparisonExpression(op1, "notContains", op2);
		final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));

		// verify
		assertTrue(result.isSuccess());
	}

	@Test
	public void test_operator_notContains_notHappy() {
		// setup
		final Operand op1 = new OperandLiteral("This is a big sentence that contains, contains.");
		final Operand op2 = new OperandLiteral("contains");

		// test
		final ComparisonExpression expression = new ComparisonExpression(op1, "notContains", op2);
		final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));

		// verify
		assertFalse(result.isSuccess());
		assertEquals("Condition not matched for operation \"notContains\"", result.getFailureMessage());
	}

	@Test
	public void test_operator_contain_caseSensitive() {
		// setup
		final Operand op1 = new OperandLiteral("This is a big sentence that contains, contains.");
		final Operand op2 = new OperandLiteral(", CONTAINS.");

		// test
		final ComparisonExpression expression = new ComparisonExpression(op1, "contains", op2);
		final RulesResult result = expression.evaluate(defaultContext(caseSensitiveEvaluator));

		// verify
		assertTrue(result.isSuccess());
	}


	/* **************************************************************************
	 *  Private methods
	 **************************************************************************/

	private Context defaultContext(final ConditionEvaluator conditionEvaluator) {
		return new Context(new FakeTokenFinder(new HashMap<>()), conditionEvaluator, null);
	}
}
