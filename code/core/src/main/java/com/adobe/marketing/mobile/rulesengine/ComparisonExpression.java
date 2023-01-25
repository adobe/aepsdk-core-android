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

/**
 * {@link ComparisonExpression} allows for comparison of two operands and evaluates to a True or
 * False. Comparison operators include LessThan, LessThanOrEqual to, Equal, NotEqual, GreaterThan,
 * GreaterThanOrEqual to, Contains and notContains.
 */
public class ComparisonExpression<A, B> implements Evaluable {

    private final Operand<A> lhs;
    private final Operand<B> rhs;
    private final String operationName;

    /**
     * Initializer. Constructs the {@link ComparisonExpression} object with operands and operation
     * name.
     *
     * @param lhs an {@link Operand}
     * @param operationName A {@code String} value defining the operation.
     * @param rhs an {@link Operand}
     */
    public ComparisonExpression(
            final Operand<A> lhs, final String operationName, final Operand<B> rhs) {
        this.lhs = lhs;
        this.operationName = operationName;
        this.rhs = rhs;
    }

    /**
     * Call this method to evaluate this {@link ComparisonExpression}.
     *
     * <p>This method always returns a valid non null {@link RulesResult} object. Returns
     * RulesResult failure if the operation name or either of the operands are null.
     *
     * @param context The context containing details for token swapping and transforming the operand
     *     if required. A non null value of {@link Context} is expected to be passed to this method.
     * @return A {@link RulesResult} object representing a evaluated rule
     */
    public RulesResult evaluate(final Context context) {
        if (operationName == null) {
            return new RulesResult(
                    RulesResult.FailureType.MISSING_OPERATOR,
                    "Operator is null, Comparison returned false");
        }

        if (lhs == null || rhs == null) {
            return new RulesResult(
                    RulesResult.FailureType.INVALID_OPERAND,
                    "Operand is null, Comparison returned false.");
        }

        A resolvedLhs = lhs.resolve(context);
        B resolvedRhs = rhs.resolve(context);

        if (resolvedLhs == null || resolvedRhs == null) {
            return new RulesResult(
                    RulesResult.FailureType.INVALID_OPERAND,
                    String.format(
                            "Comparison %s %s %s returned false",
                            resolvedLhs, operationName, resolvedRhs));
        }

        return context.evaluator.evaluate(resolvedLhs, operationName, resolvedRhs);
    }
}
