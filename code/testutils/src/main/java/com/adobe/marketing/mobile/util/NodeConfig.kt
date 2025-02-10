/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util

import org.junit.Assert.fail
import java.util.Objects

/**
 * An interface that defines a multi-path configuration.
 *
 * This interface provides the necessary properties to configure multiple paths
 * within a node configuration context. It is designed to be used where multiple
 * paths need to be specified along with associated configuration options.
 */
interface MultiPathConfig {
    /**
     * A Boolean value indicating whether the configuration is active.
     */
    val config: NodeConfig.Config

    /**
     * A `NodeConfig.Scope` value defining the scope of the configuration, such as whether it is applied to a single node or a subtree.
     */
    val scope: NodeConfig.Scope

    /**
     * An array of optional strings representing the paths to be configured.
     * Each string in the array represents a distinct path. `null` indicates the top-level object.
     */
    val paths: List<String?>

    /**
     * A `NodeConfig.OptionKey` value that specifies the type of option applied to the paths.
     */
    val optionKey: NodeConfig.OptionKey
}

/**
 * A data class representing the configuration for a single path.
 *
 * This data class is used to define the configuration details for a specific path within
 * a node configuration context. It encapsulates the path's specific options and settings.
 */
data class PathConfig(
    /**
     * A Boolean value indicating whether the configuration is active.
     */
    var config: NodeConfig.Config,

    /**
     * A `NodeConfig.Scope` value defining the scope of the configuration, such as whether it is applied to a single node or a subtree.
     */
    var scope: NodeConfig.Scope,

    /**
     * An optional String representing the path to be configured. `null` indicates the top-level object.
     */
    var path: String?,

    /**
     * A `NodeConfig.OptionKey` value that specifies the type of option applied to the path.
     */
    var optionKey: NodeConfig.OptionKey
)

/**
 * Validation option which specifies: Array elements from `expected` may match elements from `actual` regardless of index position.
 * When combining any position option indexes and standard indexes, standard indexes are validated first.
 */
