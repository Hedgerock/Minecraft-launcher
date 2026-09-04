[← Назад к списку решений](README.md)

# ADR-0033: Определить интерпретацию Java executable reference из manifest metadata

## Статус

Accepted

---

## Контекст

После ADR-0031 launcher различает две формы Java executable reference

- command name
- explicit filesystem path

После ADR-0032 command name может быть разрешен через PATH-oriented lookup до readiness check

На момент принятия решения `ManifestJavaExecutableReferenceResolver` преобразует любое значение
`LaunchInfo.javaExecutable` в `JavaExecutableReference.commandName(...)`

Это сохраняет минимальный сценарий `java`, но не отражает ситуацию, где manifest metadata уже содержит
explicit filesystem path к Java executable

Если explicit filesystem path из manifest metadata продолжать трактовать как command name,
`JavaCommandPathResolver` будет пытаться искать всю строку через PATH-oriented lookup

Это делает невозможным корректный сценарий, где manifest явно указывает конкретный Java executable

---

## Решение

`ManifestJavaExecutableReferenceResolver` должен интерпретировать значение `LaunchInfo.javaExecutable` перед созданием
`JavaExecutableReference`

Если значение выглядит как filesystem path, resolver должен возвращать

```text
JavaExecutableReference.explicitPath(...)
```

Если значение выглядит как имя команды, resolver должен возвращать

```text
JavaExecutableReference.commandName(...)
```

Минимальное правило интерпретации

- Значение с path separator считается explicit filesystem path
- Значение без path separator считается command name

```text
LaunchInfo.javaExecutable
    -> ManifestJavaExecutableReferenceResolver
        -> JavaExecutableReference.commandName
        -> JavaExecutableReference.explicitPath
```

`ManifestJavaExecutableReferenceResolver` не должен проверять существование файла

`ManifestJavaExecutableReferenceResolver` не должен выполнять PATH lookup

`ManifestJavaExecutableReferenceResolver` не должен выбирать Java version

---

## Последствия

Manifest-provided command name `java` продолжает проходить через PATH-oriented lookup

Manifest-provided explicit filesystem path пропускает PATH-oriented lookup и передается в readiness
check как explicit path

`DefaultJavaExecutableReadinessChecker` становится точкой проверки существования explicit filesystem path

`JavaCommandPathResolver` остается ответственным только за command name resolution

`GameLaunchCommandBuilder` продолжает получать уже интерпретированную и подготовленную Java executable reference

---

## Не входит в решение

- Проверка существования Java executable
- PATH lookup
- Поиск Java installations вне `PATH`
- Выбор Java version
- Автоматическая установка Java
- Изменение manifest JSON contract
- Изменение `GameService`
- Изменение `GameLaunchCommandBuilder`
- Поддержка configured Java override

---

## Связанные решения

- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0030: Определить границу проверки Java executable](ADR-0030-java-executable-readiness-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
- [ADR-0032: Определить границу PATH resolution для Java command name](ADR-0032-java-command-path-resolution-boundary.md)
