package yellow

class YellowInstance(
    private val cls: YellowClass,
) {

  private val fields = mutableMapOf<String, Any?>()

  fun get(name: Token): Any? {
    if (fields.containsKey(name.lexeme)) {
      return fields[name.lexeme]
    }
    val method = cls.findMethod(name.lexeme)
    method?.let {
      return method.bind(this)
    }

    throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
  }

  fun set(name: Token, value: Any?) {
    fields[name.lexeme] = value
  }

  override fun toString(): String {
    return "<instance <class ${cls.name}>>"
  }
}
