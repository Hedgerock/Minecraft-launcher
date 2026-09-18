[← Назад к общему пути](general-roadmap.md)

# Путь развития библиотек

## Текущий план

- Подтвердить проверки release candidate перед созданием тега
- После релиза провести ревизию проекта перед выбором следующего направления

---

## Milestone v0.7.0 – Manifest Runtime Flow

Manifest resources и launch metadata flow расширены без изменения последовательности launcher operations

Ключевые результаты

- Assets получили собственные модели, manifest JSON mapping и projection в `ManifestResources`
- `LaunchInfo.authArgs` проходят через JSON mapping и включаются в launch command
- Manifest mapping и launch planning покрыты интеграционными тестами
- `ResourceSetPlanner` подготавливает согласованный набор перед verification и download
- Совместимые локальные назначения объединяются, конфликтующие записи отклоняются до обработки ресурсов
- Resource recovery flow и отклонение конфликтующего download plan подтверждены интеграционными тестами

Полноценная авторизация, Minecraft asset index compatibility и resource cache policy не входят в milestone

Подробные итоги зафиксированы в [Ретроспективе manifest runtime flow](../retrospective/2026-09-manifest-runtime-flow.md)

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

Минимальный launch metadata arguments flow завершен в рамках milestone `v0.7.0`

Дальнейшее развитие требует подтвержденного сценария и определения источника новых runtime-данных

---

## Правило развития

Новые возможности library/native flow должны добавляться через отдельные ADR

Отложенные темы не считаются скрытым техническим долгом, пока для них явно указано условие возврата и
они не блокируют текущий runtime lifecycle
