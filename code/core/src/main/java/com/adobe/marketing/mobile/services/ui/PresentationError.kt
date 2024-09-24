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

package com.adobe.marketing.mobile.services.ui

/**
 * Defines a hierarchy of errors that can occur when managing a UI element
 */
sealed interface PresentationError

/**
 * Types of Presentation errors and each of these classes represent their type hierarchies as needed.
 */
sealed class ShowFailed(val reason: String) : PresentationError
sealed class HideFailed(val reason: String) : PresentationError
sealed class DismissFailed(val reason: String) : PresentationError

// ---- ShowFailed types ---- //
/**
 * Represents a failure to show a Presentable because a conflicting presentation is already shown.
 */
object ConflictingPresentation : ShowFailed("Conflict")

/**
 * Represents a failure to show a Presentable because there is no activity to show it on.
 */
object NoAttachableActivity : ShowFailed("No attachable activity available.")

/**
 * Represents a failure to show a Presentable because the delegate gate was not met.
 */
@Deprecated("Use SuppressedByAppDeveloper instead", ReplaceWith("SuppressedByAppDeveloper"))
object DelegateGateNotMet : ShowFailed("PresentationDelegate suppressed the presentation from being shown.")

/**
 * Represents a failure to show a Presentable because the app developer has suppressed [Presentable]s.
 */
object SuppressedByAppDeveloper : ShowFailed("SuppressedByAppDeveloper")

/**
 * Represents a failure to show a Presentable because it is already shown.
 */
object AlreadyShown : ShowFailed("Presentable is already being shown.")

// ---- HideFailed types ---- //
/**
 * Represents a failure to hide a Presentable because there is no activity to hide it from.
 */
object AlreadyHidden : HideFailed("Presentable is already hidden.")

// ---- DismissFailed types ---- //
/**
 * Represents a failure to dismiss a Presentable because there is no activity to dismiss it from.
 */
object NoActivityToDetachFrom : DismissFailed("No activity available to detach from.")

/**
 * Represents a failure to dismiss a Presentable because it is not shown.
 */
object AlreadyDismissed : DismissFailed("Presentable is already dismissed.")
