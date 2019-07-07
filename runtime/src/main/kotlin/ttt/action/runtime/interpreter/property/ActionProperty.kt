package ttt.action.runtime.interpreter.property

import ttt.action.Action
import ttt.action.compound.CompoundAction
import ttt.action.jvm.JvmAction
import ttt.action.jvm.Main
import ttt.action.jvm.Named
import ttt.action.runtime.interpreter.Runner
import ttt.action.runtime.interpreter.environment.Environment
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

sealed class ActionProperty : Runner {
    abstract val action: Action
    abstract val input: Set<Variable>
    abstract val output: Set<Variable>

    companion object : (Action) -> ActionProperty {
        override fun invoke(action: Action): ActionProperty {
            return when (action) {
                is JvmAction -> JvmActionProperty(action)
                is CompoundAction -> CompoundActionProperty(action)
                else -> throw AssertionError()
            }
        }
    }
}

data class JvmActionProperty(
    override val action: Action,
    override val input: Set<Variable>,
    override val output: Set<Variable>,
    val mainMethod: KFunction<*>,
    val parameterMap: Map<Variable, KParameter>
) : ActionProperty() {

    override fun run(environment: Environment) {
        val parameterMap: Map<KParameter, Any?> = parameterMap
            .mapNotNull { entry ->
                environment[entry.key]?.let { entry.value to it }
            }
            .plus(mainMethod.parameters[0] to action)
            .toMap()
        val result = mainMethod.callBy(parameterMap)

        if (result != null && output.isNotEmpty()) {
            environment[output.first()] = result
        }
    }

    companion object : (JvmAction) -> JvmActionProperty {
        override fun invoke(action: JvmAction): JvmActionProperty {
            val mainMethod =
                findMainMethod(action.javaClass.kotlin)

            val parameters = mainMethod.parameters

            val parameterMap: Map<Variable, KParameter> = parameters
                .drop(1)
                .map {
                    Variable(
                        getName(
                            it
                        ), Type.jvm(it.type), !it.isOptional
                    ) to it
                }
                .toMap()


            val result = if (mainMethod.returnType == Unit::class) {
                setOf()
            } else {
                setOf(
                    Variable(
                        getName(
                            mainMethod
                        ), Type.jvm(mainMethod.returnType), true
                    )
                )
            }

            return JvmActionProperty(action, parameterMap.keys, result, mainMethod, parameterMap)
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
}

data class CompoundActionProperty(
    override val action: Action,
    override val input: Set<Variable>,
    override val output: Set<Variable>,
    val actionPropertyList: List<ActionProperty>
) : ActionProperty() {
    override fun run(environment: Environment) {
        actionPropertyList.forEach { it.run(environment) }
    }

    companion object : (CompoundAction) -> CompoundActionProperty {
        override fun invoke(action: CompoundAction): CompoundActionProperty {

            val properties = action.actions
                .map { ActionProperty(it) }

            val allInput = properties
                .flatMap { actionProperty -> actionProperty.input.map { it to actionProperty } }
                .groupBy { it.first.id }
                .mapValues { (key, value) ->
                    InputInfo(Variable(key, value.any { it.first.required }), value.map { it.second })
                }

            val allOutput = properties
                .flatMap { actionProperty -> actionProperty.output.map { it to actionProperty } }
                .groupBy { it.first.id }
                .mapValues { (_, value) ->
                    if (value.size > 1) {
                        throw AssertionError("存在两个Action输出了相同的变量，${value.map { it.second }}")
                    }
                    value[0]
                }

            val internalInputAndOutput = allInput.entries
                .mapNotNull { (inputId, inputInfo) ->
                    allOutput[inputId]?.let { (outputVariable, outputActionProperty) ->
                        if (!outputVariable.required && inputInfo.variable.required) {
                            throw AssertionError("Action的输入输出属性无法匹配，输入是必选的，而输出是可选的,$inputInfo,$outputVariable")
                        }
                        inputId to Triple(inputInfo, outputVariable, outputActionProperty)
                    }
                }
                .toMap()

            val actualInput = allInput
                .filterNot { it.key in internalInputAndOutput.keys }
                .values
                .map { it.variable to it.action }
                .toMap()

            val actualOutput = allOutput
                .filterNot { it.key in internalInputAndOutput.keys }
                .values
                .map { it.first to it.second }
                .toMap()

            val inputAndOutput = internalInputAndOutput.values
                .flatMap { triple -> triple.first.action.map { it to triple.third } }
                .toMutableList()

            val moveMarker = BooleanArray(properties.size) { false }
            val orderedProperties = mutableListOf<ActionProperty>()
            while (orderedProperties.size != properties.size) {
                var counter = 0
                properties.withIndex().filterNot { moveMarker[it.index] }
                    .forEach { (index, property) ->
                        if (inputAndOutput.none { it.first == property }) {
                            // 不依赖其他Action执行
                            counter += 1
                            moveMarker[index] = true
                            orderedProperties.add(property)
                            // Action已经移入队列，移除所有依赖该Action的记录
                            inputAndOutput.removeAll { it.second == property }
                        }
                    }
                if (counter == 0) {
                    // 本次循环没有处理任何Action，说明出现了循环引用
                    throw AssertionError("循环引用," +
                            "${properties.withIndex().filterNot { moveMarker[it.index] }.map { it.value }}"
                    )
                }
            }


            return CompoundActionProperty(
                action,
                actualInput.keys,
                actualOutput.keys,
                orderedProperties
            )
        }

        data class InputInfo(val variable: Variable, val action: List<ActionProperty>)

    }
}
