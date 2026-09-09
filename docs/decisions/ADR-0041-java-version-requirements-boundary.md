[← Назад к списку решений](README.md)

# ADR-0041: Определить границу Java version requirements

## Статус

Accepted

---

## Контекст

После `v0.5.0-java-runtime-foundation` launcher умеет выбрать Java executable, разрешить command name через
PATH-oriented lookup и проверить readiness explicit filesystem path

После configured Java override и Java runtime failure model Java executable flow получил более явные границы выбора и
ошибок

На момент принятия решения launcher еще не различает требуемую Java version и фактически выбранную Java runtime

`LaunchInfo.javaExecutable` описывает только executable reference

Этого достаточно, чтобы построить команду запуска, но недостаточно, чтобы выразить требование вида

```text
game version requires Java 17
launcher should use Java 21
profile requires at least Java 17
```

Если добавить Java version check в `GameService`, запуск процесса начнет отвечать за runtime compatibility

Если добавить Java version check в `GameLaunchCommandBuilder`, построение команды начнет проверять runtime environment

Если добавить Java version requirement напрямую в `JavaExecutableReference`, ссылка на executable начнет смешиваться с
требованиями совместимости

Если сразу добавить Java installation discovery, launcher начнет решать более широкий вопрос поиска и выбора подходящей
установки Java

Перед расширением manifest metadata или runtime selection нужно отделить Java version requirement от Java executable
selection и Java installation discovery

---

## Решение

Java version requirement должен быть отдельной моделью runtime requirement

Он описывает требуемую версию Java, но не описывает конкретный executable, installation path или способ поиска Java

`JavaExecutableReference` остается моделью ссылки на executable

`JavaRuntimeSelector` продолжает выбирать Java executable reference и не становится Java installation
discovery component

Проверка соответствия выбранного executable требованиям Java version должна быть отдельной boundary, если появится
подтвержденный сценарий

Например

```text
JavaVersionRequirement
    -> required version range

JavaExecutableReference
    -> command name
    -> explicit filesystem path

Future compatibility boundary
    -> selected executable
    -> version requirement
    -> compatibility result
```

Manifest metadata может стать одним из источников Java version requirement

Launcher configuration также может стать источником override или локального требования

Если несколько источников требований появятся одновременно, приоритеты между ними должны быть определены
отдельным решением

На момент принятия решения не вводится выбор Java installation и не выполняется запуск `java -version`

Граница выглядит так

```text
manifest metadata / launcher configuration
    -> JavaVersionRequirement

Java runtime selection
    -> JavaExecutableReference

Future compatibility check
    -> JavaExecutableReference
    -> JavaVersionRequirement
    -> compatibility result
```

---

## Последствия

Java executable selection остается отделенным от Java version compatibility

`GameService` не получает ответственность за проверку версии Java

`GameLaunchCommandBuilder` продолжает строить команду из уже подготовленных данных

`JavaExecutableReference` не раздувается до модели Java runtime identity

Появляется место для будущей проверки Java version без немедленного Java installation discovery

Manifest JSON contract может быть расширен позже отдельной итерацией

Configured Java override не начинает автоматически означать совместимость с требованиями версии

Java installation discovery, fallback policy и automatic provisioning остаются отложенными темами

---

## Не входит в решение

- Изменение manifest JSON contract
- Добавление `JavaVersionRequirement` в production code
- Запуск `java -version`
- Парсинг вывода Java version
- Проверка совместимости Java version
- Java installation discovery
- Java version management
- Automatic Java provisioning
- Fallback policy при несовместимой версии Java
- UI выбор Java version
- Хранение пользовательского Java runtime preference
- Изменение `GameService`
- Изменение `GameLaunchCommandBuilder`

---

## Связанные решения

- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
- [ADR-0032: Определить границу PATH resolution для Java command name](ADR-0032-java-command-path-resolution-boundary.md)
- [ADR-0038: Определить границу configured Java override](ADR-0038-configured-java-override-boundary.md)
- [ADR-0039: Определить модель ошибок Java runtime](ADR-0039-java-runtime-failure-model.md)
- [ADR-0040: Определить границу operation failure diagnostics](ADR-0040-operation-failure-diagnostics-boundary.md)
