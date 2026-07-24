# Download Events

## Overview

DownloadOperation публикует события, описывающие жизненный цикл загрузки файлов

События являются неизменяемыми фактами и не используются для управления выполнения операций

Ошибки загрузки передаются через Result и обрабатываются OperationManager

## DownloadStarted

### Publisher

DownloadOperation

### Subscribers

- Launcher UI
- Telemetry

### Payload

- OperationId
- TotalFiles
- TotalBytes
- StartedAt

### Guarantees

Публикуется ровно один раз для каждой DownloadOperation

---

## DownloadProgressChanged

### Publisher

DownloadOperation

### Subscribers

- Launcher UI
- Telemetry

### Payload

- DownloadFiles
- TotalFiles
- DownloadBytes
- TotalBytes

### Guarantees

Публикуется произвольное количество раз

Значения прогресса никогда не уменьшается

---

## DownloadCompleted

### Publisher

Download Operation

### Subscribers

- Launcher UI
- Telemetry
- Logger

### Payload

- DownloadReport
- FinishedAt

### Guarantees

Публикуется не более одного раза

Является завершающим событием жизненного цикла DownloadOperation

---

## Notes

DownloadOperation не публикует события ошибок

Любые ошибки возвращаются через Result.failure(...)

Инфраструктурные проблемы (например, потеря сети) могут публиковаться отдельно
инфраструктурным слоем и не являются частью жизненного цикла DownloadOperation











