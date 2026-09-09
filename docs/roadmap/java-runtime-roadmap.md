[← Назад к общему пути](general-roadmap.md)

# Путь развития Java runtime

## Текущий план

- Определить границу operation failure diagnostics после реализации Java runtime failure model
- Не поднимать Java-specific failure reason в operation layer без generic failure context
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

---

## Активное направление

- Operation failure diagnostics boundary

---

## Возможные следующие направления

- Java version requirements
- Java installation discovery
- Java process lifecycle diagnostics

---

## Почему operation failure diagnostics следующим

Java runtime failure model уже классифицирует ошибки внутри Java runtime boundary

Однако operation lifecycle по-прежнему получает только текстовое сообщение ошибки

Перед добавлением UI, fallback policy или recovery behavior нужно определить, должна ли structured failure information
подниматься выше runtime boundary

На текущем шаге важно не протащить Java-specific reason напрямую в operation layer

Если structured diagnostics потребуется на уровне operation, она должна появиться как generic operation failure context

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
