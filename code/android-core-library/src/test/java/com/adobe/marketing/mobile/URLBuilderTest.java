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
import org.junit.Test;

import java.util.HashMap;

public class URLBuilderTest extends BaseTest {

	private String ascii;
	@Before
	public void beforeEach() {
		byte[] bytes = new byte[128];

		for (int i = 0; i < 128; i++) {
			bytes[i] = (byte)i;
		}

		ascii = new String(bytes);
	}

	@Test
	public void happyPath() {
		String url = new URLBuilder().setServer("server").addPath("path").addQuery("query").build();
		Assert.assertEquals("http://server/path?query", url);
	}

	@Test
	public void returnNull_When_ServerIsNull() {
		String url = new URLBuilder().setServer(null).addPath("path").addQuery("query").build();
		Assert.assertNull(url);
	}

	@Test
	public void noPathInUrl_When_PathIsNull() {
		String url = new URLBuilder().setServer("server").addPath(null).addQuery("query").build();
		Assert.assertEquals("http://server?query", url);
	}

	@Test
	public void noPathInUrl_When_PathIsEmpty() {
		String url = new URLBuilder().setServer("server").addPath("").addQuery("query").build();
		Assert.assertEquals("http://server?query", url);
	}

	@Test
	public void encodeSlash_When_PathStartsWithASlash() {
		String url = new URLBuilder().setServer("server").addPath("/path").addQuery("query").build();
		Assert.assertEquals("http://server/%2Fpath?query", url);
	}

	@Test
	public void noQueryInUrl_When_QueryIsNull() {
		String url = new URLBuilder().setServer("server").addPath("path").addQuery(null).build();
		Assert.assertEquals("http://server/path", url);
	}

	@Test
	public void noQueryInUrl_When_QueryIsEmpty() {
		String url = new URLBuilder().setServer("server").addPath("path").addQuery("").build();
		Assert.assertEquals("http://server/path", url);
	}


	@Test
	public void justServerAddress_When_QueryAndPathAreNull() {
		String url = new URLBuilder().setServer("server").build();
		Assert.assertEquals("http://server", url);
	}

	@Test
	public void doNotEncodeDot_When_ServerContainsDot() {
		String url = new URLBuilder().setServer("www.adobe.com").build();
		Assert.assertEquals("http://www.adobe.com", url);
	}

	@Test
	public void usingHttps_When_SSLIsEnabled() {
		String url = new URLBuilder().enableSSL(true).setServer("server").addPath("path").addQuery("query").build();
		Assert.assertEquals("https://server/path?query", url);
	}

	@Test
	public void encodePath_When_PathContainsSpecialCharacter() {
		String url = new URLBuilder().enableSSL(true).setServer("server").addPath(ascii).build();
		Assert.assertEquals("https://server/%00%01%02%03%04%05%06%07%08%09%0A%0B%0C%0D%0E%0F%10%11%12%13%14%15%16%17%18%19%1A%1B%1C%1D%1E%1F%20%21%22%23%24%25%26%27%28%29%2A%2B%2C-.%2F0123456789%3A%3B%3C%3D%3E%3F%40ABCDEFGHIJKLMNOPQRSTUVWXYZ%5B%5C%5D%5E_%60abcdefghijklmnopqrstuvwxyz%7B%7C%7D~%7F",
							url);
	}

	@Test
	public void encodeQuery_When_QueryContainsSpecialCharacter() {
		String url = new URLBuilder().enableSSL(true).setServer("server").addQuery(ascii).build();
		Assert.assertEquals("https://server?%00%01%02%03%04%05%06%07%08%09%0A%0B%0C%0D%0E%0F%10%11%12%13%14%15%16%17%18%19%1A%1B%1C%1D%1E%1F%20%21%22%23%24%25%26%27%28%29%2A%2B%2C-.%2F0123456789%3A%3B%3C%3D%3E%3F%40ABCDEFGHIJKLMNOPQRSTUVWXYZ%5B%5C%5D%5E_%60abcdefghijklmnopqrstuvwxyz%7B%7C%7D~%7F",
							url);
	}

	@Test
	public void notEncodeQuery_When_EncodetypeIsNone() {
		String url = new URLBuilder().enableSSL(true).setServer("server").addQuery("abc/def+ghi",
				URLBuilder.EncodeType.NONE).build();
		Assert.assertEquals("https://server?abc/def+ghi", url);
	}

	@Test
	public void encodeQuery_When_EncodetypeIsEnocode() {
		String url = new URLBuilder().enableSSL(true).setServer("server").addQuery("abc/def+ghi",
				URLBuilder.EncodeType.ENCODE).build();
		Assert.assertEquals("https://server?abc%2Fdef%2Bghi", url);
	}

