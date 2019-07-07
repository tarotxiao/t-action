package ttt.action.runtime.interpreter.environment

import ttt.action.runtime.interpreter.property.Variable

interface Environment {
    operator fun get(variable: Variable): Any?

    operator fun set(variable: Variable, value: Any)
}