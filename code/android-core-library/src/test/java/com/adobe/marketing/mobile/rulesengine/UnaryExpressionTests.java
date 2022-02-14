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
