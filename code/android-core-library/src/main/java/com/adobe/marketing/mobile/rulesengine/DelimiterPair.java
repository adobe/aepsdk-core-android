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
 * Class representing delimiter pair for tokens.
 *
 * The default delimiter pair from launch rules are "{%" "%}"
 * eg token: {%region.cityName%}
 */
class DelimiterPair {
	private final String startTag;
	private final String endTag;

	/**
	 * Constructor.
	 *
	 * @param startString
	 * @param endString
	 */
	DelimiterPair(final String startString, final String endString) {
		this.startTag = startString;
		this.endTag = endString;
	}

	/**
	 * @return the startTag for this {@link DelimiterPair}
	 */
	String getStartTag() {
		return startTag;
	}

	/**
	 * @return the endTag for this {@link DelimiterPair}
	 */
	String getEndTag() {
		return endTag;
	}

	/**
	 * @return the character length of startTag of this delimiter
	 */
	int getStartLength() {
		return startTag.length();
	}

	/**
	 * @return the character length of endTag of this delimiter
	 */
	int getEndTagLength() {
		return endTag.length();
	}
}