package yellow

class YellowClass(
    val name: String,
    val methods: Map<String, YellowFunction>,
    val superclass: YellowClass?,
) : YellowCallable {

  fun findMethod(name: String): YellowFunction? {
    if (methods.containsKey(name)) {
      return methods[name]
    }
    superclass?.let {
      return superclass.findMethod(name)
    }

    return null
  }

  override fun arity(): Int {
    val initialiser = findMethod("init")
    initialiser?.let {
      return it.arity()
    }
    return 0
  }

  override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
    val instance = YellowInstance(this)
    val initialiser = findMethod("init")
    initialiser?.let { initialiser.bind(instance).call(interpreter, args) }
    return instance
  }

  override fun toString(): String {
    return "<class ${name}>"
  }
}
