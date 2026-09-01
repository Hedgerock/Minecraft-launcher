# ADR-0027: Определить передачу директории natives в launch arguments

## Статус

Accepted

---

## Контекст

После развития native extraction flow launcher умеет выбирать native artifacts для текущей `OperatingSystem`,
загружать их как обычные ресурсы и распаковывать в директорию `LauncherDirectories.nativesDirectory`

`EXTRACT_NATIVES` выполняется после `PREPARE_DIRECTORIES` и до `BUILD_GAME_LAUNCH_PLAN`

`DefaultNativeExtractionService` использует `NativeExtractionPlan` и применяет `extract.exclude` rules при распаковке
archive entries

Однако после распаковки natives директория `natives` пока не участвует в построении launch command

Для JVM-based запуска native libraries должны быть доступны процессу игры через launch argument

Если `GameLaunchPlanBuilder` начнет самостоятельно добавлять JVM argument, для natives directory,
manifest launch metadata и launch command building начнут смешиваться

Launcher уже поддерживает подстановку launch variables в `launchInfo.jvmArgs` и `launchInfo.gameArgs`

На текущем этапе `LaunchVariables` содержит

- `versionName`
- `gameDirectory`
- `classpath`

Эта модель уже является точкой передачи runtime paths в `DefaultLaunchArgumentResolver`

---

## Решение

Директория natives должна передаваться в launch command через launch variable

`LaunchVariables` должен получить новое поле

- `nativesDirectory`

`GameLaunchPlanBuilder` должен брать директорию natives из `DirectoryProvider`

```text
DirectoryProvider
    -> LauncherDirectories.nativesDirectory
        -> GameLaunchPlanBuilder
            -> LaunchVariables.nativesDirectory
```

`DefaultLaunchArgumentResolver` должен поддержать новую подстановку

```text
${natives_directory}
```

Minecraft launch metadata должен оставаться источником конкретного JVM argument

Например

```text
-Djava.library.path=${natives_directory}
```

Граница выглядит так

```text
Manifest.launchInfo.jvmArgs
    -> DefaultGameLaunchCommandBuilder
        -> DefaultLaunchArgumentResolver
            -> LaunchVariables.nativesDirectory
                -> resolved launch command
```

`GameLaunchPlanBuilder` не должен самостоятельно добавлять `-Djava.library.path`

Он только передает значение `nativesDirectory` в `LaunchVariables`

`DefaultLaunchArgumentResolver` не должен знать, зачем используется директория natives

Он только выполняет подстановку `${natives_directory}`

---

## Последствия

Native extraction flow получает связь с launch command без смешивания ответственности

Manifest metadata остается источником конкретных launch arguments

`GameLaunchPlanBuilder` продолжает собирать runtime context, но не принимает решение о составе
JVM arguments

`DefaultLaunchArgumentResolver` расширяется новой поддерживаемой подстановкой

Если manifest не содержит `${natives_directory}`, launch command не изменяется

Если manifest содержит `${natives_directory}`, значение берется из `LauncherDirectories.nativesDirectory`

Для полноценного Minecraft-style запуска manifest сможет указывать JVM argument вида
`-Djava.library.path=${natives_directory}`

---

## Не входит в решение

- Автоматическое добавление `-Djava.library.path`
- Изменение `EXTRACT_NATIVES` operation
- Изменение `DefaultNativeExtractionService`
- Очистка директории natives перед запуском
- Версионирование директории natives
- Architecture-specific natives selection
- Проверка фактического наличия native files перед запуском
- Автоматический выбор Java runtime
- Изменение game classpath flow

---

## Связанные решения

- [ADR-0016: Зафиксировать правила безопасности resource path](ADR-0016-resource-path-safety.md)
- [ADR-0021: Определить границу обработки native artifacts](ADR-0021-native-artifact-processing-boundary.md)
- [ADR-0023: Использовать RuntimeLibrarySelection.libraries как источник game classpath](ADR-0023-use-libraries-as-game-classpath-source.md)
- [ADR-0024: Определить границу native extraction operation](ADR-0024-native-extraction-operation-boundary.md)
- [ADR-0025: Определить реализацию native extraction service](ADR-0025-native-extraction-service-implementation.md)
- [ADR-0026: Определить правила исключения при распаковке native artifacts](ADR-0026-native-extraction-exclude-rules.md)
