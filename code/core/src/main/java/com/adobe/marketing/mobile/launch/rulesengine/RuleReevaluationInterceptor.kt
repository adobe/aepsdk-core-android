package com.adobe.marketing.mobile.launch.rulesengine

import com.adobe.marketing.mobile.Event

/** A callback that should be invoked when the asynchronous work is complete.  */
interface CompletionCallback {
    fun onComplete()
}

/**
 * An interface for an interceptor that is triggered when a [LaunchRule] with the
 * re-evaluation flag is triggered. The interceptor is responsible for updating the rules and
 * invoking the [CompletionCallback] when complete.
 */
interface RuleReevaluationInterceptor {
    fun onReevaluationTriggered(
        event: Event?,
        revaluableRules: MutableList<LaunchRule?>?,
        callback: CompletionCallback?
    )
}