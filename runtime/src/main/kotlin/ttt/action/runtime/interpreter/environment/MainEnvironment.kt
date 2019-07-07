package ttt.action.runtime.interpreter.environment

import ttt.action.runtime.interpreter.property.Variable
import ttt.action.runtime.interpreter.property.VariableId

object MainEnvironment : Environment {

    val cache = mutableMapOf<VariableId, Any>()

    override fun get(variable: Variable): Any? {
        return cache[variable.id]
    }

    override fun set(variable: Variable, value: Any) {
        cache[variable.id] = value
    }
}