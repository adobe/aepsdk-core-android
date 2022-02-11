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

public class UnaryExpressionTests {

	@Test
	public void test_operator_exists() {
		// setup
		final Operand op1 = new OperandLiteral("String");
		final Operand op2 = new OperandLiteral(2);
		final Operand op3 = new OperandLiteral(true);
		final Operand op4 = new OperandLiteral(33.33);

		// test
		RulesResult result1 = new UnaryExpression(op1, "exists").evaluate(defaultContext());
		RulesResult result2 = new UnaryExpression(op2, "exists").evaluate(defaultContext());
		RulesResult result3 = new UnaryExpression(op3, "exists").evaluate(defaultContext());
		RulesResult result4 = new UnaryExpression(op4, "exists").evaluate(defaultContext());
		RulesResult result5 = new UnaryExpression(null, "exists").evaluate(defaultContext());

		// verify
		assertTrue(result1.isSuccess());
		assertTrue(result2.isSuccess());
		assertTrue(result3.isSuccess());
		assertTrue(result4.isSuccess());
		assertFalse(result5.isSuccess());
	}

	@Test
	public void test_operator_notExists() {
		// setup
		final Operand op1 = new OperandLiteral("String");
		final Operand op2 = new OperandLiteral(2);
		final Operand op3 = new OperandLiteral(true);
		final Operand op4 = new OperandLiteral(33.33);

		// test
		RulesResult result1 = new UnaryExpression(op1, "notExist").evaluate(defaultContext());
		RulesResult result2 = new UnaryExpression(op2, "notExist").evaluate(defaultContext());
		RulesResult result3 = new UnaryExpression(op3, "notExist").evaluate(defaultContext());
		RulesResult result4 = new UnaryExpression(op4, "notExist").evaluate(defaultContext());
		RulesResult result5 = new UnaryExpression(null, "notExist").evaluate(defaultContext());

		// verify
		assertFalse(result1.isSuccess());
		assertFalse(result2.isSuccess());
		assertFalse(result3.isSuccess());
		assertFalse(result4.isSuccess());
		assertTrue(result5.isSuccess());
	}

	private Context defaultContext() {
		return new Context(new FakeTokenFinder(new HashMap<>()), new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT),
						   null);
	}

}
