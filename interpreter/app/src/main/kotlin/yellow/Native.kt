package yellow

// TODO: Extend Native functions to register native classes

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
