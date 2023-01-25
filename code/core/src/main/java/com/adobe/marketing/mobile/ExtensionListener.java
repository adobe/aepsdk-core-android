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

import com.adobe.marketing.mobile.internal.eventhub.EventHub;
import com.adobe.marketing.mobile.internal.eventhub.ExtensionContainer;

/**
 * Abstract class that defines the {@code Event} listener for an {@code Extension}.
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
@Deprecated
public abstract class ExtensionListener {

    private static final String LOG_TAG = "ExtensionListener";
    private final ExtensionApi extensionApi;
    private final String type;
    private final String source;

    /**
     * Extension listener constructor. Must be implemented by any extending classes and must be
     * called from all the implementors
     *
     * @param extensionApi parent {@link ExtensionApi} that owns this listener
     * @param type {@link String} event type to register this listener for
     * @param source {@link String} event source to register this listener for
     */
    protected ExtensionListener(
            final ExtensionApi extensionApi, final String type, final String source) {
        this.extensionApi = extensionApi;
        this.type = type;
        this.source = source;
    }

    public void onUnregistered() {}

    public abstract void hear(Event event);

    /**
     * This provides access to the parent extension that registered this listener in order to
     * process the received event and to use the extension services.
     *
     * <p>The returned {@link Extension} needs to be casted to the registered extension type to
     * access the specific methods.
     *
     * @return the {@link Extension} registered with the {@link EventHub}
     */
    protected Extension getParentExtension() {
        return ((ExtensionContainer) extensionApi).getExtension();
    }
}
