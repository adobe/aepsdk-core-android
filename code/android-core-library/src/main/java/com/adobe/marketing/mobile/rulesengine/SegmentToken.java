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
 * SegmentToken represents token, whose value is substituted by the {@link TokenFinder} or {@link
 * Transforming}.
 */
public class SegmentToken implements Segment {

    private final MustacheToken mustacheToken;

    public SegmentToken(final String mustacheString) {
        this.mustacheToken = new MustacheToken(mustacheString);
    }

    public MustacheToken getMustacheToken() {
        return mustacheToken;
    }

    /** Retrieves the evaluated value of this {@link SegmentToken} */
    @Override
    public String getContent(final TokenFinder tokenFinder, final Transforming transformer) {
        Object resolvedToken = mustacheToken.resolve(tokenFinder, transformer);

        if (resolvedToken != null) {
            return resolvedToken.toString();
        }

        return "";
    }
}
