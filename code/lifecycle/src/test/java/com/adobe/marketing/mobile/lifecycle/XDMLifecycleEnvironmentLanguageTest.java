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

package com.adobe.marketing.mobile.lifecycle;

import static org.junit.Assert.assertEquals;

import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.DataReaderException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class XDMLifecycleEnvironmentLanguageTest {

    @Parameterized.Parameters(name = "{index}: {0} ({2})")
    public static Collection<Object[]> data() {
        // List of various language tags, some use BCP 47 formatting while others use
        // Locale.toString formatting
        // LifecycleUtil.formatLocaleXDM trims the language tag so not all these values are seen in
        // end-to-end scenarios.
        // 0: test name, 1: expected result 2: language tag to test
        return Arrays.asList(
                new Object[][] {
                    {
                        // Language only
                        "Language only", "en", "en"
                    },
                    {
                        // Language + Region
                        "Language + Region", "es-US", "es-US"
                    },
                    {
                        // Language + Region + Script
                        "Language + Script + Region", "zh-Hant-HK", "zh-Hant-HK"
                    },
                    {
                        // Language + Region + Variant
                        "Language + Region + Variant", "de-DE-POSIX", "de-DE-POSIX"
                    },
                    {
                        // Language + Region + Script + Variant
                        "Language + Script + Region + Variant",
                        "de-Latn-DE-POSIX",
                        "de-Latn-DE-POSIX"
                    },
                    {
                        // Undefined Language + Region
                        "Undefined Language + Region", "und-US", "und-US"
                    },
                    {
                        // Undefined Language + Script
                        "Undefined Language + Script", "und-Hant", "und-Hant"
                    },
                    {
                        // Undefined Language + Variant
                        "Undefined Language + Variant", "und-POSIX", "und-POSIX"
                    },
                    {
                        // Japanese Calendar - for compatibility
                        "Japanese Calendar", "ja-JP-x-lvariant-JP", "ja-JP-x-lvariant-JP"
                    },
                    {
                        // Japanese Calendar
                        "Japanese Calendar", "ja-JP-u-ca-japanese", "ja-JP-u-ca-japanese"
                    },
                    {
                        // Thai Buddhist Calendar
                        "Thai Buddhist Calendar",
                        "th-TH-u-ca-buddhist-nu-thai",
                        "th-TH-u-ca-buddhist-nu-thai"
                    },
                    {
                        // Serbian Montenegro - for compatibility
                        "Serbian Montenegro", "sr-ME-x-lvariant-Latn", "sr-ME-x-lvariant-Latn"
                    },
                    {
                        // Grandfathered
                        "Grandfather Klingon", "i-klingon", "i-klingon"
                    },
                    {
                        // Null Locale
                        "Null locale", null, null
                    },
                    {
                        // Empty Locale
                        "Empty locale", null, ""
                    },
                    {
                        // Non BCP 47 tag - pound sign
                        "Invalid script format '-#'", null, "zh-HK-#Hant"
                    },
                    {
                        // Non BCP 47 tag - ampersand
                        "Invalid calendar format '@'", null, "en-US@calendar=buddhist"
                    },
                    {
                        // Non BCP 47 tag - double hyphen
                        "Invalid language + variant '--'", null, "de--POSIX"
                    },
                    {
                        // Non BCP 47 tag - leading hyphen
                        "Invalid country only '-'", null, "-US"
                    },
                    {
                        // Non BCP 47 tag - pound sign
                        "Invalid script + extension '#'", null, "zh-TW-#Hant-x-java"
                    },
                    {
                        // Non BCP 47 tag - underscore instead of hyphen
                        "Invalid underscore", null, "en_US"
                    },
                    {
                        // Non BCP 47 tag - Thai special case
                        "Invalid Thai special case", null, "th-TH-TH-#u-nu-thai"
                    },
                });
    }

    @Parameterized.Parameter() public String testName;

    @Parameterized.Parameter(1)
    public String expected;

    @Parameterized.Parameter(2)
    public String languageTag;

    // Test various language tag strings and verify only valid language tags are returned in XDM
    // mapping.
    @Test
    public void testSerializeToXDM_and_isValidLanguageTag() throws DataReaderException {
        final XDMLifecycleEnvironment environment = new XDMLifecycleEnvironment();
        environment.setLanguage(languageTag);
        final Map<String, Object> result = environment.serializeToXdm();

        if (expected != null) {
            assertEquals(1, result.size());
            Map<String, Object> dublinCore = DataReader.getTypedMap(Object.class, result, "_dc");
            assertEquals(expected, DataReader.getString(dublinCore, "language"));
        } else {
            assertEquals(0, result.size());
        }
    }
}