data class AnyOrderMatch(
    override val config: NodeConfig.Config = NodeConfig.Config(isActive = true),
    override val scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode,
    override val paths: List<String?> = listOf(null),
    override val optionKey: NodeConfig.OptionKey = NodeConfig.OptionKey.AnyOrderMatch
) : MultiPathConfig {
    companion object {
        private val defaultPaths = listOf(null)
        private val defaultScope = NodeConfig.Scope.SingleNode
    }

    /**
     * Initializes a new instance with the specified parameters.
     *
     * @param isActive Boolean value indicating whether the configuration is active.
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths A list of optional path strings.
     */
    constructor(isActive: Boolean = true, scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, paths: List<String?> = defaultPaths) :
        this(NodeConfig.Config(isActive = isActive), scope, paths)

    // Secondary constructor permutations are explicitly defined for Java compatibility
    constructor(isActive: Boolean, paths: List<String?>) :
        this(isActive, defaultScope, paths)

    constructor(scope: NodeConfig.Scope, paths: List<String?>) :
        this(true, scope, paths)

    constructor(isActive: Boolean, scope: NodeConfig.Scope) :
        this(isActive, scope, defaultPaths)

    constructor(isActive: Boolean) :
        this(isActive, defaultPaths)

    constructor(scope: NodeConfig.Scope) :
        this(scope, defaultPaths)

    constructor(paths: List<String?>) :
        this(defaultScope, paths)

    // Variadic initializers rely on their List<*> constructor counterparts
    /**
     * Variadic initializer allowing multiple string paths.
     *
     * @param isActive Boolean value indicating whether the configuration is active.
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths Vararg of optional path strings.
     */
    constructor(isActive: Boolean = true, scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, vararg paths: String?) :
        this(isActive, scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(isActive: Boolean, vararg paths: String?) :
        this(isActive, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(scope: NodeConfig.Scope, vararg paths: String?) :
        this(scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(vararg paths: String?) :
        this(if (paths.isEmpty()) defaultPaths else paths.toList())
}

/**
 * Validation option which specifies: Collections (objects and/or arrays) must have the same number of elements.
 */
data class CollectionEqualCount(
    override val config: NodeConfig.Config = NodeConfig.Config(isActive = true),
    override val scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode,
    override val paths: List<String?> = listOf(null),
    override val optionKey: NodeConfig.OptionKey = NodeConfig.OptionKey.CollectionEqualCount,
) : MultiPathConfig {
    companion object {
        private val defaultPaths = listOf(null)
        private val defaultScope = NodeConfig.Scope.SingleNode
    }

    /**
     * Initializes a new instance with the specified parameters.
     *
     * @param isActive Boolean value indicating whether the configuration is active.
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths A list of optional path strings.
     */
    constructor(isActive: Boolean = true, scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, paths: List<String?> = defaultPaths) :
        this(NodeConfig.Config(isActive = isActive), scope, paths)

    // Secondary constructor permutations are explicitly defined for Java compatibility
    constructor(isActive: Boolean, paths: List<String?>) :
        this(isActive, defaultScope, paths)

    constructor(scope: NodeConfig.Scope, paths: List<String?>) :
        this(true, scope, paths)

    constructor(isActive: Boolean, scope: NodeConfig.Scope) :
        this(isActive, scope, defaultPaths)

    constructor(isActive: Boolean) :
        this(isActive, defaultPaths)

    constructor(scope: NodeConfig.Scope) :
        this(scope, defaultPaths)

    constructor(paths: List<String?>) :
        this(defaultScope, paths)

    // Variadic initializers rely on their List<*> constructor counterparts
    /**
     * Variadic initializer allowing multiple string paths.
     *
     * @param isActive Boolean value indicating whether the configuration is active.
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths Vararg of optional path strings.
     */
    constructor(isActive: Boolean = true, scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, vararg paths: String?) :
        this(isActive, scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(isActive: Boolean, vararg paths: String?) :
        this(isActive, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(scope: NodeConfig.Scope, vararg paths: String?) :
        this(scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(vararg paths: String?) :
        this(if (paths.isEmpty()) defaultPaths else paths.toList())
}

/**
 * Validation option which specifies: The given number of elements (dictionary keys and array elements)
 * must be present.
 */
data class ElementCount(
    override val config: NodeConfig.Config = NodeConfig.Config(isActive = true),
    override val scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode,
    override val paths: List<String?> = listOf(null),
    override val optionKey: NodeConfig.OptionKey = NodeConfig.OptionKey.ElementCount,
) : MultiPathConfig {
    companion object {
        private val defaultPaths = listOf(null)
        private val defaultScope = NodeConfig.Scope.SingleNode
    }

    /**
     * Initializes a new instance with the specified parameters.
     *
     * @param requiredCount The number of elements required.
     * @param isActive Boolean value indicating whether the configuration is active.
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths A list of optional path strings.
     */
    constructor(requiredCount: Int?, isActive: Boolean = true, scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, paths: List<String?> = defaultPaths) :
        this(NodeConfig.Config(isActive = isActive, elementCount = requiredCount), scope, paths)

    // Secondary constructor permutations are explicitly defined for Java compatibility
    constructor(requiredCount: Int?, isActive: Boolean, paths: List<String?>) :
        this(requiredCount, isActive, defaultScope, paths)

    constructor(requiredCount: Int?, scope: NodeConfig.Scope, paths: List<String?>) :
        this(requiredCount, true, scope, paths)

    constructor(requiredCount: Int?, isActive: Boolean, scope: NodeConfig.Scope) :
        this(requiredCount, isActive, scope, defaultPaths)

    constructor(requiredCount: Int?, isActive: Boolean) :
        this(requiredCount, isActive, defaultPaths)

    constructor(requiredCount: Int?, scope: NodeConfig.Scope) :
        this(requiredCount, scope, defaultPaths)

    constructor(requiredCount: Int?, paths: List<String?>) :
        this(requiredCount, defaultScope, paths)

    // Variadic initializers rely on their List<*> constructor counterparts
    /**
     * Variadic initializer allowing multiple string paths.
     *
     * @param requiredCount The number of elements required.
     * @param isActive Boolean value indicating whether the configuration is active.
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths Vararg of optional path strings.
     */
    constructor(requiredCount: Int?, isActive: Boolean = true, scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, vararg paths: String?) :
        this(requiredCount, isActive, scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(requiredCount: Int?, isActive: Boolean, vararg paths: String?) :
        this(requiredCount, isActive, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(requiredCount: Int?, scope: NodeConfig.Scope, vararg paths: String?) :
        this(requiredCount, scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(requiredCount: Int?, vararg paths: String?) :
        this(requiredCount, if (paths.isEmpty()) defaultPaths else paths.toList())
}

/**
 * Validation option which specifies: `actual` must not have the key name specified.
 */
data class KeyMustBeAbsent(
    override val config: NodeConfig.Config = NodeConfig.Config(isActive = true),
    override val scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode,
    override val paths: List<String?> = listOf(null),
    override val optionKey: NodeConfig.OptionKey = NodeConfig.OptionKey.KeyMustBeAbsent,
) : MultiPathConfig {
    companion object {
        private val defaultPaths = listOf(null)
        private val defaultScope = NodeConfig.Scope.SingleNode
    }

    /**
     * Initializes a new instance with the specified parameters.
     *
     * @param isActive Boolean value indicating whether the configuration is active.
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths A list of optional path strings.
     */
    constructor(isActive: Boolean = true, scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, paths: List<String?> = defaultPaths) :
        this(NodeConfig.Config(isActive = isActive), scope, paths)

    // Secondary constructor permutations are explicitly defined for Java compatibility
    constructor(isActive: Boolean, paths: List<String?>) :
        this(isActive, defaultScope, paths)

    constructor(scope: NodeConfig.Scope, paths: List<String?>) :
        this(true, scope, paths)

    constructor(isActive: Boolean, scope: NodeConfig.Scope) :
        this(isActive, scope, defaultPaths)

    constructor(isActive: Boolean) :
        this(isActive, defaultPaths)

    constructor(scope: NodeConfig.Scope) :
        this(scope, defaultPaths)

    constructor(paths: List<String?>) :
        this(defaultScope, paths)

    // Variadic initializers rely on their List<*> constructor counterparts
    /**
     * Variadic initializer allowing multiple string paths.
     *
     * @param isActive Boolean value indicating whether the configuration is active.
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths Vararg of optional path strings.
     */
    constructor(isActive: Boolean = true, scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, vararg paths: String?) :
        this(isActive, scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(isActive: Boolean, vararg paths: String?) :
        this(isActive, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(scope: NodeConfig.Scope, vararg paths: String?) :
        this(scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(vararg paths: String?) :
        this(if (paths.isEmpty()) defaultPaths else paths.toList())
}

/**
 * Validation option which specifies: values must have the same type but the literal values must not be equal.
 */
data class ValueNotEqual(
    override val config: NodeConfig.Config = NodeConfig.Config(isActive = true),
    override val scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode,
    override val paths: List<String?> = listOf(null),
    override val optionKey: NodeConfig.OptionKey = NodeConfig.OptionKey.ValueNotEqual,
) : MultiPathConfig {
    companion object {
        private val defaultPaths = listOf(null)
        private val defaultScope = NodeConfig.Scope.SingleNode
    }

    /**
     * Initializes a new instance with the specified parameters.
     *
     * @param isActive Boolean value indicating whether the configuration is active.
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths A list of optional path strings.
     */
    constructor(isActive: Boolean = true, scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, paths: List<String?> = defaultPaths) :
        this(NodeConfig.Config(isActive = isActive), scope, paths)

    // Secondary constructor permutations are explicitly defined for Java compatibility
    constructor(isActive: Boolean, paths: List<String?>) :
        this(isActive, defaultScope, paths)

    constructor(scope: NodeConfig.Scope, paths: List<String?>) :
        this(true, scope, paths)

    constructor(isActive: Boolean, scope: NodeConfig.Scope) :
        this(isActive, scope, defaultPaths)

    constructor(isActive: Boolean) :
        this(isActive, defaultPaths)

    constructor(scope: NodeConfig.Scope) :
        this(scope, defaultPaths)

    constructor(paths: List<String?>) :
        this(defaultScope, paths)

    // Variadic initializers rely on their List<*> constructor counterparts
    /**
     * Variadic initializer allowing multiple string paths.
     *
     * @param isActive Boolean value indicating whether the configuration is active.
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths Vararg of optional path strings.
     */
    constructor(isActive: Boolean = true, scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, vararg paths: String?) :
        this(isActive, scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(isActive: Boolean, vararg paths: String?) :
        this(isActive, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(scope: NodeConfig.Scope, vararg paths: String?) :
        this(scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(vararg paths: String?) :
        this(if (paths.isEmpty()) defaultPaths else paths.toList())
}

/**
 * Validation option which specifies that values must have the same type and literal value.
 * This class applies to specified paths within a data structure, ensuring that values at these paths
 * are exactly the same both in type and value.
 *
 * @property paths List of optional string paths indicating where the exact match validation is applied.
 * @property optionKey Constant from NodeConfig.OptionKey indicating the specific validation option for exact matches.
 * @property config Configuration details indicating whether this validation is active.
 * @property scope Scope of the validation, indicating the extent of the data structure this rule applies to.
 */
data class ValueExactMatch(
    override val config: NodeConfig.Config = NodeConfig.Config(isActive = true),
    override val scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode,
    override val paths: List<String?> = listOf(null),
    override val optionKey: NodeConfig.OptionKey = NodeConfig.OptionKey.PrimitiveExactMatch,
) : MultiPathConfig {
    companion object {
        private val defaultPaths = listOf(null)
        private val defaultScope = NodeConfig.Scope.SingleNode
    }

    /**
     * Initializes a new instance with the specified parameters.
     *
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths A list of optional path strings.
     */
    constructor(scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, paths: List<String?> = defaultPaths) :
        this(NodeConfig.Config(isActive = true), scope, paths)

    // Secondary constructor permutations are explicitly defined for Java compatibility
    constructor(scope: NodeConfig.Scope) :
        this(scope, defaultPaths)

    constructor(paths: List<String?>) :
        this(defaultScope, paths)

    // Variadic initializers rely on their List<*> constructor counterparts
    /**
     * Variadic initializer allowing multiple string paths.
     *
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths Vararg of optional path strings.
     */
    constructor(scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, vararg paths: String?) :
        this(scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(vararg paths: String?) :
        this(if (paths.isEmpty()) defaultPaths else paths.toList())
}

/**
 * Validation option which specifies: values must have the same type but their literal values can be different.
 */
data class ValueTypeMatch(
    override val config: NodeConfig.Config = NodeConfig.Config(isActive = false),
    override val scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode,
    override val paths: List<String?> = listOf(null),
    override val optionKey: NodeConfig.OptionKey = NodeConfig.OptionKey.PrimitiveExactMatch,
) : MultiPathConfig {
    companion object {
        private val defaultPaths = listOf(null)
        private val defaultScope = NodeConfig.Scope.SingleNode
    }

    /**
     * Initializes a new instance with the specified parameters.
     *
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths A list of optional path strings.
     */
    constructor(scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, paths: List<String?> = defaultPaths) :
        this(NodeConfig.Config(isActive = false), scope, paths)

    // Secondary constructor permutations are explicitly defined for Java compatibility
    constructor(scope: NodeConfig.Scope) :
        this(scope, defaultPaths)

    constructor(paths: List<String?>) :
        this(defaultScope, paths)

    // Variadic initializers rely on their List<*> constructor counterparts
    /**
     * Variadic initializer allowing multiple string paths.
     *
     * @param scope The scope of configuration, defaulting to single node.
     * @param paths Vararg of optional path strings.
     */
    constructor(scope: NodeConfig.Scope = NodeConfig.Scope.SingleNode, vararg paths: String?) :
        this(scope, if (paths.isEmpty()) defaultPaths else paths.toList())

    constructor(vararg paths: String?) :
        this(if (paths.isEmpty()) defaultPaths else paths.toList())
}

/**
 * A class representing the configuration for a node in a tree structure.
 *
 * `NodeConfig` provides a way to set configuration options for nodes in a hierarchical tree structure.
 * It supports different types of configuration options, including options that apply to individual nodes
 * or to entire subtrees.
 */
class NodeConfig {
    /**
     * Represents the scope of the configuration; that is, to which nodes the configuration applies.
     */
    enum class Scope(val value: String) {
        SingleNode("SingleNode"),
        Subtree("Subtree")
    }

    /**
     * Defines the types of configuration options available for nodes.
     */
    enum class OptionKey(val value: String) {
        AnyOrderMatch("AnyOrderMatch"),
        CollectionEqualCount("CollectionEqualCount"),
        ElementCount("ElementCount"),
        KeyMustBeAbsent("KeyMustBeAbsent"),
        PrimitiveExactMatch("PrimitiveExactMatch"),
        ValueNotEqual("ValueNotEqual")
    }

    /**
     * Represents the configuration details for a comparison option
     */
    data class Config(val isActive: Boolean, val elementCount: Int? = null) {
        fun deepCopy(): Config {
            return Config(isActive, elementCount)
        }
    }

    private data class PathComponent(
        var name: String?,
        var isAnyOrder: Boolean,
        var isArray: Boolean,
        var isWildcard: Boolean
    )

    /**
     * A string representing the name of the node. `null` refers to the top level object
     */
    private var name: String? = null
    /**
     * Options set specifically for this node. Specific `OptionKey`s may or may not be present - it is optional.
     */
    private var options: MutableMap<OptionKey, Config> = mutableMapOf()
    /**
     * Options set for the subtree, used as the default option when no node-specific options are set. All `OptionKey`s MUST be
     * present.
     */
    private var subtreeOptions: MutableMap<OptionKey, Config> = mutableMapOf()

    /**
     * The set of child nodes.
     */
    private var _children: MutableSet<NodeConfig> = mutableSetOf()
    val children: MutableSet<NodeConfig>
        get() = _children
    /**
     * The node configuration for wildcard children
     */
    private var wildcardChildren: NodeConfig? = null

    // Property accessors for each option which use the `options` set for the current node
    // and fall back to subtree options.
    var anyOrderMatch: Config
        get() = options[OptionKey.AnyOrderMatch]
            ?: subtreeOptions[OptionKey.AnyOrderMatch]
            ?: Config(false)
        set(value) { options[OptionKey.AnyOrderMatch] = value }

    var collectionEqualCount: Config
        get() = options[OptionKey.CollectionEqualCount]
            ?: subtreeOptions[OptionKey.CollectionEqualCount]
            ?: Config(false)
        set(value) { options[OptionKey.CollectionEqualCount] = value }

    var elementCount: Config
        get() = options[OptionKey.ElementCount]
            ?: subtreeOptions[OptionKey.ElementCount]
            ?: Config(true)
        set(value) { options[OptionKey.ElementCount] = value }

    var keyMustBeAbsent: Config
        get() = options[OptionKey.KeyMustBeAbsent]
            ?: subtreeOptions[OptionKey.KeyMustBeAbsent]
            ?: Config(false)
        set(value) { options[OptionKey.KeyMustBeAbsent] = value }

    var primitiveExactMatch: Config
        get() = options[OptionKey.PrimitiveExactMatch]
            ?: subtreeOptions[OptionKey.PrimitiveExactMatch]
            ?: Config(false)
        set(value) { options[OptionKey.PrimitiveExactMatch] = value }

    var valueNotEqual: Config
        get() = options[OptionKey.ValueNotEqual]
            ?: subtreeOptions[OptionKey.ValueNotEqual]
            ?: Config(false)
        set(value) { options[OptionKey.ValueNotEqual] = value }

    /**
     * Creates a new node with the given values.
     *
     * Make sure to specify **all** `OptionKey` values for `subtreeOptions`, especially when the node is intended to be the root.
     * These subtree options will be used for all descendants unless otherwise specified. If any subtree option keys are missing,
     * a default value will be provided.
     */
    @JvmOverloads
    constructor(
        name: String?,
        options: MutableMap<OptionKey, Config> = mutableMapOf(),
        subtreeOptions: MutableMap<OptionKey, Config>,
        children: MutableSet<NodeConfig> = mutableSetOf(),
        wildcardChildren: NodeConfig? = null
    ) {
        // Validate subtreeOptions has every option defined
        val validatedSubtreeOptions = subtreeOptions.toMutableMap()
        OptionKey.values().forEach { key ->
            if (!validatedSubtreeOptions.containsKey(key)) {
                validatedSubtreeOptions[key] = Config(isActive = false)
            }
        }

        this.name = name
        this.options = options
        this.subtreeOptions = validatedSubtreeOptions
        this._children = children
        this.wildcardChildren = wildcardChildren
    }

    companion object {
        /**
         * Resolves a given node's option using the following precedence:
         * 1. Single node config
         *    a. Child node
         *    b. Parent's wildcard node
         *    c. Parent node
         * 2. Subtree config
         *    a. Child node (by definition supersedes wildcard subtree option)
         *    b. Parent node (only if child node doesn't exist)
         *
         * This is to handle the case where an array has a node-specific option like AnyPosition match which
         * should apply to all direct children (that is, only 1 level down), but one of the children has a
         * node specific option disabling AnyPosition match.
         */
        @JvmStatic
        fun resolveOption(option: OptionKey, childName: String?, parentNode: NodeConfig): Config {
            val childNode = parentNode.getChild(childName)
            // Single node options
            // Current node
            childNode?.options?.get(option)?.let {
                return it
            }
            // Parent's wildcard child
            parentNode.wildcardChildren?.options?.get(option)?.let {
                return it
            }
            // Check parent array's node-specific option
            parentNode.options[option]?.let {
                return it
            }
            // Check subtree options in the same order of precedence, with the condition that if childNode exists,
            // it must have a subtree definition. Fallback to parentNode only if childNode doesn't exist.
            return childNode?.subtreeOptions?.get(option) ?: parentNode.subtreeOptions[option] ?: Config(false)
        }

        @JvmStatic
        fun resolveOption(option: OptionKey, childName: Int?, parentNode: NodeConfig): Config {
            return resolveOption(option, childName?.toString(), parentNode)
        }
    }

    /**
     * Determines if two `NodeConfig` instances are equal based on their properties.
     */
    override fun equals(other: Any?): Boolean = other is NodeConfig &&
        name == other.name &&
        options == other.options &&
        subtreeOptions == other.subtreeOptions

    /**
     * Generates a hash code for a `NodeConfig`.
     */
    override fun hashCode(): Int = Objects.hash(name, options, subtreeOptions)

    /**
     * Creates a deep copy of the current `NodeConfig` instance.
     */
    fun deepCopy(): NodeConfig {
        return NodeConfig(
            name = name,
            options = HashMap(options),
            subtreeOptions = HashMap(subtreeOptions),
            children = _children.map { it.deepCopy() }.toMutableSet(),
            wildcardChildren = wildcardChildren?.deepCopy()
        )
    }

    /**
     * Gets a child node with the specified name.
     */
    fun getChild(name: String?): NodeConfig? = _children.firstOrNull { it.name == name }

    /**
     * Gets a child node at the specified index if it represents as a string.
     */
    fun getChild(index: Int?): NodeConfig? {
        return index?.let {
            val indexString = it.toString()
            _children.firstOrNull { child -> child.name == indexString }
        }
    }

    /**
     * Gets the next node for the given name, falling back to wildcard or asFinalNode if not found.
     */
    fun getNextNode(forName: String?): NodeConfig =
        getChild(forName) ?: wildcardChildren ?: asFinalNode()

    /**
     * Gets the next node for the given index, falling back to wildcard or asFinalNode if not found.
     */
    fun getNextNode(forIndex: Int?): NodeConfig =
        getChild(forIndex) ?: wildcardChildren ?: asFinalNode()

    /**
     * Creates a new NodeConfig instance representing the final node configuration.
     * Basically sets the last known subtree options to be used as the default comparison options
     * for the rest of the validation.
     *
     * @return A new NodeConfig instance with the current subtree options.
     */
    private fun asFinalNode(): NodeConfig {
        // Should not modify self since other recursive function calls may still depend on children.
        // Instead, return a new instance with the proper values set
        return NodeConfig(name = null, options = mutableMapOf(), subtreeOptions = deepCopySubtreeOptionsWithElementCountReset(subtreeOptions))
    }

    /**
     * Provides access to the [Config] for a given option key at the [Scope.SingleNode] level.
     */
    fun getSingleNodeOption(key: OptionKey): Config? {
        return options[key]
    }

    /**
     * Provides access to the [Config] for a given option key at the [Scope.Subtree] level.
     */
    fun getSubtreeNodeOption(key: OptionKey): Config? {
        return subtreeOptions[key]
    }

    /**
     * Creates or updates nodes based on multiple path configurations.
     * This function processes a collection of paths and updates or creates the corresponding nodes.
     *
     * @param multiPathConfig Configuration for multiple paths including common option key, config, and scope.
     */
    fun createOrUpdateNode(multiPathConfig: MultiPathConfig) {
        val pathConfigs = multiPathConfig.paths.map {
            PathConfig(
                path = it,
                optionKey = multiPathConfig.optionKey,
                config = multiPathConfig.config,
                scope = multiPathConfig.scope
            )
        }
        for (pathConfig in pathConfigs) {
            createOrUpdateNode(pathConfig)
        }
    }

    /**
     * Helper method to create or traverse nodes.
     * This function processes a single path configuration and updates or creates nodes accordingly.
     *
     * @param pathConfig Configuration for a single path including option key, config, and scope.
     */
    fun createOrUpdateNode(pathConfig: PathConfig) {
        val pathComponents = getProcessedPathComponents(pathConfig.path)
        updateTree(mutableListOf(this), pathConfig, pathComponents)
    }

    /**
     * Updates a tree of nodes based on the provided path configuration and path components.
     * This function recursively applies configurations to nodes, traversing through the path defined by the path components.
     * It supports applying options to individual nodes or entire subtrees based on the scope defined in the path configuration.
     *
     * @param nodes The list of current nodes to update.
     * @param pathConfig The configuration to apply, including the option key and its scope.
     * @param pathComponents The components of the path, dictating how deep the configuration should be applied.
     */
    private fun updateTree(nodes: MutableList<NodeConfig>, pathConfig: PathConfig, pathComponents: MutableList<PathComponent>) {
        if (nodes.isEmpty()) return
        // Reached the end of the pathComponents - apply the PathConfig to the current nodes
        if (pathComponents.isEmpty()) {
            // Apply the node option to the final node
            nodes.forEach { node ->
                if (pathConfig.scope == Scope.Subtree) {
                    // Propagate this subtree option update to all children
                    propagateSubtreeOption(node, pathConfig)
                } else {
                    node.options[pathConfig.optionKey] = pathConfig.config.deepCopy()
                }
            }
            return
        }

        // Path components are added in order - the first element is closer to the root
        // Ex: "key1[0].key2[23]" -> ["key1", "0" (isArray), "key2", "23" (isArray)]
        // Note: there cannot be collisions between array index names as strings and object key names
        // as integer strings since the collection type itself during actual traversal prevents this overlap
        val pathComponent = pathComponents.removeFirst()
        val nextNodes = mutableListOf<NodeConfig>()

        nodes.forEach { node ->
            // Note: wildcard node names are the same as their reserved strings: `*` and `[*]`
            pathComponent.name?.let { pathComponentName ->
                val child = findOrCreateChild(node, pathComponentName, pathComponent.isWildcard)
                nextNodes.add(child)

                // Current path component adds all existing specific index/key name children of the current node
                // so that they also get the configuration from the wildcard applied to them too (wildcard is a superset)
                if (pathComponent.isWildcard) {
                    nextNodes.addAll(node._children)
                }
            }
        }
        updateTree(nextNodes, pathConfig, pathComponents)
    }

    private fun deepCopySubtreeOptionsWithElementCountReset(map: MutableMap<OptionKey, Config>): MutableMap<OptionKey, Config> {
        val deepCopiedSubtreeOptions = map
            .mapValues { it.value.deepCopy() }
            .toMutableMap()
            .apply {
                // - Subtree options should always exist, but backup value defaults to false
                // - ElementCount's requiredCount value should be removed for nodes that are not explicitly the
                // node that had that option set, otherwise the expectation propagates improperly to all children
                this[OptionKey.ElementCount] = Config(this[OptionKey.ElementCount]?.isActive ?: true, null)
            }
        return deepCopiedSubtreeOptions
    }

    /**
     * Processes the given path string into individual path components with detailed properties.
     * This function analyzes a path string, typically representing a navigation path in a structure,
     * and breaks it down into components that specify details about how each segment of the path should be treated,
     * such as whether it's an array, a wildcard, or requires any specific order handling.
     *
     * @param pathString The path string to be processed.
     * @return A list of [PathComponent] reflecting the structured breakdown of the path string.
     */
    private fun getProcessedPathComponents(pathString: String?): MutableList<PathComponent> {
        val objectPathComponents = getObjectPathComponents(pathString)
        val pathComponents = mutableListOf<PathComponent>()
        for (objectPathComponent in objectPathComponents) {
            // Remove escaped dot notations from the path string name provided
            val key = objectPathComponent.replace("\\.", ".")
            // Extract the string part and array component part(s) from the key string
            val (stringComponent, arrayComponents) = getArrayPathComponents(key)
            // Process object key path components
            stringComponent?.let {
                // Check if the current component is the reserved object key wildcard character: `*`
                val isWildcard = stringComponent == "*"
                pathComponents.add(
                    PathComponent(
                        // Remove escape character from escaped wildcard in original path when not interpreted
                        // as a wildcard
                        name = if (isWildcard) stringComponent else stringComponent.replace("\\*", "*"),
                        isAnyOrder = false,
                        isArray = false,
                        isWildcard = isWildcard
                    )
                )
            }

            // Process array path components
            for (arrayComponent in arrayComponents) {
                // Check if the current component is the reserved array wildcard index sequence: `[*]`
                if (arrayComponent == "[*]") {
                    pathComponents.add(
                        PathComponent(
                            name = arrayComponent,
                            isAnyOrder = false,
                            isArray = true,
                            isWildcard = true
                        )
                    )
                } else {
                    val indexResult = getArrayIndexAndAnyOrder(arrayComponent)
                    indexResult?.let {
                        pathComponents.add(
                            PathComponent(
                                name = it.first.toString(),
                                isAnyOrder = it.second,
                                isArray = true,
                                isWildcard = false
                            )
                        )
                    }
                        ?: return pathComponents // Test failure emitted by extractIndexAndWildcardStatus
                }
            }
        }
        return pathComponents
    }

    /**
     * Finds or creates a child node within the given node, handling the assignment to the proper descendants' location.
     * This method ensures that if the child node already exists, it is returned; otherwise, a new child node is created.
     * If a wildcard child node is needed, it either returns an existing wildcard child or creates a new one and assigns it.
     *
     * @param parentNode The parent node in which to find or create a child.
     * @param childNodeName The name of the child node to find or create.
     * @param childNodeIsWildcard Indicates whether the child node to be created should be treated as a wildcard node.
     * @return The found or newly created child node.
     */
    private fun findOrCreateChild(parentNode: NodeConfig, childNodeName: String, childNodeIsWildcard: Boolean): NodeConfig {
        return if (childNodeIsWildcard) {
            parentNode.wildcardChildren ?: run {
                // Apply subtreeOptions to the child
                val newChild = NodeConfig(name = childNodeName, subtreeOptions = deepCopySubtreeOptionsWithElementCountReset(parentNode.subtreeOptions))
                parentNode.wildcardChildren = newChild
                newChild
            }
        } else {
            parentNode._children.firstOrNull { it.name == childNodeName } ?: run {
                // If a wildcard child already exists, use that as the base, deep copying its existing setup
                parentNode.wildcardChildren?.deepCopy()?.apply {
                    this.name = childNodeName
                    parentNode._children.add(this)
                    // If a wildcard child doesn't exist, create a new child from scratch
                } ?: run {
                    // Apply subtreeOptions to the child
                    val newChild = NodeConfig(name = childNodeName, subtreeOptions = deepCopySubtreeOptionsWithElementCountReset(parentNode.subtreeOptions))
                    parentNode._children.add(newChild)
                    newChild
                }
            }
        }
    }

    /**
     * Propagates a subtree option from the given path configuration to the specified node and all its descendants.
     * In the ElementCount case, removes the element count assertion when propagating to child nodes.
     *
     * @param node The node from which to start propagating the subtree option.
     * @param pathConfig The configuration containing the option to propagate.
     */
    private fun propagateSubtreeOption(node: NodeConfig, pathConfig: PathConfig) {
        val key = pathConfig.optionKey
        // Set the subtree configuration for the current node and its wildcard config (if it exists)
        node.subtreeOptions[key] = pathConfig.config.deepCopy()
        // A non-null elementCount means the ElementCount assertion is active at the given node;
        // however, child nodes (including wildcard children) should not inherit this assertion.
        // The element counter is set to null so that while the direct path target of the subtree
        // option has the counter applied, this assertion is not propagated to any children.
        val elementCountRemovedConfig = Config(pathConfig.config.isActive, null)
        node.wildcardChildren?.subtreeOptions?.set(key, elementCountRemovedConfig)
        val elementCountRemovedPathConfig = PathConfig(elementCountRemovedConfig, pathConfig.scope, pathConfig.path, pathConfig.optionKey)
        for (child in node._children) {
            // Only propagate the subtree value for the specific option key,
            // otherwise, previously set subtree values will be reset to the default values
            child.subtreeOptions[key] = elementCountRemovedPathConfig.config
            propagateSubtreeOption(child, elementCountRemovedPathConfig)
        }
    }

    /**
     * Extracts and returns a pair with a valid index and a flag indicating whether it's an `AnyOrder` index from a single array path segment.
     *
     * This method considers a key that matches the array access format (ex: `[*123]` or `[123]`).
     * It identifies an index by optionally checking for the wildcard marker `*`.
     *
     * @param pathComponent A single path component which may contain a potential index with or without a wildcard marker.
     * @return A Pair containing an optional valid `Int` index and a boolean indicating whether it's a wildcard index,
     *   returns `null` if no valid index is found.
     *
     * Note:
     * Examples of conversions:
     * - `[*123]` -> Pair(123, true)
     * - `[123]` -> Pair(123, false)
     * - `[*ab12]` causes a failure since "ab12" is not a valid integer.
     */
    private fun getArrayIndexAndAnyOrder(pathComponent: String): Pair<Int, Boolean>? {
        val arrayIndexValueRegex = "^\\[(.*?)\\]$".toRegex()
        val arrayIndexValue = arrayIndexValueRegex.find(pathComponent)?.groupValues?.get(1)

        if (arrayIndexValue == null) {
            fail("Error: unable to find valid index value from path component: $pathComponent")
            return null
        }

        val isAnyOrder = arrayIndexValue.startsWith("*")
        val indexString = if (isAnyOrder) arrayIndexValue.drop(1) else arrayIndexValue

        val validIndex = indexString.toIntOrNull()
        if (validIndex == null) {
            fail("Error: Index is not a valid Int: $indexString")
            return null
        }

        return Pair(validIndex, isAnyOrder)
    }

    /**
     * Breaks a path string into its nested *object* segments. Any trailing *array* style access components are bundled with a
     * preceding object segment (if the object segment exists).
     *
     * For example, the key path: `"key0\.key1.key2[1][2].key3"`, represents a path to an element in a nested
     * JSON structure. The result for the input is: `["key0\.key1", "key2[1][2]", "key3"]`.
     *
     * The method breaks each object path segment separated by the `.` character and escapes
     * the sequence `\.` as a part of the key itself (that is, it ignores `\.` as a nesting indicator).
     *
     * @param path The key path string to be split into its nested object segments.
     * @return A list of strings representing the individual components of the key path. If the input `path` is null or empty,
     * a list containing an empty string is returned. If no components are found, an empty list is returned.
     */
    fun getObjectPathComponents(path: String?): List<String> {
        // Handle edge case where input is null
        if (path == null) {
            return emptyList()
        }
        // Handle edge case where input is empty
        if (path.isEmpty()) return listOf("")

        val segments = mutableListOf<String>()
        var startIndex = 0
        var inEscapeSequence = false

        // Iterate over each character in the input string with its index
        path.forEachIndexed { index, char ->
            when {
                char == '\\' -> inEscapeSequence = true
                char == '.' && !inEscapeSequence -> {
                    // Add the segment from the start index to current index (excluding the dot)
                    segments.add(path.substring(startIndex, index))

                    // Update the start index for the next segment
                    startIndex = index + 1
                }
                else -> inEscapeSequence = false
            }
        }

        // Add the remaining segment after the last dot (if any)
        segments.add(path.substring(startIndex))

        // Handle edge case where input ends with a dot (but not an escaped dot)
        if (path.endsWith(".") && !path.endsWith("\\.") && segments.last().isNotEmpty()) {
            segments.add("")
        }

        return segments
    }

    /**
     * Extracts valid array format access components from a given path component and returns the separated components.
     *
     * Given `"key1[0][1]"`, the result is `["key1", "[0]", "[1]"]`.
     * Array format access can be escaped using a backslash character preceding an array bracket. Valid bracket escape sequences are cleaned so
     * that the final path component does not have the escape character.
     * For example: `"key1\[0\]"` results in the single path component `"key1[0]"`.
     *
     * @param pathComponent The path component to be split into separate components given valid array formatted components.
     * @return A Pair containing the string component of the path, if any, and a list of string path components representing
     * the individual elements of the array accesses, if present.
     */
    fun getArrayPathComponents(pathComponent: String): Pair<String?, List<String>> {
        // Handle edge case where input is empty
        if (pathComponent.isEmpty()) return Pair("", listOf())

        var stringComponent = ""
        val arrayComponents = mutableListOf<String>()
        var bracketCount = 0
        var componentBuilder = StringBuilder()
        var lastArrayAccessEnd = pathComponent.length // to track the end of the last valid array-style access

        fun isNextCharBackslash(index: Int): Boolean {
            if (index == 0) {
                // There is no character before the startIndex.
                return false
            }
            // Since we're iterating in reverse, the "next" character is before i
            return pathComponent[index - 1] == '\\'
        }

        for (index in pathComponent.indices.reversed()) {
            when {
                pathComponent[index] == ']' && !isNextCharBackslash(index) -> {
                    bracketCount += 1
                    componentBuilder.append("]")
                }
                pathComponent[index] == '[' && !isNextCharBackslash(index) -> {
                    bracketCount -= 1
                    componentBuilder.append("[")
                    if (bracketCount == 0) {
                        arrayComponents.add(0, componentBuilder.toString().reversed())
                        componentBuilder.clear()
                        lastArrayAccessEnd = index
                    }
                }
                pathComponent[index] == '\\' -> {
                    componentBuilder.append('\\')
                }
                bracketCount == 0 && index < lastArrayAccessEnd -> {
                    stringComponent = pathComponent.substring(0, index + 1)
                    break
                }
                else -> componentBuilder.append(pathComponent[index])
            }
        }

        // Add any remaining component that's not yet added
        if (componentBuilder.isNotEmpty()) {
            stringComponent = componentBuilder.toString().reversed()
        }
        if (stringComponent.isNotEmpty()) {
            stringComponent = stringComponent
                .replace("\\[", "[")
                .replace("\\]", "]")
        }

        if (lastArrayAccessEnd == 0) {
            return Pair(null, arrayComponents)
        }
        return Pair(stringComponent, arrayComponents)
    }
}
