# Общий путь развития проекта

## Текущий фокус

- Определить минимальный источник manifest URI для запуска из UI
  согласно [ADR-0059](../decisions/records/ADR-0059-launcher-manifest-source-boundary.md)
- Не вводить Java installation discovery без подтвержденного сценария
- Не расширять Java runtime fallback policy до появления подтвержденного сценария несовместимой Java version
- Не вводить domain-specific failure codes в operation layer без отдельного подтвержденного сценария
- Использовать правила planning builders при будущих изменениях operation planning boundaries
- Java runtime flow временно закрыт до появления подтвержденного сценария

## Доменные направления

- [Путь развития библиотек](libraries-roadmap.md)
- [Путь развития Java runtime](java-runtime-roadmap.md)
- [Путь развития presentation layer](presentation-roadmap.md)

---

## Перспективные направления

- [Перспективы развития проекта](potential-roadmap.md)

---

## Выполнено

- Правила безопасности `ResourceEntry.path` зафиксированы в [ADR-0016](../decisions/records/ADR-0016-resource-path-safety.md) и реализованы через общий `ResourcePathResolver`
- Введен минимальный quality gate для проверки кода и документации
- Зафиксированы правила написания ADR
- Зафиксированы правила написания git commits
- Зафиксированы правила ведения roadmap
- Завершен milestone `v0.4.0-library-native-flow`
- Подключен production PATH resolution для Java command name в application assembly
- Проведена stabilization cleanup-итерация: усилен quality gate, стабилизирована Gradle build foundation
  и зафиксированы правила написания тестов
- Зафиксирована политика reserved modules для `launcher-auth`, `launcher-common` и `launcher-ui`
- Добавлен минимальный GitHub Actions CI pipeline для проверки `./gradlew clean check`
- Завершена foundation stabilization после `v0.4.0-library-native-flow`
- Подготовлен release `v0.5.0-java-runtime-foundation`
- Java version requirement flow доведен до manifest source и подключенной compatibility boundary
- Подготовлен release `v0.6.0-java-runtime-compatibility`
- Зафиксирована граница launcher lifecycle integration testing и добавлен первый application assembly integration test
- Зафиксирована внешняя launcher boundary для обработки `LaunchResult` перед развитием future UI boundary
- Реализована минимальная обработка `LaunchResult` на application boundary без добавления presentation logic
- Завершена минимальная реализация generic operation failure context через operation result, task execution boundary
  и failed operation event без расширения `LaunchResult`
- Подготовлен release `v0.6.1-operation-lifecycle-boundary`
- Assets index flow доведен до минимальной manifest resources projection
- Минимальный launch metadata arguments flow реализован через manifest model, JSON mapping и command building,
  с интеграционным покрытием launch planning
- Реализована общая подготовка согласованного набора ресурсов перед verification и download
- Интеграционно подтверждены восстановление совместимых повторных назначений и отклонение конфликтующего download plan до
  загрузки ресурсов
- Завершен milestone `v0.7.0-manifest-runtime-flow`; подробные итоги зафиксированы в [ретроспективе manifest runtime flow](../retrospective/2026-09-manifest-runtime-flow.md)
- Завершен milestone `v0.8.0-presentation-launch-boundary`; подробные итоги зафиксированы
  в [ретроспективе presentation launch boundary](../retrospective/2026-09-presentation-launch-boundary.md)
- Реализована локальная диагностика неожиданных сбоев принятого presentation launch request согласно
  [ADR-0057](../decisions/records/ADR-0057-presentation-launch-diagnostics-boundary.md) без передачи технической причины в UI
- Реализована передача этапов принятого presentation launch request от launcher lifecycle до JavaFX UI согласно
  [ADR-0058](../decisions/records/ADR-0058-presentation-launch-phase-reporting-boundary.md)
