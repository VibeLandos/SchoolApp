# Карта проекта

Android-приложение **Дневник**: расписание по дням недели + домашка на конкретную дату. Пакет `com.example.firstandroidap`. Модуль один: `:app`.

## Точки входа

| Файл | Роль |
|---|---|
| `app/src/main/AndroidManifest.xml` | Application + launcher Activity |
| `app/src/main/java/com/example/firstandroidap/SchoolApplication.kt` | Room + `SchoolRepository` + `ThemeSettings` |
| `app/src/main/java/com/example/firstandroidap/MainActivity.kt` | Compose, `DiaryTheme(mode)`, `SchoolApp` |
| `app/src/main/java/com/example/firstandroidap/ui/SchoolApp.kt` | Навигация, нижнее меню, drawer оформления (свайп/назад закрывают, с края не открывают), шиты |
| `app/src/main/java/com/example/firstandroidap/ui/SchoolViewModel.kt` | Состояние дня/недели, запись в репозиторий |

## Данные (`.../data/`)

| Файл | Роль |
|---|---|
| `Models.kt` | `Lesson`, `Homework`, `HomeworkPhoto` (несколько фото на д/з) |
| `Daos.kt` | `LessonDao`, `HomeworkDao`, `HomeworkPhotoDao` |
| `AppDatabase.kt` | Room `diary.db`, v2 (миграция с v1) |
| `SchoolRepository.kt` | upsert/удаление; пустой текст и без фото = удалить д/з |
| `HomeworkPhotoStore.kt` | копии фото в `filesDir/homework_photos`, до 8 штук |
| `Catalog.kt` | предметы, звонки 1–8, даты (пн–сб), подпись предмета для ДЗ |
| `ThemeSettings.kt` | режим темы: обложка / система / ночная страница |

## Экраны (`.../ui/`)

| Файл | Роль |
|---|---|
| `diary/DiaryScreen.kt` | Страница дневника: пейджер пн–сб (6 страниц, без пересоздания), таблица № / предмет / д/з |
| `components/HomeworkPhotoThumb.kt` | Превью фото (декод в IO) |
| `homework/HomeworkListScreen.kt` | Вкладка «Задания»: список д/з, переход на день |
| `components/EditHomeworkSheet.kt` | Вписать/стереть д/з, прикрепить несколько фото |
| `components/EditLessonSheet.kt` | Предмет + кабинет; время из звонков, не вручную |
| `theme/Color.kt`, `Type.kt`, `Theme.kt` | Палитры обложки, ночной страницы и Material You; `LocalDiaryPalette` |
| `menu/AppDrawer.kt` | Боковое меню: переключатель оформления |

Нижние вкладки: **Дневник** (`diary`), **Задания** (`tasks`). Меню: иконка «гамбургер» на обоих экранах.

## Сборка

- Gradle: корневой `build.gradle.kts`, `app/build.gradle.kts`, версии в `gradle/libs.versions.toml`
- JDK для Gradle: `org.gradle.java.home` в `gradle.properties` → Android Studio `jbr` (не системная Java 25)
- Compose + Material3, Navigation, Room (KSP), desugar для `java.time`; Compose Compiler strong skipping
- Release: R8 minify + debug-подпись, чтобы ставить на телефон без keystore
- Имя приложения: `app/src/main/res/values/strings.xml` → «Дневник»

## Документы для агента

`docs/MAP.md` (этот файл), `docs/PLAN.md`, `docs/CHANGELOG.md`, правило `.cursor/rules/project.mdc`
