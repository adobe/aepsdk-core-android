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

import java.util.regex.Pattern;

public class ConditionEvaluator implements Evaluating {

    private final Option option;
    private static final String OPERATOR_EQUALS = "equals";
    private static final String OPERATOR_NOT_EQUALS = "notEquals";
    private static final String OPERATOR_GREATER_THAN = "greaterThan";
    private static final String OPERATOR_GREATER_THAN_OR_EQUALS = "greaterEqual";
    private static final String OPERATOR_LESS_THAN = "lessThan";
    private static final String OPERATOR_LESS_THAN_OR_EQUALS = "lessEqual";
    private static final String OPERATOR_CONTAINS = "contains";
    private static final String OPERATOR_NOT_CONTAINS = "notContains";
    private static final String OPERATOR_STARTS_WITH = "startsWith";
    private static final String OPERATOR_ENDS_WITH = "endsWith";
    private static final String OPERATOR_EXISTS = "exists";
    private static final String OPERATOR_NOT_EXISTS = "notExist";

    public enum Option {
        DEFAULT, // For case sensitive string operations
        CASE_INSENSITIVE, // For case insensitive string operations
    }

    public ConditionEvaluator(final Option option) {
        this.option = option;
    }

    public ConditionEvaluator() {
        this.option = Option.DEFAULT;
    }

    /**
     * Runs operation on the operands.
     *
     * <p>This method always returns a valid non null {@link RulesResult} object. {@link
     * RulesResult#SUCCESS} is returned if the operation on the operands evaluates to true.
     *
     * @param lhs A resolved {@link Operand}
     * @param operation A {@link String} representing the operation to be performed on the operands
     * @param rhs A resolved {@code Operand}
     */
    @Override
    public <A, B> RulesResult evaluate(final A lhs, final String operation, final B rhs) {
        boolean evaluationResult;

        switch (operation) {
            case OPERATOR_EQUALS:
                evaluationResult = this.checkEqual(lhs, rhs);
                break;
            case OPERATOR_NOT_EQUALS:
                evaluationResult = this.notEqual(lhs, rhs);
                break;
            case OPERATOR_STARTS_WITH:
                evaluationResult = this.startsWith(lhs, rhs);
                break;
            case OPERATOR_ENDS_WITH:
                evaluationResult = this.endsWith(lhs, rhs);
                break;
            case OPERATOR_GREATER_THAN:
                evaluationResult = this.greaterThan(lhs, rhs);
                break;
            case OPERATOR_GREATER_THAN_OR_EQUALS:
                evaluationResult = this.greaterThanEquals(lhs, rhs);
                break;
            case OPERATOR_LESS_THAN:
                evaluationResult = this.lesserThan(lhs, rhs);
                break;
            case OPERATOR_LESS_THAN_OR_EQUALS:
                evaluationResult = this.lesserThanOrEqual(lhs, rhs);
                break;
            case OPERATOR_CONTAINS:
                evaluationResult = this.contains(lhs, rhs);
                break;
            case OPERATOR_NOT_CONTAINS:
                evaluationResult = this.notContains(lhs, rhs);
                break;
            default:
                return new RulesResult(
                        RulesResult.FailureType.MISSING_OPERATOR,
                        String.format("Operator is invalid \"%s\"", operation));
        }

        return evaluationResult
                ? RulesResult.SUCCESS
                : new RulesResult(
                        RulesResult.FailureType.CONDITION_FAILED,
                        String.format("Condition not matched for operation \"%s\"", operation));
    }

    @Override
    public <A> RulesResult evaluate(final String operation, final A lhs) {
        boolean evaluationResult;

        switch (operation) {
            case OPERATOR_EXISTS:
                evaluationResult = this.exists(lhs);
                break;
            case OPERATOR_NOT_EXISTS:
                evaluationResult = this.notExists(lhs);
                break;
            default:
                return new RulesResult(
                        RulesResult.FailureType.MISSING_OPERATOR,
                        String.format("Operator is invalid \"%s\"", operation));
        }

        return evaluationResult
                ? RulesResult.SUCCESS
                : new RulesResult(
                        RulesResult.FailureType.CONDITION_FAILED,
                        String.format("Condition not matched for operation \"%s\"", operation));
    }

