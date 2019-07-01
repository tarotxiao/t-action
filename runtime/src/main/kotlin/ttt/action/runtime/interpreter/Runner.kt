package ttt.action.runtime.interpreter

import ttt.action.runtime.interpreter.environment.Environment
import ttt.action.runtime.interpreter.environment.MainEnvironment

interface Runner {
    fun run(environment: Environment)

    fun runMain() {
        run(MainEnvironment)
    }
}