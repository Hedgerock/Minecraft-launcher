# ADR-0023: Использовать RuntimeLibrarySelection.libraries как источник game classpath

## Статус

Accepted

> Примечание: решение реализовано в итерации `feat(core): use runtime libraries for game classpath`

---

## Контекст

После добавления `RuntimeLibrarySelection` результат runtime library selection содержит две отдельные проекции

- `libraries`
- `nativeArtifacts`

Для сохранения совместимости текущего verification/download flow `Manifest.libraries` временно содержит
compatibility projection через `RuntimeLibrarySelection.selectedArtifacts()`

Это позволяет `ManifestResources` продолжать строить общий список ресурсов из `Manifest.files` и `Manifest.libraries`,
не различая обычные libraries и selected native artifacts

Такое поведение подходит для verification/download flow, потому что оба типа artifacts должны быть проверены и загружены

Однако `GameClasspathBuilder` использует `Manifest.libraries` как источник classpath entries

Из-за этого selected native artifacts могут попасть в game classpath как обычные libraries

Для Minecraft-style запуска это неверная граница ответственности: native artifacts должны быть
источником native extraction flow, а не частью game classpath

---

## Решение

Game classpath должен строиться из `RuntimeLibrarySelection.libraries`

`RuntimeLibrarySelection.nativeArtifacts` не должен попадать в game classpath

`Manifest.libraries` остается compatibility projection для текущего verification/download flow

`RuntimeLibrarySelection` становится source-of-truth для downstream runtime flows, которым важно
различать обычные libraries и native artifacts

Граница выглядит так

```text
RuntimeLibrarySelection
    -> libraries
        -> GameClasspathBuilder
            -> GameClasspath
                -> launch command

RuntimeLibrarySelection.selectedArtifacts()
    -> Manifest.libraries
        -> ManifestResources
            -> verification/download flow
```

---

## Последствия

Verification/download flow продолжает работать через `ManifestResources` и не требует изменения текущего поведения

`GameClasspathBuilder` больше не должен использовать `Manifest.libraries`, если
доступен `RuntimeLibrarySelection.libraries`

`BuildGameLaunchPlanTask` или `GameLaunchPlanBuilder` должны получить доступ к `RuntimeLibrarySelection`

`LaunchContext` уже хранит `RuntimeLibrarySelection` после `LOAD_MANIFEST`, поэтому следующий кодовый шаг
может использовать существующее состояние без добавления новой модели

Fallback на `launchInfo.classpath` остается допустимым для минимальных manifest-сценариев, где
runtime library selection не содержит обычных libraries

Native artifacts остаются доступными для будущего `NativeExtractionPlan` и не смешиваются с game classpath

---

## Не входит в решение

- Реализация native extraction
- Добавление `EXTRACT_NATIVES` operation
- Распаковка native artifacts
- Добавление JVM argument для natives directory
- Изменение download/verification flow
- Удаление compatibility projection из `Manifest.libraries`
- Изменение manifest JSON contract

---

## Связанные решения

- [ADR-0014: Использовать ManifestResources как источник verification flow](ADR-0014-manifest-resources-verification-flow.md)
- [ADR-0017: Определить границу runtime metadata для libraries](ADR-0017-library-runtime-metadata-boundary.md)
- [ADR-0021: Определить границу обработки native artifacts](ADR-0021-native-artifact-processing-boundary.md)
- [ADR-0022: Определить границу результата загрузки manifest](ADR-0022-manifest-load-result-boundary.md)
