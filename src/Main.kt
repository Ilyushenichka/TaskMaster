
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.random.Random


data class Task(
    val id: Int,
    var title: String,
    var description: String,
    var priority: String,
    var dueDate: String,
    var isCompleted: Boolean,
    var category: String,
    val createdAt: String
)

val priorityOptions: List<String> = listOf("Низкий 🔵", "Средний 🟡", "Высокий 🟠", "Срочный 🔴")
val defaultCategories: List<String> = listOf("Работа", "Личное", "Учеба", "Здоровье", "Финансы")

val categories: MutableList<String> = defaultCategories.toMutableList()
val tasks: MutableList<Task> = mutableListOf()
var idCounter: Int = 1000
val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

const val COLOR_RESET = "\u001B[0m"
const val COLOR_RED = "\u001B[31m"
const val COLOR_GREEN = "\u001B[32m"
const val COLOR_YELLOW = "\u001B[33m"
const val COLOR_BLUE = "\u001B[34m"
const val COLOR_CYAN = "\u001B[36m"

fun prompt(text: String): String {
    print(text)
    return readLine()?.trim() ?: ""
}

fun pressEnterToContinue() {
    println("\nНажмите Enter для продолжения...")
    readLine()
}

fun parseDateOrNull(input: String): LocalDate? = try {
    LocalDate.parse(input, dateFormatter)
} catch (e: DateTimeParseException) {
    null
}

fun today(): String = LocalDate.now().format(dateFormatter)

fun generateId(): Int {
    idCounter += 1
    return idCounter
}

fun readIntInRange(promptText: String, range: IntRange, allowEmpty: Boolean = false): Int? {
    while (true) {
        val raw = prompt(promptText)
        if (allowEmpty && raw.isEmpty()) return null
        val number = raw.toIntOrNull()
        if (number != null && number in range) return number
        println("${COLOR_RED}Ошибка: введите число в диапазоне ${range.first}-${range.last}${COLOR_RESET}")
    }
}

fun chooseFromList(title: String, options: List<String>, allowEmpty: Boolean = false): String? {
    println(title)
    options.forEachIndexed { index, value -> println("${index + 1}. $value") }
    val selection = readIntInRange("Ваш выбор (1-${options.size})${if (allowEmpty) " или Enter для пропуска" else ""}: ", 1..options.size, allowEmpty)
    return if (selection == null) null else options[selection - 1]
}

fun colorForPriority(priority: String): String = when {
    priority.contains("🔴") -> COLOR_RED
    priority.contains("🟠") -> COLOR_YELLOW
    priority.contains("🟡") -> COLOR_YELLOW
    priority.contains("🔵") -> COLOR_BLUE
    else -> COLOR_RESET
}

fun statusEmoji(done: Boolean): String = if (done) "✅" else "⏳"

fun overdue(task: Task): Boolean {
    val due = parseDateOrNull(task.dueDate) ?: return false
    val now = LocalDate.now()
    return !task.isCompleted && due.isBefore(now)
}

fun printTask(task: Task, index: Int? = null) {
    val idx = index?.let { "$it. " } ?: ""
    val priColor = colorForPriority(task.priority)
    val overdueMark = if (overdue(task)) " ${COLOR_RED}Просрочена${COLOR_RESET}" else ""
    println("$idx=== ${COLOR_CYAN}ЗАДАЧА${COLOR_RESET} ===")
    println("${statusEmoji(task.isCompleted)} ${if (overdue(task)) "🔴" else "🟠"} [${task.id}] ${task.title}")
    if (task.description.isNotBlank()) println("📄 ${task.description}")
    println("📁 Категория: ${task.category}")
    println("📅 Создана: ${task.createdAt} | Выполнить до: ${task.dueDate}$overdueMark")
    println("🎯 Приоритет: ${priColor}${task.priority}${COLOR_RESET}")
    println("--------------------------------------------")
}

fun printHeader(title: String) {
    println("\n=================================================")
    println("$title")
    println("=================================================")
}

