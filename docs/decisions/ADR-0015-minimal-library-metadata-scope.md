[← Назад к списку решений](README.md)

# ADR-0015: Зафиксировать минимальный scope library metadata

## Статус

Accepted

## Контекст

`Manifest.libraries` уже используется в нескольких runtime-сценариях

- Построение `GameClasspath`
- projection в `ManifestResources`
- verification/download lifecycle через `ResourceEntry`

На текущем этапе `LibraryEntry` содержит минимальную физическую метадату ресурса

- `path`
- `sha256`
- `size`
- `url`

Этого достаточно, чтобы библиотека могла:

- Участвовать в построении classpath
- Проверяться через verification flow
- Восстанавливаться через download flow

При этом реальный Minecraft manifest может содержать более сложные library metadata

- Maven coordinates
- downloads/artifact
- classifiers
- natives
- rules
- OS-specific selection
- extract/exclude rules

Если добавить все эти детали сразу в текущую модель, `LibraryEntry` быстро станет слишком
широкой моделью и начнет смешивать разные задачи

- Описание физического ресурса
- Выбор platform-specific варианта
- Построение classpath
- Восстановление файла
- Правила распаковки natives

## Решение

На текущем этапе `LibraryEntry` остается минимальной recoverable library моделью

Она описывает только тот library artifact, который уже выбран для текущего runtime-сценария и может быть

- Добавлен в classpath
- Проверен
- Скачан

`LibraryEntry` не моделирует полный Manifest library contract

Поддержка `natives`, `classifiers`, `rules`, `OS-specific` выбора и extract/exclude rules
не добавляется в текущую модель напрямую

Для будущего расширения эти правила должны быть введены отдельными моделями или отдельным слоем
projection/selection перед формированием `LibraryEntry`

## Последствия

`LibraryEntry` остается простой и стабильной моделью

Verification/download flow продолжает работать через `ResourceEntry` и не зависит от Minecraft-specific
деталей выбора libraries

Classpath building использует уже готовый список `Manifest.libraries`

Будущий platform-specific selection может быть добавлен до формирования runtime `Manifest` или
отдельной projection-моделью

## Не входит в решение

- Полная поддержка Mojang/Minecraft manifest library schema
- Выбор natives по OS
- Распаковка natives
- Maven coordinate resolution
- Dependency conflict resolution
- Retry/backoff downloader