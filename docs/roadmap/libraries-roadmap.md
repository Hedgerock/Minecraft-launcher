[← Назад к общему пути](general-roadmap.md)

# Путь развития библиотек

## Текущий план

- Подтвердить связку получения, проверки и преобразования vanilla assets в manifest resources интеграционным тестом
- Довести подготовку vanilla assets до включения в окончательный управляемый манифест согласно
  [ADR-0080](../decisions/records/ADR-0080-vanilla-asset-preparation-boundary.md)
- Сверить ресурсы и аргументы одной выбранной vanilla Minecraft version с возможностями текущего manifest lifecycle
- Другие направления library/native flow возобновлять после появления подтвержденного сценария

---

## Milestone `v0.7.0-manifest-runtime-flow` — Manifest runtime flow

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

## Milestone `v0.4.0-library-native-flow` — Library native flow

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
- Loader-specific правила запуска

---

## Возможные следующие направления

### Runtime identity

Ввести стабильную модель идентичности runtime-сценария только после появления подтвержденной
необходимости различать profile, version, manifest hash или installation

### Assets

Файл asset index и объявленные asset objects проходят manifest mapping и существующий verification/download flow

Их совместная работа с client artifact подтверждена launcher lifecycle integration test

Граница подготовки полного набора объектов на стороне публикации определена в [ADR-0080](../decisions/records/ADR-0080-vanilla-asset-preparation-boundary.md)

`launcher-preparer` получает файл индекса и объекты по HTTPS, проверяет их байты и сохраняет URI источников в подготовленных
результатах согласно [ADR-0081](../decisions/records/ADR-0081-vanilla-asset-resource-source.md)

`VanillaAssetsMapper` формирует `AssetsIndex` с проверкой соответствия объектов индексу, определением локальных путей и
сохранением метаданных проверенных ресурсов

Следующий шаг — интеграционно подтвердить связку подготовки assets перед сборкой окончательного управляемого манифеста

### Launch metadata

Граница launch metadata arguments зафиксирована в [ADR-0050](../decisions/records/ADR-0050-launch-metadata-arguments-boundary.md)

Минимальный launch metadata arguments flow завершен в рамках milestone `v0.7.0`

Дальнейшее развитие требует подтвержденного сценария и определения источника новых runtime-данных

---

## Правило развития

Новые возможности library/native flow должны добавляться через отдельные ADR

Отложенные темы не считаются скрытым техническим долгом, пока для них явно указано условие возврата и
они не блокируют текущий runtime lifecycle
