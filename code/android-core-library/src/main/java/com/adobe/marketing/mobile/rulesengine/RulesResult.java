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

public class RulesResult {

    public enum FailureType {
        UNKNOWN,
        CONDITION_FAILED,
        TYPE_MISMATCHED,
        MISSING_OPERATOR,
        INVALID_OPERAND,
    }

    private final boolean isSuccess;
    private final String failureMessage;
    private final FailureType failureType;

    public static final RulesResult SUCCESS = new RulesResult(true);

    public RulesResult(final FailureType failureType, final String failureMessage) {
        this.isSuccess = false;
        this.failureMessage = failureMessage;
        this.failureType = failureType;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public FailureType getFailureType() {
        return failureType;
    }

    private RulesResult(final boolean isSuccess) {
        this.isSuccess = isSuccess;
        this.failureMessage = null;
        this.failureType = null;
    }
}
