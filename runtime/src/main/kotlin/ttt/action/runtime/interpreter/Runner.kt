package ttt.action.runtime.interpreter

import ttt.action.Action
import ttt.action.runtime.interpreter.environment.Environment
import ttt.action.runtime.interpreter.environment.MainEnvironment
import ttt.action.runtime.interpreter.property.ActionProperty

interface Runner {
    fun run(environment: Environment)

    fun runMain() {
        run(MainEnvironment)
    }
}

fun Action.toRunner(): Runner = ActionProperty(this)
