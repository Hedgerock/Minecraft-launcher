[← Назад к общему пути](general-roadmap.md)

# Путь развития библиотек

## Текущий план

- Реализовать подготовку согласованного набора ресурсов согласно [ADR-0051](../decisions/records/ADR-0051-manifest-resource-consistency-boundary.md)
- Подключить общий planning-компонент к verification/download flow и покрыть конфликтующие назначения тестами
- Assets index flow доведен до минимальной manifest resources projection
- Дальнейшие изменения library/native flow должны добавляться через отдельные ADR

---

## Milestone v0.4.0 — Library Native Flow

Library/native flow доведен до состояния, где manifest metadata проходит путь от JSON-контракта
до runtime selection, verification/download lifecycle, native extraction и построения launch command

Подробные итоги зафиксированы в [Ретроспективе library/native flow](../retrospective/2026-09-library-native-flow.md)

Ключевые результаты

- Добавлен manifest JSON contract
- Добавлен manifest JSON mapping
- Добавлены runtime library metadata модели
- Добавлен `RuntimeEnvironment`
- Добавлен `RuntimeEnvironmentProvider`
- Добавлены OS-specific library rules
- Добавлены classifiers и natives metadata
- Добавлен `RuntimeLibrarySelection`
- `RuntimeLibrarySelection.libraries` стал источником game classpath
- Selected native artifacts участвуют в verification/download flow через compatibility projection
- Добавлен `NativeExtractionPlan`
- Добавлен `EXTRACT_NATIVES`
- Добавлен модуль `launcher-natives`
- Добавлен `DefaultNativeExtractionService`
- Добавлена launch variable `${natives_directory}`
- Добавлена output policy для повторной распаковки natives

---

## Assets index flow

Assets index flow доведен до минимального состояния

### Закрыто

- Зафиксирована граница assets index flow
- Добавлена модель `AssetEntry`
- Добавлена модель `AssetsIndex`
- `AssetsIndex` добавлен в `Manifest`
- Manifest JSON mapping поддерживает optional `assets`
- Отсутствие `assets` преобразуется в пустой `AssetsIndex`
- `ManifestResources` включает assets в общий `ResourceEntry` projection

Дальнейшее развитие assets flow должно выполняться отдельными решениями после появления подтвержденного сценария

---

## Согласованность manifest resources

Следующий candidate flow — проверка согласованности общего набора ресурсов из `files`, выбранных libraries и assets

Несколько записей могут указывать на один локальный файл и содержать противоречивую физическую метадату

До реализации нужно определить

- Правило определения совпадающего локального назначения
- Поведение для одинаковых ресурсов
- Поведение при различиях `sha256`, `size` и `url`
- Границу проверки всего набора до выполнения resource verification и загрузки

Решение должно учитывать общий `ResourcePathResolver` и сохранять semantic ownership manifest-specific моделей

---

## Отложено

- Stable runtime identity для profile/version/manifest/installation
- Версионирование директории natives
- Cleanup policy для старых natives directories
- Параллельная поддержка нескольких runtime native sets
- Architecture-specific natives selection
- Rules на основе features
- Дальнейшее развитие assets flow после появления подтвержденного сценария
- Loader-specific правила запуска

---

## Возможные следующие направления

### Runtime identity

Ввести стабильную модель идентичности runtime-сценария только после появления подтвержденной
необходимости различать profile, version, manifest hash или installation

### Assets

Минимальный assets index flow закрыт через manifest model, JSON mapping и `ManifestResources` projection

Дальнейшее развитие assets flow должно начинаться с отдельного подтвержденного сценария и нового решения

### Launch metadata

Граница launch metadata arguments зафиксирована в [ADR-0050](../decisions/records/ADR-0050-launch-metadata-arguments-boundary.md)

Минимальный auth arguments flow реализован через `LaunchInfo`, manifest JSON mapping и построение `GameLaunchPlan.command`

Связка manifest JSON mapping и launch planning покрыта интеграционными тестами

Проверяются порядок аргументов, поддерживаемые подстановки в `authArgs`, сохранение неизвестных подстановок и отсутствие
`authArgs` в JSON

Минимальный launch metadata arguments flow завершен

Дальнейшее развитие требует подтвержденного сценария и определения источника новых runtime-данных

---

## Правило развития

Новые возможности library/native flow должны добавляться через отдельные ADR

Отложенные темы не считаются скрытым техническим долгом, пока для них явно указано условие возврата и
они не блокируют текущий runtime lifecycle
