# Карта проекта

Android-приложение **Дневник**: расписание по дням недели + домашка на конкретную дату. Пакет `com.example.firstandroidap`. Модуль один: `:app`.

## Точки входа

| Файл | Роль |
|---|---|
| `app/src/main/AndroidManifest.xml` | Application + launcher Activity |
| `app/src/main/java/com/example/firstandroidap/SchoolApplication.kt` | Room + `SchoolRepository` + `ThemeSettings` |
| `app/src/main/java/com/example/firstandroidap/MainActivity.kt` | Compose, `DiaryTheme(mode)`, `SchoolApp` |
| `app/src/main/java/com/example/firstandroidap/ui/SchoolApp.kt` | Навигация, нижнее меню (дневник / сейчас / задания), drawer, шиты |
| `app/src/main/java/com/example/firstandroidap/ui/SchoolViewModel.kt` | Состояние дня/недели, запись в репозиторий |

## Данные (`.../data/`)

| Файл | Роль |
|---|---|
| `Models.kt` | `Lesson`, `Homework`, `HomeworkPhoto` (несколько фото на д/з) |
| `Daos.kt` | `LessonDao`, `HomeworkDao`, `HomeworkPhotoDao` |
| `AppDatabase.kt` | Room `diary.db`, v2 (миграция с v1) |
| `SchoolRepository.kt` | upsert/удаление; пустой текст и без фото = удалить д/з |
| `HomeworkPhotoStore.kt` | копии фото в `filesDir/homework_photos`, до 8 штук |
| `Catalog.kt` | звонки по умолчанию 1–8, свободный номер урока, даты (пн–сб); предметы — `res/values*/strings.xml` |
| `BellSchedule.kt` | рецепт звонков, расписание на неделю и отдельные дни (`WeekBells`) |
| `ScheduleText.kt` | текст дня/недели для отправки и вставки (уроки + звонки, без д/з) |
| `NowStatus.kt` | сейчас урок / перемена / до начала / конец дня / выходной |
| `ThemeSettings.kt` | режим темы, язык, звонки, длина учебной недели (5/6) |

## Экраны (`.../ui/`)

| Файл | Роль |
|---|---|
| `diary/DiaryScreen.kt` | Страница дневника: пейджер пн–сб, только вписанные уроки + «добавить урок»; время из звонков этого дня |
| `components/HomeworkPhotoThumb.kt` | Превью фото (декод в IO) |
| `homework/HomeworkListScreen.kt` | Вкладка «Задания»: список д/з, переход на день |
| `now/NowScreen.kt` | Вкладка «Сейчас»: сколько до конца урока или перемены и до конца дня |
| `components/EditHomeworkSheet.kt` | Вписать/стереть д/з, прикрепить несколько фото |
| `components/EditLessonSheet.kt` | Предмет, кабинет, номер урока; время из звонков |
| `components/ImportScheduleSheet.kt` | вставить текст расписания |
| `theme/Color.kt`, `Type.kt`, `Theme.kt` | Палитры обложки, ночной страницы и Material You; `LocalDiaryPalette` |
| `menu/AppDrawer.kt` | Боковое меню: оформление, язык, неделя, звонки, отправка/вставка |
| `menu/BellScheduleSheet.kt` | Первый заход — быстрая настройка; далее звонки по дням пн–сб |

Нижние вкладки: **Дневник** (`diary`), **Сейчас** (`now`), **Задания** (`tasks`). Меню: иконка «гамбургер» на всех экранах. Язык: русский по умолчанию, английский из меню. Учебная неделя: 5 или 6 дней. Звонки: быстрая настройка на неделю, день можно задать отдельно. Расписание дня/недели можно отправить текстом и вставить обратно.

## Сборка

- Gradle: корневой `build.gradle.kts`, `app/build.gradle.kts`, версии в `gradle/libs.versions.toml`
- JDK для Gradle: `org.gradle.java.home` в `gradle.properties` → `jbr-21.0.11` (не системная Java 25 и не сломанный `jbr` новой Studio)
- Кэш Gradle: wrapper и `.idea/gradle.xml` плюс задача `pinCursorGradleCache` — junction `Temp/cursor-sandbox-cache/*/gradle` → `%USERPROFILE%/.gradle`, потому что Studio из Cursor не запускает wrapper и ходит в стёртый Temp.
- Compose + Material3, Navigation, Room (KSP), desugar для `java.time`; Compose Compiler strong skipping
- Release: R8 minify + debug-подпись, чтобы ставить на телефон без keystore
- Имя приложения: `app/src/main/res/values/strings.xml` → «Дневник» / `values-en` → «Diary»

## Документы для агента

`docs/MAP.md` (этот файл), `docs/PLAN.md`, `docs/CHANGELOG.md`, правило `.cursor/rules/project.mdc`
