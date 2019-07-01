package ttt.action.compiler

import ttt.action.Action
import ttt.action.compiler.action.property.ActionProperty
import ttt.action.compiler.compiler.CompoundActionGenerator
import ttt.action.compiler.compiler.JvmActionGenerator
import ttt.action.compound.CompoundAction
import ttt.action.jvm.JvmAction

object CompilerMain {

    fun compile(action: Action): ActionProperty = when (action) {
        is JvmAction -> JvmActionGenerator.generate(action)
        is CompoundAction -> CompoundActionGenerator.generate(action)
        else -> throw AssertionError("不支持的Action类型")
    }
}