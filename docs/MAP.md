# Карта проекта

Android-приложение **Дневник**: расписание по дням недели + домашка на конкретную дату. Пакет `com.arzabc.school`, версия **6.7**. Модуль один: `:app`.

## Точки входа

| Файл | Роль |
|---|---|
| `app/src/main/AndroidManifest.xml` | Application + launcher Activity |
| `app/src/main/java/com/arzabc/school/SchoolApplication.kt` | Room + `SchoolRepository` + `ThemeSettings` |
| `app/src/main/java/com/arzabc/school/MainActivity.kt` | Compose, `DiaryTheme(appearance)`, `SchoolApp` |
| `app/src/main/java/com/arzabc/school/ui/SchoolApp.kt` | Навигация, нижнее меню MD3 или стеклянный pill, FAB / капсула «урок», drawer, шиты |
| `app/src/main/java/com/arzabc/school/ui/SchoolViewModel.kt` | Состояние дня/недели, запись в репозиторий |

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
| `ThemeSettings.kt` | стиль (You / стекло), яркость, seed-цвет, язык, звонки, длина учебной недели (5/6) |

## Экраны (`.../ui/`)

| Файл | Роль |
|---|---|
| `diary/DiaryScreen.kt` | Страница дня: пейджер пн–сб, карточки уроков + «добавить урок»; время из звонков; метка «идёт сейчас» |
| `components/AppSurfaces.kt` | Шапка экрана, карточка, бейдж номера, кружки цветов, прогресс |
| `components/HomeworkPhotoThumb.kt` | Превью фото (декод в IO) |
| `homework/HomeworkListScreen.kt` | Вкладка «Задания»: список д/з, переход на день |
| `now/NowScreen.kt` | Вкладка «Сейчас»: до конца урока/перемены и дня, полоска прогресса |
| `components/EditHomeworkSheet.kt` | Вписать/стереть д/з, прикрепить несколько фото |
| `components/EditLessonSheet.kt` | Предмет, кабинет, номер урока; время из звонков |
| `components/ImportScheduleSheet.kt` | вставить текст расписания |
| `theme/Color.kt`, `Type.kt`, `Theme.kt`, `Glass.kt` | MD3 seed-палитры, стекло, `Appearance` / `LocalUiStyle` |
| `menu/AppDrawer.kt` | Боковое меню: You / стекло / яркость строками, язык, неделя, звонки, отправка/вставка |
| `menu/BellScheduleSheet.kt` | Первый заход — быстрая настройка; далее звонки по дням пн–сб |

Нижние вкладки: **Дневник** (`diary`), **Сейчас** (`now`), **Задания** (`tasks`). Меню: иконка «гамбургер» на всех экранах. Оформление: Material You (обои или seed) либо iOS-стекло; светлая / тёмная / система. Язык: русский по умолчанию, английский из меню. Учебная неделя: 5 или 6 дней. Звонки: быстрая настройка на неделю, день можно задать отдельно. Расписание дня/недели можно отправить текстом и вставить обратно.

Макет-спека: `docs/design/prototype.html` (Gemini HTML: MD3 + стекло, все экраны).

## Сборка

- Gradle: корневой `build.gradle.kts`, `app/build.gradle.kts`, версии в `gradle/libs.versions.toml`
- Идентификатор: `com.arzabc.school`, `versionName` 6.7 / `versionCode` 67 — не поднимать без просьбы
- JDK для Gradle: `org.gradle.java.home` в `gradle.properties` → `jbr-21.0.11` (не системная Java 25 и не сломанный `jbr` новой Studio)
- Кэш Gradle: в `.idea/gradle.xml` задан `gradleUserHome` = `%USERPROFILE%/.gradle`. Задача `pinCursorGradleCache` перед dex/aapt кладёт пропавший `transforms-4` (в т.ч. хеш `navigation-common-ktx`) из настоящего кэша в Cursor Temp. `android.useFullClasspathForDexingTransform` — чтобы dex не требовал стёртые transform-папки.
- Compose + Material3, Navigation, Room (KSP), desugar для `java.time`; Compose Compiler strong skipping
- Release: R8 minify + подпись v1/v2/v3. Свой keystore: `signing/keystore.properties` (по образцу `signing/keystore.properties.example`); иначе debug-ключ. APK для файла — `assembleRelease` / `assembleDebug`, не Run из Studio (`testOnly`)
- Имя приложения: `app/src/main/res/values/strings.xml` → «Дневник» / `values-en` → «Diary»

## Документы для агента

`docs/MAP.md` (этот файл), `docs/PLAN.md`, `docs/CHANGELOG.md`, `docs/design/prototype.html` (макет MD3 + стекло), правило `.cursor/rules/project.mdc`
