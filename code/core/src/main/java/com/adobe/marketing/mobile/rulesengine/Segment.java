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
 * A segment represents a part of text that can be evaluated to a value. There are two types of
 * Segment - {@link SegmentToken} - {@link SegmentText}
 *
 * <p>The following string is parsed by {@link TemplateParser} to have 3 segments. "Hi {{username}},
 * Welcome to New York" 1. Hi --> (SegmentText) 2. {{username}} --> (SegmentToken) 3. , Welcome to
 * New York --> (Segment Text)
 */
interface Segment {
    String getContent(final TokenFinder tokenFinder, final Transforming transformers);
}