fun createTaskInteractive() {
    printHeader("=== ДОБАВЛЕНИЕ НОВОЙ ЗАДАЧИ ===")
    val title = generateSequence {
        val t = prompt("Введите название задачи: ")
        if (t.isBlank()) {
            println("${COLOR_RED}Название не может быть пустым${COLOR_RESET}")
            null
        } else t
    }.first()

    val description = prompt("Введите описание (или Enter для пропуска): ")
    val chosenPriority = chooseFromList("\nВыберите приоритет:", priorityOptions)!!

    println("\nДоступные категории:")
    categories.forEachIndexed { i, c -> println("${i + 1}. $c") }
    println("${categories.size + 1}. Создать новую категорию")
    val catChoice = readIntInRange("Ваш выбор (1-${categories.size + 1}): ", 1..(categories.size + 1))!!
    val category = if (catChoice == categories.size + 1) {
        generateSequence {
            val c = prompt("Введите название новой категории: ")
            if (c.isBlank()) {
                println("${COLOR_RED}Категория не может быть пустой${COLOR_RESET}")
                null
            } else c
        }.first().also { if (!categories.contains(it)) categories.add(it) }
    } else categories[catChoice - 1]

    val dueDate = generateSequence {
        val input = prompt("Введите дату выполнения (дд.мм.гггг или Enter для сегодня): ")
        val value = if (input.isBlank()) today() else input
        if (parseDateOrNull(value) == null) {
            println("${COLOR_RED}Некорректный формат даты${COLOR_RESET}")
            null
        } else value
    }.first()

    val now = today()
    val id = generateId()
    tasks.add(
        Task(
            id = id,
            title = title,
            description = description,
            priority = chosenPriority,
            dueDate = dueDate,
            isCompleted = false,
            category = category,
            createdAt = now
        )
    )
    println("${COLOR_GREEN}✅ Задача '$title' добавлена с ID: $id${COLOR_RESET}")
}

fun listTasksInteractive() {
    if (tasks.isEmpty()) {
        println("📭 Список задач пуст")
        return
    }
    println("\nФильтр статуса:")
    println("1. Все  2. Активные  3. Выполненные")
    val option = readIntInRange("Ваш выбор (1-3) или Enter для 'Все': ", 1..3, allowEmpty = true) ?: 1
    val filtered = when (option) {
        2 -> tasks.filter { !it.isCompleted }
        3 -> tasks.filter { it.isCompleted }
        else -> tasks
    }
    if (filtered.isEmpty()) {
        println("📭 Ничего не найдено по фильтру")
        return
    }

    val grouped = filtered.groupBy { it.category }
    var idx = 1
    grouped.toSortedMap(String.CASE_INSENSITIVE_ORDER).forEach { (cat, list) ->
        println("\n📁 Категория: ${cat} (${list.size})")
        println("--------------------------------------------")
        list.sortedWith(compareBy<Task> { it.isCompleted }.thenBy { it.title.lowercase(Locale.getDefault()) })
            .forEach { task ->
                printTask(task, idx)
                idx += 1
            }
    }
}

fun findTaskByIdInteractive(): Task? {
    val id = prompt("Введите ID задачи: ").toIntOrNull()
    if (id == null) {
        println("${COLOR_RED}Некорректный ID${COLOR_RESET}")
        return null
    }
    val task = tasks.find { it.id == id }
    if (task == null) println("${COLOR_RED}Задача с ID $id не найдена${COLOR_RESET}")
    return task
}

fun markCompletedInteractive() {
    val task = findTaskByIdInteractive() ?: return
    if (task.isCompleted) {
        println("${COLOR_YELLOW}Задача уже отмечена выполненной${COLOR_RESET}")
        return
    }
    task.isCompleted = true
    println("${COLOR_GREEN}✅ Задача '${task.title}' отмечена выполненной${COLOR_RESET}")
}

fun editTaskInteractive() {
    val task = findTaskByIdInteractive() ?: return
    if (task.isCompleted) {
        println("${COLOR_RED}Нельзя редактировать выполненную задачу${COLOR_RESET}")
        return
    }

    println("Оставьте поле пустым чтобы сохранить текущее значение")
    val newTitle = prompt("Название [${task.title}]: ")
    if (newTitle.isNotBlank()) task.title = newTitle else if (task.title.isBlank()) {
        println("${COLOR_RED}Название не может быть пустым${COLOR_RESET}"); return
    }

    val newDesc = prompt("Описание [${if (task.description.isBlank()) "-" else task.description}]: ")
    if (newDesc.isNotBlank()) task.description = newDesc

    val pri = chooseFromList("Новый приоритет (текущий: ${task.priority}) или Enter для пропуска:", priorityOptions, allowEmpty = true)
    if (pri != null) task.priority = pri

    println("\nДоступные категории (текущая: ${task.category}):")
    categories.forEachIndexed { i, c -> println("${i + 1}. $c") }
    println("${categories.size + 1}. Создать новую категорию")
    val catIdx = readIntInRange("Ваш выбор (1-${categories.size + 1}) или Enter: ", 1..(categories.size + 1), allowEmpty = true)
    if (catIdx != null) {
        val newCat = if (catIdx == categories.size + 1) {
            val c = generateSequence {
                val v = prompt("Введите название новой категории: ")
                if (v.isBlank()) null else v
            }.first()
            if (!categories.contains(c)) categories.add(c)
            c
        } else categories[catIdx - 1]
        task.category = newCat
    }

    val newDueRaw = prompt("Дата выполнения [${task.dueDate}] (дд.мм.гггг) или Enter: ")
    if (newDueRaw.isNotBlank()) {
        if (parseDateOrNull(newDueRaw) == null) {
            println("${COLOR_RED}Некорректная дата — изменения не сохранены${COLOR_RESET}")
        } else task.dueDate = newDueRaw
    }

    println("${COLOR_GREEN}Изменения сохранены${COLOR_RESET}")
}

