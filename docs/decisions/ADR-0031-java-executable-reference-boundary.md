[← Назад к списку решений](README.md)

# ADR-0031: Определить границу Java executable reference

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(core): add java executable reference model`
> `feat(core): use java executable reference in launch plan flow`

---

## Контекст

После ADR-0029 launcher получил отдельный контракт выбора Java runtime — `JavaRuntimeSelector`

После ADR-0030 launcher получил отдельный контракт проверки готовности выбранного
Java executable — `JavaExecutableReadinessChecker`

`GameLaunchPlanBuilder` теперь выбирает Java executable через `JavaRuntimeSelector`, выполняет readiness check
и передает выбранное значение в `GameLaunchCommandBuilder`

На текущем этапе application assembly использует `NoOpJavaExecutableReadinessChecker`

Это сделано потому, что `LaunchInfo.javaExecutable` хранится как строка из manifest metadata и может означать разные
формы ссылки на Java executable

Например

```text
java
```

может означать command name, который должен быть найден через PATH

```text
C:/Program Files/Eclipse Adoptium/jdk-21/bin/java.exe
```

может означать explicit filesystem path

Если проверять оба значения одинаково через `Files.exists(...)`, command name `java` будет
ошибочно считаться недоступным, даже если Java доступна через PATH

Для дальнейшего развития Java runtime flow нужно определить, где проходит граница между command name,
explicit path и будущим PATH resolution

---

## Решение

`LaunchInfo.javaExecutable` остается manifest-provided metadata в виде строки

Эта строка не должна автоматически считаться filesystem path

Java executable reference должен быть отдельной смысловой границей между manifest metadata и filesystem readiness

На текущем этапе launcher различает две формы Java executable reference

- command name
- explicit filesystem path

Command name описывает имя executable, которое может быть найдено окружением process через PATH

Explicit filesystem path описывает путь к конкретному executable file

`DefaultJavaExecutableReadinessChecker` должен применяться только к explicit filesystem path
сценариям

`NoOpJavaExecutableReadinessChecker` остается безопасной application assembly policy для текущего
manifest-provided command name сценария

Будущий resolver может преобразовать command name в explicit filesystem path до вызова filesystem readiness check

Граница выглядит так

```text
LaunchInfo.javaExecutable
    -> Java executable reference
        -> command name
            -> future PATH resolution
        -> explicit filesystem path
            -> JavaExecutableReadinessChecker
```

`GameLaunchCommandBuilder` не должен различать command name и explicit path

Он получает уже выбранное значение Java executable и добавляет его в command

`GameService` не должен выполнять PATH resolution или filesystem readiness check

---

## Последствия

Текущий сценарий manifest-provided `java` не ломается из-за filesystem проверки

Проект получает явную границу для будущего PATH resolution

`NoOpJavaExecutableReadinessChecker` перестает выглядеть как временный костыль без причины

`DefaultJavaExecutableReadinessChecker` остается полезным для explicit filesystem path сценариев

Модель `JavaExecutableReference` может использоваться без изменения ответственности `GameLaunchCommandBuilder` и
`GameService`

В будущем можно будет добавить поддержку

- `JavaExecutableReference`
- PATH-based command resolution
- Configured Java executable path
- Detected Java installations
- Readable failure для command name, который не найден в PATH

---

## Не входит в решение

- Реализация PATH resolution
- Поиск Java installations
- Выбор Java version
- Fallback на системную Java
- Автоматическая установка Java
- Изменение manifest JSON contract
- Изменение `GameService`
- Изменение `GameLaunchCommandBuilder`
- Подключение `DefaultJavaExecutableReadinessChecker` в application assembly

---

## Связанные решения

- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0030: Определить границу проверки Java executable](ADR-0030-java-executable-readiness-boundary.md)
