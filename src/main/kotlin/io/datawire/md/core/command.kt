package io.datawire.md.core

import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


data class Result(val exitCode: Int, val data: String)


fun execute(command: List<String>, cwd: String, env: Map<String, String>, print: Boolean = true): Result {
    val pb = ProcessBuilder().apply {
        command(command)
        directory(File(cwd))
        environment().putAll(env)
        redirectErrorStream(true)
    }

    val proc = pb.start()

    val lines = mutableListOf<String>()
    proc.inputStream.bufferedReader().use {
        it.readLine()?.let {
            lines += it
            if (print) {
                println(it)
            }
        }
    }

    return if (proc.waitFor(10L, TimeUnit.MINUTES)) {
        Result(proc.exitValue(), lines.joinToString("\n"))
    } else {
        throw TimeoutException("Timed out while running command: `${pb.command()}`")
    }
}
