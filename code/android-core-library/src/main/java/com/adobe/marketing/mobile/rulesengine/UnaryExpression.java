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

public class UnaryExpression<A> implements Evaluable {

    private final Operand<A> lhs;
    private final String operationName;

    public UnaryExpression(final Operand<A> lhs, final String operationName) {
        this.lhs = lhs;
        this.operationName = operationName;
    }

    @Override
    public RulesResult evaluate(final Context context) {
        A resolvedLhs = null;

        if (lhs != null) {
            resolvedLhs = lhs.resolve(context);
        }

        if (operationName == null || operationName.isEmpty()) {
            return new RulesResult(
                    RulesResult.FailureType.INVALID_OPERAND,
                    String.format("Evaluating %s %s returned false", resolvedLhs, operationName));
        }

        return context.evaluator.evaluate(operationName, resolvedLhs);
    }
}
