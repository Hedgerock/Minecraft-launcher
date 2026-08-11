# ADR-0001 LaunchContext Responsibility

## Статус

Accepted

> Историческая заметка: ADR фиксирует решение в контексте ранней архитектуры проекта
> После перехода с `TaskPipeline` на `OperationManager`, часть деталей реализации изменилась, но
> архитектурная мотивация документа сохраняется
> 
> Актуальная модель `LaunchContext` описана в [operation-model.md](../architecture/operation/operation-model.md)
> Актуальное описание жизненного цикла Launcher см. в [launcher-lifecycle.md](../architecture/launcher/launcher-lifecycle.md)

---

## Контекст

LaunchContext используется при выполнении сценария запуска лаунчера и передается между задачами
TaskPipeline

> Update: после перехода на operation-модель TaskPipeline больше не является основным механизмом
> выполнения. LaunchContext передается в OperationManager, затем используется внутри LaunchOperation,
> ExecutionStrategy и LauncherTask

По мере развития проекта существует риск превращения LaunchContext в универсальный контейнер,
в который начинают помещать любые данные приложения

Такое решение приводит к высокой связанности компонентов и нарушению Single Responsibility
Principle

Необходимо определить четкие границы ответственности LaunchContext

---

## Принципы:

1. LaunchContext существует только в рамках одного запуска Operation
2. Каждый LauncherTask может читать и изменять только те данные LaunchContext, которые относятся
к его ответственности
3. Все объекты, помещенные в LaunchContext, должны иметь понятного владельца

---

## Последствия

+ Уменьшается связанность
+ LaunchContext остается небольшим и понятным
+ Упрощается тестирование задач
+ Легче добавить новые этапы Operation

- Потребуется больше небольших моделей
- Иногда придется создавать отдельный объект вместо добавления нового поля в LaunchContext