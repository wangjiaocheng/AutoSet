package top.autoget.autokit

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

object ShellKit {
    @JvmOverloads
    fun execCmd(
        command: String, isRooted: Boolean, isNeedResultMsg: Boolean = true
    ): CommandResult = execCmd(arrayOf(command), isRooted, isNeedResultMsg)

    @JvmOverloads
    fun execCmd(
        commands: MutableList<String>?, isRooted: Boolean, isNeedResultMsg: Boolean = true
    ): CommandResult = execCmd(commands?.toTypedArray(), isRooted, isNeedResultMsg)

    data class CommandResult(
        var result: Int = -1, var successMsg: String = "", var errorMsg: String = ""
    )

    @JvmOverloads
    fun execCmd(
        commands: Array<String>?, isRooted: Boolean, isNeedResultMsg: Boolean = true
    ): CommandResult = when {
        commands == null || commands.isEmpty() -> CommandResult()
        else -> Runtime.getRuntime().exec(if (isRooted) "su" else "sh").let { process ->
            try {
                process.outputStream.use {
                    DataOutputStream(it).use { dataOutputStream ->
                        dataOutputStream.run {
                            for (command in commands) {
                                write(command.toByteArray())//writeBytes中文字符集错误
                                writeBytes("\n")
                                flush()
                            }
                            writeBytes("exit\n")
                            flush()
                        }
                    }
                }
                val result = process.waitFor()
                val successMsg = StringBuilder()
                val errorMsg = StringBuilder()
                if (isNeedResultMsg) {
                    process.inputStream.use {
                        InputStreamReader(it, "UTF-8").use { inputStreamReader ->
                            BufferedReader(inputStreamReader).use { successResult ->
                                successResult.run {
                                    readLine()?.let { line -> successMsg.append(line) }
                                    while (true) {
                                        readLine()?.let { line -> successMsg.append("\n$line") }
                                            ?: break
                                    }
                                }
                            }
                        }
                    }
                    process.errorStream.use {
                        InputStreamReader(it, "UTF-8").use { inputStreamReader ->
                            BufferedReader(inputStreamReader).use { errorResult ->
                                errorResult.run {
                                    readLine()?.let { line -> errorMsg.append(line) }
                                    while (true) {
                                        readLine()?.let { line -> errorMsg.append("\n$line") }
                                            ?: break
                                    }
                                }
                            }
                        }
                    }
                }
                CommandResult(result, successMsg.toString(), errorMsg.toString())
            } catch (e: IOException) {
                e.printStackTrace()
                CommandResult()
            } catch (e: Exception) {
                e.printStackTrace()
                CommandResult()
            } finally {
                process.destroy()
            }
        }
    }
}