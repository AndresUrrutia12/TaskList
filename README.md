# 📝 TaskList

A command-line task management application written in **Kotlin** as part of the **JetBrains Academy / Hyperskill** track.

This tool allows users to create, organize, prioritize, and persist daily tasks with strict validation for dates, times, and task metadata.

### ✨ Key Features
- **Task Management:** Add, edit, delete, and view prioritized tasks.
- **Date & Time Validation:** Uses Kotlin's `kotlinx-datetime` library to validate task deadlines and dates (handling leap years, past dates, etc.).
- **Task Urgency Tracking:** Automatically categorizes tasks based on due dates (e.g., *In time*, *Today*, *Overdue*).
- **Data Persistence:** Saves and loads tasks to/from a local JSON file using `kotlinx.serialization`.
- **Custom Terminal Formatting:** Displays tasks in structured ASCII/table formats with color-coded priorities.

### 🛠️ Tech Stack & Concepts
- **Language:** Kotlin
- **Libraries:** `kotlinx-datetime`, `kotlinx.serialization`
- **Concepts:** File I/O, JSON Serialization, Custom Input Validation, CLI UI Design
