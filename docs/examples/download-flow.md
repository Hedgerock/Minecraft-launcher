# Поток загрузки

## Цель

Описать процесс восстановления локального состояния игры путем загрузки отсутствующих или
поврежденных ресурсов

---

## Текущий статус

`LauncherEngine` запускает `DOWNLOAD_FILES` после успешного построения `DownloadPlan`

`DownloadFilesTask` выполняет загрузку через `DownloadService` и публикует специализированные события
жизненного цикла загрузки

После `DOWNLOAD_FILES` `LauncherEngine` повторно запускает `VERIFY_FILES`, потому что загрузка файла
сама по себе не доказывает корректность локального состояния

---

## Предусловия

- `VERIFY_FILES` завершена успешно
- В `LaunchContext` сохранен `VerificationPlan`
- `VerificationPlan` содержит файлы, требующие восстановления
- `BUILD_DOWNLOAD_PLAN` успешно построен
- `DownloadPlan` сохранен в `LaunchContext`

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

### 1.Анализ `VerificationPlan`

`DownloadPlanBuilder` получает `VerificationPlan` и выбирает файлы со статусами 

- `MISSING`
- `OUTDATED`
- `CORRUPTED`

Файлы со статусом `VALID` не попадают в `DownloadPlan`

### 2.Построение `DownloadPlan`

`BuildDownloadPlanTask` сохраняет построенный `DownloadPlan` в `LaunchContext`

### 3.Выполнение загрузки

`DownloadFilesTask` получает `DownloadPlan` из `LaunchContext` и передает его в `DownloadService`

`DefaultDownloadService` получает `game directory` через `DirectoryProvider`, строит целевой путь для каждого файла
относительно `game directory` и передает `url` и `targetPath` в `FileDownloader`

### 4.Загрузка отдельного файла

`FileDownloader` загружает файл во временный файл после успешной загрузки перемещает его в целевой `targetPath`

При ошибке временный файл удаляется, а ошибка передается вызывающему коду

### 5.Повторная проверка

После успешного `DOWNLOAD_FILES` `LauncherEngine` повторно запускает `VERIFY_FILES`

Повторная проверка определяет, действительно ли локальное состояние соответствует ожидаемому состоянию

Если повторный `VerificationPlan` валиден, `LauncherEngine` переходит в `RUNNING`

Если проверка завершается ошибкой, или план остается невалидным, `LauncherEngine` переходит в `FAILED`

---

## Текущие ограничения

- `DownloadFilesTask` не получает потоковый прогресс от `DownloadService`
- `DownloadProgressChangedEvent` публикуется один раз после успешного завершения загрузки и содержит
  полностью завершенный прогресс
- Проверка `checksum` не выполняется непосредственно `DownloadService` или `FileDownloader`
- Итоговая корректность загруженных файлов подтверждается `VERIFY_FILES`

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

`DownloadPlan` содержит только файлы, требующие восстановления

D-2

`DownloadFilesTask` не строит `DownloadPlan` самостоятельно

D-3

`DefaultDownloadService` не принимает решений о составе загрузки

D-4

`FileDownloader` отвечает только за загрузку одного фалйа в указанный targetPath

D-5

Успешное завершение `DOWNLOAD_FILES` не является подтверждением корректности локального состояния

Корректность подтверждается повторным VERIFY_FILES

D-6

`DownloadFilesTask` не знает конкретных получателей событий. События публикуются через `EventBus`

D-7

Публикация `DownloadCompletedEvent` разрешена только после успешного завершения загрузки

---

## Связанные документы

- `download-events.md` - контракт событий жизненного цикла загрузки
- `verification-flow.md` - поток проверки файлов
- `operation-model.md` - общая модель выполнения `Operation`