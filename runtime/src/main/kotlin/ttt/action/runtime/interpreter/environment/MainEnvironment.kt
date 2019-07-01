package ttt.action.runtime.interpreter.environment

import ttt.action.compiler.action.property.Variable
import ttt.action.compiler.action.property.VariableId

object MainEnvironment : Environment {

    val cache = mutableListOf<VariableContainer>()

    override fun get(variable: Variable): Any? {
        return cache.firstOrNull { it.allow(variable) }?.value
    }

    override fun set(variable: Variable, value: Any) {
        cache.removeIf { it.allow(variable) }
        cache.add(VariableContainer(variable.id, value))
    }


}

data class VariableContainer(val id: VariableId, val value: Any) {
    fun allow(variable: Variable) = variable.id == id
}

