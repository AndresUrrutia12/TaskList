package tasklist
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.Scanner
import kotlinx.datetime.*
import java.io.File
import kotlin.io.path.Path


data class Task(var priority: String,
                var date: String,
                var hour: String,
                var tag: Char,
                var description: String)


fun main() {
    val filePath = Path("tasklist.json")

    val tasks: MutableMap<Int, Task> = if (filePath.toFile().exists()) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        // 1. Read the file as a List (JSON Array)
        val listType = Types.newParameterizedType(List::class.java, Task::class.java)
        val taskAdapter: JsonAdapter<List<Task>> = moshi.adapter(listType)

        val jsonString = filePath.toFile().readText()
        val taskList = taskAdapter.fromJson(jsonString) ?: emptyList()

        // 2. Convert the List back into a MutableMap with 1-based indexing
        taskList.mapIndexed { index, task -> (index + 1) to task }.toMap().toMutableMap()
    } else {
        mutableMapOf()
    }
    do {
        println("Input an action (add, print, edit, delete, end):")
        val option = readln().trim()
        when (option) {
            "add" -> addTask(tasks)
            "print" -> printTaskList(tasks)
            "edit" -> editTask(tasks)
            "delete" -> deleteTask(tasks)
            "end" -> endEntering()
            else -> println("The input action is invalid")
        }
    } while (option != "end")
}


fun deleteTask(tasks: MutableMap<Int, Task>) {
    var taskToDelete: Int
    var validTaskNumber: Boolean
    if (tasks.isEmpty()){
        println("No tasks have been input")
    } else {
        printTaskList(tasks)
         do {
            try {
                println("Input the task number (1-${tasks.size}):")
                taskToDelete = readln().toInt()
                validTaskNumber = if (taskToDelete !in 1..tasks.size){
                    println("Invalid task number")
                    false
                } else {
                    tasks.remove(taskToDelete)
                    renumberTasks(tasks)
                    writeTaskList(tasks)
                    println("The task is deleted")
                    true
                }
            } catch(e: NumberFormatException) {
                validTaskNumber = false
                println("Invalid task number")
            }
        } while (!validTaskNumber)

    }
}

fun editTask(tasks: MutableMap<Int, Task>) {
    if (tasks.isEmpty()) {
        println("No tasks have been input")
        return
    }

    printTaskList(tasks)

    while (true) {
        println("Input the task number (1-${tasks.size}):")
        val taskNumber = readln().trim().toIntOrNull()

        if (taskNumber == null || taskNumber !in 1..tasks.size) {
            println("Invalid task number")
            continue
        }

        val task = tasks[taskNumber] ?: continue

        val validFields = listOf("priority", "date", "time", "task")
        var fieldToEdit: String

        do {
            println("Input a field to edit (priority, date, time, task):")
            fieldToEdit = readln().trim()

            if (fieldToEdit !in validFields) {
                println("Invalid field")
            }
        } while (fieldToEdit !in validFields)

        var changed = true

        when (fieldToEdit) {
            "priority" -> {
                val validPriority = listOf("C", "H", "N", "L")
                var newPriority: String

                do {
                    println("Input the task priority (C, H, N, L):")
                    newPriority = readln().trim().uppercase()
                    // Uncomment this if your project requires an invalid priority message
                    // if (newPriority !in validPriority) println("Invalid priority")

                } while (newPriority !in validPriority)

                task.priority = newPriority
                writeTaskList(tasks)
            }

            "date" -> {
                while (true) {
                    println("Input the date (yyyy-mm-dd):")
                    val newDate = readln().trim()

                    try {
                        val parsedDate = parseDate(newDate)
                        task.date = parsedDate.toString()
                        task.tag = calculateTag(task.date)
                        writeTaskList(tasks)
                        break
                    } catch (e: Exception) {
                        println("The input date is invalid")
                    }
                }
            }

            "time" -> {
                while (true) {
                    println("Input the time (hh:mm):")
                    val newTime = readln().trim()

                    try {
                        task.hour = parseTime(newTime).toString()
                        writeTaskList(tasks)
                        break
                    } catch (e: Exception) {
                        println("The input time is invalid")
                    }
                }
            }

            "task" -> {
                println("Input a new task (enter a blank line to end):")

                val lines = mutableListOf<String>()

                while (true) {
                    val line = readln().trim()
                    if (line.isBlank()) {
                        break
                    }

                    lines.add(line)
                }

                if (lines.isEmpty()) {
                    println("The task is blank")
                    changed = false
                } else {
                    task.description = lines.joinToString(separator = "\n")
                    writeTaskList(tasks)
                }
            }
        }

        if (changed) {
            println("The task is changed")
        }
        break
    }
}

