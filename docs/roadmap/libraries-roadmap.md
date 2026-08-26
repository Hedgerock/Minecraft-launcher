# Путь развития библиотек

## Текущий план

- Спроектировать отдельный шаг runtime library selection перед формированием `libraryEntry`

---

## Выполнено

- Добавлена модель `ResourceEntry`
- Добавлена projection `ManifestResources` для `Manifest.files` и `Manifest.libraries`
- Зафиксировано решение использовать `ManifestResources` как источник verification flow
- Подключен `ManifestResources` к verification flow
- Зафиксирован минимальный scope library metadata
- Зафиксирована граница runtime metadata для `libraries`