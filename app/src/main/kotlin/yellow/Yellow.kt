package yellow

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

object Yellow {

  // ================== Data =======================
  private var err = false
  private var runtimeErr = false

  private const val PROMPT = "yellow|>>> "

  // ================= Public Methods ======================

  public fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    val source = String(bytes, Charset.defaultCharset())
    run(source)
    if (err) System.exit(65)
  }

  public fun runPrompt() {
    while (true) {
      print(PROMPT)
      val line = readLine()
      when (line) {
        null -> break
        else -> run(line)
      }
      err = false
    }
  }

  private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scan()
    for (token in tokens) {
      println(token)
    }
  }

  public fun error(line: Int, message: String) {
    reportError(line, "", message)
    err = true
  }

  public fun error(tkn: Token, msg: String) {
    if (tkn.type == TokenType.EOF) reportError(tkn.line, " at end", msg)
    else reportError(tkn.line, " at '" + tkn.lexeme + "'", msg)
    err = true
  }

  // =============== Private Methods =====================

  private fun reportError(line: Int, pos: String, message: String) {
    println("ERROR $pos: $message - [line:$line]")
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
