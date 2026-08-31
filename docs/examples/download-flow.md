# Поток загрузки

## Цель

Описать процесс восстановления локального состояния игры путем загрузки отсутствующих или
поврежденных ресурсов

---

## Текущий статус

Если первичный `VerificationPlan` валиден, `LauncherEngine` запускает `PREPARE_DIRECTORIES` без построения
`DownloadPlan`

После успешной подготовки директорий `LauncherEngine` пропускает процесс построения `DownloadPlan` и продолжает двигаться
дальше по жизненному циклу [`launcher-lifecycle`](../architecture/launcher/launcher-lifecycle.md)

В случае когда `VerificationPlan` невалиден, `LauncherEngine` запускает `BUILD_DOWNLOAD_PLAN`

`LauncherEngine` запускает `DOWNLOAD_FILES` после успешного построения `DownloadPlan`

Если при построении `DownloadPlan` произошла ошибка `LauncherEngine` переходит в `FAILED`

`DownloadFilesTask` выполняет загрузку через `DownloadService` и публикует специализированные события
жизненного цикла загрузки

После `DOWNLOAD_FILES` `LauncherEngine` повторно запускает `VERIFY_FILES`, потому что загрузка файла
сама по себе не доказывает корректность локального состояния

После успешной повторной проверки `LauncherEngine` продолжает двигаться дальше по жизненному циклу [`launcher-lifecycle`](../architecture/launcher/launcher-lifecycle.md)

На текущем этапе `DownloadPlan` строится из результатов проверки ресурсов

`VerificationPlan` формируется на основе `ManifestResources.from(...)`, поэтому `DownloadPlanBuilder` может включать в
`DownloadPlan` невалидные ресурсы из `Manifest.files` и `Manifest.libraries`

`DownloadPlan` содержит только ресурсы, требующие восстановления

На текущем этапе `Manifest.libraries` может содержать compatibility projection
из `RuntimeLibrarySelection.selectedArtifacts()`, поэтому flow не различает обычные libraries и
selected native artifacts

## Предусловия

- `VERIFY_FILES` завершена успешно
- В `LaunchContext` сохранен `VerificationPlan`
- `VerificationPlan` содержит ресурсы, требующие восстановления
- `BUILD_DOWNLOAD_PLAN` успешно построен
- `DownloadPlan` сохранен в `LaunchContext`

---


## Разрешение локального пути ресурса

Download flow не строит `targetPath` напрямую через

```java
gameDirectory.resolve(resource.path())
```

Перед скачиванием локальный путь ресурса разрешается через общий `ResourcePathResolver`, который
проверяет, что путь из manifest остается внутри `gameDirectory`

После успешного разрешения пути `DefaultDownloadService` передает безопасный `targetPath` в `FileDownloader`,
а затем выполняет проверку размера скачанного ресурса

---

## Последовательность

```text
LauncherEngine
    -> VERIFY_FILES
        -> VerificationPlan
    -> BUILD_DOWNLOAD_PLAN
        -> BuildDownloadPlanTask
            -> DownloadPlanBuilder
        -> DownloadPlan
    -> DOWNLOAD_FILES
        -> DownloadFilesTask
            -> DownloadService
                -> DefaultDownloadService
                    -> FileDownloader
    -> VERIFY_FILES
        -> VerificationPlan
```

---

## Этапы

### 1. Анализ `VerificationPlan`

`DownloadPlanBuilder` получает `VerificationPlan` и выбирает ресурсы со статусами

- `MISSING`
- `OUTDATED`
- `CORRUPTED`

Ресурсы со статусом `VALID` не попадают в `DownloadPlan`

### 2. Построение `DownloadPlan`

`BuildDownloadPlanTask` сохраняет построенный `DownloadPlan` в `LaunchContext`

### 3. Выполнение загрузки

`DownloadFilesTask` получает `DownloadPlan` из `LaunchContext` и передает его в `DownloadService`

