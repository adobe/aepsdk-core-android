/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.lifecycle;

import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class XDMLanguage {
    private final String languageRegex =
            "^(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z](-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)|((en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang)))$";
    private final Pattern languagePattern = Pattern.compile(languageRegex);
    private final String language;

    XDMLanguage(final String language) {
        if (StringUtils.isNullOrEmpty(language) || !isValidLanguageTag(language)) {
            throw new IllegalArgumentException("Language tag failed validation");
        }

        this.language = language;
    }

    String getLanguage() {
        return this.language;
    }

    Map<String, Object> serializeToXdm() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("language", this.language);
        return map;
    }

    /**
     * Validate the language tag is formatted per the XDM Environment Schema required pattern.
     *
     * @param tag the language tag to validate
     * @return true if the language tag matches the pattern.
     */
    private boolean isValidLanguageTag(@NonNull final String tag) {
        return languagePattern.matcher(tag).matches();
    }
}
