# Поток верификации

## Цель

Описать последовательность проверки локального состояния игровых ресурсов перед выбором
следующего шага `LauncherEngine`

Проверка определяет, соответствует ли локальное состояние ресурсов ожидаемому состоянию
описанному в `Manifest`

---

## Текущий статус

На текущем этапе verification flow использует `ManifestResources.from(...)` как источник проверяемых
ресурсов

`ManifestResources` представляет resource-level projection для `Manifest.files`, `Manifest.libraries` и `Manifest.assetsIndex`

Проверяемой единицей verification flow является `ResourceEntry`

На текущем этапе `Manifest.libraries` может содержать compatibility projection
из `RuntimeLibrarySelection.selectedArtifacts()`, поэтому flow не различает обычные libraries и
selected native artifacts

---

## Предусловия

- `Manifest` успешно загружен
- `LaunchContext` создан
- `VERIFY_FILES` запущена через `OperationManager`

---

## Последовательность

```text
LauncherEngine
    -> VERIFY_FILES
        -> VerificationOperation
            -> VerifyFilesTask
                -> VerificationService
                    -> DefaultVerificationService
                        -> ResourceSetPlanner
                            -> ResourcePathResolver
                                -> SafeResourcePathResolver
                        -> FileVerifier
                            -> HashService
                            -> ResourceVerificationResult
                        -> VerificationPlan
                -> LaunchContext
                    -> VerificationPlan
```
---

## Разрешение локального пути ресурса

Verification flow не собирает локальный путь ресурса через

```java
gameDirectory.resolve(resource.path())
```

Перед передачей ресурса в `FileVerifier` путь разрешается через общий `ResourcePathResolver`, который

- Принимает базовую директорию игры
- Принимает `ResourceEntry.path`
- Нормализует итоговый путь
- Отклоняет небезопасные пути, выходящие за пределы `gameDirectory`

По итогу verification отвечает за проверку ресурса, при этом не дублирует правила безопасного
построения локального пути

---

## Подготовка набора ресурсов

`DefaultVerificationService` подготавливает полный набор из `ManifestResources.from(...)` через `ResourceSetPlanner`

Planner разрешает локальные назначения через общий `ResourcePathResolver` относительно игровой директории

Совместимые записи с одинаковым нормализованным назначением объединяются с сохранением первой записи и порядка уникальных
назначений

Записи совместимы, если их значения `sha256`, `size` и `url` равны

При конфликте подготовка завершается с `ResourceSetConflictException`

До успешного завершения подготовки `FileVerifier` не вызывается

После подготовки `FileVerifier` получает исходную метадату выбранного ресурса и локальный путь из `ResourceSetPlan`

Конфликт метаданных является ошибкой выполнения проверки, а не статусом локального файла

---

## Этапы

### 1. Получение `Manifest`

`VerifyFilesTask` получает `Manifest` из `LaunchContext`

`VerifyFilesTask` передает `Manifest` в `VerificationService`

### 2. Проверка ресурсов

`VerificationService` координирует проверку ресурсов

`ResourceSetPlanner` выполняет проверку входящего списка ресурсов согласно [Подготовке набора ресурсов](verification-flow.md#подготовка-набора-ресурсов)

`FileVerifier` выполняет проверку отдельного файла

Для каждого файла определяется его состояние, включая

- Существует ли файл
- Соответствует ли размер ожидаемому значению
- Совпадает ли checksum

Операция с checksum выполняется через `HashService`

`FileVerifier` формирует `ResourceVerificationResult` для каждого проверяемого файла

### 3. Формирование `VerificationPlan`

`VerificationService` формирует `VerificationPlan`, содержащий результаты проверки ресурсов

`VerificationPlan` является неизменяемой моделью результата проверки

`VerifyFilesTask` сохраняет `VerificationPlan` в `LaunchContext`

### 4. Использование результата

`LauncherEngine` получает `VerificationPlan` из `LaunchContext`

`VerificationPlan.isValid()` используется для выбора следующего шага

Если план валиден

```text
VERIFY_FILES
    -> VerificationPlan.isValid()
        -> PREPARE_DIRECTORIES
            -> EXTRACT_NATIVES
                -> BUILD_GAME_LAUNCH_PLAN
                    -> LAUNCH_GAME
                        -> RUNNING
```

Если план невалиден

```text
VERIFY_FILES
    -> VerificationPlan.isValid()
        -> BUILD_DOWNLOAD_PLAN
```

После загрузки ресурсов `LauncherEngine` повторно запускает `VERIFY_FILES`

Повторная проверка необходима для подтверждения фактической корректности локального состояния

### 5. Завершение

`VerifyFilesTask` возвращает результат выполнения через `Result`

При успешном выполнении проверки операция завершается с `Result.success(...)`

Ошибки выполнения проверки возвращаются через `Result.failure(...)`

События жизненного цикла операции публикуются `LaunchOperation` через `EventBus`

## Компоненты

- `OperationManager`
- `VerifyFilesTask`
- `VerificationService`
- `DefaultVerificationService`
- `FileVerifier`
- `HashService`
- `ResourceVerificationResult`
- `VerificationPlan`
- `LaunchContext`
- `ResourceSetPlanner`
- `PlannedResource`
- `ResourceSetPlan`

## Результат

Если `VerificationPlan.isValid()` возвращает `true`, `LauncherEngine` может пропустить этап построения
`DownloadPlan` и перейти сразу к `PREPARE_DIRECTORIES`, затем `EXTRACT_NATIVES`, затем `BUILD_GAME_LAUNCH_PLAN`,
затем `LAUNCH_GAME` и затем в `RUNNING`

Если `VerificationPlan.isValid()` возвращает `false`, `LauncherEngine` запускает `BUILD_DOWNLOAD_PLAN`

После успешной загрузки ресурсов `VERIFY_FILES` выполняется повторно

Только валидный результат повторной проверки позволяет перейти к следующему этапу жизненного цикла `LauncherEngine`

## Инварианты

V-1

Верификация не изменяет локальные файлы

V-2

`VerificationPlan` содержит результаты проверки ресурсов и является неизменяемой моделью

V-3

`VerificationPlan.isValid()` используется `LauncherEngine` для выбора следующего шага жизненного цикла

V-4

`VerifyFilesTask` получает `Manifest` из `LaunchContext` и сохраняет сформированный `VerificationPlan`
обратно в `LaunchContext`

V-5

`VerificationService` координирует проверку множества ресурсов и не отвечает за изменение локального состояния

V-6

Повторная проверка после `DOWNLOAD_FILES` обязательна для подтверждения корректности восстановленного локального
состояния

V-7

Подготовка полного набора ресурсов завершается до проверки первого файла
