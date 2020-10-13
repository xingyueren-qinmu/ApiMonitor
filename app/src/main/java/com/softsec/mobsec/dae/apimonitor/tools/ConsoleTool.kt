package com.softsec.mobsec.dae.apimonitor.tools

import org.apache.commons.cli.*
import kotlin.system.exitProcess


class ConsoleTool {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("test")
            val options = Options()
            options.addOption("v", "version", false, "show current version")
            options.addOption("h", "help", false, "show this message")

            val parser: CommandLineParser = DefaultParser()
            val formatter = HelpFormatter()
            val cmd: CommandLine

            cmd = try {
                parser.parse(options, args)
            } catch (e: ParseException) {
                System.err.println(e.message)
                formatter.printHelp("ConsoleTool", options)
                exitProcess(1)
            }

            if (cmd.hasOption("help")) {
                formatter.printHelp("ConsoleTool", options)
                return
            }
            if (cmd.hasOption("version")) {
                println("1.0")
                return
            }
        }
    }
}