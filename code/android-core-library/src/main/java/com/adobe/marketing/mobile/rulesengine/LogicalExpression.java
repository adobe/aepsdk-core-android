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

import java.util.ArrayList;
import java.util.List;

public class LogicalExpression implements Evaluable {

    public final List<Evaluable> operands;
    public final String operationName;

    public LogicalExpression(final List<Evaluable> operands, final String operationName) {
        this.operands = operands;
        this.operationName = operationName;
    }

    @Override
    public RulesResult evaluate(final Context context) {
        ArrayList<RulesResult> resolvedOperands = new ArrayList<>();

        if (operationName == null || operationName.isEmpty()) {
            return new RulesResult(
                    RulesResult.FailureType.MISSING_OPERATOR,
                    "Null or empty operator for logical expression");
        }

        if (operands != null) {
            for (Evaluable evaluable : operands) {
                if (evaluable != null) {
                    resolvedOperands.add(evaluable.evaluate(context));
                }
            }
        }

        switch (operationName) {
            case "and":
                return performAndOperation(resolvedOperands);
            case "or":
                return performOrOperation(resolvedOperands);
            default:
                return new RulesResult(
                        RulesResult.FailureType.MISSING_OPERATOR,
                        String.format("Unknown conjunction operator - %s.", operationName));
        }
    }

    private RulesResult performAndOperation(final List<RulesResult> resolvedOperands) {
        for (RulesResult rulesResult : resolvedOperands) {
            if (!rulesResult.isSuccess()) {
                return new RulesResult(
                        RulesResult.FailureType.CONDITION_FAILED, "AND operation returned false.");
            }
        }

        return RulesResult.SUCCESS;
    }

    private RulesResult performOrOperation(final List<RulesResult> resolvedOperands) {
        for (RulesResult rulesResult : resolvedOperands) {
            if (rulesResult.isSuccess()) {
                return RulesResult.SUCCESS;
            }
        }

        return new RulesResult(
                RulesResult.FailureType.CONDITION_FAILED, "OR operation returned false.");
    }
}
