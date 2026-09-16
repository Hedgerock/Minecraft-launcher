[← Назад к общему пути](general-roadmap.md)

# Путь развития библиотек

## Текущий план

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

## Отложено

- Stable runtime identity для profile/version/manifest/installation
- Версионирование директории natives
- Cleanup policy для старых natives directories
- Параллельная поддержка нескольких runtime native sets
- Architecture-specific natives selection
- Rules на основе features
- Дальнейшее развитие assets flow после появления подтвержденного сценария
- Auth launch arguments
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

Расширить manifest launch metadata аргументами авторизации, loader-specific правилами и дополнительными
runtime-подстановками только после появления конкретного сценария

---

## Правило развития

Новые возможности library/native flow должны добавляться через отдельные ADR

Отложенные темы не считаются скрытым техническим долгом, пока для них явно указано условие возврата и
они не блокируют текущий runtime lifecycle
