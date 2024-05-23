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

import com.adobe.marketing.mobile.lifecycle.LifecycleExtension;

public class Lifecycle {

    private static final String EXTENSION_VERSION = "3.0.1";

    public static final Class<? extends Extension> EXTENSION = LifecycleExtension.class;

    private Lifecycle() {}

    /**
     * Returns the version of the {@link Lifecycle} extension
     *
     * @return The version as {@code String}
     */
    public static String extensionVersion() {
        return EXTENSION_VERSION;
    }
}
