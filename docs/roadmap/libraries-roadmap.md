# Путь развития библиотек

## Текущий план

- Реализовать `NativeExtractionService` в отдельном модуле `launcher-natives`

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
- Источником selected native artifacts для будущего `NativeExtractionPlan` стала модель `RuntimeLibrarySelection`
- `RuntimeLibrarySelector` теперь возвращает разделение selected libraries и native artifacts
- Добавлен `NativeExtractionPlanBuilder`, который строит `NativeExtractionPlan`
  из `RuntimeLibrarySelection.nativeArtifacts`
- Зафиксирована граница результата загрузки manifest для сохранения `RuntimeLibrarySelection`
- Добавлена модель `ManifestLoadResult` для результата загрузки manifest
- `RuntimeLibrarySelection` сохраняется в `LaunchContext` после загрузки manifest
- Произведен stabilization pass по library/native selection flow перед добавлением новых возможностей
- Зафиксировано решение об использовании `RuntimeLibrarySelection.libraries` как источника game classpath
- Game classpath теперь строится из `RuntimeLibrarySelection.libraries`, а не из `Manifest.libraries`
- Обновлена документация после изменения источника game classpath
- Зафиксирована граница `EXTRACT_NATIVES` как отдельной operation после `PREPARE_DIRECTORIES`
- `EXTRACT_NATIVES` добавлена в launcher lifecycle после `PREPARE_DIRECTORIES`
- Зафиксировано решение о реализации `NativeExtractionService`
