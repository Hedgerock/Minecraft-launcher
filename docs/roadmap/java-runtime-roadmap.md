[← Назад к общему пути](general-roadmap.md)

# Путь развития Java runtime

## Текущий план

- Не выбирать следующий Java runtime step без подтвержденного сценария
- Не смешивать process diagnostics с compatibility decision, Java installation discovery и fallback policy
- Не смешивать version detection с compatibility decision и fallback policy
- Использовать правила planning builders при будущих изменениях operation planning boundaries
- Не вводить Java installation discovery и Java version management без отдельного подтвержденного сценария

---

## Milestone `v0.6.0-java-runtime-compatibility` — Java runtime compatibility flow

Java runtime compatibility flow доведен до production-ready состояния без Java installation discovery и fallback policy

### Закрыто

- Configured Java executable override
- Java runtime failure model
- Java version requirement model
- Manifest source для Java version requirement
- Java runtime compatibility boundary
- Java runtime version model
- Java runtime version detection boundary
- Adapter-level Java runtime version output parser
- Production Java runtime version detector
- Production Java runtime compatibility checker
- Application assembly wiring для version detector и compatibility checker
- Test coverage для compatibility failure path в launch planning

Подробности зафиксированы в [ретроспективе Java runtime compatibility](../retrospective/2026-09-java-runtime-compatibility.md)

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

- Java runtime flow временно закрыт до появления подтвержденного сценария

---

## Возможные следующие направления

- Java installation discovery
- Java runtime fallback policy

---

## История последовательности активных решений

### Итог Java process lifecycle diagnostics revision

Ревизия подтвердила, что Java process lifecycle diagnostics достаточно закрыта для текущего Java runtime flow

Adapter-level diagnostics покрывает минимальные process lifecycle scenarios

- process не удалось запустить
- process завершился с non-zero exit code
- process вернул пустой output
- process вернул output, который невозможно распарсить как Java runtime version
- ожидание завершения Java process было interrupted

Следующий Java runtime step не выбран

Java installation discovery, Java runtime fallback policy и structured operation diagnostics остаются отложенными до
появления подтвержденного сценария

### Java process lifecycle diagnostics revision

Java process lifecycle diagnostics была успешно интегрирована в adapter-level detection flow, с покрытием минимального
набора сценариев согласно принятому решению [ADR-0045](../decisions/ADR-0045-java-process-lifecycle-diagnostics-boundary.md)

Следующий шаг — провести ревизию Java process lifecycle diagnostics flow, чтобы выбрать нового кандидата на реализацию
или усиление уже существующего flow

### Java process lifecycle diagnostics

После `v0.6.0-java-runtime-compatibility` Java runtime compatibility flow доведен до production-ready состояния

Launcher уже умеет выбрать Java executable, определить фактическую `JavaRuntimeVersion` и сравнивать ее с
`JavaVersionRequirement`

Следующий слабый участок находится не в compatibility decision, а в adapter-level запуске Java process для определения
runtime version

На момент описания detection flow уже умеет запускать selected Java executable и parsing process output, но ошибки
process lifecycle остаются минимально выраженными

Минимальные сценарии, которые нужно уточнить

- process не удалось запустить
- process завершился с non-zero exit code
- process вернул пустой output
- process вернул output, который невозможно распарсить как Java runtime version
- ожидание завершения Java process было interrupted

Этот шаг не требует Java installation discovery, automatic provisioning или fallback policy

Java process lifecycle diagnostics должна усилить существующий detection flow, но не должна выбирать альтернативную Java
installation или менять compatibility policy

### Итог Java runtime compatibility flow revision

Ревизия подтвердила, что Java runtime compatibility flow достаточно закрыт для текущего milestone

Следующий шаг — выбрать следующий runtime candidate либо временно закрыть Java runtime flow как достаточный
для текущего milestone

### Java runtime compatibility flow revision

Java runtime compatibility flow доведен до production-поведения без fallback policy

`GameLaunchPlanBuilder` уже передает detected `JavaRuntimeVersion` и `LaunchInfo.javaVersionRequirement` в
`JavaRuntimeCompatibilityChecker`

Application assembly использует `DefaultJavaRuntimeCompatibilityChecker`

Если detected Java runtime version ниже requirement, launcher получает Java runtime failure с причиной
`INCOMPATIBLE_JAVA_VERSION`

Следующий логический шаг — провести ревизию Java runtime flow и выбрать новый кандидат развития

На момент описания не нужно автоматически переходить к Java installation discovery, automatic provisioning или
fallback policy без отдельного подтвержденного сценария

Первый результат ревизии — усиление test coverage для compatibility failure path на уровне launch planning

После ревизии нужно либо выбрать следующий runtime candidate, либо временно закрыть Java runtime flow как достаточный
для текущего milestone

### Java runtime version compatibility check

Java runtime version detection flow уже умеет определить фактическую `JavaRuntimeVersion` выбранного Java executable
через `DefaultJavaRuntimeVersionDetector`

`GameLaunchPlanBuilder` уже передает detected `JavaRuntimeVersion` и `LaunchInfo.javaVersionRequirement` в
`JavaRuntimeCompatibilityChecker`

На момент выбора этого направления application assembly все еще использовал `NoOpJavaRuntimeCompatibilityChecker`,
поэтому production проверка совместимости Java version еще не выполнялась

Следующий минимальный runtime слой — заменить `NoOpJavaRuntimeCompatibilityChecker` на production проверку detected
`JavaRuntimeVersion` относительно `JavaVersionRequirement`

Этот шаг не требует Java installation discovery, automatic provisioning или fallback policy

Если detected Java runtime version не соответствует requirement, launcher должен получать явную runtime failure, но не
должен автоматически искать альтернативную Java installation без отдельного решения

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