    // --------------------------------------------------------------------------
    // Private - Operator definitions
    // --------------------------------------------------------------------------

    private boolean checkEqual(final Object lhs, final Object rhs) {
        if (lhs instanceof String && rhs instanceof String && option == Option.CASE_INSENSITIVE) {
            String lhsValue = lhs.toString();
            String rhsValue = rhs.toString();
            return lhsValue.equalsIgnoreCase(rhsValue);
        }

        return lhs.equals(rhs);
    }

    private boolean notEqual(final Object lhs, final Object rhs) {
        return !checkEqual(lhs, rhs);
    }

    private boolean startsWith(final Object lhs, final Object rhs) {
        if (lhs instanceof String && rhs instanceof String) {
            String lhsValue = lhs.toString();
            String rhsValue = rhs.toString();
            String matcherMode = option == ConditionEvaluator.Option.CASE_INSENSITIVE ? "(?i)" : "";
            return lhsValue.matches(matcherMode + Pattern.quote(rhsValue) + ".*");
        }

        return false;
    }

    private boolean endsWith(final Object lhs, final Object rhs) {
        if (lhs instanceof String && rhs instanceof String) {
            String lhsValue = lhs.toString();
            String rhsValue = rhs.toString();
            String matcherMode = option == ConditionEvaluator.Option.CASE_INSENSITIVE ? "(?i)" : "";
            return lhsValue.matches(matcherMode + ".*" + Pattern.quote(rhsValue));
        }

        return false;
    }

    private boolean exists(final Object lhs) {
        return lhs != null;
    }

    private boolean notExists(final Object lhs) {
        return lhs == null;
    }

    // --------------------------------------------------------------------------
    // Private - Operator definitions coming soon
    // --------------------------------------------------------------------------

    private boolean greaterThan(final Object lhs, final Object rhs) {
        Double resolvedLhs = tryParseDouble(lhs);
        Double resolvedRhs = tryParseDouble(rhs);

        if (resolvedLhs == null || resolvedRhs == null) {
            return false;
        }

        return resolvedLhs > resolvedRhs;
    }

    private boolean greaterThanEquals(final Object lhs, final Object rhs) {
        Double resolvedLhs = tryParseDouble(lhs);
        Double resolvedRhs = tryParseDouble(rhs);

        if (resolvedLhs == null || resolvedRhs == null) {
            return false;
        }
        return resolvedLhs >= resolvedRhs;
    }

    private boolean lesserThan(final Object lhs, final Object rhs) {
        Double resolvedLhs = tryParseDouble(lhs);
        Double resolvedRhs = tryParseDouble(rhs);

        if (resolvedLhs == null || resolvedRhs == null) {
            return false;
        }
        return resolvedLhs < resolvedRhs;
    }

    private boolean lesserThanOrEqual(final Object lhs, final Object rhs) {
        Double resolvedLhs = tryParseDouble(lhs);
        Double resolvedRhs = tryParseDouble(rhs);

        if (resolvedLhs == null || resolvedRhs == null) {
            return false;
        }
        return resolvedLhs <= resolvedRhs;
    }

    private boolean contains(final Object lhs, final Object rhs) {
        if (lhs instanceof String && rhs instanceof String) {
            String lhsValue = lhs.toString();
            String rhsValue = rhs.toString();

            if (option == ConditionEvaluator.Option.CASE_INSENSITIVE) {
                lhsValue = lhsValue.toLowerCase();
                rhsValue = rhsValue.toLowerCase();
            }

            return lhsValue.contains(rhsValue);
        }

        return false;
    }

    private boolean notContains(final Object lhs, final Object rhs) {
        return !contains(lhs, rhs);
    }

    private Double tryParseDouble(final Object value) {
        try {
            return Double.valueOf(value.toString());
        } catch (Exception ex) {
            return null;
        }
    }
}
