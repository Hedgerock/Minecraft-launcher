# Download Events

## Статус

Документ описывает планируемы специализированные события загрузки

На текущем этапе `DOWNLOAD_FILES` публикует только общие события жизненного цикла `Operation`

- `OperationStartedEvent`
- `OperationCompletedEvent`
- `OperationFailedEvent`

Специализированные события `DownloadStarted`, `DownloadProgressChanged` и `DownloadCompleted` еще не реализованы

## Обзор

Будущая специализированная `download operation` будет публиковать события, описывающие жизненный цикл загрузки файлов

События являются неизменяемыми фактами и не используются для управления выполнения операций

Ошибки загрузки передаются через Result и обрабатываются `OperationManager`

## DownloadStarted

### Источник

`DownloadOperation`

### Подписчики

- Launcher UI
- Telemetry

### Данные события

- `OperationId`
- `TotalFiles`
- `TotalBytes`
- `StartedAt`

### Гарантии

Публикуется ровно один раз для каждой DownloadOperation

---

## DownloadProgressChanged

### Источник

DownloadOperation

### Подписчики

- `Launcher UI`
- `Telemetry`

### Данные события

- `DownloadFiles`
- `TotalFiles`
- `DownloadBytes`
- `TotalBytes`

### Гарантии

Публикуется произвольное количество раз

Значения прогресса никогда не уменьшается

---

## DownloadCompleted

### Источник

`Download Operation`

### Подписчики

- `Launcher UI`
- `Telemetry`
- `Logger`

### Данные события

- Future `DownloadReport`
- `FinishedAt`

### Гарантии

Публикуется не более одного раза

Является завершающим событием жизненного цикла `DownloadOperation`

---

## Примечания

`DownloadOperation` не публикует события ошибок

Любые ошибки возвращаются через `Result.failure(...)`

Инфраструктурные проблемы (например, потеря сети) могут публиковаться отдельно
инфраструктурным слоем и не являются частью жизненного цикла `DownloadOperation`











