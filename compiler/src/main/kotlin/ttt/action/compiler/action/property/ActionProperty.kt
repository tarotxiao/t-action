package ttt.action.compiler.action.property

import ttt.action.Action

data class ActionProperty(
    val action: Action,
    val input: Set<Variable>,
    val output: Set<Variable>,
    val extendProperty: ExtendProperty
) {
}

data class ExtendProperty(val list: List<KeyValue<*>>) {

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: Key<T>): T? {
        return list.first { it.key == key }.value as T?
    }
}

data class Key<T>(val name: String) {
    fun with(value: T) = KeyValue(this, value)
}

data class KeyValue<T>(val key: Key<T>, val value: T)