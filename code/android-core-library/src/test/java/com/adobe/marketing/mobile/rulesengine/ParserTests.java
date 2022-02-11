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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParserTests {

	private static final HashMap tokens = new HashMap() {
		{
			put("one", true);
			put("two", "2");
			put("three", 3);
		}
	};
	private TokenFinder tokenFinder = new FakeTokenFinder(tokens);
	private Transformer transformer = new Transformer();

	@Before()
	public void setup() { }

	@Test
	public void parse_text_token_text() {
		// test
		List<Segment> tokens = TemplateParser.parse("aa{{two}}cc", null);

		// verify
		assertEquals(3, tokens.size());
		assertEquals(SegmentText.class, tokens.get(0).getClass());
		assertEquals(SegmentToken.class, tokens.get(1).getClass());
		assertEquals(SegmentText.class, tokens.get(2).getClass());

		assertEquals("aa", tokens.get(0).getContent(tokenFinder, transformer));
		assertEquals("2", tokens.get(1).getContent(tokenFinder, transformer));
		assertEquals("cc", tokens.get(2).getContent(tokenFinder, transformer));
	}

	@Test
	public void parse_text() {
		List<Segment> tokens = TemplateParser.parse("puretext", null);
		assertEquals(1, tokens.size());
		assertEquals(SegmentText.class, tokens.get(0).getClass());
	}

	@Test
	public void parse_token() {
		List<Segment> tokens = TemplateParser.parse("{{token}}", null);
		assertEquals(1, tokens.size());
		assertEquals(SegmentToken.class, tokens.get(0).getClass());
	}

	@Test
	public void parse_token_type2() {
		List<Segment> tokens = TemplateParser.parse("{{one}{{two}}", null);
		assertEquals(1, tokens.size());
	}

	@Test
	public void parse_emptyToken() {
		List<Segment> tokens = TemplateParser.parse("{{}}", null);
		assertEquals(1, tokens.size());
		assertEquals(SegmentToken.class, tokens.get(0).getClass());
	}

	@Test
	public void parse_emptyText() {
		List<Segment> tokens = TemplateParser.parse("", null);
		assertEquals(0, tokens.size());
	}

	@Test
	public void parse_Null() {
		List<Segment> tokens = TemplateParser.parse(null, null);
		assertEquals(0, tokens.size());
	}

	@Test
	public void parse_token_differentDelimiter() {
		List<Segment> tokens = TemplateParser.parse("%!two!%", new DelimiterPair("%!", "!%"));
		assertEquals(1, tokens.size());
		assertEquals(SegmentToken.class, tokens.get(0).getClass());
	}

	@Test
	public void parse_token_token_token() {
		Template template = new Template("{{one}}{{two}}{{three}}");
		String templateString = template.render(tokenFinder, transformer);

		assertEquals("true23", templateString);

		List<Segment> tokens = TemplateParser.parse("{{one}}{{two}}{{three}}", null);
		assertEquals(3, tokens.size());
		assertEquals(SegmentToken.class, tokens.get(0).getClass());
		assertEquals(SegmentToken.class, tokens.get(1).getClass());
		assertEquals(SegmentToken.class, tokens.get(2).getClass());
	}

	@Test
	public void parse_invalidToken_type1() {
		List<Segment> tokens = TemplateParser.parse("{{one", null);
		assertEquals(0, tokens.size());
	}

}
