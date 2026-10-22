# StitchTrack

Android-приложение для отслеживания вязальных проектов: прогресса, счётчиков, времени работы, заметок и PDF-схем.

Сборка debug-APK выполняется в GitHub Actions. Android Studio на компьютере преподавателя для получения APK не нужна.

## Как получить APK

1. Откройте репозиторий на GitHub.
2. Перейдите во вкладку **Actions**.
3. Выберите workflow **Android Debug APK**.
4. Нажмите **Run workflow** и подтвердите запуск на ветке `main`.
5. Дождитесь завершения сборки. Зелёная отметка означает, что `assembleDebug` прошёл.
6. В завершённой сборке откройте **Artifacts**.
7. Скачайте `stitchtrack-debug-apk`.
8. Распакуйте архив и установите `app-debug.apk` на Android-устройство с API 26 или новее.

Прямая ссылка на сборки:

```text
https://github.com/sebinger/knit-app/actions/workflows/android-debug.yml
```

APK появляется в артефакте после успешного запуска workflow. GitHub хранит артефакты этой сборки ограниченное время, поэтому при необходимости запустите workflow ещё раз.

## Как открыть проект

Проект можно открыть в Android Studio, но для сборки APK Android Studio не требуется: сборка выполняется автоматически в GitHub Actions.

1. Клонируйте репозиторий:

```bash
git clone https://github.com/sebinger/knit-app.git
cd knit-app
```

2. Откройте папку в Android Studio.
3. Дождитесь завершения Gradle Sync.
4. Выберите эмулятор или подключённое Android-устройство.
5. Нажмите Run.

## Локальная сборка

Если установлены JDK 17 и Android SDK, `local.properties` не нужен в репозитории: путь к SDK задаётся переменной `ANDROID_HOME`.

```bash
./gradlew assembleDebug
```

На Windows:

```bat
gradlew.bat assembleDebug
```

APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Тесты

```bash
./gradlew testDebugUnitTest
```

## Возможности

- Создание и редактирование проектов
- Цветные статусные теги: Not Started, In Progress, Completed, Paused
- Счётчики rows, stitches и repeats
- Основной счётчик и расчёт прогресса
- Undo последнего изменения счётчика
- Цели и прогресс до цели
- Таймер вязания с паузой и общим временем
- Заметки
- Выбор и просмотр PDF-схемы
- Работа полностью offline

## Технологии

- Kotlin
- Jetpack Compose
- Material 3
- Room
- Hilt
- Navigation Compose
- Coroutines и Flow
- JUnit

## Требования

| Инструмент | Версия |
|---|---|
| Android Studio | Последняя стабильная версия (для открытия проекта; для APK не обязательна) |
| JDK | 17. Эту версию использует GitHub Actions. Локально подходит JDK 17 или новее |
| Android SDK | На CI ставится автоматически. Локально — через Android Studio. `compileSdk` 37 |
| Устройство | Эмулятор или Android-устройство с API 26+ |

Версии SDK заданы в `gradle/libs.versions.toml`: `minSdk` 26, `compileSdk` 37. Android Gradle Plugin 9.4.1, Gradle Wrapper 9.6.0.

## Структура проекта

```text
app/src/main/java/com/stitchtrack/app/
├─ core/
├─ data/
├─ domain/
└─ feature/
```

## Известные ограничения

- Приложение работает offline.
- Нет учётных записей и синхронизации.
- Нет тёмной темы.
- Поддерживается одна PDF-схема на проект.
- Счётчики не группируются по секциям.
