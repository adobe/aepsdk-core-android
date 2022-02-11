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

import static org.junit.Assert.*;

public class MustacheTokenTests {

	private ConditionEvaluator defaultEvaluator = new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT);

	@Before()
	public void setup() { }

	@Test
	public void Operand_MustacheToken() {
		// setup
		final OperandMustacheToken<String> tokenString = new OperandMustacheToken("{{Hero}}");
		final OperandMustacheToken<Number> tokenInteger = new OperandMustacheToken("{{integerToken}}");
		final OperandMustacheToken<Boolean> tokenBoolean = new OperandMustacheToken("{{booleanToken}}");

		// test
		final String result1 = tokenString.resolve(defaultContext(defaultEvaluator));
		final Number result2 = tokenInteger.resolve(defaultContext(defaultEvaluator));
		final Boolean result3 = tokenBoolean.resolve(defaultContext(defaultEvaluator));

		// verify
		assertEquals("Soldier", result1);
		assertEquals(33, result2);
		assertEquals(false, result3);
	}

	@Test
	public void Operand_MustacheToken_multipleToken() {
		// setup
		final OperandMustacheToken<String> operand = new OperandMustacheToken("{{Beer}}{{Corona}}");

		// test
		final Object result = operand.resolve(defaultContext(defaultEvaluator));

		// verify that operand mustache token resolves only the first token
		assertEquals("Corona", result);
	}

	@Test
	public void Operand_MustacheToken_InvalidToken() {
		// setup
		final OperandMustacheToken<String> example1 = new OperandMustacheToken("{{Beer");
		final OperandMustacheToken<String> example2 = new OperandMustacheToken("Beer{{Hero}}");
		final OperandMustacheToken<String> example3 = new OperandMustacheToken("onlyString");
		final OperandMustacheToken<String> example4 = new OperandMustacheToken("");
		final OperandMustacheToken<String> example5 = new OperandMustacheToken(null);

		// test and verify
		assertNull(example1.resolve(defaultContext(defaultEvaluator)));
		assertNull(example2.resolve(defaultContext(defaultEvaluator)));
		assertNull(example3.resolve(defaultContext(defaultEvaluator)));
		assertNull(example4.resolve(defaultContext(defaultEvaluator)));
		assertNull(example5.resolve(defaultContext(defaultEvaluator)));
	}

	@Test
	public void Operand_WrongDataType_Token() {
		// setup
		final OperandMustacheToken<String> wrongToken = new OperandMustacheToken("{{booleanToken}}");

		// test
		final Object result1 = wrongToken.resolve(defaultContext(defaultEvaluator));

		// verify
		assertTrue(result1 instanceof Boolean);
	}

	@Test
	public void testComparisonExpression_MustacheToken() {
		// setup
		final Operand lhs = new OperandMustacheToken("{{Hero}}");
		final Operand rhs = new OperandLiteral("Soldier");

		// test
		final ComparisonExpression expression = new ComparisonExpression(lhs, "equals", rhs);

		// verify
		final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_MustacheFunction() {
		// setup
		final Operand lhs = new OperandMustacheToken("{{addExtraString(Beer)}}");
		final Operand rhs = new OperandLiteral("Corona extra");

		// test
		final ComparisonExpression expression = new ComparisonExpression(lhs, "equals", rhs);

		// verify
		final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_MustacheFunction_and_Token() {
		// setup
		final Operand lhs = new OperandMustacheToken("{{addExtraString(Beer)}}");
		final Operand rhs = new OperandMustacheToken("{{answer}}");

		// test
		final ComparisonExpression expression = new ComparisonExpression(lhs, "equals", rhs);

		// verify
		final RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());
	}


	/* **************************************************************************
	 *  Private methods
	 **************************************************************************/
	private Context defaultContext(final ConditionEvaluator conditionEvaluator) {
		HashMap<String, Object> context = new HashMap<String, Object>();
		context.put("Beer", "Corona");
		context.put("Hero", "Soldier");
		context.put("Soda", "Pepsi");
		context.put("answer", "Corona extra");
		context.put("integerToken", 33);
		context.put("booleanToken", false);
		return new Context(new FakeTokenFinder(context), conditionEvaluator, FakeTransformer.create());
	}
}
