/*
  Copyright 2026 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.internal

import com.adobe.marketing.mobile.plugin.IAepPlugin
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * [PluginRegistry] is a process-wide singleton, so each test below declares its own local,
 * single-use fake contract/type. That guarantees no two tests can ever observe each other's
 * registrations, without needing a reset hook on the registry.
 *
 * Contracts are declared as local abstract classes rather than local interfaces - Kotlin does not
 * allow local interfaces - but they still stand in for IAepPlugin's real interface-based contracts
 * for the purpose of exercising PluginRegistry's type-based lookup.
 */
class PluginRegistryTest {

    @Test
    fun addPlugin_singleRegistration_isRetrievableByGetPlugin() {
        abstract class SingleRegistrationContract : IAepPlugin
        class SingleRegistrationPlugin : SingleRegistrationContract()

        val plugin = SingleRegistrationPlugin()

        PluginRegistry.addPlugin(plugin)

        assertSame(plugin, PluginRegistry.getPlugin(SingleRegistrationContract::class.java))
    }

    @Test
    fun addPlugin_sameInstanceRegisteredTwice_isNotDuplicated() {
        abstract class IdempotentContract : IAepPlugin
        class IdempotentPlugin : IdempotentContract()

        val plugin = IdempotentPlugin()

        PluginRegistry.addPlugin(plugin)
        PluginRegistry.addPlugin(plugin)

        assertSame(plugin, PluginRegistry.getPlugin(IdempotentContract::class.java))
        assertEquals(1, countRegisteredAssignableTo(IdempotentContract::class.java))
    }

    @Test
    fun addPlugin_twoDifferentInstancesOfSameContract_bothStoredButFirstRegisteredWins() {
        abstract class CollisionContract : IAepPlugin
        class CollisionPluginA : CollisionContract()
        class CollisionPluginB : CollisionContract()

        val first = CollisionPluginA()
        val second = CollisionPluginB()

        PluginRegistry.addPlugin(first)
        PluginRegistry.addPlugin(second)

        assertSame(first, PluginRegistry.getPlugin(CollisionContract::class.java))
        assertEquals(2, countRegisteredAssignableTo(CollisionContract::class.java))
    }

    @Test
    fun addPlugin_multipleDistinctContracts_eachRetrievableIndependently() {
        abstract class DistinctContractA : IAepPlugin
        abstract class DistinctContractB : IAepPlugin
        class DistinctPluginA : DistinctContractA()
        class DistinctPluginB : DistinctContractB()

        val pluginA = DistinctPluginA()
        val pluginB = DistinctPluginB()

        PluginRegistry.addPlugin(pluginA)
        PluginRegistry.addPlugin(pluginB)

        assertSame(pluginA, PluginRegistry.getPlugin(DistinctContractA::class.java))
        assertSame(pluginB, PluginRegistry.getPlugin(DistinctContractB::class.java))
    }

    @Test
    fun getPlugin_typeNeverRegistered_returnsNull() {
        abstract class NeverRegisteredContract : IAepPlugin

        assertNull(PluginRegistry.getPlugin(NeverRegisteredContract::class.java))
    }

    @Test
    fun getPlugin_requestedAsSupertype_returnsInstanceViaIsInstance() {
        abstract class SupertypeContract : IAepPlugin
        abstract class SubtypeContract : SupertypeContract()
        class SubtypePlugin : SubtypeContract()

        val plugin = SubtypePlugin()

        PluginRegistry.addPlugin(plugin)

        assertSame(plugin, PluginRegistry.getPlugin(SupertypeContract::class.java))
        assertSame(plugin, PluginRegistry.getPlugin(SubtypeContract::class.java))
    }

    @Test
    fun addPlugin_concurrentRegistrationFromMultipleThreads_noExceptionsAndResolvableAfterwards() {
        abstract class ConcurrentContract : IAepPlugin
        class ConcurrentPlugin : ConcurrentContract()

        val threadCount = 16
        val executor = Executors.newFixedThreadPool(threadCount)
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val instances = List(threadCount) { ConcurrentPlugin() }
        val errors = java.util.Collections.synchronizedList(mutableListOf<Throwable>())

        instances.forEach { instance ->
            executor.submit {
                readyLatch.countDown()
                startLatch.await()
                try {
                    PluginRegistry.addPlugin(instance)
                } catch (t: Throwable) {
                    errors.add(t)
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        readyLatch.await(5, TimeUnit.SECONDS)
        startLatch.countDown()
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS))
        executor.shutdown()

        assertTrue(errors.isEmpty())
        assertEquals(threadCount, countRegisteredAssignableTo(ConcurrentContract::class.java))
        assertTrue(instances.contains(PluginRegistry.getPlugin(ConcurrentContract::class.java)))
    }

    /** Reads the private backing list via reflection to verify de-dup/collision invariants that aren't otherwise observable through the public API. */
    private fun countRegisteredAssignableTo(type: Class<*>): Int {
        val field = PluginRegistry::class.java.getDeclaredField("plugins")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val plugins = field.get(PluginRegistry) as List<IAepPlugin>
        return plugins.count { type.isInstance(it) }
    }
}
