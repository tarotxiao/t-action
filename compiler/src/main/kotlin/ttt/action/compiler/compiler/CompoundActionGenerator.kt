package ttt.action.compiler.compiler

import ttt.action.Action
import ttt.action.compiler.CompilerMain
import ttt.action.compiler.action.property.ActionProperty
import ttt.action.compiler.action.property.ExtendProperty
import ttt.action.compiler.action.property.Key
import ttt.action.compiler.action.property.Variable
import ttt.action.compound.CompoundAction

object CompoundActionGenerator {

    val SORTED_ACTIONS = Key<List<Action>>("COMPOUND_ACTION_SORTED_ACTIONS")

    fun generate(action: CompoundAction): ActionProperty {

        val input = mutableSetOf<Variable>()
        val output = mutableSetOf<Variable>()

        action.actions
            .map { CompilerMain.compile(it) }
            .forEach {
                input.addAll(it.input)
                output.addAll(it.output)
            }

        return ActionProperty(
            action, input.toSet(), output.toSet(), ExtendProperty(
                listOf(
                    SORTED_ACTIONS.with(action.actions)
                )
            )
        )
    }

}