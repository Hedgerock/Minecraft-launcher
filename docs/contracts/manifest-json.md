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
    -> JsonManifestMapper
        -> Manifest
            -> LauncherEngine
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
      "url": "https://localhost/libraries/org/example/example.jar"
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

`libraries` преобразуется в список `LibraryEntry`

`launchInfo.javaExecutable` используется как первый элемент команды запуска игры

`JsonManifestMapper` не выполняет запуск, загрузку файлов или проверку хеша

Его ответственность ограничена преобразованием внешнего JSON-контракта в доменную модель

`JavaLauncherHttpClient` отвечает только за получение JSON-строки

`JsonManifestMapper` отвечает только за преобразование JSON-строки в `Manifest`

---

## Подстановка аргументов запуска

`launchInfo.jvmArgs` и `launchInfo.gameArgs` могут содержать поддерживаемые подстановки

На текущем этапе поддерживается

| Подстановка         | Значение                                                                                    |
|---------------------|---------------------------------------------------------------------------------------------|
| `${version_name}`   | Версия Minecraft из `Manifest.minecraftVersion`                                             |
| `${game_directory}` | Путь к игровой директории из `DirectoryProvider`                                            |
| `${classpath}`      | Отформатированный classpath, построенный из `libraries` или fallback `launchInfo.classpath` |

`libraries` является основным источником classpath entries и описывает библиотеки для построения classpath

На текущем этапе `LibraryEntry` содержит `path`, `sha256`, `size` и `url`

Даже после добавления physical metadata `LibraryEntry` остается library-specific metadata и не заменяет `FileEntry`

Включение `libraries` в verification/download flow является отдельным архитектурным решением

Если `libraries` пустой, launcher использует `launchInfo.classpath` как fallback для минимальных сценариев

После добавления physical metadata `libraries` могут быть использованы как источник восстанавливаемых ресурсов

Однако `LibraryEntry` не должен напрямую заменять `FileEntry`

Для verification/download flow должен использоваться общий resource-level contract, который выражает только `path`,
`sha256`, `size` и `url`

`GameClasspathBuilder` продолжает использовать `LibraryEntry`, потому что построение classpath зависит от семантики
`libraries`

Во время построения `GameLaunchPlan` выбранный источник classpath преобразуется в `GameClasspath`,
разрешается относительно игровой директории и формируются в строку с использованием системного разделителя путей

```text
Manifest.libraries
    -> GameClasspathBuilder
        -> GameClasspath
            -> ClasspathFormatter
                -> LaunchVariables.classpath
                    -> ${classpath}
                    
fallback:
    launchInfo.classpath
        -> GameClasspathBuilder
```

Полная модель `libraries`, правила выбора natives и OS-specific зависимости пока не входят в контракт текущей версии

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

- Полная модель libraries с metadata, rules, classifiers и natives
- Автоматический выбор Java runtime
- Проверка существования Java executable на файловой системе
- Правило подстановки переменных
- Аргументы авторизации
- Assets index
- Natives
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