# Контракт manifest JSON

## Назначение

`manifest JSON` описывает минимальные данные, необходимые launcher для проверки файлов,
восстановления отсутствующих ресурсов и построения плана запуска игры

На текущем этапе контракт отражает только те поля, которые уже используются runtime-жизненным циклом

---

## Владелец

Разбор JSON принадлежит модулю `launcher-api`

`launcher-core` не зависит от JSON-формата и работает только с доменной моделью `Manifest`

Получение manifest JSON по HTTP принадлежит модулю `launcher-api`

`JavaLauncherHttpClient` выполняет HTTP GET и возвращает тело ответа как строку

`HttpManifestClient` использует `LauncherHttpClient` для загрузки JSON по `manifestUri`

```text
JSON manifest
    -> ManifestJson
        -> RuntimeLibraryMetadata
            -> LibraryArtifactMetadata

RuntimeEnvironment
    -> RuntimeLibrarySelector
        -> LibraryEntry
            -> Manifest
```

---

## Минимальная структура

```json
{
  "minecraftVersion": "1.12.1",
  "loader": {
    "type": "fabric",
    "version": "0.16.10"
  },
  "files": [
    {
      "path": "mods/example.jar",
      "sha256": "cb285f51cd38267e1e85b1b174578b11b35460edb630ad7b861d15c6d78698a6",
      "size": 123456789,
      "url": "https://localhost/files/mods/example.jar"
    }
  ],
  "launchInfo": {
    "mainClass": "net.minecraft.client.main.Main",
    "jvmArgs": ["-Xmx2G"],
    "gameArgs": ["--username", "Player"],
    "classpath": ["libraries/example.jar", "client.jar"],
    "javaExecutable": "java"
  },
  "libraries": [
    {
      "path": "libraries/org/example/example.jar",
      "sha256": "535436571185ff3d2564aceead2dfece7bf7b69fc80ea0174a508b133110af2e",
      "size": 987654321,
      "url": "https://localhost/libraries/org/example/example.jar",
      "rules": [
        {
          "action": "allow",
          "os": "windows"
        }
      ],
      "classifiers": {
        "natives-windows": {
          "path": "libraries/org/example/example/natives-windows.jar",
          "sha256": "0a25f1fc0a064dcfb819195351e236343acc842edb439dd2e3339e9e48980fb4",
          "size": 123456789,
          "url": "https://localhost/files/libraries/org/example/example/natives-windows.jar"
        }
      },
      "natives": {
        "windows": "natives-windows"
      },
      "extract": {
        "exclude": [
          "META-INF/"
        ]
      }
    }
  ]
}
```

---

## Правила преобразования

`minecraftVersion` переносится в `Manifest.minecraftVersion`

`loader` преобразуется в `LoaderInfo`

`files` преобразуется в список `FileEntry`

`launchInfo` преобразуется в `LaunchInfo`

`ResourceEntry` является общей защитой уровня ресурсов для физической метадаты `path`, `sha256`, `size` и `url`

`ManifestResources` строит список `ResourceEntry` из `Manifest.files` и `Manifest.libraries`

`FileEntry` и `LibraryEntry` остаются manifest-specific моделями и не заменяются напрямую `ResourceEntry`

На текущем этапе `ManifestResources` используется как источник verification flow, поэтому `Manifest.files`
и `Manifest.libraries` участвуют в verification/download lifecycle через общий `ResourceEntry` контракт

`libraries` сначала преобразуется в список `RuntimeLibraryMetadata`

Каждая `RuntimeLibraryMetadata` содержит `LibraryArtifactMetadata`, описывающий downloadable artifact
библиотеки, список `rules`, `classifiers` и `natives` metadata

`LibraryArtifactMetadata` содержит физическую метадату

- `path`
- `sha256`
- `size`
- `url`

`classifiers` преобразуется в `LibraryClassifiersMetadata`

`natives` преобразуется в `LibraryNativesMetadata`

Отсутствие `classifiers` и `natives` означает пустые metadata

`extract.exclude` преобразуется в `NativeExtractionRules`

Отсутствие `extract` или `extract.exclude` означает пустые правила распаковки

`RuntimeLibrarySelector` сохраняет extraction rules вместе с selected native artifact

`DefaultNativeExtractionService` использует правила при распаковке native artifacts

Затем `RuntimeLibrarySelector` формирует `RuntimeLibrarySelection` на основе `RuntimeLibraryMetadata`

`RuntimeLibrarySelection` разделяет выбранные обычные libraries и selected native artifacts

Для совместимости текущего verification/download flow `Manifest.libraries` содержит compatibility projection
через `RuntimeLibrarySelection.selectedArtifacts()`

`RuntimeLibrarySelector` получает `RuntimeEnvironment` как входные данные

Текущий `RuntimeEnvironment` предоставляется через `RuntimeEnvironmentProvider`

На текущем этапе `RuntimeEnvironment` содержит только `OperatingSystem`

`RuntimeLibraryMetadata` также может содержать список `LibraryRule`

Отсутствие `rules` означает, что library доступна для любого `RuntimeEnvironment`

Если `rules` указаны, `DefaultRuntimeLibrarySelector` выбирает library на основе текущего
`RuntimeEnvironment.operatingSystem`

