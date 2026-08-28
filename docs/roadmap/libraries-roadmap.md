# Путь развития библиотек

## Текущий план

- Определить источник selected native artifacts для построения `NativeExtractionPlan`

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
- Зафиксирована граница classifiers и natives metadata для library selection
- Добавлена минимальная модель `LibraryClassifiersMetadata`
- Добавлена минимальная модель `LibraryNativesMetadata`
- Classifiers и natives metadata подключены к `RuntimeLibraryMetadata`
- Manifest JSON mapping преобразует classifiers и natives metadata в `RuntimeLibraryMetadata`
- Расширен runtime library selection выбором native artifact для текущей `OperatingSystem`
- Зафиксирована граница обработки выбранных native artifacts после runtime library selection
- Добавлена директория `natives` в `LauncherDirectories`
- Подготовка директорий теперь создает директорию `natives` для будущей распаковки native artifacts
- Добавлена модель `NativeExtractionPlan` для будущей распаковки selected native artifacts
