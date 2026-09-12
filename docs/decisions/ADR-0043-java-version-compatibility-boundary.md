[← Назад к списку решений](README.md)

# ADR-0043: Определить границу проверки совместимости Java version

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(core): add java runtime compatibility contract`
> `feat(core): wire java runtime compatibility boundary into launch planning`
> `feat(core): check java runtime version compatibility`
> `feat(app): wire default java runtime compatibility checker`

---

## Контекст

После ADR-0041 в проекте появилась отдельная модель `JavaVersionRequirement`

После ADR-0042 manifest launch metadata стал первым источником Java version requirement для game runtime

На момент принятия решения `JavaRuntimeSelector` выбирает Java executable reference из configured override или
manifest-provided `LaunchInfo.javaExecutable`

`JavaExecutableReadinessChecker` проверяет filesystem readiness выбранного executable

Однако launcher еще не проверяет, соответствует ли выбранный Java executable требованию
`LaunchInfo.javaVersionRequirement`

Если добавить проверку версии Java в `GameService`, запуск процесса начнет отвечать за runtime compatibility

Если добавить проверку версии Java в `GameLaunchCommandBuilder`, построение команды запуска начнет зависеть от проверки
runtime environment

Если добавить проверку версии Java в `JavaRuntimeSelector`, selector начнет смешивать выбор executable, compatibility
validation и потенциальный fallback policy

Если сразу добавить Java installation discovery, launcher начнет решать более широкий вопрос поиска подходящей установки
Java

Поэтому перед реализацией проверки версии Java нужно определить отдельную compatibility boundary

---

## Решение

Проверка совместимости Java version должна быть отдельной runtime boundary

Эта boundary должна получать уже выбранный Java executable и требование `JavaVersionRequirement`

Java version compatibility boundary не должна

- Выбирать Java executable самостоятельно
- Выполнять Java installation discovery
- Запускать игровой процесс
- Строить команду запуска игры

Граница выглядит так

```text
JavaRuntimeSelector
    -> selected JavaExecutableReference

JavaCommandPathResolver
    -> resolved JavaExecutableReference

JavaVersionRequirement
    -> required minimum Java major version

Java runtime compatibility boundary
    -> resolved JavaExecutableReference
    -> JavaVersionRequirement
    -> compatibility result
```

`GameLaunchPlanBuilder` может использовать compatibility boundary после выбора и разрешения Java executable, но до
построения финального `GameLaunchPlan`

Compatibility boundary должна быть отделена от readiness check

Readiness check отвечает на вопрос

```text
Можно ли использовать этот исполняемый файл в качестве исполняемого файла файловой системы?
```

Compatibility check отвечает на вопрос

```text
Соответствует ли этот исполняемый файл требуемой версии Java?
```

Будущая реализация может получить отдельный контракт для проверки совместимости Java runtime

Например

```text
JavaRuntimeCompatibilityChecker
JavaRuntimeCompatibilityRequest
JavaRuntimeCompatibilityResult
```

Конкретный способ определения версии Java должен быть adapter-level detail

Запуск `java -version`, парсинг вывода и обработка platform-specific ошибок не должны протекать в доменную модель

Если в будущем появится Java installation discovery или fallback policy, они должны быть оформлены отдельными решениями

---

## Рассмотренные варианты

### Проверять Java version в `GameService`

Вариант отклонен

`GameService` должен запускать уже подготовленный `GameLaunchPlan`

Если добавить туда проверку версии Java, запуск процесса начнет отвечать за runtime selection и compatibility

### Проверять Java version в `GameLaunchCommandBuilder`

Вариант отклонен

`GameLaunchCommandBuilder` должен строить команду запуска из подготовленных данных

Если добавить туда compatibility check, command builder начнет зависеть от runtime environment

### Расширить `JavaRuntimeSelector`

Вариант отложен

`JavaRuntimeSelector` сейчас выбирает executable reference

Если добавить в него compatibility check, selector начнет смешивать выбор executable, validation и будущий fallback

Такое расширение может стать оправданным только после появления Java installation discovery или fallback policy

### Сразу добавить Java installation discovery

Вариант признан преждевременным

На момент принятия решения у проекта уже есть выбранный Java executable и manifest-provided requirement

Этого достаточно, чтобы определить compatibility boundary без поиска альтернативных Java installations

---

## Последствия

Проверка Java version получает отдельное место в Java runtime flow

`GameService` остается адаптером запуска игрового процесса

`GameLaunchCommandBuilder` остается компонентом построения команды запуска

`JavaRuntimeSelector` не превращается в Java installation discovery или fallback engine

`JavaExecutableReadinessChecker` остается отдельной проверкой filesystem readiness

`JavaVersionRequirement` начинает участвовать в runtime flow, но не смешивается с `JavaExecutableReference`

Появляется дополнительный шаг перед построением `GameLaunchPlan`

Это усложняет `GameLaunchPlanBuilder`, но сохраняет явную последовательность подготовки запуска

Будущая реализация compatibility check может потребовать adapter-level выполнения Java process и parsing version output

Ошибки compatibility check должны быть частью Java runtime failure model или отдельного уточненного failure reason,
если текущей модели окажется недостаточно

---

## Не входит в решение

- Реализация `JavaRuntimeCompatibilityChecker`
- Реализация `JavaRuntimeCompatibilityRequest`
- Реализация `JavaRuntimeCompatibilityResult`
- Запуск `java -version`
- Парсинг вывода Java version
- Java installation discovery
- Java version management
- Automatic Java provisioning
- Fallback policy при несовместимой Java version
- UI выбор Java version
- Launcher runtime requirement для работы самого launcher
- Изменение `GameService`
- Изменение `GameLaunchCommandBuilder`
- Изменение manifest JSON contract

---

## Связанные решения

- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0030: Определить границу проверки Java executable](ADR-0030-java-executable-readiness-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
- [ADR-0032: Определить границу PATH resolution для Java command name](ADR-0032-java-command-path-resolution-boundary.md)
- [ADR-0038: Определить границу configured Java override](ADR-0038-configured-java-override-boundary.md)
- [ADR-0039: Определить модель ошибок Java runtime](ADR-0039-java-runtime-failure-model.md)
- [ADR-0041: Определить границу Java version requirements](ADR-0041-java-version-requirements-boundary.md)
- [ADR-0042: Определить manifest source для Java version requirement](ADR-0042-manifest-java-version-requirement-source.md)
