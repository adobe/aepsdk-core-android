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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OperatorStartsWithTests {
	private ConditionEvaluator defaultEvaluator = new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT);
	private ConditionEvaluator caseInSensitiveEvaluator = new ConditionEvaluator(
		ConditionEvaluator.Option.CASE_INSENSITIVE);

	@Before()
	public void setup() { }

	/* **************************************************************************
	 *  Operand - StartsWith
	 **************************************************************************/

	@Test
	public void testComparisonExpression_StartsWith_Happy() {
		// setup
		final Operand key = new OperandLiteral("mat");
		final Operand op1 = new OperandLiteral("mat");
		final Operand op2 = new OperandLiteral("match");
		final Operand op3 = new OperandLiteral("matchme since this sentence starts with match");


		// test when both strings match
		ComparisonExpression expression = new ComparisonExpression(op1, "startsWith", key);
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());

		// test when string starts with given key
		expression = new ComparisonExpression(op2, "startsWith", key);
		result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());

		// test when string is a sentence that starts with given key
		expression = new ComparisonExpression(op3, "startsWith", key);
		result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_StartsWith_UnHappyCases() {
		// setup
		final Operand key = new OperandLiteral("300");
		final Operand booleanOperand = new OperandLiteral(false);
		final Operand numericOperand = new OperandLiteral(300);
		final Operand invalidStringOperand = new OperandLiteral("3300");

		// test when comparison string is a boolean
		ComparisonExpression expression = new ComparisonExpression(booleanOperand, "startsWith", key);
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertFalse(result.isSuccess());

		// test when comparison string is a numeric
		expression = new ComparisonExpression(numericOperand, "startsWith", key);
		result = expression.evaluate(defaultContext(defaultEvaluator));
		assertFalse(result.isSuccess());

		// test when comparison string doesnt not start with the given key
		expression = new ComparisonExpression(invalidStringOperand, "startsWith", key);
		result = expression.evaluate(defaultContext(defaultEvaluator));
		assertFalse(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_StartsWith_CaseSensitive() {
		// setup
		final Operand key = new OperandLiteral("MatchMe");
		final Operand caseMatch = new OperandLiteral("MatchMe I think it will");
		final Operand caseNotMatch = new OperandLiteral("matchme i Think nothing");


		// test default evaluator when case matches
		ComparisonExpression expression = new ComparisonExpression(caseMatch, "startsWith", key);
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());

		// test default evaluator when case does not match
		expression = new ComparisonExpression(caseNotMatch, "startsWith", key);
		result = expression.evaluate(defaultContext(defaultEvaluator));
		assertFalse(result.isSuccess());

		// test case-insensitive evaluator when case does not match
		expression = new ComparisonExpression(caseNotMatch, "startsWith", key);
		result = expression.evaluate(defaultContext(caseInSensitiveEvaluator));
		assertTrue(result.isSuccess());
	}


	/* **************************************************************************
	 *  Operand - EndsWith
	 **************************************************************************/

	@Test
	public void testComparisonExpression_EndsWith_Happy() {
		// setup
		final Operand key = new OperandLiteral("tch");
		final Operand op1 = new OperandLiteral("tch");
		final Operand op2 = new OperandLiteral("match");
		final Operand op3 = new OperandLiteral("matchme since this sentence starts with match");


		// test when both strings match
		ComparisonExpression expression = new ComparisonExpression(op1, "endsWith", key);
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());

		// test when string starts with given key
		expression = new ComparisonExpression(op2, "endsWith", key);
		result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());

		// test when string is a sentence that starts with given key
		expression = new ComparisonExpression(op3, "endsWith", key);
		result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_EndsWith_UnHappyCases() {
		// setup
		final Operand key = new OperandLiteral("300");
		final Operand booleanOperand = new OperandLiteral(false);
		final Operand numericOperand = new OperandLiteral(300);
		final Operand invalidStringOperand = new OperandLiteral("2330");

		// test when comparison string is a boolean
		ComparisonExpression expression = new ComparisonExpression(booleanOperand, "endsWith", key);
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertFalse(result.isSuccess());

		// test when comparison string is a numeric
		expression = new ComparisonExpression(numericOperand, "endsWith", key);
		result = expression.evaluate(defaultContext(defaultEvaluator));
		assertFalse(result.isSuccess());

		// test when comparison string doesnt not start with the given key
		expression = new ComparisonExpression(invalidStringOperand, "endsWith", key);
		result = expression.evaluate(defaultContext(defaultEvaluator));
		assertFalse(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_EndsWith_CaseInSensitive() {
		// setup
		final Operand key = new OperandLiteral("nothing");
		final Operand caseMatch = new OperandLiteral("it is nothing");
		final Operand caseNotMatch = new OperandLiteral("it is NOTHING");

		// test default evaluator when case matches
		ComparisonExpression expression = new ComparisonExpression(caseMatch, "endsWith", key);
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());

		// test default evaluator when case does not match
		expression = new ComparisonExpression(caseNotMatch, "endsWith", key);
		result = expression.evaluate(defaultContext(defaultEvaluator));
		assertFalse(result.isSuccess());

		// test case-insensitive evaluator when case does not match
		expression = new ComparisonExpression(caseNotMatch, "endsWith", key);
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
