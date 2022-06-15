package yellow

// TODO: Extend Native functions to register native classes

class NativeFunctionError(message: String) : RuntimeException(message) {}

val __print__ =
    object : YellowCallable {
      override fun arity(): Int {
        return 1
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        print(args[0])
        return null
      }
    }

val __println__ =
    object : YellowCallable {
      override fun arity(): Int {
        return 1
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        println(args[0])
        return null
      }
    }

val __input__ =
    object : YellowCallable {
      override fun arity(): Int {
        return 1
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        print(args[0])
        return readLine()
      }
    }

val __list__ =
    object : YellowCallable {

      override fun arity(): Int {
        return 0
      }

      override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        return mutableListOf<Any?>()
      }
    }

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
