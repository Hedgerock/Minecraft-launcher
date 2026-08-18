# События операций

## Обзор

LaunchOperation публикует события исключительно для уведомления других подсистем об изменении
собственного жизненного цикла

LaunchOperation не знает, кто является подписчиком событий

---

## OperationStartedEvent

### Источник

LaunchOperation

### Подписчики

- Launcher UI
- Telemetry
- Logger

### Данные события

- OperationType

### Гарантии

- Публикуется один раз для каждой LaunchOperation
- Всегда является первым событием жизненного цикла Operation
- Публикуется до создания LauncherTask

### Примечания

После публикации начинается выполнение жизненного цикла Operation

---

## OperationCompletedEvent

### Источник

LaunchOperation

### Подписчики

- Launcher UI
- Logger
- Telemetry

### Данные события

- OperationType

### Гарантии

- Публикуется только после успешного завершения Operation
- Публикуется не более одного раза
- Завершает жизненный цикл успешной Operation

### Примечания

К моменту публикации:

- Все LauncherTask завершены
- Выполнен afterExecute(...)
- Выполнен finalizeOperation(...)

---

## OperationFailedEvent

### Источник

LaunchOperation

### Подписчики

- Launcher UI
- Logger
- Telemetry

### Данные события

- OperationType
- Сообщение об ошибке (errorMessage)

### Гарантии

- Публикуется только при неуспешном завершении Operation
- Публикуется не более одного раза
- Завершает жизненный цикл неуспешной Operation
- Публикуется также в случае ошибки внутри finalizeOperation(...)

### Примечания

Operation считается завершенной независимо от причины возникновения ошибки

Перед публикацией всегда выполняется попытка finalizeOperation(...)

Error message формируется на основе OperationResult

Если ошибка возникла как исключение без сообщения, используется имя класса исключения

Если finalizeOperation(...) выбрасывает исключение, Operation считается завершенной с ошибкой

Ошибка финализации преобразуется в OperationResult.failure(...), после чего публикуется OperationFailedEvent