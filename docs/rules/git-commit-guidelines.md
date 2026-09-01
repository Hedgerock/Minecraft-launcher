# Правила написания git commits

## Назначение

Commit фиксирует один завершенный инженерный шаг

Commit должен отражать фактически выполненное изменение и не обещать больше, чем уже сделано

---

## Формат commit message

Commit message должен использовать компактный формат

```text
type(scope): description
```

Если scope не добавляет ясности, его можно не указывать

```text
type: description
```

---

## Основные types

Для проекта используются следующие основные types

- `feat` — добавление нового поведения или новой модели
- `fix` — исправление ошибки
- `refactor` — изменение структуры без добавления нового поведения
- `docs` — изменение документации
- `test` — изменение тестов без production code
- `style` — форматирование без изменения поведения
- `build` — изменения сборки, quality gate или build scripts

---

## Scope

Scope должен указывать область изменения

Примеры

```text
feat(core): add native extraction operation boundary
feat(natives): apply native extraction exclude rules
feat(api): preserve native extraction rules in selection
refactor(natives): carry selected native artifacts in extraction plan
docs: decide native extraction exclude rules
docs: document native extraction exclude rules implementation
build: add minimal quality checks
style: remove trailing whitespace
```

Scope должен быть коротким и соответствовать модулю или архитектурной области

---

## Семантика commit message

Commit message должен описывать результат, а не процесс

Хорошо

```text
feat(natives): apply native extraction exclude rules
```

Плохо

```text
feat(natives): work on native extraction
```

Commit message не должен обещать реализацию, если был добавлен только ADR или документация

Хорошо

```text
docs: decide natives directory launch argument
```

Плохо

```text
feat(core): add natives directory launch argument
```

если код еще не был реализован

---

## Размер commit

Commit должен быть достаточно маленьким, чтобы его можно было понять отдельно

Предпочтительно разделять

- Архитектурное решение
- Изменение модели
- Изменение поведения
- Обновление документации
- Механический style cleanup
- Build/quality gate изменения

---

## Когда разделять commits

Commits нужно разделять, если изменения отвечают на разные вопросы

Например

```text
docs: decide native extraction exclude rules
feat(api): preserve native extraction rules in selection
feat(natives): apply native extraction exclude rules
docs: document native extraction exclude rules implementation
```

Такой порядок показывает эволюцию решения

```text
decision
    -> data flow
        -> behavior
            -> documentation
```

---

## Когда допустим один commit

Один commit допустим, если изменения являются частью одного маленького инженерного шага

Например

- Production code и тесты одного поведения
- Документ и его ссылка из index/rules документа
- Небольшой refactor и обновление связанных тестов
- Исправление опечаток в одном документе

---

## Documentation commits

Documentation commit должен быть отдельным, если документация догоняет уже реализованное поведение

Для архитектурных решений используется формулировка

```text
docs: decide ...
```

Для описания реализованного поведения используется формулировка

```text
docs: document ...
```

Для синхронизации устаревшей документации используется формулировка

```text
docs: update ...
docs: align ...
```

---

## Pre-commit review

Перед commit желательно проверить

```text
git status --short
git diff --stat
git diff --check
```

Если есть staged и unstaged изменения одного файла, нужно повторно выполнить `git add`

Особенно важно обращать внимание на статус

```text
AM
MM
```

Такой статус означает, что файл уже был добавлен в index, но после этого снова изменился

---

## Quality gate

Перед code commit желательно запускать релевантные тесты

Для обычной кодовой итерации

```text
./gradlew test
```

Для проверки качества документации и whitespace

```text
./gradlew qualityCheck
```

Если изменения затрагивают сборку или несколько модулей, лучше запускать полный quality gate

---

## Чего избегать

В commit message не стоит использовать

- Слишком общие формулировки
- Описание процесса вместо результата
- Обещание будущего поведения
- Смешение несвязанных изменений
- Документацию и код в одном commit без причины
- Большой cleanup вместе с feature change