fun addTask(tasks: MutableMap<Int, Task>) {
    val priority = addPriority()
    val date = addDateTime()
    val hour = addHour()
    val tag = calculateTag(date)

    println("Input a new task (enter a blank line to end):")

    val scanner = Scanner(System.`in`)
    val listOfTask: MutableList<String> = mutableListOf()

   while (true){
       val line = scanner.nextLine().trim()
       if (line.isBlank()) {
           break
       }
       listOfTask.add(line)
   }

    if (listOfTask.isEmpty()) {
        println("The task is blank")
        return
    }

    val nextId = tasks.size + 1

    tasks[nextId] = Task(
        priority = priority,
        date = date,
        hour = hour,
        tag = tag,
        description = listOfTask.joinToString(separator = "\n")
    )

    writeTaskList(tasks)
}
fun calculateTag(date: String): Char {
    val taskDate = parseDate(date)
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date

    return when {
        taskDate == currentDate -> 'T'
        taskDate > currentDate -> 'I'
        else -> 'O'
    }
}

fun addPriority(): String{
    val validPriority = listOf("C", "H", "N", "L")
    var priority: String
    do {
        println("Input the task priority (C, H, N, L):")
        priority = readln().trim().uppercase()
    } while (priority !in validPriority)
    return priority
}

fun addDateTime(): String {
    while (true) {
        println("Input the date (yyyy-mm-dd):")
        val input = readln().trim()

        try {
            return parseDate(input).toString()
        } catch (e: Exception) {
            println("The input date is invalid")
        }
    }
}

fun addHour(): String {
    while (true) {
        println("Input the time (hh:mm):")
        val input = readln().trim()

        try {
            return parseTime(input).toString()
        } catch (e: Exception) {
            println("The input time is invalid")
        }
    }
}

fun printTaskList(tasks: MutableMap<Int, Task>) {
    if (tasks.isEmpty()){
        println("No tasks have been input")
        return
    }
        val topBorder = "+----+------------+-------+---+---+--------------------------------------------+"
        val header = "| N  |    Date    | Time  | P | D |                    Task                    |"
        val separator = "+----+------------+-------+---+---+--------------------------------------------+"
        val bottomBorder = "+----+------------+-------+---+---+--------------------------------------------+"

        println(topBorder)
        println(header)
        println(separator)
        for ((key, value) in tasks) {
            val taskNumber = key.toString().padEnd(3)

            val pColorCode = getPriorityColorCode(value.priority)
            val dColorCode = getDueColorCode(value.tag)

            val coloredP = "\u001B[${pColorCode}m \u001B[0m"
            val coloredD = "\u001B[${dColorCode}m \u001B[0m"

            // 1. Split the description into separate lines first
            val lines = value.description.split("\n")
            var isFirstLine = true

            // 2. Process each line independently
            for (line in lines) {
                // Chunk this specific line into 44-character pieces and pad them
                val chunks = line.chunked(44).map { it.padEnd(44, ' ') }

                for (chunk in chunks) {
                    if (isFirstLine) {
                        // First chunk of the first line gets all the metadata
                        println("| $taskNumber| ${value.date} | ${value.hour} | $coloredP | $coloredD |$chunk|")
                        isFirstLine = false
                    } else {
                        // All subsequent chunks/lines get empty columns
                        println("|    |            |       |   |   |$chunk|")
                    }
                }
            }
            // Print the bottom border after the task is fully printed
            println(bottomBorder)
        }

}


fun getPriorityColorCode(priority: String): String {
    return when (priority) {
        "C" -> "101"
        "H" -> "103"
        "N" -> "102"
        "L" -> "104"
        else -> "0"
    }
}
fun getDueColorCode(tag: Char): String {
    return when (tag) {
        'T' -> "103"
        'I' -> "102"
        'O' -> "101"
        else -> "0"
    }
}

fun renumberTasks(tasks: MutableMap<Int, Task>) {
    val orderedTasks = tasks.toSortedMap().values.toList()

    tasks.clear()

    orderedTasks.forEachIndexed { index, task ->
        tasks[index + 1] = task
    }
}
fun parseDate(input: String): LocalDate {
    val parts = input.trim().split("-")

    if (parts.size != 3) {
        throw IllegalArgumentException("Invalid date")
    }

    val year = parts[0].toInt()
    val month = parts[1].toInt()
    val day = parts[2].toInt()

    return LocalDate(year, month, day)
}
fun parseTime(input: String): LocalTime {
    val parts = input.trim().split(":")

    if (parts.size != 2) {
        throw IllegalArgumentException("Invalid time")
    }

    val hour = parts[0].toInt()
    val minute = parts[1].toInt()

    return LocalTime(hour, minute)
}

fun writeTaskList(tasks: MutableMap<Int, Task>) {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    //Convert the map values to a sorted List to ensure correct order and JSON array format
    val taskList = tasks.toSortedMap().values.toList()

    // Tell Moshi to expect a List of Tasks, which creates a JSON array [...]
    val listType = Types.newParameterizedType(List::class.java, Task::class.java)
    val taskAdapter = moshi.adapter<List<Task>>(listType)

    // Use relative path for the testing server
    val file = File("tasklist.json")
    file.writeText(taskAdapter.toJson(taskList))
}
fun endEntering(){
    println("Tasklist exiting!")
}


