# Download Flow

## Цель

Описать процесс восстановления локального состояния игры путем загрузки отсутствующих или
поврежденных ресурсов

## Предусловия

- VerificationOperation завершена
- Получен VerificationReport
- OperationContext существует
- Manifest содержит актуальное описание ресурсов

## Последовательность

LauncherEngine
|
▼
OperationManager
|
▼
DownloadOperation
|
▼
DownloadService
|
▼
Manifest
|
▼
Determine missing resources
|
▼
DownloadQueue
|
▼
Downloader
|
▼
FileStorage
|
▼
VerificationReport update
|
▼
TelemetryReport
|
▼
Result

## Этапы

### 1.Анализ VerificationReport

DownloadOperation получает список файлов, требующих восстановления

### 2.Построение DownloadQueue

Формируется очередь файлов

### 3.Загрузка (для каждого элемента очереди)

Download
|
▼
Save
|
▼
Publish Progress

### 4.Завершение

Создается:
- DownloadReport
- TelemetryReport

Публикуется:
- DownloadCompleted

## Компоненты

- OperationManager
- DownloadOperation
- DownloadService
- Downloader
- DownloadQueue
- FileStorage
- DownloadReport
- TelemetryReport

## Результат

Если загрузка успешна

Operation completed
|
▼
PreparingGame

Если произошла ошибка

Result.failure(...)
|
▼
LauncherStateMachine
|
▼
FAILED

## Инварианты

D-1
DownloadOperation изменяет только файл, требующие восстановления

D-2
DownloadQueue является неизменяемой после построения

D-3
Downloader не принимает решений о составе очереди

D-4
Telemetry не влияет на выполнение загрузки

D-5
DownloadOperation завершается только после обработки всех элементов очереди




























