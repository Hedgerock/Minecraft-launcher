[← Назад к общему пути](general-roadmap.md)

# Путь развития библиотек

## Текущий план

- Новых активных задач в library/native flow сейчас нет
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

## Отложено

- Stable runtime identity для profile/version/manifest/installation
- Версионирование директории natives
- Cleanup policy для старых natives directories
- Параллельная поддержка нескольких runtime native sets
- Architecture-specific natives selection
- Rules на основе features
- Assets index
- Auth launch arguments
- Loader-specific правила запуска

---

## Возможные следующие направления

### Runtime identity

Ввести стабильную модель идентичности runtime-сценария только после появления подтвержденной
необходимости различать profile, version, manifest hash или installation

### Assets

Добавить отдельную модель assets/index после завершения текущего library/native milestone

### Launch metadata

Расширить manifest launch metadata аргументами авторизации, loader-specific правилами и дополнительными
runtime-подстановками только после появления конкретного сценария

---

## Правило развития

Новые возможности library/native flow должны добавляться через отдельные ADR

Отложенные темы не считаются скрытым техническим долгом, пока для них явно указано условие возврата и
они не блокируют текущий runtime lifecycle
