package dev.ja.sync

import dev.ja.sync.model.SyncConfig
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.time.YearMonth
import java.time.format.DateTimeParseException
import kotlin.system.exitProcess

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val argsList = args.toMutableList()

        val readOnly = when {
            argsList.firstOrNull() == "--read-only" -> {
                argsList.removeFirst()
                true
            }

            else -> false
        }

        if (argsList.size != 2 && argsList.size != 3) {
            System.err.println("Usage: --read-only path/to/config.json year-month [year-month]")
            exitProcess(1)
        }

        val configFile = Path.of(argsList.removeFirst())

        val firstMonth: YearMonth
        val lastMonth: YearMonth
        try {
            firstMonth = YearMonth.parse(argsList[0])

            lastMonth = when {
                argsList.size == 2 -> YearMonth.parse(argsList[1])
                else -> firstMonth
            }
        } catch (e: DateTimeParseException) {
            System.err.println("Error parsing year-month: " + argsList[0] + ", error: " + e.message)
            exitProcess(1)
        }

        runBlocking {
            WiseBhbSync(createSyncConfig(readOnly, configFile)).sync(firstMonth, lastMonth)
        }
    }

    private fun createSyncConfig(readOnly: Boolean, filePath: Path): SyncConfig {
        return SyncConfig.loadFromYaml(filePath).copy(readOnly = readOnly)
    }
}