[← Назад к списку решений](README.md)

# ADR-0026: Определить правила исключения при распаковке native artifacts

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(api): preserve native extraction rules in selection`
> `feat(natives): apply native extraction exclude rules`

---

## Контекст

После добавления `EXTRACT_NATIVES` launcher умеет распаковывать выбранные native artifacts в директорию
`LauncherDirectories.nativesDirectory`

`DefaultNativeExtractionService` уже работает с `NativeExtractionPlan` и распаковывает archive entries из
выбранных artifacts

На текущем этапе каждый selected native artifact представлен как `LibraryEntry`

Это подходит для verification/download flow, потому что native artifacts должны проверяться и загружаться
как обычные ресурсы

Однако для native extraction flow одного `LibraryEntry` уже недостаточно

Manifest metadata может содержать `extract.exclude` rules, которые описывают archive entries, которые не должны попадать
в директорию natives

Пример

```text
META-INF/
```

Если добавить rules распаковки напрямую в `LibraryEntry`, модель начнет смешивать

- Идентичность downloadable/verifiable resource
- Правила выбора runtime artifact
- Правила распаковки native artifact

Это нарушит уже зафиксированную границу, где `LibraryEntry` остается моделью выбранного ресурса, а не контейнером
всех правил его использования

---

## Решение

`LibraryEntry` остается моделью downloadable/verifiable resource

Правила распаковки native artifact должны быть представлены отдельной моделью

Selected native artifact должен содержать

- `LibraryEntry`
- Native extraction rules

Граница выглядит так

```text
RuntimeLibraryMetadata
    -> selected native artifact
        -> LibraryEntry
        -> native extraction rules
            -> NativeExtractionPlan
                -> DefaultNativeExtractionService
```

`RuntimeLibrarySelector` должен сохранять extraction rules вместе с выбранным native artifact

`RuntimeLibrarySelection.nativeArtifacts` должен представлять selected native artifacts вместе с правилами
распаковки, а не только plain `LibraryEntry`

Compatibility projection для verification/download flow должна сохраниться через `selectedArtifacts()`

```text
RuntimeLibrarySelection.selectedArtifacts()
    -> Manifest.libraries
        -> ManifestResources
            -> verification/download flow
```

`NativeExtractionPlan` должен получать selected native artifacts вместе с extraction rules

`DefaultNativeExtractionService` должен применять `extract.exclude` во время чтения archive entries

`exclude` интерпретируется как путь или префикс внутри архива

К примеру правило

```text
META-INF/
```

должно исключать entries вида

```text
META-INF/MANIFEST.MF
META-INF/signature.SF
META-INF/native.properties
```

Перед проверкой exclude rules archive entry path должен быть приведен к единому виду с `/` как разделителем

Exclude rules не являются glob pattern и не интерпретируются как regular expressions

Проверка безопасности пути распаковки остается обязательной независимо от exclude rules

---

## Последствия

`LibraryEntry` не получает rules распаковки native artifacts

Verification/download flow продолжает работать с обычными `LibraryEntry`

Native extraction flow получает отдельный контекст, необходимый для корректной распаковки natives

`RuntimeLibrarySelection` становится source-of-truth не только для выбранных runtime libraries, но и для selected native
artifacts вместе с правилами их распаковки

`NativeExtractionPlanBuilder` должен передавать selected native artifacts в `NativeExtractionPlan` без потери
extraction rules

`DefaultNativeExtractionService` должен пропускать excluded archive entries до записи в target directory

Защита от Zip Slip остается частью `DefaultNativeExtractionService` и не заменятся правилами exclude

---

## Не входит в решение

- Glob matching для exclude rules
- Regular expression matching для exclude rules
- Include rules
- Очистка директории natives перед распаковкой
- Версионирование директорий natives
- Overwrite policy для уже распакованных файлов
- Добавление JVM argument для natives directory
- Изменение verification/download flow
- Изменение `ManifestResources`
- Добавление extraction rules в `LibraryEntry`

---

## Связанные решения

- [ADR-0016: Зафиксировать правила безопасности resource path](ADR-0016-resource-path-safety.md)
- [ADR-0020: Определить границу classifiers и natives metadata для libraries](ADR-0020-library-classifiers-and-natives-boundary.md)
- [ADR-0021: Определить границу обработки native artifacts](ADR-0021-native-artifact-processing-boundary.md)
- [ADR-0024: Определить границу native extraction operation](ADR-0024-native-extraction-operation-boundary.md)
- [ADR-0025: Определить реализацию native extraction service](ADR-0025-native-extraction-service-implementation.md)
