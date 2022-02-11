package com.adobe.marketing.mobile.rulesengine;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class OperandFunctionTests {

	private ConditionEvaluator defaultEvaluator = new ConditionEvaluator(ConditionEvaluator.Option.DEFAULT);

	@Test
	public void test_OperandFunction() {
		// setup
		final Operand stringOperand = new OperandLiteral("bossbaby");
		final OperandFunction<String> functionOperator = new OperandFunction<String>(new FunctionBlock() {
			@Override
			public String execute(Object... args) {
				StringBuilder builder = new StringBuilder();

				for (Object each : args) {
					builder.append(each.toString());
				}

				return builder.toString();
			}

		}, "boss", "baby");

		// test
		final RulesResult result = new ComparisonExpression(stringOperand, "equals",
				functionOperator).evaluate(defaultContext(defaultEvaluator));

		// verify
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
