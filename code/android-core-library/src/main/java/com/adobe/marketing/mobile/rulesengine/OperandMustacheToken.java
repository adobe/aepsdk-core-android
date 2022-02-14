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
import java.util.List;

/**
 * Class to handle the mustache token Operand.
 */
public class OperandMustacheToken<T> implements Operand<T> {
	private final MustacheToken mustacheToken;


	/**
	 * Constructor.
	 * Initialize this operand using a tokenString. A valid tokenString has only one token.
	 * For example :
	 *  {{region.city}}
	 *  {%~state.com.adobe.marketing,mobile.lifecycle.contextdata.deviceName%}
	 *
	 * Following are considered are invalid token strings
	 * 1. region.city          - (this string does not contain token delimiters)
	 * 2. {{region.city        - (this string is not enclosed between delimiters)
	 * 3. some{{region.city}}  - (this string does not start with a valid token)
	 *
	 * @param tokenString string representing a mustache token operand
	 */
	public OperandMustacheToken(final String tokenString) {

		// if token string is invalid make the mustache Token null.
		// There by the operand always returns null on resolve. This is equivalent to OperandNone in swift.
		if (tokenString == null || tokenString.isEmpty()) {
			mustacheToken = null;
			return;
		}

		final List<Segment> segmentList = TemplateParser.parse(tokenString);

		// Mustache token operands must have only one token.
		// Hence we ignore other
		if (segmentList.size() > 0 && segmentList.get(0) instanceof SegmentToken) {
			SegmentToken segmentToken = (SegmentToken) segmentList.get(0);
			mustacheToken = segmentToken.getMustacheToken();
			return;
		}

		mustacheToken = new MustacheToken(tokenString);
	}


	/**
	 * Returns the resolved value of the MustacheToken operand.
	 * An invalid mustacheToken operand returns null.
	 *
	 * @param context The context contains details for token swapping and transforming the operand if required
	 * @return the resolved operand value
	 */
	@Override
	public T resolve(final Context context) {
		if (mustacheToken == null) {
			return null;
		}

		return (T) mustacheToken.resolve(context.tokenFinder, context.transformer);
	}

}