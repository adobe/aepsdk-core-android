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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ParserTests {

    private static final HashMap<String, Object> tokens =
            new HashMap<String, Object>() {
                {
                    put("one", true);
                    put("two", "2");
                    put("three", 3);
                }
            };
    private final TokenFinder tokenFinder = new FakeTokenFinder(tokens);
    private final Transformer transformer = new Transformer();

    @Before
    public void setup() {}

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
