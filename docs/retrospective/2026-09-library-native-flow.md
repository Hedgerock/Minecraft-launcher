# Ретроспектива: развитие library/native flow

## Контекст

Данная итерация развила launcher после `v0.3.0-launch-runtime` от базового runtime lifecycle к более реалистичной
модели manifest metadata, library selection и native artifacts

До начала этапа launcher уже умел загружать manifest, проверять ресурсы, восстанавливать отсутствующие файлы,
строить `GameLaunchPlan` и запускать игровой процесс

Однако модель libraries оставалась минимальной и не отражала реальные требования Minecraft-style запуска

- Выбор libraries по runtime environment
- Classifiers
- Native artifacts
- Rules
- `extract.exclude`
- Связь распакованных native с launch arguments

Целью этапа было постепенно довести library/native flow до состояния, где выбранные libraries и native artifacts
проходят через verification/download lifecycle, участвуют в native extraction и корректно используются при построении
launch command

---

## Что было сделано

- Добавлен контракт manifest JSON
- Добавлен реальный manifest JSON mapping
- Добавлена минимальная модель library metadata
- Добавлена resource-level projection `ManifestResources`
- Verification/download flow переведен на `ResourceEntry`
- Добавлен общий `ResourcePathResolver`
- Classpath entries стали безопасно разрешаться относительно `gameDirectory`
- Добавлена модель `RuntimeLibraryMetadata`
- Добавлена модель `LibraryArtifactMetadata`
- Добавлена модель `RuntimeEnvironment`
- Добавлен `RuntimeEnvironmentProvider`
- Добавлены OS-specific rules для library selection
- Добавлены classifiers и natives metadata
- Добавлена модель `RuntimeLibrarySelection`
- Runtime library selection разделяет обычные libraries и selected native artifacts
- `ManifestLoadResult` сохраняет `Manifest` и `RuntimeLibrarySelection`
- Game classpath строится из `RuntimeLibrarySelection.libraries`
- Native artifacts участвуют в verification/download flow через compatibility projection
- Добавлена директория `natives`
- Добавлен `NativeExtractionPlan`
- Добавлен `NativeExtractionPlanBuilder`
- Добавлена operation `EXTRACT_NATIVES`
- Добавлен модуль `launcher-natives`
- Добавлена реализация `DefaultNativeExtractionService`
- Добавлена поддержка `extract.exclude`
- Добавлена launch variable `${natives_directory}`
- Добавлена output policy для повторной распаковки natives
- Добавлены правила написания ADR
- Добавлены правила написания git commits
- Добавлен минимальный quality gate

---

## Что подтвердилось

Эволюционный подход с ADR перед кодом хорошо сработал для большой темы

Library/native flow оказался слишком объемным, чтобы реализовывать его одним решением.
Разделение на маленькие ADR позволило не смешивать:

- raw manifest JSON
- domain metadata
- runtime selection
- verification/download projection
- native extraction
- launch command building
- output policy

`RuntimeLibrarySelection` стала удачной границей между manifest parsing и runtime lifecycle

Она позволила сохранить совместимость verification/download flow и одновременно дать отдельный
источник данных для game classpath и native extraction

`ManifestResources` подтвердил свою роль как resource-level projection

Благодаря этому verification/download flow не знает, является ресурс обычным file, library или
selected native artifact

---

## Что было улучшено архитектурно

`launcher-core` сохранил роль orchestration layer

Core владеет lifecycle, operations, tasks, plans и ports, но не содержит конкретную реализацию
native extraction

`launcher-api` стал владельцем manifest JSON parsing и runtime library selection

`launcher-model` получил более выразительные immutable-модели для manifest, runtime metadata и selected artifacts

`launcher-natives` стал отдельным инфраструктурным модулем для работы с archive-based native artifacts

`GameLaunchPlanBuilder` остался builder-ом runtime context и не начал самостоятельно добавлять
JVM arguments

Конкретный JVM argument для natives остается частью manifest launch metadata

---

## Что осталось отложенным

- Версионирование директории natives
- Cleanup policy для старых natives directories
- Stable runtime identity для profile/version/manifest/installation
- Architecture-specific natives selection
- Rules на основе features
- Автоматический выбор Java runtime
- Проверка существования Java executable
- Assets index
- Аргументы авторизации
- Loader-specific правила запуска
- Более строгие архитектурные тесты между модулями

---

## Технический долг

Технический долг остается контролируемым

Большая часть текущих ограничений не является скрытым долгом, а зафиксирована как отложенные
решения

Ключевым отложенным условием для следующего уровня сложности является появление stable runtime identity

До появления profile, version, manifest hash или installation id преждевременно вводить versioned natives directory,
cleanup policy или несколько параллельных runtime native sets

Текущий quality gate уже помогает удерживать базовую чистоту документации и Java-кода

Следующим направлением улучшения quality gate может стать постепенное подключение более формальных
инструментов

- Checkstyle
- PMD
- SpotBugs
- JaCoCo
- dependency analysis

---

## Главный вывод

Milestone подтвердил, что launcher можно развивать как набор небольших архитектурных решений, не теряя
цельности общего runtime flow

После этого этапа library/native flow проходит полный путь

```text
Manifest JSON
    -> RuntimeLibraryMetadata
        -> RuntimeLibrarySelection
            -> ManifestResources
                -> verification/download flow
            -> RuntimeLibrarySelection.libraries
                -> game classpath
            -> RuntimeLibrarySelection.nativeArtifacts
                -> NativeExtractionPlan
                    -> EXTRACT_NATIVES
                        -> ${natives_directory}
                            -> launch command
```

Проект продолжает двигаться в сторону реалистичного Minecraft-style runtime, но при этом
не ввел преждевременную архитектуру для профилей, версий, assets и Java runtime selection