Селектор учитывает только правила, совпадающие с текущей `OperatingSystem`

Если совпадающих правил несколько, применяется последнее совпадающее правило

Library включается в `RuntimeLibrarySelection.libraries`, если последнее совпадающее правило имеет
`action` `ALLOW`

Library исключается из `RuntimeLibrarySelection.libraries`, если совпадающих правил нет или последнее
совпадающее правило имеет `action` `DISALLOW`

`launchInfo.javaExecutable` описывает manifest-provided Java executable

На текущем этапе `ManifestJavaRuntimeSelector` использует это значение как источник выбранного
Java executable для построения команды запуска игры

`JsonManifestMapper` не выполняет запуск, загрузку файлов или проверку хеша

Его ответственность ограничена преобразованием внешнего JSON-контракта в доменную модель

`JavaLauncherHttpClient` отвечает только за получение JSON-строки

`JsonManifestMapper` отвечает только за преобразование JSON-строки в `ManifestLoadResult`

`ManifestLoadResult` содержит `Manifest` и `RuntimeLibrarySelection`

---

## Подстановка аргументов запуска

`launchInfo.jvmArgs` и `launchInfo.gameArgs` могут содержать поддерживаемые подстановки

На текущем этапе поддерживается

| Подстановка            | Значение                                                                                    |
|------------------------|---------------------------------------------------------------------------------------------|
| `${version_name}`      | Версия Minecraft из `Manifest.minecraftVersion`                                             |
| `${game_directory}`    | Путь к игровой директории из `DirectoryProvider`                                            |
| `${classpath}`         | Отформатированный classpath, построенный из `libraries` или fallback `launchInfo.classpath` |
| `${natives_directory}` | Путь к директории natives из `DirectoryProvider`                                            |

`libraries` является основным источником classpath entries и описывает библиотеки для построения classpath

На текущем этапе `LibraryEntry` содержит `path`, `sha256`, `size` и `url`

Даже после добавления physical metadata `LibraryEntry` остается library-specific metadata и не заменяет `FileEntry`

Включение `libraries` в verification/download flow зафиксировано отдельным архитектурным решением и реализовано через
`ManifestResources`

Если `RuntimeLibrarySelection.libraries` пустой, launcher использует `launchInfo.classpath` как fallback для минимальных
сценариев

После добавления physical metadata `libraries` могут быть использованы как источник восстанавливаемых ресурсов

Однако `LibraryEntry` не должен напрямую заменять `FileEntry`

Для verification/download flow должен использоваться общий resource-level contract, который выражает только `path`,
`sha256`, `size` и `url`

`GameClasspathBuilder` строит classpath из `RuntimeLibrarySelection.libraries`

Если `RuntimeLibrarySelection.libraries` пустой, launcher использует fallback `launchInfo.classpath`

Во время построения `GameLaunchPlan` выбранный источник classpath преобразуется в `GameClasspath`,
разрешается относительно игровой директории и форматируется в строку с использованием системного разделителя путей

Перед добавлением в `GameClasspath` каждый classpath entry разрешается через общий `ResourcePathResolver` относительно
игровой директории

`Manifest.libraries` остается compatibility projection для verification/download flow и не является источником
game classpath

`GameClasspathBuilder` не выполняет прямой `gameDirectory.resolve(...)`, а делегирует построение локального
пути общему resolver-у

```text
RuntimeLibrarySelection.libraries
    -> GameClasspathBuilder
        -> GameClasspath
            -> ClasspathFormatter
                -> LaunchVariables.classpath
                    -> ${classpath}

fallback:
    launchInfo.classpath
        -> GameClasspathBuilder
```

Модель `libraries` поддерживает OS-specific rules, classifiers и natives metadata на уровне JSON mapping
и runtime library selection

Подстановки применяются во время построения команды запуска

```text
LaunchInfo
    -> GameLaunchPlanBuilder
        -> LaunchVariables
            -> LaunchArgumentResolver
                -> GameLaunchPlan.command
```
Неизвестные подстановки сохраняются без изменения

---

## Валидация

Базовая структурная валидация выполняется во время преобразования JSON

Предметная валидация остается ответственностью доменных моделей

- `Manifest`
- `LoaderInfo`
- `FileEntry`
- `LaunchInfo`
- `LibraryEntry`

Если JSON невозможно прочитать или преобразовать в корректный `Manifest`, mapper завершает работу ошибкой
`ManifestMappingException`

`LaunchInfo` требует непустые значения `mainClass` и `javaExecutable`

`Manifest` требует наличие списка `libraries`, пустой список допустим для минимальных сценариев

---

## Не входит в контракт текущей версии

- Правила без OS
- Правила на основе features
- Правила на основе architecture
- Автоматический выбор Java runtime
- Проверка существования Java executable на файловой системе
- Расширение механизма подстановки переменных
- Аргументы авторизации
- Assets index
- Loader-specific правила запуска

Данные поля должны появляться отдельными итерациями после появления подтвержденных runtime-сценариев

---

## Связанные компоненты

- `JsonManifestMapper`
- `ManifestMapper`
- `HttpManifestService`
- `Manifest`
- `LoaderInfo`
- `FileEntry`
- `LaunchInfo`