`DefaultDownloadService` получает `game directory` через `DirectoryProvider`, разрешает локальный путь каждого ресурса
через `ResourcePathResolver` и передает `url` и безопасный `targetPath` в `FileDownloader`

### 4. Загрузка отдельного файла

`FileDownloader` загружает файл во временный файл после успешной загрузки перемещает его в целевой `targetPath`

При ошибке временный файл удаляется, а ошибка передается вызывающему коду

### 5. Повторная проверка

После успешного `DOWNLOAD_FILES` `LauncherEngine` повторно запускает `VERIFY_FILES`

Повторная проверка определяет, действительно ли локальное состояние соответствует ожидаемому состоянию

Если повторный `VerificationPlan` валиден, `LauncherEngine` переходит в `PREPARE_DIRECTORIES`, затем `EXTRACT_NATIVES`,
затем `BUILD_GAME_LAUNCH_PLAN`, затем `LAUNCH_GAME` и затем в `RUNNING`

Если проверка завершается ошибкой, или план остается невалидным, `LauncherEngine` переходит в `FAILED`

---

## Текущие ограничения

- `DownloadFilesTask` не получает потоковый прогресс от `DownloadService`
- `DownloadProgressChangedEvent` публикуется один раз после успешного завершения загрузки и содержит
  полностью завершенный прогресс
- Проверка `checksum` не выполняется непосредственно `DownloadService` или `FileDownloader`
- Итоговая корректность загруженных файлов подтверждается `VERIFY_FILES`

---

## Модель ошибок загрузки

Ошибки загрузки считаются техническими ошибками download-слоя

`DefaultFileDownloader` преобразует ошибки получения данных, записи временного файла и
переноса файла в `DownloadException`

`DefaultDownloadService` преобразует ошибки размера загруженного файла в `DownloadException`

`DownloadFilesTask` не анализирует тип ошибки download-слоя. Он преобразует исключения в `Result.failure(...)`

При ошибке загрузки:

- `DownloadStartedEvent` может быть уже опубликован, если `DownloadPlan` был непустым
- `DownloadProgressChangedEvent` не публикуется
- `DownloadCompletedEvent` не публикуется
- `LauncherEngine` переводит launcher в `FAILED`

`DownloadException` должен сохранять исходную причину ошибки, если она доступна

Ошибка загрузки содержит структурированный контекст

- `reason`
- `url`
- `path`, если ошибка связана с `ResourceEntry`
- `targetPath`, если ошибка связана с локальным файлом
- `cause`, если исходная причина доступна

---

## Компоненты

- `OperationManager`
- `BuildDownloadPlanTask`
- `DownloadPlanBuilder`
- `DownloadPlan`
- `DownloadFilesOperation`
- `DownloadFilesTask`
- `DownloadService`
- `DefaultDownloadService`
- `FileDownloader`
- `DefaultFileDownloader`

---

## Инварианты

D-1

`DownloadPlan` содержит только ресурсы, требующие восстановления

D-2

`DownloadFilesTask` не строит `DownloadPlan` самостоятельно

D-3

`DefaultDownloadService` не принимает решений о составе загрузки

D-4

`FileDownloader` отвечает только за загрузку одного файла в указанный targetPath

D-5

Успешное завершение `DOWNLOAD_FILES` не является подтверждением корректности локального состояния

Корректность подтверждается повторным VERIFY_FILES

D-6

`DownloadFilesTask` не знает конкретных получателей событий. События публикуются через `EventBus`

D-7

Публикация `DownloadCompletedEvent` разрешена только после успешного завершения загрузки

---

## Связанные документы

- [`download-events.md`](../event-flow/download-events.md) - контракт событий жизненного цикла загрузки
- [`verification-flow.md`](verification-flow.md) - поток проверки файлов
- [`operation-model.md`](../architecture/operation/operation-model.md) - общая модель выполнения `Operation`