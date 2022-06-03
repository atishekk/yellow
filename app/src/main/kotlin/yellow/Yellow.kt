package yellow

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

object Yellow {

  const val PROMPT = "yellow|>>> "

  public fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    val source = String(bytes, Charset.defaultCharset())
    run(source)
  }

  public fun runPrompt() {
    while (true) {
      print(PROMPT)
      val line = readLine()
      when (line) {
        null -> break
        else -> run(line)
      }
    }
  }

  private fun run(source: String) {
    println("Running the source: " + source)
  }
}

fun main(args: Array<String>) {
  if (args.size > 1) {
    println("Usage: yellow [script]")
    System.exit(64)
  } else if (args.size == 1) {
    Yellow.runFile(args[0])
  } else {
    Yellow.runPrompt()
  }
}
