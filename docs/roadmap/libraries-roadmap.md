# Путь развития библиотек

## Текущий план

- Развить runtime library selection для OS-specific rules, classifiers и natives

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