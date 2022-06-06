package yellow

class YellowClass(
    val name: String,
    val methods: Map<String, YellowFunction>,
) : YellowCallable {
  override fun arity(): Int {
    TODO()
  }

  override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
    TODO()
  }

  override fun toString(): String {
    return "<class ${name}>"
  }
}
