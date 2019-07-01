package ttt.action.runtime.interpreter.environment

import ttt.action.compiler.action.property.Variable

interface Environment {
    operator fun get(variable: Variable): Any?

    operator fun set(variable: Variable, value: Any)
}