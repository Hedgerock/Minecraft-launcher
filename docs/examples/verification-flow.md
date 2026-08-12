# Поток верификации

## Цель 

Описать последовательность проверки локального состояния игровых файлов перед выбором
следующего шага `LauncherEngine`

Проверка определяет, соответствует ли локальное состояние файлов ожидаемому состоянию
описанному в `Manifest`

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
                        -> FileVerifier
                            -> HashService
                            -> FileVerificationResult
                        -> VerificationPlan
                -> LaunchContext
                    -> VerificationPlan
```
---

## Этапы

### 1. Получение `Manifest`

`VerifyFilesTask` получает `Manifest` из `LaunchContext`

`VerifyFilesTask` передает `Manifest` в `VerificationService`

### 2. Проверка файлов

`VerificationService` координирует проверку файлов

`FileVerifier` выполняет проверку отдельного файла

Для каждого файла определяется его состояние, включая

- Существует ли файл
- Соответствует ли размер ожидаемому значению
- Совпадает ли checksum

Операция с checksum выполняется через `HashService`

`FileVerifier` формирует `FileVerificationResult` для каждого проверяемого файла

### 3. Формирование `VerificationPlan`

`VerificationService` формирует `VerificationPlan`, содержащий результаты проверки файлов

`VerificationPlan` является неизменяемой моделью результата проверки

`VerifyFilesTask` сохраняет `VerificationPlan` в `LaunchContext`

### 4. Использование результата

`LauncherEngine` получает `VerificationPlan` из `LaunchContext`

`VerificationPlan.isValid()` используется для выбора следующего шага

Если план валиден

```text
VERIFY_FILES
    -> VerificationPlan.isValid()
        -> RUNNING
```

Если план невалиден

```text
VERIFY_FILES
    -> VerificationPlan.isValid()
        -> BUILD_DOWNLOAD_PLAN
```

После загрузки файлов `LauncherEngine` повторно запускает `VERIFY_FILES`

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
- `FileVerificationResult`
- `VerificationPlan`
- `LaunchContext`

## Результат

Если `VerificationPlan.isValid()` возвращает `true`, `LauncherEngine` может пропустить этап построения
`DownloadPlan` и перейти сразу к `PREPARE_DIRECTORIES`, затем `LAUNCH_GAME` и затем в `RUNNING`

Если `VerificationPlan.isValid()` возвращает `false`, `LauncherEngine` запускает `BUILD_DOWNLOAD_PLAN`

После успешной загрузки файлов `VERIFY_FILES` выполняется повторно

Только валидный результат повторной проверки позволяет перейти к следующему этапу жизненного цикла `LauncherEngine`

## Инварианты

V-1

Верификация не изменяет локальные файлы

V-2

`VerificationPlan` содержит результаты проверки файлов и является неизменяемой моделью

V-3

`VerificationPlan.isValid()` используется `LauncherEngine` для выбора следуюущего шага жизненного цикла

V-4

`VerifyFilesTask` получает `Manifest` из `LaunchContext` и сохраняет сформированный `VerificationPlan` 
обратно в `LaunchContext`

V-5

`VerificationService` координирует проверку множества файлов и не отвечает за изменение локального состояния

V-6

Повторная проверка после `DOWNLOAD_FILES` обязательна для подтверждения корректности восстановленного локального
состояния
























