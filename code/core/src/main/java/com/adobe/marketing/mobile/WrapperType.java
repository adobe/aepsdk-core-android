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

import com.adobe.marketing.mobile.internal.CoreConstants;

public enum WrapperType {
    NONE(CoreConstants.Wrapper.Type.NONE),
    REACT_NATIVE(CoreConstants.Wrapper.Type.REACT_NATIVE),
    FLUTTER(CoreConstants.Wrapper.Type.FLUTTER),
    CORDOVA(CoreConstants.Wrapper.Type.CORDOVA),
    UNITY(CoreConstants.Wrapper.Type.UNITY),
    XAMARIN(CoreConstants.Wrapper.Type.XAMARIN);

    private final String wrapperTag;

    WrapperType(final String wrapperTag) {
        this.wrapperTag = wrapperTag;
    }

    public String getWrapperTag() {
        return this.wrapperTag;
    }

    public static WrapperType fromString(final String wrapperTag) {
        if (CoreConstants.Wrapper.Type.REACT_NATIVE.equals(wrapperTag)) {
            return REACT_NATIVE;
        } else if (CoreConstants.Wrapper.Type.FLUTTER.equals(wrapperTag)) {
            return FLUTTER;
        } else if (CoreConstants.Wrapper.Type.CORDOVA.equals(wrapperTag)) {
            return CORDOVA;
        } else if (CoreConstants.Wrapper.Type.UNITY.equals(wrapperTag)) {
            return UNITY;
        } else if (CoreConstants.Wrapper.Type.XAMARIN.equals(wrapperTag)) {
            return XAMARIN;
        }

        return NONE;
    }

    public String getFriendlyName() {
        switch (this) {
            case REACT_NATIVE:
                return CoreConstants.Wrapper.Name.REACT_NATIVE;
            case FLUTTER:
                return CoreConstants.Wrapper.Name.FLUTTER;
            case CORDOVA:
                return CoreConstants.Wrapper.Name.CORDOVA;
            case UNITY:
                return CoreConstants.Wrapper.Name.UNITY;
            case XAMARIN:
                return CoreConstants.Wrapper.Name.XAMARIN;
            case NONE:
            default:
                return CoreConstants.Wrapper.Name.NONE;
        }
    }
}
