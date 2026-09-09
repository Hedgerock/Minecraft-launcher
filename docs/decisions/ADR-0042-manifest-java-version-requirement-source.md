[← Назад к списку решений](README.md)

# ADR-0042: Определить manifest source для Java version requirement

## Статус

Accepted

---

## Контекст

После ADR-0041 в проекте появилась отдельная граница Java version requirements

`JavaVersionRequirement` описывает требование к версии Java, но не описывает Java executable,
installation path или способ поиска Java

На момент принятия решения модель `JavaVersionRequirement` уже добавлена в `launcher-model`, но еще не имеет источника
в manifest metadata или launcher configuration

Сейчас `LaunchInfo.javaExecutable` описывает manifest-provided Java executable reference

Этого достаточно для выбора executable, PATH resolution и readiness check, но не выражает требование к версии Java для
запуска конкретной сборки

Java version requirement может появиться из разных источников

- manifest metadata
- launcher configuration
- profile metadata
- user preference
- future launcher runtime policy

Если первым источником сделать launcher configuration, launcher начнет смешивать требование сборки и локальное
предпочтение пользователя

Если добавить requirement в `JavaExecutableReference`, executable reference начнет отвечать за runtime compatibility

Если сразу добавить compatibility check, launcher начнет проверять выбранный executable до определения источника
требования

Поэтому первым source-of-truth для требования версии Java должен стать manifest launch metadata

---

## Решение

Manifest launch metadata может содержать Java version requirement для запуска конкретной сборки

`LaunchInfo` должен стать владельцем manifest-provided Java version requirement на уровне доменной модели

`LaunchInfo.javaExecutable` продолжает описывать Java executable reference

Java version requirement должен храниться отдельно от Java executable reference

Минимальная доменная форма требования выражается через `JavaVersionRequirement`

На первом шаге requirement описывает только минимальную major version Java

Граница выглядит так

```text
manifest JSON
    -> launchInfo
        -> java executable metadata
        -> java version requirement

launcher-api
    -> maps manifest JSON into LaunchInfo

LaunchInfo
    -> javaExecutable
    -> JavaVersionRequirement
```

Manifest metadata становится источником требования, но не выполняет проверку совместимости

`JsonManifestMapper` и manifest DTO отвечают только за преобразование JSON в доменную модель

`JavaRuntimeSelector` продолжает выбирать Java executable reference

`JavaExecutableReadinessChecker` продолжает проверять filesystem readiness executable

Compatibility check выбранного executable с Java version requirement должен быть отдельной boundary, если появится
подтвержденный сценарий

Launcher configuration может получить собственный Java version requirement или override позже, но приоритет между
manifest requirement и configuration requirement должен быть отдельным решением

Требование Java version для самого launcher также не смешивается с manifest-provided game runtime requirement

Например, если будущая concurrency model потребует virtual threads и Java 21 для работы launcher, это должно быть
отдельным launcher runtime requirement, а не частью manifest game runtime metadata

---

## Последствия

Manifest становится первым источником Java version requirement для game runtime

`LaunchInfo` начинает описывать не только executable metadata, но и минимальное требование к версии Java

`JavaExecutableReference` остается простой моделью ссылки на executable

`JavaRuntimeSelector`, `GameLaunchCommandBuilder` и `GameService` не получают ответственность за проверку версии Java

Manifest JSON contract может быть расширен отдельной кодовой итерацией

Появляется основа для будущего compatibility check без Java installation discovery

Launcher runtime requirement и game runtime requirement остаются разными понятиями

---

## Не входит в решение

- Проверка совместимости Java version
- Запуск `java -version`
- Парсинг вывода Java version
- Java installation discovery
- Java version management
- Automatic Java provisioning
- Fallback policy при несовместимой Java version
- UI выбор Java version
- Launcher runtime requirement для работы самого launcher
- Требование Java 21 для virtual threads
- Приоритет configuration requirement над manifest requirement
- Изменение `JavaRuntimeSelector`
- Изменение `JavaExecutableReadinessChecker`
- Изменение `GameService`
- Изменение `GameLaunchCommandBuilder`

---

## Связанные решения

- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
- [ADR-0038: Определить границу configured Java override](ADR-0038-configured-java-override-boundary.md)
- [ADR-0041: Определить границу Java version requirements](ADR-0041-java-version-requirements-boundary.md)
