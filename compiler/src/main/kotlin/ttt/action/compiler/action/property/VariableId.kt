package ttt.action.compiler.action.property


data class VariableId(val name: String, val type: Type?) {
    init {
        if (name == "" && type == null) {
            throw AssertionError("对于变量而言，类型和名称至少有一个不为空")
        }
    }
}