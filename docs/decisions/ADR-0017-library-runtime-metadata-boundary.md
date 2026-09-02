[← Назад к списку решений](README.md)

# ADR-0017: Определить границу runtime metadata для libraries

## Статус

Accepted

## Контекст

На текущем этапе manifest содержит `libraries`, которые преобразуются в `LibraryEntry`

`LibraryEntry` содержит минимальную физическую метадату

- `path`
- `sha256`
- `size`
- `url`

Эта модель уже используется в нескольких runtime-сценариях

- Как источник classpath entries
- Как источник recoverable resources через `ManifestResources`
- Как часть verification/download lifecycle

После добавления безопасного разрешения путей `LibraryEntry.path` также проходит
через общий `ResourcePathResolver` при построении `GameClasspath`

При этом полный library contract Minecraft manifest шире текущей модели и может включать

- `downloads.artifact`
- `downloads.classifiers`
- `natives`
- `rules`
- OS-specific выбор зависимостей
- Правила распаковки natives
- Исключения при распаковке

Если начать добавлять эти поля напрямую в `LibraryEntry`, модель быстро смешает две разные
ответственности

- Описание уже выбранного recoverable library artifact
- Правила выбора подходящего artifact для конкретного runtime окружения

## Решение

`LibraryEntry` остается моделью уже выбранного library artifact

Она описывает только тот ресурс, который

- Должен попасть в classpath
- Может быть проверен через verification flow
- Может быть восстановлен через download flow
- Имеет физическую метадату `path`, `sha256`, `size` и `url`

`LibraryEntry` не становится полной моделью исходного Minecraft library contract

Metadata, необходимая для выбора подходящего library artifact, должна вводиться отдельно от
`LibraryEntry`

Для будущих итераций выделяется отдельная зона ответственности


```text
Raw library manifest metadata
    -> Runtime library selection
        -> LibraryEntry
            -> GameClasspath
            -> ManifestResources
```

Это означает, что `LibraryEntry` находится после выбора runtime-compatible artifact

Она не должна знать

- Почему выбран именно этот артефакт
- Какие classifiers были доступны
- Какие rules были применены
- Какие natives подходят для текущей OS
- Какие extract/exclude правила нужны для распаковки natives

## Последствия

`LibraryEntry` остается простой и стабильной моделью

Verification/download flow продолжает работать через `ManifestResources`

Classpath building продолжает использовать `Manifest.libraries`, но получает уже выбранные library entries

Полная поддержка Minecraft libraries должна появиться отдельными итерациями до формирования LibraryEntry

Будущие модели могут описывать

- Raw library metadata
- Artifact metadata
- Classifiers
- Natives mapping
- OS/rules matching
- Native extraction metadata

Эти модели не должны ломать текущий контракт `LibraryEntry`, если runtime selection уже может
преобразовать их в выбранный `LibraryEntry`

## Не входит в решение

- Реализация парсера полного Minecraft library contract
- Выбор natives по OS
- Распаковка natives
- Maven coordinates
- Правила загрузки classifiers
- Изменение verification/download lifecycle
- Изменение `ResourceEntry`
- Изменение `ManifestResources`

## Связанные решения

- [ADR-0014: Использовать manifest resources как источник verification flow](ADR-0014-manifest-resources-verification-flow.md)
- [ADR-0015: Зафиксировать минимальный scope library metadata](ADR-0015-minimal-library-metadata-scope.md)
- [ADR-0016: Зафиксировать правила безопасности resource-path](ADR-0016-resource-path-safety.md)