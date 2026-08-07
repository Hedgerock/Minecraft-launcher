# Verification Events

## Обзор

Verification публикует события исключительно для уведомления других подсистем о ходе
выполнения проверки

Verification не знает, кто является подписчиком событий

---

## VerificationStarted

### Источник

VerificationOperation

### Подписчики

- Launcher UI
- Telemetry
- Logger

### Гарантии

- Публикуется один раз для каждой VerificationOperation
- Всегда является первым событием жизненного цикла проверки

### Примечания

После публикации начинается последовательная обработка LauncherTask

---

## VerificationProgressChanged

### Источник

VerificationOperation

### Подписчики

- Launcher UI
- Telemetry

### Данные события

- OperationId
- ProcessedFiles
- TotalFiles
- CurrentFile (optional

### Гарантии

- Может публиковаться произвольное количество раз
- Процент выполнения никогда не уменьшается

---

## VerificationCompleted

### Подписчики

- Launcher UI
- Logger
- Telemetry

### Данные события

- OperationId
- VerificationPlan
- FinishedAt

### Гарантии

- Публикуется не более одного раза
- Завершает жизненный цикл VerificationOperation