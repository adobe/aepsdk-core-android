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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class OperatorEqualTests {

	private ConditionEvaluator defaultEvaluator = new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT);
	private ConditionEvaluator caseSensitiveEvaluator = new ConditionEvaluator(ConditionEvaluator.Option.CASE_INSENSITIVE);

	@Before()
	public void setup() {
	}

	@Test
	public void testComparisonExpression_String_Equals_String() {
		// setup
		final Operand lhs = new OperandLiteral("This is a big string");
		final Operand rhs = new OperandLiteral("This is a big string");

		// test
		ComparisonExpression expression = new ComparisonExpression(lhs, "equals", rhs);

		// verify
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_Number_Equals_Number() {
		// setup
		final Operand lhs = new OperandLiteral(34);
		final Operand rhs = new OperandLiteral(34);

		// test
		ComparisonExpression expression = new ComparisonExpression(lhs, "equals", rhs);

		// verify
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_Double_Equals_Double() {
		// setup
		final Operand lhs = new OperandLiteral(55.55);
		final Operand rhs = new OperandLiteral(55.55);

		// test
		ComparisonExpression expression = new ComparisonExpression(lhs, "equals", rhs);

		// verify
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_Boolean_Equals_Boolean() {
		// setup
		final Operand lhs = new OperandLiteral(false);
		final Operand rhs = new OperandLiteral(false);

		// test
		ComparisonExpression expression = new ComparisonExpression(lhs, "equals", rhs);

		// verify
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertTrue(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_String_Equals_DifferentString() {
		// setup
		final Operand lhs = new OperandLiteral("This is a big string");
		final Operand rhs = new OperandLiteral("This is another big string");

		// test
		final ComparisonExpression expression = new ComparisonExpression(lhs, "equals", rhs);

		// verify
		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertFalse(result.isSuccess());
		assertEquals(RulesResult.FailureType.CONDITION_FAILED, result.getFailureType());
	}

	@Test
	public void testComparisonExpression_String_Equals_CapedString() {
		// setup
		Operand lhs = new OperandLiteral("Deal");
		Operand rhs = new OperandLiteral("DEAL");

		ComparisonExpression expression = new ComparisonExpression(lhs, "equals", rhs);

		RulesResult result = expression.evaluate(defaultContext(defaultEvaluator));
		assertFalse(result.isSuccess());
		assertEquals(RulesResult.FailureType.CONDITION_FAILED, result.getFailureType());
	}

	@Test
	public void testComparisonExpression_String_Equals_String_CaseInsensitive() {
		// setup
		Operand lhs = new OperandLiteral("This is a big string");
		Operand rhs = new OperandLiteral("This IS a BIG STRING");

		// test
		ComparisonExpression expression = new ComparisonExpression(lhs, "equals", rhs);

		// verify
		RulesResult result = expression.evaluate(defaultContext(caseSensitiveEvaluator));
		assertTrue(result.isSuccess());
	}

	@Test
	public void testComparisonExpression_NotEquals() {
		// setup
		final Operand op1 = new OperandLiteral(3);
		final Operand op2 = new OperandLiteral("string");
		final Operand op3 = new OperandLiteral(false);
		final Operand op4 = new OperandLiteral(3.3);
		final Operand op5 = new OperandLiteral("diffString");

		// test string not equals int
		ComparisonExpression expression = new ComparisonExpression(op1, "notEquals", op2);
		RulesResult result = expression.evaluate(defaultContext(caseSensitiveEvaluator));

		assertTrue(result.isSuccess());

		// test string not equals boolean
		expression = new ComparisonExpression(op2, "notEquals", op3);
		result = expression.evaluate(defaultContext(caseSensitiveEvaluator));
		assertTrue(result.isSuccess());

		// test boolean not equals double
		expression = new ComparisonExpression(op3, "notEquals", op4);
		result = expression.evaluate(defaultContext(caseSensitiveEvaluator));
		assertTrue(result.isSuccess());


		// test string not equals different string
		expression = new ComparisonExpression(op2, "notEquals", op5);
		result = expression.evaluate(defaultContext(caseSensitiveEvaluator));
		assertTrue(result.isSuccess());
	}


	/* **************************************************************************
	 *  Private methods
	 **************************************************************************/
	private Context defaultContext(final ConditionEvaluator conditionEvaluator) {
		return new Context(new FakeTokenFinder(new HashMap<>()), conditionEvaluator, null);
	}

}
