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

package com.adobe.marketing.mobile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SignalTemplateTest {

	@Before()
	public void beforeEach() throws Exception {
	}

	@Test
	public void returnNullSignalTemplate_when_JsonObjectIsNull() {
		Assert.assertNull(SignalTemplate.createSignalTemplateFromConsequence(null));
	}

	@Test
	public void returnNullSignalTemplate_when_NoMessageIdInJson() {
		Assert.assertNull(SignalTemplate.createSignalTemplateFromConsequence(new HashMap<String, Variant>()));
	}

	@Test
	public void returnNullSignalTemplate_when_MessageIdIsEmptyInJson() {
		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString(""));
		Assert.assertNull(SignalTemplate.createSignalTemplateFromConsequence(map));
	}

	@Test
	public void returnNullSignalTemplate_when_DetailIsEmpty() {
		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("id"));
		map.put("detail", Variant.fromVariantMap(new HashMap<String, Variant>()));
		map.put("type", Variant.fromString("pb"));

		Assert.assertNull(SignalTemplate.createSignalTemplateFromConsequence(map));
	}

	@Test
	public void returnNullSignalTemplate_when_UrlIsEmptyInJson() {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));
		map.put("detail", Variant.fromStringMap(detail));

		Assert.assertNull(SignalTemplate.createSignalTemplateFromConsequence(map));
	}

	@Test
	public void returnNullSignalTemplate_when_UrlIsHttpWithPiiType() {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://xyz.com");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));
		map.put("type", Variant.fromString("pii"));
		map.put("detail", Variant.fromStringMap(detail));

		Assert.assertNull(SignalTemplate.createSignalTemplateFromConsequence(map));
	}

	@Test
	public void returnValidSignalTemplate_when_UrlIsHttpsWithPiiType() {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "https://xyz.com");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));
		map.put("type", Variant.fromString("pii"));
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertNotNull(signal);
		Assert.assertEquals("https://xyz.com", signal.getSignalHit().url);
	}

	@Test
	public void returnValidSignalTemplate_when_UrlIsHttpWithPostbackType() {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://xyz.com");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));
		map.put("type", Variant.fromString("pb"));
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertNotNull(signal);
		Assert.assertEquals("http://xyz.com", signal.getSignalHit().url);
	}

	@Test
	public void returnValidSignalTemplate_when_UrlIsValidInJson() {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://my-url");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertEquals("what-id", signal.getSignalTemplateId());
	}

	@Test
	public void signalHitHasCorrectUrl_when_UrlIsValidInJson() {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://my-url");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertEquals("what-id", signal.getSignalTemplateId());
		Assert.assertEquals("http://my-url", signal.getSignalHit().url);
	}

	@Test
	public void signalHitHasNullPostBody_when_TemplatebodyIsEmptyInJson() {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://my-url");
		detail.put("templatebody", "");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertEquals("", signal.getSignalHit().body);
	}

	@Test
	public void signalHitHasNullPostBody_when_TemplatebodyIsNullInJson() {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://my-url");
		detail.put("templatebody", null);

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertEquals("", signal.getSignalHit().body);
	}

	@Test
	public void signalHitHasValidPostBody_when_DecodedTemplatebodyIsValid() throws Exception {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://my-url");
		detail.put("templatebody", "body");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertEquals("body", signal.getSignalHit().body);
	}

	@Test
	public void signalHitHasNullContentType_when_DecodedTemplatebodyIsInvalid() throws Exception {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://my-url");
		detail.put("templatebody", "");
		detail.put("contenttype", "json");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));;
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertNull(signal.getSignalHit().contentType);
	}

	@Test
	public void signalHitHasNullPostBody_when_TemplatebodyIsMissingInJson() {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://my-url");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));;
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertEquals("", signal.getSignalHit().body);
	}

	@Test
	public void signalHitHasValidContentType_when_DecodedTemplatebodyIsValid() throws Exception {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://my-url");
		detail.put("templatebody", "body");
		detail.put("contenttype", "json");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));;
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertEquals("json", signal.getSignalHit().contentType);
	}

	@Test
	public void signalHitUseTheDefaultTimeout_when_NoTimeoutInJson() throws Exception {
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("templateurl", "http://myurl");

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));;
		map.put("detail", Variant.fromStringMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertEquals(2, signal.getSignalHit().timeout);
	}

	@Test
	public void signalHitUseConfiguredTimeout_when_ContainsTimeoutInJson() throws Exception {
		Map<String, Variant> detail = new HashMap<String, Variant>();
		detail.put("templateurl", Variant.fromString("http://myurl"));
		detail.put("timeout", Variant.fromInteger(5));

		Map<String, Variant> map = new HashMap<String, Variant>();
		map.put("id", Variant.fromString("what-id"));;
		map.put("detail", Variant.fromVariantMap(detail));

		SignalTemplate signal = SignalTemplate.createSignalTemplateFromConsequence(map);
		Assert.assertEquals(5, signal.getSignalHit().timeout);
	}
}
