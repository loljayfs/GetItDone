# Get It Done

A simple, focused Android task app inspired by the ideas in *Redeeming Your Time* by Jordan Raynor: know why a task matters, do first things first, take one clear next action at a time, rest, and review.

## Features

- **Today's Focus** – your top 3 tasks are highlighted; the rest sit under them in one list.
- **Drag to reorder** – press and hold a task, then drag it into place. The order is saved.
- **Next action** – every task has a concrete next physical action.
- **Why it matters** – an optional purpose line on each task.
- **Check-off flow** – tapping the check mark asks whether to *move to the next action* or mark the *whole task done*.
- **History** – finished tasks are grouped by day with a last-7-days count and a weekly review prompt. You can restore or delete them, or clear the history.
- **Edit and delete** – tap a task to edit it; deleting asks for confirmation and offers Undo.
- **Due dates and filters** – optional due dates (overdue shown in red) and All / Personal / Work filters.
- **Theme** – baby blue and sage, light and dark mode, high contrast.
- **Offline** – tasks are stored locally in `tasks.json` in the app's private storage.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- `AndroidViewModel` for state, Gson for JSON storage
- [`sh.calvin.reorderable`](https://github.com/Calvin-LL/Reorderable) for drag and drop
- Gradle 9.2, Android Gradle Plugin 9.0, Kotlin 2.2.20
- minSdk 29, targetSdk 34

## Project structure

| File | Purpose |
| --- | --- |
| `MainActivity.kt` | Main screen: Focus / History tabs, filters, reordering |
| `TaskViewModel.kt` | Task state and actions (add, update, complete, restore, delete, move) |
| `TaskRepository.kt` | Loads and saves `tasks.json` (atomic writes) |
| `TaskItem.kt` | Task data model |
| `TaskItemRow.kt` | Task card |
| `AddTaskDialog.kt` | Add / edit / delete dialog |
| `CompleteTaskDialog.kt` | "Next action or whole task done" dialog |
| `HistoryScreen.kt` | Finished tasks |
| `DateFormatting.kt` | Date helpers |
| `ui/theme/` | Colours, typography, theme |

## Build and run

Requires Android Studio (or the Android SDK) and a JDK 17 or newer.

```bash
./gradlew assembleDebug        # build a debug APK
./gradlew testDebugUnitTest    # run unit tests
./gradlew installDebug         # install on a connected device or emulator
```

If `JAVA_HOME` is not set, point it at a JDK, for example Android Studio's bundled one.
