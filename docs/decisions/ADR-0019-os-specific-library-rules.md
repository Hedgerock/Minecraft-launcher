# ADR-0019: Определить минимальные OS-specific rules для libraries

## Статус

Accepted

---

## Контекст

`RuntimeLibrarySelector` уже получает `RuntimeEnvironment`

`RuntimeEnvironment` на текущем этапе содержит `OperatingSystem`

Это позволяет начать использовать окружение не только как подготовленную зависимость, но и как
фактор выбора runtime-compatible libraries

Следующий шаг развития library-selection — поддержать минимальные OS-specific rules

Полный Minecraft manifest library contract может включать более сложные правила

- Несколько условий внутри одного rule
- Rules без OS
- Rules с features
- Classifiers
- Natives
- Architecture-specific выбор
- Порядок применения правил исходного manifest contract

На текущем этапе реализация полного контракта будет считаться избыточной 

Текущая задача добавить минимальную модель, достаточную для первого environment-aware
выбора library entries

---

## Решение

Ввести минимальную модель library rules

```text
RuntimeLibraryMetadata
    -> LibraryArtifactMetadata
    -> LibraryRule
        -> LibraryRuleAction
        -> OperatingSystem
```

`LibraryRuleAction` содержит действия

- ALLOW
- DISALLOW

`LibraryRule` содержит

- `action`
- `operatingSystem`

`RuntimeLibraryMetadata` содержит

- основной `LibraryArtifactMetadata`
- список `LibraryRule`

Отсутствие rules означает, что library разрешена для любого runtime environment

Если rules есть, selector применяет только rules, совпадающие с текущим `RuntimeEnvironment.operatingSystem`

### Правила выбора

Если matching rules есть - применяется последняя matching rule

Library включается если

- `rules` пустой
- Последняя matching rule имеет `ALLOW`

Library исключается если

- Matching rules нет
- Последняя matching rule имеет `DISALLOW`

На текущем этапе matching учитывает только `OperatingSystem`

---

## Последствия

`RuntimeLibrarySelector` впервые начинает реально использовать `RuntimeEnvironment`

Library selection становится детерминированным и тестируемым для разных OS

`LibraryEntry` остается моделью уже выбранного artifact и не получает rules

`LibraryArtifactMetadata` остается моделью downloadable artifact и не получает rules

`RuntimeLibraryMetadata` становится контейнером library metadata до выбора runtime-compatible `LibraryEntry`

Будущая поддержка classifiers и natives сможет использовать тот же selection flow

---

## Не входит в решение

- Полный Minecraft rules contract
- Rules без OS
- Feature-based rules
- Architecture-specific rules
- Classifiers
- Natives
- Распаковка natives
- Проверка extract/exclude rules
- Изменение verification/download flow
- Изменение `LibraryEntry`
- Изменение `LibraryArtifactMetadata`

---

## Связанные решения

- [ADR-0017: Определить границу runtime metadata для libraries](ADR-0017-library-runtime-metadata-boundary.md)
- [ADR-0018: Определить границу runtime environment](ADR-0018-runtime-environment-boundary.md)
