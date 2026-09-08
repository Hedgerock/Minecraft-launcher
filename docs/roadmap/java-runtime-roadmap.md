[← Назад к общему пути](general-roadmap.md)

# Путь развития Java runtime

## Текущий план

- Развить configured Java override как следующий Java runtime milestone после `v0.5.0-java-runtime-foundation`
- Сначала определить boundary configured override, затем переходить к модели и production wiring
- Не вводить Java installation discovery и Java version management без отдельного подтвержденного сценария

---

## Milestone — Java executable runtime flow

Java executable runtime flow доведен до минимального production-ready состояния

### Закрыто

- Добавлен `JavaRuntimeSelector`
- Добавлена модель `JavaExecutableReference`
- Добавлена интерпретация command name и explicit filesystem path
- Добавлен PATH-oriented command resolution
- Добавлен readiness check для resolved explicit filesystem path
- Production assembly переведен на default resolver и checker
- Некорректные entries из `PATH` игнорируются provider-ом
- Некорректный explicit filesystem path возвращается как readiness failure

Подробности зафиксированы в [ретроспективе Java executable runtime flow](../retrospective/2026-09-java-executable-runtime-flow.md)

---

## Отложено

- Выбор Java version
- Проверка совместимости Java version с manifest metadata
- Поиск Java installations вне `PATH`
- Автоматическая установка Java
- Fallback policy для отсутствующего Java executable
- Более структурированная модель ошибок Java runtime
- Сохранение исходного `cause` в Java executable runtime exceptions
- Диагностика прав доступа к Java executable
- Отдельная модель Java installation
- Интеграция Java runtime selection с будущим profile/version flow

---

## Активное направление

- Configured Java override

---

## Возможные следующие направления

- Java version requirements
- Java runtime failure model
- Java installation discovery
- Java process lifecycle diagnostics

---

## Почему configured Java override первым

Configured Java override является ближайшим развитием после Java executable runtime foundation

Он позволяет явно передать Java executable через configuration, не вводя автоматический поиск Java
installations

Этот шаг проверяет текущие границы

- `JavaRuntimeSelector`
- `JavaExecutableReferenceResolver`
- `JavaCommandPathResolver`
- `JavaExecutableReadinessChecker`
- `GameLaunchPlanBuilder`

Java installation discovery и Java version requirements остаются отложенными, потому что требуют более сложной модели
runtime identity, version parsing и fallback policy

---

## Правило развития

Java runtime flow должен развиваться через отдельные маленькие границы ответственности

`GameService` не должен выбирать Java runtime, выполнять PATH lookup или проверять Java executable

`GameLaunchCommandBuilder` не должен выполнять runtime lookup, PATH resolution или filesystem readiness check
