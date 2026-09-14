[← Назад к списку решений](README.md)

# ADR-0045: Определить границу Java process lifecycle diagnostics

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(app): add java process diagnostic model`
> `feat(app): attach java process diagnostics to version command result`
> `feat(app): use java process diagnostic for failed version detection`
> `feat(app): diagnose empty java version process output`
> `feat(app): diagnose unparsable java version output`
> `feat(app): diagnose java version process start failure`

---

## Контекст

После `v0.6.0-java-runtime-compatibility` Java runtime compatibility flow доведен до production-ready состояния

Launcher уже умеет выбрать Java executable, разрешить command name, проверить filesystem readiness, определить
фактическую Java runtime version и сравнить ее с `JavaVersionRequirement`

Определение Java runtime version выполняется через adapter-level запуск Java process и parsing process output

На момент принятия решения ошибки этого процесса остаются минимально выраженными

Launcher может получить failure при запуске process, non-zero exit code, пустой output или output, который невозможно
распарсить

Если развивать эти сценарии внутри `JavaRuntimeCompatibilityChecker`, compatibility boundary начнет отвечать за process
lifecycle

Если развивать эти сценарии внутри `GameLaunchPlanBuilder`, launch planning начнет знать детали выполнения external
process

Если сразу поднимать structured operation diagnostics, решение станет шире Java runtime version detection

Поэтому перед расширением диагностики нужно определить отдельную границу Java process lifecycle diagnostics

---

## Решение

Java process lifecycle diagnostics должна остаться частью adapter-level Java runtime version detection flow

Эта граница должна описывать ошибки, возникающие при запуске process для определения Java runtime version

Минимальные диагностируемые сценарии

- Java process не удалось запустить
- Java process завершился с non-zero exit code
- Java process вернул пустой output
- Java process вернул output, который невозможно распарсить как Java runtime version

`JavaRuntimeVersionDetector` может использовать эту диагностику, но не должен превращаться в Java installation discovery
или fallback policy

`JavaRuntimeCompatibilityChecker` не должен знать о process lifecycle и должен работать только с уже определенной
`JavaRuntimeVersion`

`GameLaunchPlanBuilder` должен получать failure от detector как runtime failure, но не должен интерпретировать process
details

Граница выглядит так

```text
JavaExecutableReference
    -> JavaRuntimeVersionDetector
        -> Java process execution
        -> Java process lifecycle diagnostics
        -> JavaRuntimeVersion
    -> JavaRuntimeCompatibilityChecker
```

---

## Рассмотренные варианты

### Добавить диагностику в `JavaRuntimeCompatibilityChecker`

Вариант отклонен

Compatibility checker должен сравнивать detected `JavaRuntimeVersion` с `JavaVersionRequirement`

Если добавить туда process lifecycle diagnostics, checker начнет отвечать за получение версии Java, а не только за
compatibility decision

### Добавить диагностику в `GameLaunchPlanBuilder`

Вариант отклонен

`GameLaunchPlanBuilder` должен orchestrate launch planning boundaries, но не знать details external process execution

### Сразу добавить structured operation failure diagnostics

Вариант отложен

Structured operation diagnostics полезны для всего launcher lifecycle, но являются более широкой темой

Java process lifecycle diagnostics можно развивать локально внутри Java runtime detection flow без изменения общей
модели `OperationResult`

---

## Последствия

Java runtime version detection получает более понятную adapter-level диагностику

Compatibility boundary остается pure policy

Launch planning не получает process-specific responsibilities

Ошибки Java process lifecycle могут быть классифицированы ближе к месту возникновения

Это не решает generic operation failure diagnostics

Это не добавляет Java installation discovery, automatic provisioning или fallback policy

Если в будущем structured operation diagnostics будет реализована, Java process diagnostics сможет стать одним из
источников structured failure information

---

## Не входит в решение

- Java installation discovery
- Automatic Java provisioning
- Fallback policy при несовместимой Java version
- Fallback policy при failed Java process execution
- UI отображение Java process diagnostics
- Generic operation failure context
- Изменение `OperationResult`
- Изменение `JavaRuntimeCompatibilityChecker`
- Выбор альтернативной Java installation

---

## Связанные решения

- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0030: Определить границу проверки Java executable](ADR-0030-java-executable-readiness-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
- [ADR-0032: Определить границу PATH resolution для Java command name](ADR-0032-java-command-path-resolution-boundary.md)
- [ADR-0039: Определить модель ошибок Java runtime](ADR-0039-java-runtime-failure-model.md)
- [ADR-0040: Определить границу operation failure diagnostics](ADR-0040-operation-failure-diagnostics-boundary.md)
- [ADR-0043: Определить границу проверки совместимости Java version](ADR-0043-java-version-compatibility-boundary.md)
- [ADR-0044: Зафиксировать границу определения Java runtime version](ADR-0044-java-runtime-version-detection-boundary.md)
