# Путь развития библиотек

## Текущий план

- Уточнить семантику минимальной модели `libraries`
- Определить границы поддержки natives, classifiers и rules
- Подготовить следующий шаг развития manifest library realism

---

## Выполнено

- Добавлена модель `ResourceEntry`
- Добавлена projection `ManifestResources` для `Manifest.files` и `Manifest.libraries`
- Зафиксировано решение использовать `ManifestResources` как источник verification flow
- Подключен `ManifestResources` к verification flow