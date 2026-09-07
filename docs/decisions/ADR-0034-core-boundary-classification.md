[← Назад к списку решений](README.md)

# ADR-0034: Определить классификацию границ launcher-core

## Статус

Accepted

---

## Контекст

`launcher-core` отвечает за orchestration launcher lifecycle, операции, задачи, планы выполнения и порты,
через которые orchestration взаимодействует с внешними возможностями

После развития verification, download, native extraction, game launch и Java runtime flow в `launcher-core` появилось
несколько типов компонентов

- orchestration components
- operation ports
- plan models
- core policies
- concrete system/filesystem adapters

Часть компонентов действительно принадлежит `launcher-core`, потому что описывает порядок запуска, переходы
между шагами и контракты взаимодействия

Однако часть компонентов начинает размывать границу ядра, если остается внутри `launcher-core`

- Локальная файловая система
- Чтение system properties
- Чтение environment variables
- Process/runtime environment lookup
- Конкретные production adapters

Без явной классификации `launcher-core` снова начнет разрастаться и смешивать orchestration logic с infrastructure
details

---

## Решение

`launcher-core` владеет orchestration boundary проекта

В `launcher-core` могут находиться

- launcher lifecycle
- operation lifecycle
- `LauncherEngine`
- `OperationManager`
- `OperationFactory`
- `LaunchOperation`
- `LauncherTask`
- operation types
- operation results
- orchestration ports
- plan models, которые нужны для переходов между operation steps
- pure policies, которые не читают filesystem, environment variables, system properties, network или process state

`launcher-core` не должен владеть concrete adapters

К concrete adapters относятся компоненты, которые

- Читают или изменяют локальную файловую систему
- Читают system properties
- Читают environment variables
- Выполняют network calls
- Запускают process
- Работают с HTTP, JSON, ZIP, hash implementation или platform-specific runtime lookup
- Являются production implementation внешней возможности

Такие реализации должны находиться в adapter modules или в `launcher-app`, если отдельный adapter module еще не выделен

Классификация компонентов

```text
launcher-core
    -> orchestration components
    -> ports
    -> plan models
    -> pure policies

launcher-app
    -> composition root
    -> production wiring
    -> temporary home for production adapters without dedicated module

adapter modules
    -> concrete implementations
```

Правило владения

```text
core владеет тем что launcher обязан решать
adapters владеют внутренней реализации конкретного контракта
app владеет процессом запуска приложения
```

---

## Последствия

Граница `launcher-core` становится строже

`launcher-core` может продолжать владеть портами, которые нужны operation lifecycle

Concrete implementations должны постепенно переноситься из `launcher-core`, если они работают с system,
filesystem, process, environment или platform-specific runtime details

`SafeResourcePathResolver` может оставаться в `launcher-core`, потому что это pure safety policy, а не filesystem adapter

`LocalFileStorage` становится кандидатом на перенос из `launcher-core` в `launcher-storage`

`LocalDirectoryProvider` становится кандидатом на перенос из `launcher-core` в `launcher-app` или другой подходящий
adapter boundary

`SystemRuntimeEnvironmentProvider` и `SystemJavaCommandPathEnvironmentProvider` становятся кандидатами на перенос из
`launcher-core` после отдельного решения о runtime/system adapter boundary

Архитектурные тесты должны постепенно проверять не только внешние imports в `launcher-core`, но и отсутствие concrete
system/filesystem adapters внутри самого `launcher-core`

---

## Не входит в решение

- Немедленный перенос всех concrete adapters из `launcher-core`
- Создание нового `launcher-runtime` module
- Создание общего `launcher-contracts` module
- Перенос plan models из `launcher-core`
- Перенос operation ports из `launcher-core`
- Изменение launcher lifecycle
- Изменение behavior существующих операций
- Изменение production wiring

---

## Связанные решения

- [ADR-0013: Порты оркестрации принадлежат launcher-core](ADR-0013-core-owns-orchestration-ports.md)
- [ADR-0016: Зафиксировать правила безопасности resource path](ADR-0016-resource-path-safety.md)
- [ADR-0024: Определить границу native extraction operation](ADR-0024-native-extraction-operation-boundary.md)
- [ADR-0025: Определить реализацию native extraction service](ADR-0025-native-extraction-service-implementation.md)
- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0032: Определить границу PATH resolution для Java command name](ADR-0032-java-command-path-resolution-boundary.md)
