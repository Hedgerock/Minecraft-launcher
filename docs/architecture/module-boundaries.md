# Модульные границы

## Цель

Зафиксировать правила модульных зависимостей проекта и границы ответственности `launcher-core`

Документ используется как архитектурный контракт при рефакторинге пакетов, добавлении новых модулей
и проверке допустимости зависимостей

---

## Основное правило

`launcher-app` владеет composition root и отвечает за сборку конкретных реализаций

`launcher-core` содержит orchestration-логику лаунчера и не должен создавать конкретные инфраструктурные
реализации

`launcher-verification` владеет алгоритмом проверки файлов и реализациями сервисов верификации

### `launcher-core` отвечает за

- Жизненный цикл `LauncherEngine`
- Запуск `Operation`
- Управление и выполнение `ExecutionStrategy`
- Состояние события launcher lifecycle
- Контракты задач `LauncherTask`
- Формирование generic launch failure context для результата launcher lifecycle

### `launcher-core` не отвечает за реализацию

- Транспортировки HTTP
- Парсинга JSON
- Локальной файловой системы
- Установки
- Игрового процесса
- UI
- Конкретного провайдера аутентификации

`launcher-api` владеет HTTP-загрузкой manifest JSON и преобразованием внешнего JSON-контракта в доменную
модель `Manifest`

`launcher-core` владеет `DownloadPlan`, потому что он используется слоями оркестрации для выбора
следующего шага запуска

`launcher-downloader` владеет реализацией скачивания и деталями работы с сетью/файловой системой

---

## Разрешенные зависимости для launcher-core

`launcher-core` может зависеть от

- `launcher-model`
- `launcher-common`
- Собственных портов оркестрации
- Стабильных service contracts, если не содержат конкретной инфраструктурной реализации

---

## Запрещенные зависимости для launcher-core

`launcher core` не должен зависеть от конкретных классов адаптеров

Запрещенные примеры

- `JavaLauncherHttpClient`
- `HttpManifestClient`
- `JsonManifestMapper`
- `HttpManifestService`
- `LocalFileStorage`
- `LocalFileMetadataReader`
- Конкретные реализации установщиков
- Конкретные реализации игрового процесса

---

## Интерфейсы и реализации

`launcher-core` может использовать интерфейсы, если они являются портами или стабильными контрактами

Пример допустимой зависимости
- `ManifestService`

Пример недопустимой зависимости
- `HttpManifestService`

Правило:

Ядро зависит от абстракции, корень композиции от реализации

---

## Корень композиции

`launcher-app` является composition root

Создание конкретной инфраструктурной реализации должно происходить вне `launcher-core`

`launcher-app` связывает orchestration ports `launcher-core` с конкретными адаптерами

`launcher-core` не создает конкретные инфраструктурные реализации внутри жизненного цикла
операции

---

## Граница presentation layer

`launcher-ui` владеет JavaFX entrypoint, JavaFX controls и presentation-specific adapters

`launcher-app` владеет technology-neutral application boundary приема launch request и фоновым выполнением launcher lifecycle

`launcher-app` формирует `PresentationLaunchCompletion` из полученного `LaunchResult` либо фиксирует неожиданный сбой фонового
выполнения до получения результата

`launcher-ui` владеет переходами presentation state после `LaunchRequestResult` и `PresentationLaunchCompletion`

Для исхода без `LaunchResult` `launcher-ui` переводит состояние в `FAILED` и отображает общее безопасное сообщение

`launcher-core` не зависит от `PresentationLaunchCompletion` и не управляет доставкой результата в presentation layer

`launcher-app` и `launcher-core` не хранят presentation state и не управляют доступностью UI controls

Направление основной зависимости

```text
launcher-ui
    -> launcher-app
```

`launcher-ui` может напрямую зависеть от `launcher-core` только для использования внешних launcher lifecycle или configuration
models, необходимых presentation layer, например `LaunchResult` и `LauncherConfiguration`

`OperationType` используется в `launcher-ui` только как метаданные `LaunchFailure` для выбора безопасного пользовательского
сообщения

При неуспешном `LaunchResult` presentation state хранит `PresentationLaunchFailure`, полученный через `PresentationLaunchFailureMapper`,
и очищает его после принятия нового launch request

Такая зависимость не дает `launcher-ui` право управлять `LauncherEngine`, `OperationManager`, `LaunchOperation` или application
assembly напрямую

`launcher-app` не должен зависеть от JavaFX classes или presentation-specific реализаций из `launcher-ui`

Перенос обработки результата в JavaFX Application Thread является ответственностью адаптера `launcher-ui`

---

## Статус переноса

Сборки конкретных приложений, инфраструктурные фабрики и наборы сервисов находятся в модуле `launcher-app`.

`launcher-core` больше не отвечает за контракты сборок приложений.

`launcher-core` больше не зависит от `launcher-api`

