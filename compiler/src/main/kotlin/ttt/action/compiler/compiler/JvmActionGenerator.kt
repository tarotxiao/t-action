package ttt.action.compiler.compiler

import ttt.action.compiler.action.property.*
import ttt.action.jvm.JvmAction
import ttt.action.jvm.Main
import ttt.action.jvm.Named
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

object JvmActionGenerator {

    val METHOD = Key<KFunction<*>>("JVM_ACTION_METHOD")
    val PARAMETER_MAP = Key<Map<Variable, KParameter>>("JVM_ACTION_PARAMETER_MAP")

    fun generate(action: JvmAction): ActionProperty {
        val mainMethod = findMainMethod(action.javaClass.kotlin)

        val parameters = mainMethod.parameters

        val parameterMap: Map<Variable, KParameter> = parameters
            .drop(1)
            .map {
                Variable(getName(it), Type.jvm(it.type), !it.isOptional) to it
            }
            .toMap()


        val result = if (mainMethod.returnType == Unit::class) {
            setOf()
        } else {
            setOf(Variable(getName(mainMethod), Type.jvm(mainMethod.returnType), true))
        }

        return ActionProperty(
            action, parameterMap.keys, result, ExtendProperty(
                listOf(
                    METHOD.with(mainMethod),
                    PARAMETER_MAP.with(parameterMap)
                )
            )
        )
    }

    private fun getName(method: KFunction<*>): String {
        val annotation = method.findAnnotation<Named>() ?: (return "")
        return when (annotation.name) {
            "" -> method.name
            else -> annotation.name
        }
    }

    private fun getName(parameter: KParameter): String {
        val annotation = parameter.findAnnotation<Named>() ?: (return "")
        return when (annotation.name) {
            "" -> parameter.name ?: ""
            else -> annotation.name
        }
    }


    private fun findMainMethod(clazz: KClass<*>): KFunction<*> {
        val functions = clazz.memberFunctions.filterNot {
            setOf("equals", "hashCode", "toString").contains(it.name)
        }
        if (functions.size == 1) {
            return functions.first()
        }
        val mainFunctions = functions.mapNotNull { kFunction ->
            kFunction.findAnnotation<Main>()?.let { kFunction to it }
        }
        when (mainFunctions.size) {
            1 -> return mainFunctions[0].first
            else -> throw IllegalArgumentException("对象不符合要求")
        }
    }
}