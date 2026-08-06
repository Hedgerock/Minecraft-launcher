# Поток загрузки

## Цель

Описать процесс восстановления локального состояния игры путем загрузки отсутствующих или
поврежденных ресурсов

## Текущий статус

На текущем этапе `LauncherEngine` строит `DownloadPlan`, но еще не запускает `DOWNLOAD_FILES` во время
основного `launch-flow`

`DefaultFileDownloader` пока завершает выполнение исключением, потому что реальная загрузка файлов еще не реализована

## Предусловия

- `VERIFY_FILES` завершена успешно
- В `LaunchContext` сохранен `VerificationPlan`
- Если `VerificationPlan` содржит файлы, требующие восстановления, строится `DownloadPlan`
- `DownloadPlan` сохранен в `LaunchContext`

## Последовательность

```text
LauncherEngine
    -> OperationManager
        -> BUILD_DOWNLOAD_PLAN
            -> BuildDownloadPlanTask
                -> DownloadPlanBuilder
                    -> DownloadPlan
                 
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

`DOWNLOAD_FILES operation` уже существует, но `LauncherEngine` еще не запускает ее в основном потоке исполнения

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