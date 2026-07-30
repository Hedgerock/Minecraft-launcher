# LaunchOperation Lifecycle

## Цель

Описать контракт жизненного цикла LaunchOperation и правила использования lifecycle hooks

## Главный принцип

LaunchOperation является lifecycle boundary

Внешний код не должен получать случайные исключения из lifecycle hooks

Ошибки жизненного цикла преобразуются в OperationResult.failure(...), после чего публикуется
финальное событие 

## Общий порядок выполнения

```text
OperationStartedEvent
-> beforeExecute()
-> createTask()
-> executeTasks(...)
-> afterExecute(...)
-> finalizeOperation(...)
-> OperationCompletedEvent / OperationFailedEvent
-> OperationResult
```
---

## beforeExecute()

Подготовительный этап перед созданием задача

### Разрешено

- Проверить входной LaunchContext
- Подготовить легкие runtime-зависимости
- Выполнить предварительные проверки

### Запрещено

- Выполнять длительные операции
- Менять внешнее состояние системы
- Напрямую управлять UI

Ошибки:

- Исключения преобразуются в OperationResult.failure(...)

---

## createTask()

Создает список LauncherTask для выполнения

### Разрешено

- Построить список задач
- Выбрать задачи на основе LaunchContext
- Вернуть пустой список, если операция не требует задач

### Запрещено 

- Выполнять сами задачи
- Менять состояние UI
- Запускать сетевые или файловые операции вместо LauncherTask

### Ошибки

- Исключения преобразуются в OperationResult.failure(...)

---

## executeTasks(...)

Выполняет задачи через ExecutionStrategy

### Ответственность

- Порядок выполнения определяется ExecutionStrategy
- LaunchOperation не знает деталей исполнения задач
- Результат выполнения возвращается как OperationResult

### Ошибки

- Ошибки задач должны возвращаться как OperationResult.failure(...)
- Исключения стратегии преобразуются в OperationResult.failure(...)

---

## afterExecute(...)

Этап после выполнения задач

### Разрешено

- Проанализировать OperationResult
- Подготовить данные для финализации
- Выполнить легкие проверки результата

### Запрещено

- Подменять результат операции неявным side effect
- Публиковать финальное событие вручную
- Напрямую управлять UI

### Ошибки

- Исключение преобразуется в OperationResult.failure(...)

---

## finalizeOperation(...)

Завершающий этап жизненного цикла

### Разрешено

- Освободить ресурсы
- Закрыть временные состояния
- Сохранить отчет
- Выполнить cleanup

### Запрещено

- Публиковать OperationCompletedEvent/OperationFailedEvent вручную
- Менять UI напрямую
- Бросать исключения как способ управления нормальной flow

### Ошибки

- Исключение преобразуется в OperationResult.failure(...)
- После ошибки финализации публикуется OperationFailedEvent




























