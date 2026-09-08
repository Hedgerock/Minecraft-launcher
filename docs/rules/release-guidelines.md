# Правила подготовки релизов

## Назначение

Документ фиксирует правила подготовки CHANGELOG, тегов и milestone release notes

---

## Unreleased

`Unreleased` используется для изменений, которые еще не вошли в тег

Если внутри `Unreleased` появляется самостоятельный candidate release scope, его нужно выделить
отдельной подсекцией

Пример

```markdown
## Unreleased

### Java Runtime Foundation

#### Added

...

#### Changed

...

### Project Foundation Stabilization

#### Added

...

#### Changed

...
```

Это помогает заранее увидеть, нужен ли отдельный тег или изменения лучше объединить в один release

---

## Теги

Тег оправдан, если изменения закрывают самостоятельный milestone

Примеры самостоятельного milestone

- Новый production flow
- Завершенная архитектурная граница
- Существенное изменение module boundaries
- Build/CI foundation, который меняет способ проверки проекта
- Завершенная стабилизационная итерация перед следующим milestone

Тег не нужен для каждой маленькой документационной или code итерации

---

## Именование тегов

Формат

```text
vX.Y.Z-short-scope
```

Примеры

```text
v0.5.0-java-runtime-foundation
v0.5.1-foundation-stabilization
```

`minor` версия используется, если release закрывает новый feature/domain milestone

`patch` версия используется, если release закрывает стабилизацию, исправление или polish
после предыдущего milestone

---

## Ретроспектива

Ретроспектива нужна для крупного milestone

Для небольшого patch release достаточно CHANGELOG и roadmap update

Если patch release содержит архитектурно значимую стабилизацию, допускается короткая retrospective

---

## Порядок подготовки release

Минимальный порядок

```text
1. Проверить, что CHANGELOG отражает release scope
2. Обновить roadmap
3. Добавить retrospective, если milestone достаточно крупный
4. Убедиться, что CI проходит
5. Создать git tag
```
