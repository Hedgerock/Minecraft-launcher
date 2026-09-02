[← Назад к списку решений](README.md)

# ADR-0024: Определить границу native extraction operation

## Статус

Accepted

---

## Контекст

После развития library/native flow launcher уже различает обычные runtime libraries и native artifacts

`RuntimeLibrarySelection` содержит две отдельные проекции

- `libraries`
- `nativeArtifacts`

`RuntimeLibrarySelection.libraries` используется как источник game classpath

`RuntimeLibrarySelection.nativeArtifacts` используется как источник для `NativeExtractionPlan`

Также уже добавлены:

- Директория `natives` в `LauncherDirectories`
- Подготовка директории `natives` в `PREPARE_DIRECTORIES`
- Модель `NativeExtractionPlan`
- `NativeExtractionPlanBuilder`

Однако в lifecycle launcher пока нет отдельного шага, который отвечает за обработку выбранных
native artifacts перед запуском игры

Если встроить распаковку natives в `BUILD_GAME_LAUNCH_PLAN` или `LAUNCH_GAME`, границы ответственности смешаются

- Построение launch plan начнет заниматься файловой подготовкой runtime-окружения
- Запуск игры начнет выполнять подготовительные операции
- Native extraction будет сложнее тестировать отдельно
- Ошибки распаковки будут менее явно отражены в operation lifecycle

---

## Решение

Native extraction должна быть отдельной operation в launcher lifecycle

Новый шаг должен выполняться после успешной проверки локального состояния и подготовки директорий,
но до построения `GameLaunchPlan`

Целевой порядок lifecycle

```text
LOAD_MANIFEST
    -> VERIFY_FILES
        -> BUILD_DOWNLOAD_PLAN
            -> DOWNLOAD_FILES
                -> VERIFY_FILES
                    -> PREPARE_DIRECTORIES
                        -> EXTRACT_NATIVES
                            -> BUILD_GAME_LAUNCH_PLAN
                                -> LAUNCH_GAME
```

Если первичная verification успешна и download не требуется, порядок остается таким

```text
LOAD_MANIFEST
    -> VERIFY_FILES
        -> PREPARE_DIRECTORIES
            -> EXTRACT_NATIVES
                -> BUILD_GAME_LAUNCH_PLAN
                    -> LAUNCH_GAME
```

`EXTRACT_NATIVES` должна

- Использовать `NativeExtractionPlan`, построенный из `RuntimeLibrarySelection.nativeArtifacts`

`EXTRACT_NATIVES` не должна

- Выбирать native artifacts самостоятельно
- Читать raw manifest JSON
- Изменять `RuntimeLibrarySelection`, `Manifest` или game classpath

Если `NativeExtractionPlan` пустой, operation должна завершаться успешно без выполнения распаковки

---

## Последствия

Native extraction становится явной частью launcher lifecycle

Ошибки распаковки natives приводят launcher в `FAILED` до построения game launch plan

`BUILD_GAME_LAUNCH_PLAN` получает уже подготовленную директорию natives и не отвечает за распаковку файлов

`LAUNCH_GAME` остается только запуском процесса и не выполняет подготовительные filesystem-действия

`RuntimeLibrarySelection.nativeArtifacts` становится source-of-truth для native extraction flow

Verification/download flow не меняется: native artifacts по-прежнему проверяются и загружаются как обычные ресурсы
через compatibility projection

В будущем `GameLaunchPlanBuilder` сможет добавить JVM argument для директории natives, но это должно быть
отдельным решением

---

## Не входит в решение

- Реализация unzip/extract механизма
- Extract exclude rules
- Очистка директории natives перед распаковкой
- Добавление JVM argument для natives directory
- Изменение `ManifestResources`
- Изменение download/verification flow
- Изменение формата manifest JSON
- Architecture-specific natives selection

---

## Связанные решения

- [ADR-0014: Использовать ManifestResources как источник verification flow](ADR-0014-manifest-resources-verification-flow.md)
- [ADR-0020: Определить границу classifiers и natives metadata для libraries](ADR-0020-library-classifiers-and-natives-boundary.md)
- [ADR-0021: Определить границу обработки native artifacts](ADR-0021-native-artifact-processing-boundary.md)
- [ADR-0022: Определить границу результата загрузки manifest](ADR-0022-manifest-load-result-boundary.md)
- [ADR-0023: Использовать RuntimeLibrarySelection.libraries как источник game classpath](ADR-0023-use-libraries-as-game-classpath-source.md)
