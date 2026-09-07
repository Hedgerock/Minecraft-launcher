[← Назад к списку решений](README.md)

# ADR-0035: Определить границу реализации Java executable readiness checker

## Статус

Accepted

> Примечание: решение реализовано в итерации `refactor(app): move java executable readiness adapter out of core`

---

## Контекст

После ADR-0030 launcher получил отдельный контракт проверки готовности выбранного
Java executable — `JavaExecutableReadinessChecker`

После ADR-0031, ADR-0032 и ADR-0033 Java executable flow стал различать command name и explicit filesystem path

Command name сначала проходит через `JavaCommandPathResolver`

Explicit filesystem path передается в readiness check

На момент принятия решения `JavaExecutableReadinessChecker` находится в `launcher-core`, а production implementation
`DefaultJavaExecutableReadinessChecker` читает состояние локальной файловой системы

- Проверяет существование файла
- Проверяет, что path указывает на regular file
- Преобразует raw path value в filesystem path

После ADR-0034 `launcher-core` должен владеть orchestration boundary, ports, plan models и pure policies

Concrete adapters, которые читают filesystem, environment variables, system properties, network или process state,
не должны оставаться внутри `launcher-core`

Из-за этого `DefaultJavaExecutableReadinessChecker` начинает размывать границу `launcher-core`

---

## Решение

`JavaExecutableReadinessChecker` остается contract-ом `launcher-core`

Этот contract нужен orchestration flow, потому что `GameLaunchPlanBuilder` должен иметь возможность проверить
готовность выбранного Java executable до построения `GameLaunchPlan`

`DefaultJavaExecutableReadinessChecker` не должен принадлежать `launcher-core`

Production implementation readiness check должна находиться вне `launcher-core`, потому что она работает с локальной
файловой системой

На момент принятия решения `DefaultJavaExecutableReadinessChecker` должен быть перенесен в `launcher-app`

`launcher-app` является composition root и temporary home для production adapters, для которых еще не выделен
отдельный adapter module

Граница выглядит так

```text
launcher-core
    -> JavaExecutableReadinessChecker
    -> NoOpJavaExecutableReadinessChecker
    -> GameLaunchPlanBuilder

launcher-app
    -> DefaultJavaExecutableReadinessChecker
    -> production wiring
```

`NoOpJavaExecutableReadinessChecker` может оставаться в `launcher-core`

Он не читает filesystem state, не является concrete system adapter и полезен для тестовых или изолированных сценариев

---

## Последствия

`launcher-core` перестает владеть production filesystem implementation для Java executable readiness

Граница между orchestration contract и filesystem adapter становится яснее

`GameLaunchPlanBuilder` продолжает зависеть от `JavaExecutableReadinessChecker`, а не от конкретной реализации

`launcher-app` продолжает собирать production wiring и передавать concrete readiness checker в core flow

Архитектурные тесты должны дополнительно проверять, что `DefaultJavaExecutableReadinessChecker`
не возвращается в `launcher-core`

Создание отдельного runtime adapter module остается возможным будущим шагом, но не требуется для текущего размера
проекта на момент принятия решения

---

## Не входит в решение

- Создание нового `launcher-runtime` module
- Перенос `JavaExecutableReadinessChecker` contract из `launcher-core`
- Перенос `NoOpJavaExecutableReadinessChecker` из `launcher-core`
- Изменение behavior readiness check
- Изменение `GameLaunchPlanBuilder`
- Изменение `JavaCommandPathResolver`
- Изменение Java runtime selection flow
- Проверка Java version compatibility
- Поиск Java installations вне `PATH`
- Автоматическая установка Java

---

## Связанные решения

- [ADR-0013: Порты оркестрации принадлежат launcher-core](ADR-0013-core-owns-orchestration-ports.md)
- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0030: Определить границу проверки Java executable](ADR-0030-java-executable-readiness-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
- [ADR-0032: Определить границу PATH resolution для Java command name](ADR-0032-java-command-path-resolution-boundary.md)
- [ADR-0033: Определить интерпретацию Java executable reference из manifest metadata](ADR-0033-manifest-java-executable-reference-interpretation.md)
- [ADR-0034: Определить классификацию границ launcher-core](ADR-0034-core-boundary-classification.md)
