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

/**
 * Callback interface to receive errors from the {@code ExtensionApi} methods. Implementers should
 * override this to see what errors are occurring and handle them as needed.
 *
 * @param <ExtensionError> the object type passed to the {@link #error(Object)} method when an error
 *     occurs
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
@Deprecated
public interface ExtensionErrorCallback<ExtensionError> {
    void error(final ExtensionError errorCode);
}