	@Test
	public void appendQueryParameter_When_ParameterIsValid() {
		String url = new URLBuilder().enableSSL(true).setServer("server").addQueryParameter("key", "value").build();
		Assert.assertEquals("https://server?key=value", url);
	}


	@Test
	public void notAppendQueryParameter_When_QueryParameterHasEmptyKeyName() {
		String url = new URLBuilder().enableSSL(true).setServer("server").addQueryParameter("", "value").build();
		Assert.assertEquals("https://server", url);
	}

	@Test
	public void notAppendQueryParameter_When_QueryParameterHasNullKeyName() {
		String url = new URLBuilder().enableSSL(true).setServer("server").addQueryParameter(null, "value").build();
		Assert.assertEquals("https://server", url);
	}

	@Test
	public void notAppendQueryParameter_When_QueryParameterHasNullValue() {
		String url = new URLBuilder().enableSSL(true).setServer("server").addQueryParameter("key", null).build();
		Assert.assertEquals("https://server", url);
	}

	@Test
	public void notAppendQueryParameter_When_QueryParameterHasEmptyValue() {
		String url = new URLBuilder().enableSSL(true).setServer("server").addQueryParameter("key", "").build();
		Assert.assertEquals("https://server", url);
	}

	@Test
	public void appendAllParis_When_AddMultipleParameters() {
		String url = new URLBuilder().enableSSL(true).setServer("server")
		.addQueryParameter("key", "value")
		.addQueryParameter("key1", "value1")
		.addQueryParameter("key2", "value2")
		.build();
		Assert.assertEquals("https://server?key=value&key1=value1&key2=value2", url);
	}

	@Test
	public void encodeQuestionEqualPlusSymbols_When_QueryParameterContainsThem() {
		String url = new URLBuilder().enableSSL(true).setServer("server")
		.addQueryParameter("123?value=456+789", "123?value=456+789")
		.build();
		Assert.assertEquals("https://server?123%3Fvalue%3D456%2B789=123%3Fvalue%3D456%2B789", url);
	}

	@Test
	public void addParameters_When_ProvideAParametersMap() {
		String url = new URLBuilder().enableSSL(true).setServer("server")
		.addQueryParameters(new HashMap<String, String>() {
			{
				put("key", "value");
				put("key1", "value1");
			}
		})
		.build();
		Assert.assertEquals(getURLQueryParameters(url), new HashMap<String, String>() {
			{
				put("key", "value");
				put("key1", "value1");
			}
		});
	}

	@Test
	public void encodeParameters_When_ParametersMapContainsSpecialChar() {
		String url = new URLBuilder().enableSSL(true).setServer("server")
		.addQueryParameters(new HashMap<String, String>() {
			{
				put("key=", "value=");
				put("key%", "value%");
			}
		})
		.build();
		Assert.assertEquals(getURLQueryParameters(url), new HashMap<String, String>() {
			{
				put("key%25", "value%25");
				put("key%3D", "value%3D");
			}
		});
	}

	@Test
	public void noQueryParameters_When_ParametersMapIsNull() {
		String url = new URLBuilder().enableSSL(true).setServer("server")
		.addQueryParameters(null)
		.build();
		Assert.assertEquals("https://server", url);
	}

	@Test
	public void noQueryParameters_When_ParametersMapIsEmpty() {
		String url = new URLBuilder().enableSSL(true).setServer("server")
		.addQueryParameters(new HashMap<String, String>())
		.build();
		Assert.assertEquals("https://server", url);
	}

	@Test
	public void testEverything() {
		String url = new URLBuilder().enableSSL(true).setServer("server.com/serverPath")
		.addPath("path").addPath("path%2").addPath(null)
		.addQuery("query").addQuery("query1")
		.addQuery(null)
		.addQueryParameter("key1", "value1")
		.addQueryParameters(new HashMap<String, String>() {
			{
				put("key", "value");
				put("key=", "value=");
				put("key%", "value%");
				put("utf8", "哈哈");
				put("novalue", null);
			}
		})
		.build();
		Assert.assertTrue(url.startsWith("https://server.com/serverPath/path/path%252?"));
		Assert.assertEquals(getURLQueryParameters(url), new HashMap<String, String>() {
			{
				put("key%25", "value%25");
				put("key%3D", "value%3D");
				put("key", "value");
				put("key1", "value1");
				put("utf8", "%E5%93%88%E5%93%88");
			}
		});
	}
}
