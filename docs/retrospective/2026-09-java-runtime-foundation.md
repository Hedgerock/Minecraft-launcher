# Ретроспектива: Java runtime foundation

## Контекст

Данный milestone завершает развитие launcher после `v0.4.0-library-native-flow` в сторону более
реалистичного Java runtime foundation

До начала этапа launcher уже умел загружать manifest, проверять и восстанавливать ресурсы, выбирать runtime libraries,
распаковывать natives, строить `GameLaunchPlan` и запускать игровой процесс

Однако Java executable flow и foundation проекта еще требовали стабилизации

`LaunchInfo.javaExecutable` был частью manifest metadata, но его путь до launch command требовал явных границ

Также после роста проекта стало важно укрепить фундамент перед следующим runtime milestone

- Уточнить границы `launcher-core`
- Вынести concrete adapters из core
- Зафиксировать статус reserved modules
- Добавить минимальный CI
- Зафиксировать правила подготовки release

Целью milestone было довести Java executable runtime flow до минимального production-ready состояния и укрепить
проектный foundation перед следующим этапом развития

---

## Что было сделано

- Добавлен `JavaRuntimeSelector`
- Добавлена модель `JavaExecutableReference`
- Добавлена интерпретация Java executable как command name или explicit filesystem path
- Добавлен `JavaExecutableReferenceResolver`
- Добавлен `JavaCommandPathResolver`
- Добавлена модель `JavaCommandPathEnvironment`
- Добавлен provider для построения command path environment из `PATH` и `PATHEXT`
- Добавлена фильтрация некорректных entries из `PATH`
- Добавлен `JavaExecutableReadinessChecker`
- Добавлен readiness check для resolved explicit filesystem path
- Application assembly переведен на production wiring Java command path resolver и readiness checker
- Concrete runtime providers перенесены из `launcher-core` в `launcher-app`
- Concrete directory provider перенесен из `launcher-core` в `launcher-app`
- Concrete Java executable readiness checker перенесен из `launcher-core` в `launcher-app`
- `DefaultJavaCommandPathResolver` зафиксирован как осознанный core boundary компромисс
- Зафиксирована классификация границ `launcher-core`
- Зафиксирована политика reserved modules для `launcher-auth`, `launcher-common` и `launcher-ui`
- Добавлен минимальный GitHub Actions CI pipeline
- Добавлены правила подготовки release
- Обновлены roadmap, module boundaries, glossary и CHANGELOG

---

## Что подтвердилось

Java runtime flow лучше развивать через цепочку маленьких ответственностей

`JavaRuntimeSelector` выбирает Java executable reference, но не выполняет PATH lookup

`JavaExecutableReferenceResolver` интерпретирует manifest metadata, но не проверяет filesystem readiness

`JavaCommandPathResolver` отвечает за PATH-oriented command name resolution

`JavaExecutableReadinessChecker` проверяет уже resolved explicit filesystem path

`GameLaunchCommandBuilder` продолжает строить command из подготовленных данных

`GameService` не получает ответственность за выбор Java runtime, PATH lookup или readiness check

Также подтвердилось, что `launcher-core` должен оставаться orchestration boundary, а concrete production adapters лучше
выносить в adapter modules или `launcher-app`

---

## Что было улучшено архитектурно

Java executable перестал быть raw string, которая напрямую попадает в launch command

Flow стал явным

```text
LaunchInfo.javaExecutable
    -> JavaExecutableReferenceResolver
    -> JavaRuntimeSelector
    -> JavaCommandPathResolver
    -> JavaExecutableReadinessChecker
    -> GameLaunchCommandBuilder
    -> GameLaunchPlan
```

`launcher-app` стал явнее владеть production wiring и temporary adapters без отдельного dedicated module

`launcher-core` сохранил ownership над contracts, orchestration, plan models и runtime policies

Reserved modules перестали быть неявным техническим долгом

`launcher-common` получил строгое ограничение и не должен превращаться в общий utility module

GitHub CI сделал проверку проекта внешней по отношению к локальному окружению разработки

---

## Что осталось отложенным

- Выбор Java version
- Проверка совместимости Java version с manifest metadata
- Configured Java override
- Поиск Java installations вне `PATH`
- Автоматическая установка Java
- Fallback policy для отсутствующего Java executable
- Более структурированная модель ошибок Java runtime
- Сохранение исходного `cause` в Java runtime exceptions
- Диагностика прав доступа к Java executable
- Создание отдельного `launcher-runtime` module
- Более строгий dependency analysis между модулями
- Дальнейшая полировка тестового стиля

---

## Технический долг

Технический долг остается контролируемым

Основные ограничения Java runtime flow зафиксированы явно и не блокируют текущий launch lifecycle

`DefaultJavaCommandPathResolver` остается в `launcher-core` как осознанный компромисс

Он содержит limited filesystem probing через `Files.isRegularFile(...)`, но не читает `PATH`, `PATHEXT`,
`System.getenv(...)` и не выполняет Java installation discovery

Если probing начнет развиваться отдельно, его нужно будет вынести в отдельный contract

Reserved modules остаются допустимыми, потому что их статус и ограничения зафиксированы

Дальнейшее усиление quality gate и CI стоит выполнять только при появлении повторяющихся проблем, которые выгодно
проверять автоматически

---

## Главный вывод

Milestone подтвердил, что проект можно развивать как серию небольших архитектурных решений, не теряя цельность общего
launcher flow

`v0.5.0-java-runtime-foundation` закрывает не только Java executable runtime behavior, но и важную стабилизацию
проектного foundation

Launcher стал ближе к production-ready runtime behavior, но не ввел преждевременную архитектуру для Java version
management, installation discovery, automatic provisioning или UI flow

Следующий milestone должен начинаться не с расширения `LauncherEngine`, а с выбора конкретного runtime направления и его
отдельной архитектурной границы
