[← Назад к списку решений](README.md)

# ADR-0044: Зафиксировать границу определения Java runtime version

## Статус

Accepted

---

## Контекст

После ADR-0041 в проекте появилась модель `JavaVersionRequirement`

После ADR-0042 manifest launch metadata стал первым источником Java version requirement для game runtime

После ADR-0043 в launch planning появилась отдельная Java runtime compatibility boundary

На момент принятия решения в проекте также появилась модель `JavaRuntimeVersion`, которая описывает фактически
обнаруженную major version Java runtime

Compatibility boundary уже может получить resolved `JavaExecutableReference` и `JavaVersionRequirement`, но еще не имеет
отдельного источника фактической версии Java runtime

Если определить версию Java прямо внутри `JavaRuntimeCompatibilityChecker`, compatibility checker начнет смешивать
получение фактического состояния runtime environment и сравнение с требованием

Если определять версию Java внутри `GameLaunchPlanBuilder`, planning layer начнет выполнять adapter-level runtime
diagnostics

Если определять версию Java внутри `GameService`, запуск игрового процесса начнет отвечать за runtime compatibility
и diagnostics

Если расширить `JavaRuntimeSelector`, selector начнет смешивать выбор executable, version detection и потенциальный
fallback policy

Поэтому перед реализацией проверки совместимости нужно отделить получение фактической Java runtime version от сравнения
с `JavaVersionRequirement`

---

## Решение

Определение фактической Java runtime version должно быть отдельной runtime boundary

Эта boundary должна получать already resolved `JavaExecutableReference` и возвращать `JavaRuntimeVersion`

Boundary не должна

- Сравнивать версию с `JavaVersionRequirement`
- Выбирать Java executable
- Выполнять Java installation discovery
- Принимать решение о fallback policy
- Строить `GameLaunchPlan` или запускать игровой процесс

Граница выглядит так

```text
JavaRuntimeSelector
    -> selected JavaExecutableReference

JavaCommandPathResolver
    -> resolved JavaExecutableReference

JavaRuntimeVersionDetector
    -> resolved JavaExecutableReference
    -> JavaRuntimeVersion

JavaRuntimeCompatibilityChecker
    -> JavaRuntimeVersion
    -> JavaVersionRequirement
    -> compatibility decision
```

`JavaRuntimeVersionDetector` должен быть контрактом runtime boundary

Конкретный способ определения версии Java является adapter-level detail

Production adapter может использовать запуск выбранного Java executable с аргументом версии и парсинг вывода

Формат вывода Java version, stream вывода, exit code и platform-specific ошибки не должны протекать выше
detector boundary

`JavaRuntimeCompatibilityChecker` должен использовать уже определенную `JavaRuntimeVersion`, если проверка версии Java
будет реализована отдельной итерацией

Если текущий контракт compatibility checker потребуется изменить, это должно быть сделано отдельной кодовой итерацией
после принятия данного решения

---

## Рассмотренные варианты

### Определять Java runtime version внутри `JavaRuntimeCompatibilityChecker`

Вариант отклонен

`JavaRuntimeCompatibilityChecker` должен отвечать за проверку совместимости runtime version с requirement

Если добавить туда version detection, checker начнет выполнять adapter-level diagnostics и сравнение требования
одновременно

### Определить Java runtime version внутри `GameLaunchPlanBuilder`

Вариант отклонен

`GameLaunchPlanBuilder` должен оркестрировать подготовку launch plan и использовать готовые runtime boundaries

Если добавить туда запуск Java process или parsing version output, planning layer начнет зависеть от adapter-level
runtime diagnostics

### Определить Java runtime version внутри `GameService`

Вариант отклонен

`GameService` должен запускать уже подготовленный `GameLaunchPlan`

Если добавить туда version detection, запуск игрового процесса начнет отвечать за подготовку runtime compatibility

### Расширить `JavaRuntimeSelector`

Вариант отложен

`JavaRuntimeSelector` сейчас выбирает executable reference

Если добавить туда version detection, selector начнет смешивать выбор executable, фактическую runtime identity и будущий
fallback policy

Такое расширение может стать оправданным только после появления Java installation discovery или fallback policy

---

## Последствия

Java runtime version detection получает отдельную границу ответственности

`JavaRuntimeCompatibilityChecker` не обязан самостоятельно запускать Java process для получения версии

`GameLaunchPlanBuilder` остается orchestration layer для подготовки запуска, а не adapter-level diagnostics
component

`GameService` остается адаптером запуска игрового процесса

`JavaRuntimeSelector` не превращается в Java installation discovery или fallback engine

Появляется место для production adapter, который сможет определить фактическую Java runtime version через выбранный
Java executable

Compatibility flow получает более явную последовательность

```text
select executable
    -> resolve executable
        -> check readiness
            -> detect runtime version
                -> check compatibility
                    -> build launch plan
```

Это добавляет дополнительную boundary и усложняет подготовку launch plan, но сохраняет separation of concerns между
detection и compatibility decision

---

## Не входит в решение

- Реализация `JavaRuntimeVersionDetector`
- Реализация production adapter для запуска `java -version`
- Парсинг вывода Java version
- Изменение `JavaRuntimeCompatibilityChecker`
- Изменение `GameLaunchPlanBuilder`
- Изменение `GameService`
- Изменение `GameLaunchCommandBuilder`
- Java installation discovery
- Java version management
- Automatic Java provisioning
- Fallback policy при несовместимой Java version
- UI выбор Java version
- Launcher runtime requirement для работы самого launcher

---

## Связанные решения

- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0030: Определить границу проверки Java executable](ADR-0030-java-executable-readiness-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
- [ADR-0032: Определить границу PATH resolution для Java command name](ADR-0032-java-command-path-resolution-boundary.md)
- [ADR-0038: Определить границу configured Java override](ADR-0038-configured-java-override-boundary.md)
- [ADR-0041: Определить границу Java version requirements](ADR-0041-java-version-requirements-boundary.md)
- [ADR-0042: Определить manifest source для Java version requirement](ADR-0042-manifest-java-version-requirement-source.md)
- [ADR-0043: Определить границу проверки совместимости Java version](ADR-0043-java-version-compatibility-boundary.md)
