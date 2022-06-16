package yellow

// TODO: Extend Native functions to register native classes

class NativeFunctionError(message: String) : RuntimeException(message) {}

// System Impl
val __print__ =
    object : YellowCallable {
      override fun arity(): Int {
        return 1
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        print(interpreter.stringify(args[0]))
        return null
      }
    }

val __println__ =
    object : YellowCallable {
      override fun arity(): Int {
        return 1
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        println(interpreter.stringify(args[0]))
        return null
      }
    }

val __input__ =
    object : YellowCallable {
      override fun arity(): Int {
        return 1
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        print(interpreter.stringify(args[0]))
        return readLine()
      }
    }

// List Impl
val __list__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 0
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        return mutableListOf<Any?>()
      }
    }
@Suppress("UNCHECKED_CAST")
val __list__append__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 2
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        try {
          return (args[0] as MutableList<Any?>).add(args[1])
        } catch (err: java.lang.ClassCastException) {
          throw NativeFunctionError("Invalid list append operation: Not a list object")
        }
      }
    }

@Suppress("UNCHECKED_CAST")
val __list__get__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 2
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        try {
          return (args[0] as MutableList<Any?>).get((args[1] as Double).toInt())
        } catch (err: Exception) {
          when (err) {
            is java.lang.ClassCastException ->
                throw NativeFunctionError("Invalid list get operation: Not a list object")
            is java.lang.IndexOutOfBoundsException ->
                throw NativeFunctionError("Invalid list get operation: index out of bounds")
            else -> throw NativeFunctionError("Invalid list get operation")
          }
        }
      }
    }

@Suppress("UNCHECKED_CAST")
val __list__set__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 3
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        try {
          return (args[0] as MutableList<Any?>).set((args[1] as Double).toInt(), args[2])
        } catch (err: Exception) {
          when (err) {
            is java.lang.ClassCastException ->
                throw NativeFunctionError("Invalid list set operation: Not a list object")
            is java.lang.IndexOutOfBoundsException ->
                throw NativeFunctionError("Invalid list set operation: index out of bounds")
            else -> throw NativeFunctionError("Invalid list set operation")
          }
        }
      }
    }

@Suppress("UNCHECKED_CAST")
val __list__delete__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 2
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        try {
          return (args[0] as MutableList<Any?>).removeAt((args[1] as Double).toInt())
        } catch (err: Exception) {
          when (err) {
            is java.lang.ClassCastException ->
                throw NativeFunctionError("Invalid list delete operation: Not a list object")
            is java.lang.IndexOutOfBoundsException ->
                throw NativeFunctionError("Invalid list delete operation: index out of bounds")
            else -> throw NativeFunctionError("Invalid list delete operation")
          }
        }
      }
    }

@Suppress("UNCHECKED_CAST")
val __list__len__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 1
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        try {
          return (args[0] as MutableList<Any?>).size.toDouble()
        } catch (err: Exception) {
          when (err) {
            is java.lang.ClassCastException ->
                throw NativeFunctionError("Invalid list len operation: Not a list object")
            else -> throw NativeFunctionError("Invalid list len operation")
          }
        }
      }
    }

// Map Impl
@Suppress("UNCHECKED_CAST")
val __map__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 0
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        return mutableMapOf<String, Any?>()
      }
    }

@Suppress("UNCHECKED_CAST")
val __map__set__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 3
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        try {
          if (args[1] !is String) {
            throw NativeFunctionError("Invalid map set operation: Key not a string")
          }
          return (args[0] as MutableMap<String, Any?>).set(args[1] as String, args[2])
        } catch (err: java.lang.ClassCastException) {
          throw NativeFunctionError("Invalid map operation: Not a map object")
        }
      }
    }

@Suppress("UNCHECKED_CAST")
val __map__get__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 2
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        try {
          if (args[1] !is String) {
            throw NativeFunctionError("Invalid map get operation: Key not a string")
          }
          return (args[0] as MutableMap<String, Any?>).get((args[1] as String))
        } catch (err: Exception) {
          when (err) {
            is java.lang.ClassCastException ->
                throw NativeFunctionError("Invalid map get operation: Not a map object")
            else -> throw NativeFunctionError("Invalid map get operation")
          }
        }
      }
    }

@Suppress("UNCHECKED_CAST")
val __map__len__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 1
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        try {
          return (args[0] as MutableMap<String, Any?>).size.toDouble()
        } catch (err: Exception) {
          when (err) {
            is java.lang.ClassCastException ->
                throw NativeFunctionError("Invalid map len operation: Not a map object")
            else -> throw NativeFunctionError("Invalid map len operation")
          }
        }
      }
    }

@Suppress("UNCHECKED_CAST")
val __map__delete__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 2
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        try {

          if (args[1] !is String) {
            throw NativeFunctionError("Invalid map delete operation: Key not a string")
          }
          return (args[0] as MutableMap<String, Any?>).remove(args[1] as String)
        } catch (err: Exception) {
          when (err) {
            is java.lang.ClassCastException ->
                throw NativeFunctionError("Invalid map delete operation: Not a map object")
            else -> throw NativeFunctionError("Invalid map delete operation")
          }
        }
      }
    }
