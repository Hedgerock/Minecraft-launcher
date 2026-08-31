# ADR-0025: Определить реализацию native extraction service

## Статус

Accepted

> Примечание: решение реализовано в итерации `feat(natives): add native extraction service`

---

## Контекст

`EXTRACT_NATIVES` уже добавлена в launcher lifecycle как отдельная operation после `PREPARE_DIRECTORIES` и до
`BUILD_GAME_LAUNCH_PLAN`

`launcher-core` владеет портом `NativeExtractionService`, моделью `NativeExtractionPlan`, `ExtractNativesTask` и
`ExtractNativesOperation`

На текущем этапе в `launcher-app` используется временная реализация `UnsupportedNativeExtractionService`

Для продолжения native flow требуется добавить реальную реализацию сервиса распаковки native artifacts,
не нарушая модульные границы проекта

---

## Решение

Реализация native extraction должна находиться вне `launcher-core`

Для этого вводится отдельный инфраструктурный модуль `launcher-natives`

`launcher-core` продолжает владеть только orchestration port

- `NativeExtractionService`
- `NativeExtractionPlan`
- `EXTRACT_NATIVES` operation

`launcher-natives` должен содержать конкретную реализацию

- `DefaultNativeExtractionService`

`DefaultNativeExtractionService` должен

- Получать `NativeExtractionPlan`
- Для каждого artifact определять локальный source path относительно game directory
- Распаковать artifact как zip/jar-архивы в `NativeExtractionPlan.targetDirectory`
- Создавать вложенные директории внутри `NativeExtractionPlan.targetDirectory`
- Запрещать zip entries, которые выходят за пределы `NativeExtractionPlan.targetDirectory`
- Пробрасывать ошибку как failure operation через общий operation lifecycle

`launcher-app` остается composition root и связывает `NativeExtractionService` port с реализацией
из `launcher-natives`

---

## Последствия

`launcher-core` не получает filesystem/zip responsibilities

Native extraction становится заменяемым adapter-ом

`UnsupportedNativeExtractionService` может быть удален после подключения реальной реализации

Ошибки распаковки будут приводить `EXTRACT_NATIVES` к failure, а `LauncherEngine` переведет launcher в `FAILED`

Реализация должна быть покрыта тестами на успешную распаковку, отсутствующий source artifact и защиту от
path traversal

---

## Не входит в решение

- Extract exclude rules
- Очистка директории natives перед распаковкой
- Версионирование директории natives
- Добавление JVM argument для natives directory
- Изменение manifest JSON контракта
- Изменение `RuntimeLibrarySelection`
- Изменение verification/download flow

---

## Связанные решения

- [ADR-0016: Зафиксировать правила безопасности resource path](ADR-0016-resource-path-safety.md)
- [ADR-0021: Определить границу обработки native artifacts](ADR-0021-native-artifact-processing-boundary.md)
- [ADR-0024: Определить границу native extraction operation](ADR-0024-native-extraction-operation-boundary.md)
