package ttt.action.runtime.interpreter

import ttt.action.Action
import ttt.action.compiler.CompilerMain
import ttt.action.compiler.action.property.ActionProperty
import ttt.action.compiler.compiler.CompoundActionGenerator
import ttt.action.compiler.compiler.JvmActionGenerator
import ttt.action.compound.CompoundAction
import ttt.action.jvm.JvmAction
import ttt.action.runtime.interpreter.environment.Environment
import kotlin.reflect.KParameter


fun Action.toRunner(): Runner = CompilerMain.compile(this).toRunner()

fun ActionProperty.toRunner() = when (this.action) {
    is JvmAction -> object : Runner {
        override fun run(environment: Environment) {
            val mainMethod = this@toRunner.extendProperty[JvmActionGenerator.METHOD]!!

            val parameterMap: Map<KParameter, Any?> = this@toRunner.extendProperty[JvmActionGenerator.PARAMETER_MAP]!!
                .mapNotNull { entry ->
                    environment[entry.key]?.let { entry.value to it }
                }
                .plus(mainMethod.parameters[0] to this@toRunner.action)
                .toMap()
            val result = mainMethod.callBy(parameterMap)

            if (result != null && this@toRunner.output.isNotEmpty()) {
                environment[this@toRunner.output.first()] = result
            }

        }
    }
    is CompoundAction -> object : Runner {
        override fun run(environment: Environment) {
            this@toRunner.extendProperty[CompoundActionGenerator.SORTED_ACTIONS]!!
                .forEach { it.toRunner().run(environment) }
        }
    }
    else -> throw AssertionError()
}