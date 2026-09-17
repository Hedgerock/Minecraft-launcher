[← Назад к списку решений](../README.md)

# ADR-0049: Определить границу assets index flow

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(model): add assets index model`
> `feat(model): add assets index to manifest`
> `feat(api): map assets index from manifest json`
> `feat(model): include assets in manifest resources`

---

## Контекст

На момент принятия решения manifest contract уже описывает несколько типов recoverable resources

- `files`
- runtime-compatible `libraries`
- selected native artifacts через compatibility projection

Verification/download lifecycle работает не с исходными manifest-specific моделями напрямую, а через общий resource-level
contract

```text
Manifest
    -> ManifestResources
        -> ResourceEntry
            -> VerificationPlan
            -> DownloadPlan
```

Такой подход уже позволил подключить libraries и native artifacts к verification/download flow без смешивания
library-specific metadata с generic resource lifecycle

После завершения library/native flow следующим естественным manifest metadata направлением становится assets index

Assets нужны launcher runtime для восстановления asset resources, но они не являются

- Обычными files
- Libraries
- Native artifacts
- Game classpath entries
- Launch arguments

Если добавить assets напрямую в `files` или `libraries`, manifest model начинает терять семантику исходных данных

Если добавить отдельный lifecycle operation сразу, `LauncherEngine` начнет усложняться до появления подтвержденной
необходимости

Поэтому assets index flow должен сначала получить собственную boundary model и projection в существующий
verification/download lifecycle

---

## Решение

Assets index должен быть отдельной частью manifest domain model

Assets не должны моделироваться как `FileEntry` или `LibraryEntry`

Для assets должен быть введен отдельный manifest-specific слой, который описывает asset metadata до projection в
recoverable resources

На первом этапе assets index должны участвовать в verification/download flow через общий `ResourceEntry` contract

`ManifestResources` остается точкой, где manifest-specific resources преобразуются в recoverable resources

Граница выглядит так

```text
Manifest assets metadata
    -> AssetEntry / AssetsIndex
        -> ManifestResources
            -> ResourceEntry
                -> verification/download flow
```

Assets index flow не должен менять semantic ownership существующих моделей

- `FileEntry` остается моделью обычных manifest files
- `LibraryEntry` остается моделью выбранного runtime-compatible library artifact
- `RuntimeLibrarySelection` остается source-of-truth для library/native runtime selection
- `ResourceEntry` остается generic recoverable resource contract
- `ManifestResources` остается projection layer для verification/download flow

На первом этапе assets не требуют отдельной launcher operation

Существующие этапы

```text
VERIFY_FILES
BUILD_DOWNLOAD_PLAN
DOWNLOAD_FILES
```

должны иметь возможность работать с asset resources после projection в `ResourceEntry`

---

## Рассмотренные варианты

### Добавить assets в `files`

Вариант отклонен

`files` описывает обычные manifest files

Assets имеют собственную семантику и в будущем могут потребовать отдельные правила хранения, индексации, layout или
cleanup policy

Если добавить assets в `files`, manifest model станет проще технически, но потеряет domain meaning

### Добавить assets в `libraries`

Вариант отклонен

`libraries` уже имеют runtime-specific metadata

- rules
- classifiers
- natives
- extract rules
- classpath participation

Assets не являются classpath entries и не должны проходить через library runtime selection

### Сразу добавить отдельную operation для assets

Вариант отложен

Отдельная operation может понадобиться позже, если assets получат собственный lifecycle, cache policy, cleanup policy или
отдельный progress model

На момент принятия решения достаточно подключить assets к существующему verification/download lifecycle через
`ManifestResources`

### Сразу вводить assets cache cleanup и versioning

Вариант отложен

Cleanup и versioning требуют stable runtime identity, profile/version context или отдельной cache policy

Эти темы не должны появляться до минимальной модели assets index

---

## Последствия

Assets получают собственную semantic boundary внутри manifest model

Verification/download flow сможет восстанавливать assets без создания отдельного lifecycle stage

`ManifestResources` станет общей projection-точкой для

- files
- libraries
- selected native artifacts
- assets

`ResourceEntry` сохранит роль generic recoverable resource contract

`LauncherEngine` не получает новый шаг ради появления assets metadata

В будущем assets flow сможет развиваться в сторону отдельного cache policy, progress model или cleanup behavior без ломки
базового verification/download contract

---

## Не входит в решение

- Реализация assets index model
- Изменение manifest JSON contract
- Изменение `LauncherEngine`
- Отдельная `VERIFY_ASSETS` или `DOWNLOAD_ASSETS` operation
- Asset cache cleanup policy
- Asset cache versioning
- Stable runtime identity
- Profile/version-specific assets
- UI progress для assets
- Parallel asset downloads
- CDN selection
- Asset objects directory layout
- Minecraft asset index compatibility details

---

## Связанные решения

- [ADR-0014: Использовать ManifestResources как источник verification flow](ADR-0014-manifest-resources-verification-flow.md)
- [ADR-0015: Зафиксировать минимальный scope library metadata](ADR-0015-minimal-library-metadata-scope.md)
- [ADR-0016: Зафиксировать правила безопасности resource path](ADR-0016-resource-path-safety.md)
- [ADR-0017: Определить границу runtime metadata для libraries](ADR-0017-library-runtime-metadata-boundary.md)
- [ADR-0023: Использовать RuntimeLibrarySelection.libraries как источник game classpath](ADR-0023-use-libraries-as-game-classpath-source.md)
- [ADR-0026: Определить правила исключения при распаковке native artifacts](ADR-0026-native-extraction-exclude-rules.md)
