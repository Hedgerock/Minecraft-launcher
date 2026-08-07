# Поток загрузки

## Цель

Описать процесс восстановления локального состояния игры путем загрузки отсутствующих или
поврежденных ресурсов

## Текущий статус

`LauncherEngine` запускает `DOWNLOAD_FILES` после успешного построения `DownloadPlan`

После `DOWNLOAD_FILES` `LauncherEngine` повторно запускает `VERIFY_FILES`, потому что загрузка файла
сама по себе не доказывает корректность локального состояния

## Предусловия

- `VERIFY_FILES` завершена успешно
- В `LaunchContext` сохранен `VerificationPlan`
- Если `VerificationPlan` содржит файлы, требующие восстановления, строится `DownloadPlan`
- `DownloadPlan` сохранен в `LaunchContext`

## Последовательность

```text
LauncherEngine
    -> VERIFY_FILES
        -> VerificationPlan
    -> BUILD_DOWNLOAD_PLAN
        -> DownloadPlan
    -> DOWNLOAD_FILES
        -> DownloadService
            -> DefaultDownloadService
                -> FileDownloader
    -> VERIFY_FILES
         -> VerificationPlan
                 
DOWNLOAD_FILES
    -> DownloadFilesOperation
        -> DownloadFilesTask
            -> DownloadService
                -> DefaultDownloadService
                    -> FileDownloader
```

## Этапы

### 1.Анализ `VerificationPlan`

`DownloadPlanBuilder` получает `VerificationPlan` и выбирает файлы со статусами `MISSING`, `OUTDATED` и `CORRUPTED`

Файлы со статусом `VALID` не попадают в `DownloadPlan`

### 2.Построение `DownloadPlan`

`BuildDownloadPlanTask` сохраняет построенный `DownloadPlan` в `LaunchContext`

### 3.Выполнение загрузки

`DownloadFilesTask` получает `DownloadPlan` из `LaunchContext` и передает его в `DownloadService`

`DefaultDownloadServic` строит целевой путь файла относительно game directory и 
передает url и targetPath в `FileDownloader`

### 4.Текущие ограничения

`DOWNLOAD_FILES operation` запускается из `LauncherEngine` после успешного построения `DownloadPlan`

`DefaultFileDownloader` пока не выполняет реальную загрузку файлов

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

## Инварианты

D-1

`DownloadPlan` содержит только файлы, требующие восстановления

D-2

`DownloadFilesTask` не строит `DownloadPlan` самостоятельно

D-3

`DefaultDownloadSerivce` не принимает решений о составе загрузки

D-4

`FileDownloader` отвечает только за загрузку одного фалйа в указанный targetPath

D-5

Реальная загрузка файлов должна быть подключена до запуска `DOWNLOAD_FILES` из `LauncherEngine`