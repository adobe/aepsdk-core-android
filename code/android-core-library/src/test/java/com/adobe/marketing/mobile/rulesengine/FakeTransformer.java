/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 ******************************************************************************/

package com.adobe.marketing.mobile.rulesengine;

public class FakeTransformer {

	static Transforming create() {
		Transformer transformer = new Transformer();
		transformer.register("addExtraString", new TransformerBlock<String>() {
			@Override
			public String transform(Object e) {
				if (e != null && e instanceof String) {
					return e + " extra";
				}

				return "";
			}
		});
		return transformer;
	}
}