Контракт загрузки манифеста принадлежит `launcher-core`, а HTTP-реализация находится в
`launcher-api`

`ManifestService` является orchestration port `launcher-core`

`HttpManifestService` является конкретным адаптером `launcher-api`

---

## Границы хранения

В проекте существует отдельный модуль `launcher-storage`

Поэтому новые контракты и реализации хранения не должны добавляться в `launcher-core`

Если `launcher-core` нуждается в файловом доступе, он должен зависеть от абстракции, а конкретная
реализация должна передаваться через композиционный корень

---

## Границы Java runtime

`launcher-core` владеет orchestration contracts Java runtime flow

### `launcher-core` владеет

- `JavaRuntimeSelector`
- `JavaExecutableReadinessChecker`
- `JavaCommandPathResolver`
- `JavaExecutableReferenceResolver`
- `NoOpJavaExecutableReadinessChecker`
- `DefaultJavaRuntimeCompatibilityChecker`, потому что это pure policy без доступа к filesystem, environment variables,
  system properties или process state
- No-op реализации runtime contracts, используемые в тестах и временном wiring

### `launcher-app` владеет

- `DefaultJavaRuntimeVersionDetector`
- `ProcessJavaRuntimeVersionCommandRunner`
- `JavaRuntimeVersionOutputParser`
- `SystemRuntimeEnvironmentProvider`
- `SystemJavaCommandPathEnvironmentProvider`
- `DefaultJavaExecutableReadinessChecker`
- Production wiring Java runtime flow
- Adapter-level Java process diagnostics для Java runtime version detection

Concrete Java runtime adapters не должны находиться в `launcher-core`, если они читают filesystem,
environment variables, system properties, process output или process state

---

## Граница подготовки ресурсов

`launcher-core` владеет `ResourceSetPlanner`, `PlannedResource`, `ResourceSetPlan` и `ResourceSetConflictException`

Подготовка ресурсов является pure policy без файлового или сетевого доступа

`launcher-verification` и `launcher-downloader` используют подготовленный набор для выполнения внешних действий

`launcher-app` создает planner и передает его обоим adapters

`ManifestResources` остается domain projection и не получает ответственность за локальное назначение

---

## Reserved modules

`launcher-auth` и `launcher-common` могут оставаться подключенными к Gradle build как
reserved modules

Исходный reserved status этих модулей и `launcher-ui` зафиксирован в [ADR-0037](../decisions/records/ADR-0037-reserved-modules-policy.md)

`launcher-ui` больше не является reserved module

После реализации presentation launch boundary он развивается как активный presentation module, но пока сохраняет минимальный
scope без полноценной UI state model

`launcher-common` не должен использоваться как общий utility module без подтвержденного cross-module сценария или
architecture/design решения

---

## Границы верификации

`launcher-verification` отвечает за

- Проверку файлов
- Построение `VerificationPlan`
- Работу с `FileVerifier`
- Использования `HashService` и контракты чтения метаданных файлов

`launcher-core` может запускать операции верификации, но не должен самостоятельно выполнять детали
проверки файлов

### `launcher-core` владеет

- `VerificationService`
- `VerificationPlan`
- `ResourceVerificationResult`
- `VerificationStatus`

### `launcher-verification` владеет

- `FileVerifier`
- `DefaultFileVerifier`
- `DefaultVerificationService`
- Алгоритмом верификации
- Взаимодействием с файловой системой и хешированием через контракты

---

## Направление зависимостей

```text
launcher-ui
    -> launcher-app
        -> launcher-core
            -> launcher-model
                -> launcher-common

launcher-ui
    -> launcher-core
```

`launcher-app` также может зависеть от модулей адаптеров

- `launcher-api`
- `launcher-storage`
- `launcher-verification`
- `launcher-downloader`
- `launcher-natives`
- `launcher-game`

Пример запрещенного направления
```text
launcher-core
    -> Конкретная реализация адаптера

launcher-app
    -> launcher-ui
```

---

## Проверка правила

Правила из этого документа должны быть постепенно закреплены архитектурными тестами

Первый проверяемый набор

- `launcher-core` не импортирует известные реализации API адаптеров
- `launcher-core` не импортирует известные реализации адаптеров хранения
- `launcher-core` не создает конкретные инфраструктурные реализации внутри цикла жизни операции
- `launcher-app` является composition root
- Конкретные адаптеры находятся вне `launcher-core`
- `launcher-ui` не импортирует `LauncherEngine`, внутренние типы operation layer или application assembly напрямую;
  `OperationType` допускается только для преобразования `LaunchFailure`

---

## Архитектурное эволюционное правило

Новые ограничения вводятся постепенно

Сначала правило фиксируется в документации, затем добавляется архитектурный тест, затем выполняется
кодовый перенос

После завершения переноса, правило становится постоянной архитектурной границей
