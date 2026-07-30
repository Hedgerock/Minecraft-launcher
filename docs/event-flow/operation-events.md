# Operation Events

## Overview

LaunchOperation публикует события исключительно для уведомления других подсистем об изменении
собственного жизненного цикла

LaunchOperation не знает, кто является подписчиком событий

---

## OperationStartedEvent

### Publisher

LaunchOperation

### Subscribers

- Launcher UI
- Telemetry
- Logger

### Payload

- OperationType

### Guarantees

- Публикуется один раз для каждой LaunchOperation
- Всегда является первым событием жизненного цикла Operation
- Публикуется до создания LauncherTask

### Notes

После публикации начинается выполнение жизненного цикла Operation

---

## OperationCompletedEvent

### Publisher

LaunchOperation

### Subscribers

- Launcher UI
- Logger
- Telemetry

### Payload

- OperationType

### Guarantees

- Публикуется только после успешного завершения Operation
- Публикуется не более одного раза
- Завершает жизненный цикл успешной Operation

### Notes

К моменту публикации:

- Все LauncherTask завершены
- Выполнен afterExecute(...)
- Выполнен finalizeOperation(...)

---

## OperationFailedEvent

### Publisher

LaunchOperation

### Subscribers

- Launcher UI
- Logger
- Telemetry

### Payload

- OperationType
- Error message

### Guarantees

- Публикуется только при неуспешном завершении Operation
- Публикуется не более одного раза
- Завершает жизненный цикл неуспешной Operation
- Публикуется также в случае ошибки внутри finalizeOperation(...)

### Notes

Operation считается завершенной независимо от причины возникновения ошибки

Перед публикацией всегда выполняется попытка finalizeOperation(...)

Error message формируется на основе OperationResult

Если ошибка возникла как исключение без сообщения, используется имя класса исключения

Если finalizeOperation(...) выбрасывает исключение, Operation считается завершенной с ошибкой

Ошибка финализации преобразуется в OperationResult.failure(...), после чего публикуется OperationFailedEvent