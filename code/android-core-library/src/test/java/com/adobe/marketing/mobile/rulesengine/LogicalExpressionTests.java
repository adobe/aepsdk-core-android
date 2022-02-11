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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class LogicalExpressionTests {

	private ComparisonExpression expressionTrue = new ComparisonExpression(new OperandLiteral("One"), "equals",
			new OperandLiteral("One"));
	private ComparisonExpression expressionFalse = new ComparisonExpression(new OperandLiteral("One"), "notEquals",
			new OperandLiteral("One"));

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


	@Test
	public void test_test() {
		MustacheToken mustacheToken = new MustacheToken("functi(variable)");

	}



	private Context defaultContext() {
		return new Context(new FakeTokenFinder(new HashMap<>()), new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT),
						   null);
	}

}
