[← Назад к списку решений](README.md)

# ADR-0038: Определить границу configured Java override

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(core): add configured java executable override`
> `refactor(core): introduce java runtime selection request`
> `refactor(core): introduce game launch plan builder port`
> `feat(core): prefer configured java executable override`

---

## Контекст

После `v0.5.0-java-runtime-foundation` launcher получил минимальный production-ready
Java executable runtime flow

Java executable проходит через несколько явных границ

```text
LaunchInfo.javaExecutable
    -> JavaExecutableReferenceResolver
    -> JavaRuntimeSelector
    -> JavaCommandPathResolver
    -> JavaExecutableReadinessChecker
    -> GameLaunchCommandBuilder
    -> GameLaunchPlan
```

На момент принятия решения Java executable выбирается из manifest metadata через `ManifestJavaRuntimeSelector`

Это позволяет launcher использовать manifest-provided command name или explicit filesystem path

Однако пользовательская или локальная конфигурация может потребовать запустить игру через конкретный Java executable,
не меняя manifest metadata

Например

- Локально установленный JDK
- Portable Java runtime
- Java executable, выбранный пользователем
- Java executable, заданный настройками launcher

Если добавить override напрямую в `GameLaunchCommandBuilder`, command building начнет выбирать runtime

Если добавить override в `GameService`, process launch начнет подменять уже подготовленный `GameLaunchPlan`

Если добавить override как особый случай после readiness check, flow начнет обходить существующие границы
`JavaCommandPathResolver` и `JavaExecutableReadinessChecker`

Поэтому configured Java override должен быть встроен в Java runtime selection boundary

---

## Решение

Configured Java override должен быть частью Java runtime selection flow

`LauncherConfiguration` может содержать optional Java executable override

Если override задан, Java runtime selection должна выбрать его перед manifest-provided Java executable

Если override не задан, launcher должен использовать существующий manifest-based selection flow

Override должен проходить через те же границы, что и manifest-provided Java executable

```text
Configured Java override
    -> JavaExecutableReferenceResolver
    -> JavaRuntimeSelector
    -> JavaCommandPathResolver
    -> JavaExecutableReadinessChecker
    -> GameLaunchCommandBuilder
```

Для этого `JavaRuntimeSelector` должен получить доступ к двум источникам Java executable metadata

- Configured override
- Manifest launch metadata

Configured override имеет больший приоритет, чем manifest metadata

`JavaExecutableReferenceResolver` остается ответственным за интерпретацию raw Java executable value как command name
или explicit filesystem path

`JavaCommandPathResolver` остается ответственным за command name resolution

`JavaExecutableReadinessChecker` остается ответственным за readiness check resolved explicit filesystem path

`GameLaunchPlanBuilder` может передавать данные configuration в Java runtime selection, но не должен сам выбирать
между override и manifest metadata

Граница выглядит так

```text
launcher configuration
    -> configured Java executable override

manifest metadata
    -> manifest Java executable

JavaRuntimeSelector
    -> choose configured override when present
    -> fallback to manifest metadata

JavaExecutableReferenceResolver
    -> command name
    -> explicit filesystem path
```

---

## Последствия

Появляется явное место для пользовательского Java executable override

Manifest metadata остается fallback source, если override не задан

Существующий manifest-based Java executable flow сохраняется

`GameLaunchCommandBuilder` и `GameService` не получают runtime selection responsibility

Configured override сможет использовать как command name, так и explicit filesystem path

Ошибки override будут проходить через существующий path resolution и readiness boundaries

Будущий UI сможет передавать Java executable override через configuration, не меняя manifest contract

---

## Не входит в решение

- Реализация UI для выбора Java executable
- Поиск Java installations
- Автоматическое обнаружение Java runtime
- Проверка Java version compatibility
- Хранение пользовательских настроек
- Валидация Java version
- Fallback policy при невалидном override
- Изменение manifest JSON contract
- Изменение `GameService`
- Изменение `GameLaunchCommandBuilder`
- Создание `launcher-runtime` module

---

## Связанные решения

- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0030: Определить границу проверки Java executable](ADR-0030-java-executable-readiness-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
- [ADR-0032: Определить границу PATH resolution для Java command name](ADR-0032-java-command-path-resolution-boundary.md)
- [ADR-0033: Определить интерпретацию Java executable reference из manifest metadata](ADR-0033-manifest-java-executable-reference-interpretation.md)
- [ADR-0035: Определить границу реализации Java executable readiness checker](ADR-0035-java-executable-readiness-adapter-boundary.md)
- [ADR-0036: Определить границу Java command path resolver](ADR-0036-java-command-path-resolver-boundary.md)
