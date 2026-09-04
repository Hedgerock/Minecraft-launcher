# Ретроспектива: развитие Java executable runtime flow

## Контекст

Данная итерация развила launcher после `v0.4.0-library-native-flow` в сторону более реалистичного
Java runtime flow

До начала этапа launcher уже умел загружать manifest, проверять и восстанавливать ресурсы, подготавливать
directories, строить `GameLaunchPlan`, распаковывать natives и запускать игровой процесс

Однако Java executable оставался недостаточно явно выраженной частью runtime flow

`LaunchInfo.javaExecutable` уже присутствовал в manifest metadata, но его смысловая интерпретация, PATH resolution,
readiness check и production wiring были разделены не полностью

Целью этапа было довести Java executable flow до состояния, где manifest-provided значение проходит через
явные границы ответственности до построения launch command

---

## Что было сделано

- Добавлен контракт `JavaRuntimeSelector`
- Добавлена реализация `ManifestJavaRuntimeSelector`
- Добавлена модель `JavaExecutableReference`
- Добавлено разделение Java executable reference на command name и explicit filesystem path
- Добавлен контракт `JavaExecutableReferenceResolver`
- Добавлена реализация `ManifestJavaExecutableReferenceResolver`
- Добавлен контракт `JavaExecutableReadinessChecker`
- Добавлены `DefaultJavaExecutableReadinessChecker` и `NoOpJavaExecutableReadinessChecker`
- `GameLaunchPlanBuilder` переведен на выбор Java executable через `JavaRuntimeSelector`
- `GameLaunchCommandBuilder` переведен на использование `JavaExecutableReference`
- Добавлен контракт `JavaCommandPathResolver`
- Добавлены `DefaultJavaCommandPathResolver` и `NoOpJavaCommandPathResolver`
- Добавлена модель `JavaCommandPathEnvironment`
- Добавлен контракт `JavaCommandPathEnvironmentProvider`
- Добавлена поддержка `PATH` и `PATHEXT` для command name resolution
- Добавлена фильтрация некорректных entries из `PATH`
- Application assembly переведен на production wiring через `DefaultJavaCommandPathResolver`
- Application assembly переведен на production wiring через `DefaultJavaExecutableReadinessChecker`
- `ManifestJavaExecutableReferenceResolver` начал интерпретировать manifest-provided `javaExecutable` как command name
  или explicit filesystem path
- Некорректный explicit filesystem path начал возвращаться как readiness failure
- Обновлена документация Java executable runtime flow
- Добавлено правило исторического контекста для ADR

---

## Что подтвердилось

Разделение Java executable flow на несколько маленьких границ оказалось устойчивым

`JavaRuntimeSelector` отвечает за выбор Java executable reference, но не выполняет PATH lookup и не проверяет
filesystem

`JavaExecutableReferenceResolver` отвечает за интерпретацию manifest metadata, но не выполняет readiness check

`JavaCommandPathResolver` отвечает за PATH-oriented lookup command name, но не занимается выбором Java version

`JavaExecutableReadinessChecker` отвечает за проверку уже resolved explicit filesystem path

`GameLaunchCommandBuilder` остается простым сборщиком command и не получает runtime-specific lookup logic

`GameService` не получает ответственность за выбор Java runtime, проверку Java executable или PATH resolution

Такое разделение позволило подключить production behavior постепенно, не смешивая manifest metadata, runtime selection,
filesystem readiness и process launch

---

## Что было улучшено архитектурно

Java executable перестал быть просто строкой, которая напрямую попадает в launch command

Теперь flow выражен явно

```text
LaunchInfo.javaExecutable
    -> ManifestJavaRuntimeSelector
        -> ManifestJavaExecutableReferenceResolver
            -> JavaExecutableReference.commandName
            -> JavaExecutableReference.explicitPath
    -> JavaCommandPathResolver
    -> JavaExecutableReadinessChecker
    -> GameLaunchCommandBuilder
    -> GameLaunchPlan
```

Application assembly стал владельцем production wiring для Java executable runtime components

`launcher-core` сохранил orchestration и runtime contracts, но не стал напрямую зависеть от конкретного
окружения запуска приложения

Corrupted system environment больше не ломает создание `LauncherEngine`, потому что некорректные entries из `PATH`
игнорируются при построении `JavaCommandPathEnvironment`

Explicit filesystem path теперь проходит свой путь без PATH lookup, что делает manifest-provided Java executable metadata
более выразительным

---

## Что осталось отложенным

- Выбор Java version
- Проверка совместимости Java version с manifest metadata
- Configured Java override
- Поиск Java installations вне `PATH`
- Автоматическая установка Java
- Fallback policy для отсутствующего Java executable
- Более структурированная модель ошибок Java runtime
- Сохранение исходного `cause` в Java executable runtime exceptions
- Диагностика прав доступа к Java executable
- Отдельная модель Java installation
- Интеграция Java runtime selection с будущим profile/version flow

---

## Технический долг

Технический долг остается контролируемым

Основные ограничения текущего Java executable flow зафиксированы явно и не блокируют текущий launch lifecycle

`NoOpJavaCommandPathResolver` и `NoOpJavaExecutableReadinessChecker` остаются полезными для тестов и
изолированных сценариев, но production assembly уже использует реальные реализации

Старые ADR могут содержать формулировку `на текущем этапе`, хотя для новых решений уже закреплена более точная
формулировка `на момент принятия решения`

Это не ломает актуальную документацию, но может быть исправлено отдельной cleanup-итерацией для исторических ADR

---

## Главный вывод

Этап подтвердил, что Java runtime flow лучше развивать не через один большой resolver, а через цепочку
маленьких ответственностей

Launcher получил минимальный production-ready Java executable flow

```text
manifest-provided javaExecutable
    -> semantic reference
    -> optional PATH resolution
    -> filesystem readiness check
    -> launch command
```

Проект стал ближе к реалистичному runtime launcher behavior, но при этом не ввел преждевременную
архитектуру для Java version management, installation discovery или automatic provisioning
