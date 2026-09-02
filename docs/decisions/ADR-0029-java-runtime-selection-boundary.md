# ADR-0029: Определить границу выбора Java runtime

## Статус

Accepted

---

## Контекст

После завершения library/native flow launcher умеет строить launch command на основе manifest metadata,
classpath и директории распакованных natives

Следующий этап развития launch runtime связан с выбором Java runtime

На текущем этапе `LaunchInfo` содержит `javaExecutable`, а `GameLaunchPlanBuilder` использует это значение
при построении `GameLaunchPlan`

Однако дальнейшее развитие потребует различать несколько ответственностей

- Чтение Java metadata из manifest
- Выбор Java executable для конкретного запуска
- Проверку доступности Java executable
- Построение launch command
- Запуск процесса игры

Если оставить все эти ответственности внутри `GameLaunchPlanBuilder` или `GameService`,
launch flow начнет смешивать выбор runtime, filesystem readiness и запуск процесса

---

## Решение

Выбор Java runtime должен быть выделен в отдельную ответственность launch/runtime слоя

`GameLaunchPlanBuilder` остается компонентом, который строит `GameLaunchPlan` из уже
подготовленных входных данных

`GameLaunchPlanBuilder` не должен

- Выбирать Java runtime
- Проверять существование Java executable
- Искать Java installation на машине
- Принимать решение о fallback Java

`GameService` остается компонентом, который запускает уже построенный `GameLaunchPlan`

`GameService` не должен

- Выбирать Java runtime
- Изменять launch command
- Подменять Java executable
- Выполнять runtime selection

Для выбора Java runtime должен быть введен отдельный contract, который получает launch metadata,
configuration и доступные Java installations, а возвращает выбранный Java executable или ошибку выбора

`JavaInstallation` остается моделью установленного Java runtime и не заменяет launch metadata из manifest

Минимальный будущий flow

```text
Manifest launch metadata
    -> Java runtime selection
        -> GameLaunchPlanBuilder
            -> GameLaunchPlan
                -> GameService
```

---

## Последствия

Граница между выбором runtime и запуском процесса становится явной

`GameLaunchPlanBuilder` сохраняет простую ответственность и остается легко тестируемым

`GameService` не превращается в runtime resolver

Появляется место для будущей проверки Java executable без усложнения process launch слоя

В будущем можно будет добавить поддержку

- Configured Java runtime
- Detected Java installations
- Java version requirements
- Fallback policy
- Readable failure для отсутствующего Java executable

---

## Не входит в решение

- Реализация Java runtime selector
- Поиск Java installations на машине
- Проверка версии Java
- Проверка существования Java executable
- Fallback на системную Java
- Изменение manifest JSON contract
- Изменение `GameService`
- Изменение `GameLaunchPlanBuilder`

---

## Связанные решения

- [ADR-0013: Порты оркестрации принадлежат launcher-core](ADR-0013-core-owns-orchestration-ports.md)
- [ADR-0023: Использовать RuntimeLibrarySelection.libraries как источник game classpath](ADR-0023-use-libraries-as-game-classpath-source.md)
- [ADR-0027: Определить передачу директории natives в launch arguments](ADR-0027-natives-directory-launch-argument.md)
