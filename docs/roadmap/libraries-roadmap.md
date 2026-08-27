# Путь развития библиотек

## Текущий план

- Ввести минимальные OS-specific library rules
- Подключить OS-specific rules к `RuntimeLibrarySelector`
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
