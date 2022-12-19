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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing mustache token.
 *
 * <p>Once a token is identified in any part of the rule, use this class to obtain the resolved
 * value of the token. A token can be of two types 1. Variable Following example demonstrates
 * variable token to retrieve the "city" details from the SDK context. {{region.city}} 2. Function
 * Following example demonstrates function token to urlEncode the provided webURL.
 * {{urlencode(http://google.com)}}
 */
class MustacheToken {

    private final Type tokenType;
    private final String tokenString;

    private String functionName;
    private MustacheToken innerVariable;

    /**
     * Constructor to initialize the mustache token.
     *
     * <p>This constructor automatically recognizes the token type from the provided token string
     *
     * @param tokenString the token string without the delimiters representing the token
     */
    MustacheToken(final String tokenString) {
        Matcher functionMatcher = Pattern.compile("\\(([^)]+)\\)").matcher(tokenString);
        this.tokenString = tokenString;

        // check if the token is a function
        if (functionMatcher.find()) {
            innerVariable = new MustacheToken(functionMatcher.group(1));
            functionName = tokenString.substring(0, functionMatcher.start());
            tokenType = Type.FUNCTION;
            return;
        }

        tokenType = Type.VARIABLE;
    }

    /**
     * Resolves the token into its corresponding value.
     *
     * @param tokenFinder A {@link TokenFinder} instance that contains SDK context to replace tokens
     * @param transformers A set of transformers to evaluate the function tokens.
     * @return value of resolved token.
     */
    protected Object resolve(final TokenFinder tokenFinder, final Transforming transformers) {
        if (tokenType == Type.FUNCTION) {
            return transformers.transform(
                    this.functionName, innerVariable.resolve(tokenFinder, transformers));
        } else {
            return tokenFinder.get(tokenString);
        }
    }

    private enum Type {
        FUNCTION,
        VARIABLE,
    }
}
