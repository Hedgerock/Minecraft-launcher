# Контракт manifest JSON

## Назначение

`manifest JSON` описывает минимальные данные, необходимые launcher для проверки файлов,
восстановления отсутствующих ресурсов и построения плана запуска игры

На текущем этапе контракт отражает только те поля, которые уже используются runtime-жизненным циклом

---

## Владелец

Разбор JSON принадлежит модулю `launcher-api`

`launcher-core` не зависит от JSON-формата и работает только с доменной моделью `Manifest`

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
  "launch-info": {
    "mainClass": "net.minecraft.client.main.Main",
    "jvmArgs": ["-Xmx2G"],
    "gameArgs": ["--username", "Player"]
  }
}
```

---

## Правила преобразования

`minecraftVersion` переносится в `Manifest.minecraftVersion`

`loader` преобразуется в `LoaderInfo`

`files` преобразуется в список `FileEntry`

`launch-info` преобразуется в `LaunchInfo`

`JsonManifestMapper` не выполняет запуск, загрузку файлов или проверку хеша

Его ответственность ограничена преобразованием внешнего JSON-контракта в доменную модель

---

## Подстановка аргументов запуска

`launchinfo.jvmargs` и `launchinfo.gameArgs` могут содержать поддерживаемые подстановки

На текущем этапе поддерживается

| Подстановка         | Значение                                          |
|---------------------|---------------------------------------------------|
| `${version_name}`   | Версия Minecraft из `Manifest.minecraftVersion`   |
| `${game_directory}` | Путь к игровой дироектории из `DirectoryProvider` |

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

Если JSON невозможно прочитать или преобразовать в корректный `Manifest`, mapper завершает работу ошибкой
`ManifestMappingException`

---

## Не входит в контракт текущей версии

- Описание libraries/classpath
- Выбор Java runtime
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