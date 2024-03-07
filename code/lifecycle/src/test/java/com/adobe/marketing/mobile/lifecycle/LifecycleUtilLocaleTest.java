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

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class LifecycleUtilLocaleTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        // 0: test name,  1: expected result, 2: Locale object to test with
        return Arrays.asList(
                new Object[][] {
                    {
                        // Language only
                        "Language only", "en", new Locale.Builder().setLanguage("en").build()
                    },
                    {
                        // Region only
                        "Region only", "und-US", new Locale.Builder().setRegion("US").build()
                    },
                    {
                        // Script only
                        "Script only", "und-Hant", new Locale.Builder().setScript("Hant").build()
                    },
                    {
                        // Variant only
                        "Variant only",
                        "und-POSIX",
                        new Locale.Builder().setVariant("POSIX").build()
                    },
                    {
                        // Undefined
                        "Undefined Locale", "und", new Locale.Builder().build()
                    },
                    {
                        // Null
                        "Null Locale", null, null
                    },
                    {
                        // Language + Region
                        "Language + Region",
                        "es-US",
                        new Locale.Builder().setLanguage("es").setRegion("US").build()
                    },
                    {
                        // Language + Region + Variant
                        "Language + Region + Variant",
                        "de-DE-POSIX",
                        new Locale("DE", "DE", "POSIX")
                    },
                    {
                        // Language + Variant
                        "Language + Variant",
                        "de-POSIX",
                        new Locale.Builder().setLanguage("de").setVariant("POSIX").build()
                    },
                    {
                        // Language + Script + Region + Variant
                        "Language + Script + Region + Variant",
                        "de-Latn-DE-POSIX",
                        new Locale.Builder()
                                .setLanguage("de")
                                .setRegion("DE")
                                .setScript("Latn")
                                .setVariant("POSIX")
                                .build()
                    },
                    {
                        // Language + Script + Region
                        "Chinese Hong Kong",
                        "zh-Hant-HK",
                        new Locale.Builder()
                                .setLanguage("zh")
                                .setRegion("HK")
                                .setScript("Hant")
                                .build()
                    },
                    {
                        // Language + Script + Region
                        "Chinese China",
                        "zh-Hans-CN",
                        new Locale.Builder()
                                .setLanguage("zh")
                                .setRegion("CN")
                                .setScript("Hans")
                                .build()
                    },
                    {
                        // Language + Script
                        "Language + Script",
                        "it-Latn",
                        new Locale.Builder().setLanguage("it").setScript("Latn").build()
                    },
                    {
                        // Serbian Montenegro
                        "Serbian Montenegro",
                        "sr-ME-x-lvariant-Latn",
                        new Locale("sr", "ME", "Latn")
                    },
                    {
                        // "no" is treated as Norwegian Nynorsk (nn)
                        "Norwegian Nynorsk", "nn-NO", new Locale("no", "NO", "NY")
                    },
                    {
                        // Special case for Japanese Calendar
                        "ja-JP-u-ca-japanese",
                        "ja-JP-u-ca-japanese",
                        Locale.forLanguageTag("ja-JP-u-ca-japanese")
                    },
                    {
                        // Special case for Japanese Calendar
                        "Special case: ja_JP_JP",
                        "ja-JP-u-ca-japanese-x-lvariant-JP",
                        new Locale("JA", "JP", "JP")
                    },
                    {
                        // Special case for Thai Buddhist Calendar
                        "Special case: th_TH_TH",
                        "th-TH-u-nu-thai-x-lvariant-TH",
                        new Locale("TH", "TH", "TH")
                    },
                    {
                        // Special case for Thai Buddhist Calendar
                        "th-TH-u-ca-buddhist",
                        "th-TH-u-ca-buddhist",
                        Locale.forLanguageTag("th-TH-u-ca-buddhist")
                    },
                    {
                        // Special case for Thai Buddhist Calendar
                        "th-TH-u-ca-buddhist-nu-thai",
                        "th-TH-u-ca-buddhist-nu-thai",
                        Locale.forLanguageTag("th-TH-u-ca-buddhist-nu-thai")
                    },
                    {
                        // Grandfathered locale
                        "Grandfathered i-klingon", "tlh", Locale.forLanguageTag("i-klingon")
                    },
                    {
                        // English US with Buddhist Calendar
                        "en-US-ca-buddhist", "en-US", Locale.forLanguageTag("en-US-ca-buddhist")
                    }
                });
    }

    @Parameterized.Parameter(0)
    public String testName;

    @Parameterized.Parameter(1)
    public String expected;

    @Parameterized.Parameter(2)
    public Locale testLocale;

    @Test
    public void testFormatLocaleXDM() {
        String result = LifecycleUtil.formatLocaleXDM(testLocale);
        assertEquals(expected, result);
    }
}