fun deleteTaskInteractive() {
    val task = findTaskByIdInteractive() ?: return
    println("Вы уверены, что хотите удалить задачу '${task.title}'? (y/N)")
    val confirm = readLine()?.trim()?.lowercase(Locale.getDefault())
    if (confirm == "y" || confirm == "д" || confirm == "yes") {
        tasks.removeIf { it.id == task.id }
        println("${COLOR_GREEN}Задача удалена${COLOR_RESET}")
    } else {
        println("Операция отменена")
    }
}

fun searchTasksInteractive() {
    println("\nПоиск:")
    println("1. По названию")
    println("2. По описанию")
    println("3. По категории")
    println("4. По приоритету")
    println("5. Просроченные задачи")
    val choice = readIntInRange("Ваш выбор (1-5): ", 1..5) ?: return
    val result: List<Task> = when (choice) {
        1 -> {
            val q = prompt("Введите часть названия: ").lowercase(Locale.getDefault())
            tasks.filter { it.title.lowercase(Locale.getDefault()).contains(q) }
        }
        2 -> {
            val q = prompt("Введите часть описания: ").lowercase(Locale.getDefault())
            tasks.filter { it.description.lowercase(Locale.getDefault()).contains(q) }
        }
        3 -> {
            val cat = chooseFromList("Выберите категорию:", categories) ?: return
            tasks.filter { it.category == cat }
        }
        4 -> {
            val pri = chooseFromList("Выберите приоритет:", priorityOptions) ?: return
            tasks.filter { it.priority == pri }
        }
        else -> tasks.filter { overdue(it) }
    }
    if (result.isEmpty()) {
        println("📭 Ничего не найдено")
    } else {
        result.forEachIndexed { index, task -> printTask(task, index + 1) }
    }
}

fun statsInteractive() {
    val total = tasks.size
    val completed = tasks.count { it.isCompleted }
    val active = total - completed
    val overdueCount = tasks.count { overdue(it) }
    val percent = if (total == 0) 0 else (completed * 100 / total)

    printHeader("Статистика")
    println("Всего задач: $total")
    println("Выполнено: $completed")
    println("Активно: $active")
    println("Просрочено: $overdueCount")
    println("Процент выполнения: $percent%")

    println("\nРаспределение по приоритетам:")
    priorityOptions.forEach { p ->
        val count = tasks.count { it.priority == p }
        println("- $p: $count")
    }

    println("\nРаспределение по категориям:")
    (categories.ifEmpty { defaultCategories }).forEach { c ->
        val count = tasks.count { it.category == c }
        println("- $c: $count")
    }
}


fun showMenu() {
    println("\n=================================================")
    println("СИСТЕМА УПРАВЛЕНИЯ ЗАДАЧАМИ")
    println("=================================================")
    println("🗂  1  Показать все задачи")
    println("➕  2  Добавить задачу")
    println("✅  3  Отметить задачу выполненной")
    println("✏️  4  Редактировать задачу")
    println("🗑  5  Удалить задачу")
    println("🔎  6  Поиск задач / Фильтры")
    println("📊  7  Статистика")
    println("❌  0  Выход")
    println("-------------------------------------------------")
}

fun main() {
    while (true) {
        showMenu()
        val action = prompt("Выберите действие (0-7): ").toIntOrNull()
        when (action) {
            1 -> { listTasksInteractive(); pressEnterToContinue() }
            2 -> { createTaskInteractive(); pressEnterToContinue() }
            3 -> { markCompletedInteractive(); pressEnterToContinue() }
            4 -> { editTaskInteractive(); pressEnterToContinue() }
            5 -> { deleteTaskInteractive(); pressEnterToContinue() }
            6 -> { searchTasksInteractive(); pressEnterToContinue() }
            7 -> { statsInteractive(); pressEnterToContinue() }
            0 -> return
            else -> println("${COLOR_RED}Неверный ввод. Выберите пункт 0-7.${COLOR_RESET}")
        }
    }
}
