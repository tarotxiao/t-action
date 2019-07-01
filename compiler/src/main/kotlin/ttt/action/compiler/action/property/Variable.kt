package ttt.action.compiler.action.property


data class Variable(val id: VariableId, val required: Boolean) {

    constructor(name: String, type: Type?, required: Boolean) : this(VariableId(name, type), required)
}

