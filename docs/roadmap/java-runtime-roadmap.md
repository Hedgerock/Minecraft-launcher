[← Назад к общему пути](general-roadmap.md)

# Путь развития Java runtime

## Текущий план

- Реализовать production Java runtime compatibility check без fallback policy
- Не смешивать version detection с compatibility decision и fallback policy
- Использовать правила planning builders при будущих изменениях operation planning boundaries
- Не вводить Java installation discovery и Java version management без отдельного подтвержденного сценария

---

## Выполнено после `v0.5.0-java-runtime-foundation`

- Добавлен configured Java override через `LauncherConfiguration`
- Добавлен `JavaRuntimeSelectionRequest`
- `GameLaunchPlanBuilder` передает configured override в Java runtime selection
- `ManifestJavaRuntimeSelector` выбирает configured override перед manifest metadata
- Зафиксированы правила planning builders
- Проведена ревизия operation planning builders без немедленного рефакторинга
  `DownloadPlanBuilder` и `NativeExtractionPlanBuilder`
- Добавлена минимальная Java runtime failure model
- Добавлен общий базовый exception для classified Java runtime failures
- Добавлена модель `JavaVersionRequirement` для выражения минимального требования к major version Java
- Manifest JSON теперь является источником `JavaVersionRequirement` для `LaunchInfo`
- Зафиксирована граница проверки совместимости Java version
- Добавлен контракт Java runtime compatibility boundary
- Java runtime compatibility boundary подключена в launch planning через `NoOpJavaRuntimeCompatibilityChecker`
- Добавлена модель `JavaRuntimeVersion` для выражения фактически обнаруженной major version Java
- Зафиксирована граница определения Java runtime version
- Java runtime version detection boundary подключена в launch planning через `NoOpJavaRuntimeVersionDetector`
- `JavaRuntimeCompatibilityRequest` переведен на detected `JavaRuntimeVersion`
- Добавлен adapter-level parser вывода Java runtime version
- Добавлен `DefaultJavaRuntimeVersionDetector` для определения Java runtime version через resolved Java executable
- Application assembly переведен на `DefaultJavaRuntimeVersionDetector`

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
- Расширенная диагностика Java runtime ошибок
- Сохранение исходного `cause` в Java executable runtime exceptions
- Диагностика прав доступа к Java executable
- Отдельная модель Java installation
- Интеграция Java runtime selection с будущим profile/version flow
- Generic operation failure context
- Structured operation diagnostics
- Operation failure source/details model

---

## Активное направление

- Java runtime compatibility check

---

## Возможные следующие направления

- Java installation discovery
- Java process lifecycle diagnostics
- Java runtime fallback policy

---

## Почему Java runtime version compatibility check следующим

Java runtime version detection flow уже умеет определить фактическую `JavaRuntimeVersion` выбранного Java executable
через `DefaultJavaRuntimeVersionDetector`

`GameLaunchPlanBuilder` уже передает detected `JavaRuntimeVersion` и `LaunchInfo.javaVersionRequirement` в
`JavaRuntimeCompatibilityChecker`

На момент описания application assembly все еще использует `NoOpJavaRuntimeCompatibilityChecker`, поэтому production
проверка совместимости Java version еще не выполняется

Следующий минимальный runtime слой — заменить `NoOpJavaRuntimeCompatibilityChecker` на production проверку detected
`JavaRuntimeVersion` относительно `JavaVersionRequirement`

Этот шаг не требует Java installation discovery, automatic provisioning или fallback policy

Если detected Java runtime version не соответствует requirement, launcher должен получать явную runtime failure, но не
должен автоматически искать альтернативную Java installation без отдельного решения

---

## История последовательности активных решений

### Java runtime version detection

Java executable runtime flow уже умеет выбирать Java executable, разрешить command name через PATH-oriented lookup и
проверить readiness explicit filesystem path

После появления `JavaVersionRequirement`, manifest source для requirement и compatibility boundary следующий минимальный
runtime слой — определить фактическую `JavaRuntimeVersion` выбранного Java executable

Этот шаг не требует Java installation discovery, automatic provisioning или fallback policy

На момент принятия решения важно реализовать только detection boundary, а не поиск подходящей Java installation или
автоматический выбор альтернативной Java version

Если выбранный executable не сможет предоставить корректную runtime version, launcher должен получить явную runtime
failure, но не должен автоматически искать другую Java installation без отдельного решения

### Java version compatibility

Java executable runtime flow уже умеет выбрать Java executable, разрешить command name через PATH-oriented lookup и
проверить readiness explicit filesystem path

После добавления `JavaVersionRequirement` и manifest source для requirement следующий минимальный runtime слой —
проверить, соответствует ли выбранный Java executable требованию `LaunchInfo.javaVersionRequirement`

Этот шаг не требует Java installation discovery, automatic provisioning или fallback policy

На данном этапе важно определить и реализовать только compatibility boundary, а не поиск подходящей Java installation

Если выбранный executable не соответствует требованию, launcher должен получить явную runtime failure, но не должен
автоматически искать альтернативную Java installation без отдельного решения

### Java version requirements

Java executable runtime flow уже умеет выбрать Java executable, разрешить command name через PATH-oriented lookup
и проверить readiness explicit filesystem path

После configured Java override и Java runtime failure model следующий минимальный runtime слой — понять, какие
Java version requirements могут приходить из manifest metadata или launcher configuration

Этот шаг не требует Java installation discovery, automatic provisioning или fallback policy

На данном этапе важно определить только границу требований к версии Java, а не реализовывать поиск
подходящей Java installation

### Operation failure diagnostics

Java runtime failure model уже классифицирует ошибки внутри Java runtime boundary

Однако operation lifecycle по-прежнему получает только текстовое сообщение ошибки

Перед добавлением UI, fallback policy или recovery behavior нужно определить, должна ли structured failure information
подниматься выше runtime boundary

На текущем шаге важно не протащить Java-specific reason напрямую в operation layer

Если structured diagnostics потребуется на уровне operation, она должна появиться как generic operation failure context

### Configured Java override

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
