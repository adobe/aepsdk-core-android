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
 * Abstract class that defines an {@code Extension}
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
public abstract class Extension {
	private ExtensionApi extensionApi;

	/**
	 * Construct the extension and initialize with the {@code ExtensionApi}.
	 *
	 * @param extensionApi the {@link ExtensionApi} this extension will use
	 */
	protected Extension(final ExtensionApi extensionApi) {
		this.extensionApi = extensionApi;
	}

	/**
	 * Get extension name for use by the event hub to managing shared state and for logging.
	 * This MUST be overridden by the extension. If null or empty string is returned, the extension will not be registered.
	 *
	 * @return the extension name as a {@link String}
	 */
	protected abstract String getName();

	/**
	 * Get friendly, human-readable name of the extension.
	 *
	 * @return the friendly extension name as a {@link String}
	 */
	protected String getFriendlyName() {
		return null;
	}

	/**
	 * Get extension version as a string for use by the event hub for logging.
	 *
	 * @return the extension version as a {@link String}
	 */
	protected String getVersion() {
		return null;
	}

	/**
	 * Called when the extension is unregistered by the core.
	 * Implementers can implement this method to clean up resources when the extension is released.
	 */
	protected void onUnregistered() {
		Log.debug(getLogTag(), "Extension unregistered successfully.");
	}

	/**
	 * Called when an unexpected error related to this extension has occurred during SDK processing.
	 * Implementers should override this to see what errors are occurring and handle them as needed.
	 * This should be called very infrequently for a well written extension implementation.
	 *
	 * @param extensionUnexpectedError the {@link ExtensionUnexpectedError} returned from the core
	 */
	protected void onUnexpectedError(final ExtensionUnexpectedError extensionUnexpectedError) {
		ExtensionError error = extensionUnexpectedError != null ? extensionUnexpectedError.getErrorCode() : null;

		if (error != null) {
			Log.error(getLogTag(), "Extension processing failed with error code: %s (%s), error message: %s",
					  error.getErrorCode(), error.getErrorName(), extensionUnexpectedError.getMessage());
		}
	}

	/**
	 * This provides the services the extension will need.
	 *
	 * @return the {@link ExtensionApi} used to handle this extension
	 */
	public final ExtensionApi getApi() {
		return extensionApi;
	}

	/**
	 * Get the log tag for this extension. If {@code extensionApi} is not null, then the result of
	 * {@link ExtensionApi#getLogTag()} is returned. Otherwise, the result of {@link #getName()} is returned.
	 * @return a log tag for this extension
	 */
	private String getLogTag() {
		return extensionApi != null ? extensionApi.getLogTag() : getName();
	}
}
