# Путь развития библиотек

## Текущий план

- Развить runtime library selection для classifiers и natives

---

## Выполнено

- Добавлена модель `ResourceEntry`
- Добавлена projection `ManifestResources` для `Manifest.files` и `Manifest.libraries`
- Зафиксировано решение использовать `ManifestResources` как источник verification flow
- Подключен `ManifestResources` к verification flow
- Зафиксирован минимальный scope library metadata
- Зафиксирована граница runtime metadata для `libraries`
- Добавлен seam runtime library selection перед формированием `LibraryEntry`
- Artifact metadata выделена в отдельную модель `LibraryArtifactMetadata`
- Зафиксирована граница runtime environment для будущего library selection
- Введена минимальная модель `RuntimeEnvironment`
- `RuntimeEnvironment` передан в runtime library selection без изменения текущего поведения
- Добавлен `RuntimeEnvironmentProvider` для определения текущего runtime environment
- Зафиксированы минимальные OS-specific library rules
- Добавлены модели `LibraryRule` и `LibraryRuleAction`
- Manifest JSON mapping преобразует library rules в `RuntimeLibraryMetadata`
- `RuntimeLibrarySelector` выбирает libraries с учетом текущей `OperatingSystem`
