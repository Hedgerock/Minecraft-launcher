# Verification Events

## Overview

Verification публикует события исключительно для уведомления других подсистем о ходе
выполнения проверки

Verification не знает, кто является подписчиком событий

---

## VerificationStarted

### Publisher

VerificationOperation

### Subscribers

- Launcher UI
- Telemetry
- Logger

### Guarantees

- Публикуется один раз для каждой VerificationOperation
- Всегда является первым событием жизненного цикла проверки

### Notes

После публикации начинается последовательная обработка LauncherTask

---

## VerificationProgressChanged

### Publisher

VerificationOperation

### Subscribers

- Launcher UI
- Telemetry

### Payload

- OperationId
- ProcessedFiles
- TotalFiles
- CurrentFile (optional

### Guarantees

- Может публиковаться произвольное количество раз
- Процент выполнения никогда не уменьшается

---

## VerificationCompleted

### Subscribers

- Launcher UI
- Logger
- Telemetry

### Payload

- OperationId
- VerificationReport
- FinishedAt

### Guarantees

- Публикуется не более одного раза
- Завершает жизненный цикл VerificationOperation