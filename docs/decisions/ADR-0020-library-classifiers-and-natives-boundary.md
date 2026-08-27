# ADR-0020: Определить границу classifiers и natives metadata для libraries

## Статус

Accepted

---

## Контекст

После поддержки минимальных OS-specific library rules launcher умеет выбирать libraries на основе
текущего `RuntimeEnvironment.operatingSystem`

Следующий этап развития library selection связан с поддержкой classifiers и natives

Полный Minecraft library contract может содержать

- Основной artifact
- Classifiers
- Natives mapping
- Правила выбора native artifact для конкретной OS
- Правила распаковки natives

Если добавить classifiers и natives напрямую в `LibraryEntry`, модель снова начнет смешивать
описание уже выбранного artifact и правила выбора подходящего artifact

---

## Решение

`LibraryEntry` остается моделью уже выбранного library artifact

`LibraryArtifactMetadata` остается моделью downloadable artifact metadata

`RuntimeLibraryMetadata` является контейнером metadata до выбора runtime-compatible `LibraryEntry`

Classifiers и natives должны быть представлены как metadata до выбора artifact

На текущем этапе вводится только граница ответственности

```text
RuntimeLibraryMetadata
    -> LibraryArtifactMetadata
    -> LibraryRule
    -> classifiers metadata
    -> natives mapping
        -> RuntimeLibrarySelector
            -> LibraryEntry
```

`RuntimeLibrarySelector` должен выбирать итоговый `LibraryEntry` на основе

- Основного artifact
- `LibraryRule`
- Текущего `RuntimeEnvironment`
- Classifiers metadata
- Natives mapping

Native artifact, если он выбран, должен становиться обычным `LibraryEntry` после selection

Verification/download flow продолжает работать только с выбранными `LibraryEntry` через `ManifestResources`

---

## Последствия

`LibraryEntry` не получает classifiers и natives напрямую

`ManifestResources` не должен знать, почему конкретная library была выбрана

Verification/download flow остается стабильным

`RuntimeLibrarySelector` становится единственным местом выбора runtime-compatible library artifact

Будущая распаковка natives должна быть оформлена отдельной итерацией

---

## Не входит в решение

- Реализация classifiers
- Реализация natives mapping
- Распаковка natives
- Extract/exclude rules
- Автоматический выбор Java runtime
- Architecture-specific выбор
- Изменение verification/download flow
- Изменение `LibraryEntry`
- Изменение `ManifestResources`

---

## Связанные решения

- [ADR-0017: Определить границу runtime metadata для libraries](ADR-0017-library-runtime-metadata-boundary.md)
- [ADR-0018: Определить границу runtime environment](ADR-0018-runtime-environment-boundary.md)
- [ADR-0019: Определить минимальные OS-specific rules для libraries](ADR-0019-os-specific-library-rules.md)
