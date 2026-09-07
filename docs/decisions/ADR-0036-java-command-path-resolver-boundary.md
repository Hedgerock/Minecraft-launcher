[← Назад к списку решений](README.md)

# ADR-0036: Определить границу Java command path resolver

## Статус

Accepted

---

## Контекст

После ADR-0032 launcher получил отдельный contract PATH resolution для Java command name — `JavaCommandPathResolver`

После ADR-0034 `launcher-core` должен владеть orchestration boundary, ports, plan models и pure policies

Concrete adapters, которые читают filesystem, environment variables, system properties, network или process state,
не должны оставаться внутри `launcher-core`

На момент принятия решения чтение `PATH` или `PATHEXT` уже вынесено из `launcher-core` в
`SystemJavaCommandPathEnvironmentProvider`, который находится в `launcher-app`

`DefaultJavaCommandPathResolver` больше не читает environment variables напрямую

Он получает уже подготовленную модель `JavaCommandPathEnvironment`

Однако внутри `DefaultJavaCommandPathResolver` остается filesystem probing через `Files.isRegularFile(...)`

Это создает пограничный случай

- С одной стороны, resolver реализует runtime policy поиска command name среди подготовленных candidates
- С другой стороны, проверка `Files.isRegularFile(...)` читает состояние локальной файловой системы

Если сразу вынести весь `DefaultJavaCommandPathResolver` из `launcher-core`, core потеряет важную launch/runtime policy

Если сразу выделить отдельный filesystem probing port, появится новая абстракция только для одного потребителя и одного
сценария

---

## Решение

`JavaCommandPathResolver` остается contract-ом `launcher-core`

`DefaultJavaCommandPathResolver` остается в `launcher-core` как runtime policy для преобразования command name в
explicit filesystem path

Filesystem probing через `Files.isRegularFile(...)` внутри `DefaultJavaCommandPathResolver` признается допустимым
ограниченным компромиссом на момент принятия решения

Этот компромисс допустим, потому что

- Resolver не читает `PATH` и `PATHEXT`
- Resolver не читает `System.getenv(...)`
- Resolver не выбирает Java version
- Resolver не ищет Java installations вне переданной модели
- Resolver работает только с `JavaCommandPathEnvironment`
- Resolver содержит правило выбора первого подходящего candidate path

Граница выглядит так

```text
launcher-app
    -> SystemJavaCommandPathEnvironmentProvider
        -> JavaCommandPathEnvironment

launcher-core
    -> JavaCommandPathResolver
    -> DefaultJavaCommandPathResolver
        -> command name resolution policy
        -> limited filesystem probing
```

Если filesystem probing начнет развиваться отдельно, его нужно будет выделить в отдельный contract

Потенциальная будущая граница

```text
launcher-core
    -> JavaCommandPathResolver
        -> JavaExecutablePathProbe

launcher-app или adapter module
    -> DefaultJavaExecutablePathProbe
```

---

## Последствия

`DefaultJavaCommandPathResolver` не переносится из `launcher-core` только из-за наличия `Files.isRegularFile(...)`

`launcher-core` сохраняет runtime policy PATH-oriented command resolution

Проект не вводит преждевременную абстракцию для одного потребителя

Компромисс явно задокументирован и не должен расширяться на новые filesystem operations внутри `launcher-core`

Если появится второй потребитель filesystem probing, platform-specific probing, расширенная диагностика или проверка
прав доступа, нужно вернуться к выделению отдельного probing contract

`SystemJavaCommandPathEnvironmentProvider` остается вне `launcher-core`, потому что он читает environment variables

---

## Не входит в решение

- Перенос `DefaultJavaCommandPathResolver` из `launcher-core`
- Создание отдельного filesystem probing contract
- Создание нового `launcher-runtime` module
- Поиск Java installations вне `PATH`
- Выбор Java version
- Проверка Java version compatibility
- Изменение `JavaCommandPathEnvironment`
- Изменение `SystemJavaCommandPathEnvironmentProvider`
- Изменение `GameLaunchPlanBuilder`
- Изменение `GameService`

---

## Связанные решения

- [ADR-0013: Порты оркестрации принадлежат launcher-core](ADR-0013-core-owns-orchestration-ports.md)
- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0030: Определить границу проверки Java executable](ADR-0030-java-executable-readiness-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
- [ADR-0032: Определить границу PATH resolution для Java command name](ADR-0032-java-command-path-resolution-boundary.md)
- [ADR-0033: Определить интерпретацию Java executable reference из manifest metadata](ADR-0033-manifest-java-executable-reference-interpretation.md)
- [ADR-0034: Определить классификацию границ launcher-core](ADR-0034-core-boundary-classification.md)
- [ADR-0035: Определить границу реализации Java executable readiness checker](ADR-0035-java-executable-readiness-adapter-boundary.md)
