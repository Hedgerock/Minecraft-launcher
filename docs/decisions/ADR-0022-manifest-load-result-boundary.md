# ADR-0022: Определить границу результата загрузки manifest

## Статус

Accepted

---

## Контекст

После добавления `RuntimeLibrarySelection` runtime library selection возвращает две проекции
выбранных library artifacts:

- Обычные selected libraries
- Selected native artifacts

Для сохранения совместимости текущего verification/download flow `ManifestJsonConverter` временно
преобразует результат selection через `RuntimeLibrarySelection.selectedArtifacts()` и сохраняет общий
список в `Manifest.libraries`

Это позволяет verification/download flow продолжать работать через `ManifestResources`, не различая обычные
libraries и native artifacts

Однако для будущей native extraction требуется сохранить отдельный список `RuntimeLibrarySelection.nativeArtifacts()`
после загрузки `Manifest`

Если добавить `RuntimeLibrarySelection` напрямую в `Manifest`, manifest model начнет хранить не только итоговую
manifest data, но и результат runtime selection

Если сохранять selection напрямую в `LaunchContext` без изменения результата manifest loading, остается неясным, кто
должен передать selection из mapping-слоя наружу

---

## Решение

Ввести отдельный результат загрузки manifest, который будет содержать

- `Manifest`
- `RuntimeLibrarySelection`

Этот результат должен представлять итог работы manifest loading/mapping boundary

`Manifest` остается моделью итогового manifest состояния для текущего launcher lifecycle

`RuntimeLibrarySelection` остается моделью результата runtime library selection и сохраняет разделение
обычных libraries и native artifacts

`Manifest.libraries` продолжает содержать compatibility projection через `RuntimeLibrarySelection.selectedArtifacts()`
для текущего verification/download flow

Будущий `NativeExtractionPlanBuilder` должен использовать `RuntimeLibrarySelection.nativeArtifacts()` из результата
загрузки manifest, а не пытаться отличать native artifacts внутри `Manifest.libraries`

Граница выглядит так

```text
Manifest JSON
    -> RuntimeLibraryMetadata
        -> RuntimeLibrarySelector
            -> RuntimeLibrarySelection
                -> Manifest.libraries = selectedArtifacts()
                -> RuntimeLibrarySelection.nativeArtifacts()
                    -> future NativeExtractionPlan
```

---

## Последствия

`Manifest` не получает дополнительную ответственность за хранение runtime selection details

`ManifestResources` продолжает работать только с `Manifest.files` и `Manifest.libraries`

Verification/download flow остается совместимым и продолжает проверять/загружать все selected artifacts
как обычные resources

Native extraction получает явный будущий источник данных через `RuntimeLibrarySelection.nativeArtifacts()`

Manifest loading boundary становится шире: он возвращает не только `Manifest`, но и metadata, нужную будущим
runtime steps

Потребуется обновить контракты manifest loading/mapping слоя

Потребуется сохранить runtime library selection в `LaunchContext` или передавать его через
отдельный результат `LOAD_MANIFEST` операции на следующем кодовом шаге

---

## Не входит в решение

- Реализация native extraction
- Добавление `EXTRACT_NATIVES` operation
- Изменение `ManifestResources`
- Изменение verification/download flow
- Изменение `LibraryEntry`
- Добавление JVM argument для natives directory
- Распаковка native artifacts

---

## Связанные решения

- [ADR-0014: Использовать ManifestResources как источник verification flow](ADR-0014-manifest-resources-verification-flow.md)
- [ADR-0017: Определить границу runtime metadata для libraries](ADR-0017-library-runtime-metadata-boundary.md)
- [ADR-0020: Определить границу classifiers и natives metadata для libraries](ADR-0020-library-classifiers-and-natives-boundary.md)
- [ADR-0021: Определить границу обработки native artifacts](ADR-0021-native-artifact-processing-boundary.md)
