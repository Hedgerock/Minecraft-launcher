# Ретроспектива: Java runtime compatibility flow

## Контекст

Данный milestone развил Java runtime foundation после `v0.5.0-java-runtime-foundation` в сторону проверки совместимости
выбранного Java runtime с требованиями manifest metadata

До начала этапа launcher уже умел выбрать Java executable, интерпретировать command name и explicit filesystem path,
выполнить PATH-oriented resolution, проверить filesystem readiness и передать resolved Java executable в launch command

Однако выбранный Java executable еще не проверялся на соответствие требованиям Java version

`LaunchInfo` содержал Java executable metadata, но не выражал минимальное требование к версии Java

Launcher также еще не определял фактическую версию выбранного runtime и не останавливал launch planning при несовместимости

Целью milestone было довести Java runtime compatibility flow до production-ready состояния без преждевременного
расширения в Java installation discovery, fallback policy или automatic provisioning

---

## Что было сделано

- Добавлен configured Java executable override через `LauncherConfiguration`
- Добавлена модель `JavaRuntimeSelectionRequest`
- `ManifestJavaRuntimeSelector` начал выбирать configured override перед manifest metadata
- Зафиксированы правила planning builders
- Добавлена минимальная Java runtime failure model
- Добавлен общий базовый exception для classified Java runtime failures
- Добавлена модель `JavaVersionRequirement`
- Manifest JSON стал источником `JavaVersionRequirement` для `LaunchInfo`
- Зафиксирована Java runtime compatibility boundary
- Compatibility boundary подключена в launch planning
- Добавлена модель `JavaRuntimeVersion`
- Зафиксирована Java runtime version detection boundary
- Detection boundary подключена в launch planning
- `JavaRuntimeCompatibilityRequest` переведен на detected `JavaRuntimeVersion`
- Добавлен adapter-level parser вывода Java runtime version
- Добавлен `DefaultJavaRuntimeVersionDetector`
- Application assembly переведен на `DefaultJavaRuntimeVersionDetector`
- Добавлен `DefaultJavaRuntimeCompatibilityChecker`
- Application assembly переведен на `DefaultJavaRuntimeCompatibilityChecker`
- Усилено test coverage compatibility failure path на policy, builder и operation уровнях
- Обновлены roadmap, module boundaries, glossary, lifecycle documentation и CHANGELOG

---

## Что подтвердилось

Java runtime flow лучше развивать как цепочку маленьких runtime boundaries

`JavaRuntimeSelector` отвечает за выбор Java executable reference, но не выполняет проверку версии Java

`JavaRuntimeVersionDetector` отвечает за определение фактической версии выбранного runtime

`JavaRuntimeCompatibilityChecker` отвечает только за сравнение detected runtime version с `JavaVersionRequirement`

`GameLaunchCommandBuilder` продолжает строить command из уже подготовленных данных

`GameService` не получает ответственность за runtime selection, version detection или compatibility check

Также подтвердилось, что Java installation discovery и fallback policy не должны появляться автоматически
после добавления compatibility check

Compatibility flow должен уметь остановить launch planning при несовместимости, но не должен самостоятельно искать
альтернативную Java installation

---

## Что было улучшено архитектурно

Java version requirement стал явной частью manifest launch metadata

Java runtime compatibility перестала быть неявным будущим поведением и получила отдельную runtime boundary

Flow стал выражен явно

```text
LaunchInfo.javaExecutable
    -> JavaRuntimeSelector
    -> JavaCommandPathResolver
    -> JavaExecutableReadinessChecker
    -> JavaRuntimeVersionDetector
    -> JavaRuntimeCompatibilityChecker
    -> GameLaunchCommandBuilder
    -> GameLaunchPlan
```

`DefaultJavaRuntimeCompatibilityChecker` остался в `launcher-core`, потому что это pure policy без доступа к filesystem,
environment variables, system properties или process state

`DefaultJavaRuntimeVersionDetector` остался в `launcher-app`, потому что определение версии runtime связано с запуском
external process и parsing output

`GameLaunchPlanBuilder` стал сложнее, но его сложность отражает реальную последовательность подготовки game launch plan

Тестовое покрытие стало проверять не только сам comparison policy, но и short-circuit behavior на уровне builder и
operation

---

## Что осталось отложенным

- Java installation discovery
- Поиск Java installations вне `PATH`
- Automatic Java provisioning
- Fallback policy при несовместимой Java version
- UI выбора Java version
- Java version management
- Structured operation failure diagnostics
- Generic operation failure context
- Сохранение исходного `cause` в Java runtime exceptions
- Java process lifecycle diagnostics при Java runtime version detection

---

## Технический долг

Технический долг остается контролируемым

Основные ограничения compatibility flow зафиксированы явно и не блокируют текущий launch lifecycle

`NoOpJavaRuntimeCompatibilityChecker` и `NoOpJavaRuntimeVersionDetector` остаются полезными для тестов и изолированных
сценариев, но production assembly уже использует реальные реализации

Operation layer по-прежнему получает текстовое сообщение ошибки, а не structured failure information

Это ограничение не блокирует текущий compatibility flow, но станет важнее перед UI, recovery behavior или fallback policy

Java runtime process diagnostics пока остается минимальной

Если detection errors начнут влиять на пользовательские сценарии, следующим шагом должна стать отдельная диагностика
Java process lifecycle, а не расширение compatibility checker

---

## Главный вывод

Milestone подтвердил, что Java runtime compatibility можно довести до production-ready состояния без преждевременного
перехода к Java installation discovery или fallback policy

Launcher теперь умеет выбрать Java executable, определить фактическую Java runtime version, сравнить ее с requirement из
manifest metadata и остановить launch planning при несовместимости

```text
selected Java executable
    -> detected Java runtime version
    -> Java version requirement
    -> compatibility check
    -> explicit launch planning failure
```

Следующий runtime шаг должен начинаться с выбора отдельного candidate

Наиболее естественным следующим направлением выглядит Java process lifecycle diagnostics, потому что оно усиливает
существующий detection flow и не требует преждевременного discovery или fallback policy
