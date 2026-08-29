# ADR-0021: Определить границу обработки native artifacts

## Статус

Accepted

> Примечание: после принятия решения были добавлены директория `natives`,
> `NativeExtractionPlan` и `NativeExtractionPlanBuilder`
> Сама распаковка natives и operation `EXTRACT_NATIVES` остаются вне текущей реализации

---

## Контекст

После добавления classifiers и natives metadata launcher умеет выбирать native artifact для 
текущей `OperatingSystem`

На текущем этапе выбранный native artifact становится обычным `LibraryEntry` после runtime library selection

Это сохраняет стабильность verification/download flow

```text
RuntimeLibraryMetadata
    -> RuntimeLibrarySelector
        -> LibraryEntry
            -> ManifestResources
                -> verification/download flow
```

Однако для полноценного запуска Minecraft-style natives недостаточно только скачать native artifact

Native artifact обычно должен быть

- Выбран для текущей `OperatingSystem`
- Загружен как обычный ресурс
- Распакован в отдельную директорию natives
- Передан JVM через соответствующий launch argument

Если смешать выбор library, загрузку файлов, распаковку natives и построение launch command
в одном месте, границы ответственности начнут размываться

---

## Решение

Выбор native artifact остается ответственностью `RuntimeLibrarySelector`

`RuntimeLibrarySelector` не распаковывает natives и не знает о директории распаковки

После selection native artifact представлен как обычный `LibraryEntry` и участвует в verification/download flow lifecycle 
через `ManifestResources`

Распаковка natives должна быть оформлена отдельной будущей итерацией после того, как selected native
artifacts уже загружены

Граница ответственности выглядит так

```text
Manifest JSON
    -> RuntimeLibraryMetadata
        -> RuntimeLibrarySelector
            -> selected LibraryEntry
                -> ManifestResources
                    -> verification/download lifecycle
                        -> downloaded selected libraries
                            -> future native extraction
                                -> launch command
```

Для будущей распаковки natives потребуется отдельный контракт, который будет работать уже с выбранными и загруженными
native artifacts, а не с raw JSON metadata

Этот контракт не должен возвращать библиотеки обратно в manifest model

---

## Последствия

`RuntimeLibrarySelector` остается чистым selector-ом и не получает filesystem responsibilities

`ManifestResources` продолжает работать только с ресурсами, которые нужно проверить или загрузить

Verification/download flow не различает обычные libraries и selected native artifacts

Будущая native extraction должна быть добавлена после download lifecycle, но до построения или
выполнения game launch command

Для запуска игры в будущем потребуется передать директорию распакованных natives в launch arguments

`LibraryEntry` не получает признак native artifact на текущем этапе

Если будущей распаковке потребуется отличать обычные libraries от natives, это должно быть
оформлено отдельным решением, а не добавлено неявно в текущую selection-модель

---

## Не входит в решение

- Реализация распаковки natives
- Extract/exclude rules
- Architecture-specific выбор natives
- Автоматический выбор Java runtime
- Изменение verification/download flow
- Изменение `ManifestResources`
- Изменение `LibraryEntry`
- Добавление JVM argument для natives directory

---

## Связанные решения

- [ADR-0014: Использование manifest resources как источник verification flow](ADR-0014-manifest-resources-verification-flow.md)
- [ADR-0017: Определить границу runtime metadata для libraries](ADR-0017-library-runtime-metadata-boundary.md)
- [ADR-0018: Определить границу runtime environment](ADR-0018-runtime-environment-boundary.md)
- [ADR-0019: Определить минимальные OS-specific rules для libraries](ADR-0019-os-specific-library-rules.md)
- [ADR-0020: Определить границу classifiers и natives metadata для libraries](ADR-0020-library-classifiers-and-natives-boundary.md)
