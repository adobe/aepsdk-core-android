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

import java.util.ArrayList;
import java.util.List;

public class TemplateParser {

    private static final DelimiterPair defaultDelimiter = new DelimiterPair("{{", "}}");

    static List<Segment> parse(final String templateString) {
        return TemplateParser.parse(templateString, defaultDelimiter);
    }

    static List<Segment> parse(final String templateString, final DelimiterPair delimiter) {
        List<Segment> tokens = new ArrayList<>();

        if (templateString == null || templateString.isEmpty()) {
            return tokens;
        }

        DelimiterPair currentDelimiter = delimiter == null ? defaultDelimiter : delimiter;
        int i = 0;
        int end = templateString.length();
        Parser parser = new Parser(i, State.START);

        while (i < end) {
            switch (parser.state) {
                case START:
                    if (templateString.substring(i).startsWith(currentDelimiter.getStartTag())) {
                        parser.setState(i, State.TAG);
                        i = templateString.indexOf(currentDelimiter.getStartTag(), i) + 1;
                    } else {
                        parser.setState(i, State.TEXT);
                    }

                    break;
                case TEXT:
                    if (templateString.substring(i).startsWith(currentDelimiter.getStartTag())) {
                        if (parser.index != i) {
                            tokens.add(new SegmentText(templateString.substring(parser.index, i)));
                        }

                        parser.setState(i, State.TAG);
                        i = templateString.indexOf(currentDelimiter.getStartTag(), i) + 1;
                    }

                    break;
                case TAG:
                    if (templateString.substring(i).startsWith(currentDelimiter.getEndTag())) {
                        int tokenContentStartIndex =
                                parser.index + currentDelimiter.getStartLength();
                        tokens.add(
                                new SegmentToken(
                                        templateString.substring(tokenContentStartIndex, i)));
                        parser.state = State.START;
                        i = templateString.indexOf(currentDelimiter.getEndTag(), i) + 1;
                    }

                    break;
            }

            i++;
        }

        switch (parser.state) {
            case START:
                break;
            case TEXT:
                tokens.add(new SegmentText(templateString.substring(parser.index, i)));
                break;
            case TAG:
                return new ArrayList<>();
        }

        return tokens;
    }
}

class Parser {

    int index;
    State state;

    Parser(final int index, final State state) {
        this.index = index;
        this.state = state;
    }

    public void setState(final int index, final State state) {
        this.state = state;
        this.index = index;
    }
}

enum State {
    START,
    TEXT,
    TAG,
}
