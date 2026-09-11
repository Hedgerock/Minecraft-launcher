# Общий путь развития проекта

## Текущий фокус

- Провести ревизию Java version compatibility flow перед adapter-level реализацией
- Не вводить Java installation discovery без подтвержденного сценария
- Не расширять Java runtime fallback policy до появления подтвержденного сценария несовместимой Java version
- Не расширять operation layer Java-specific failure reason без generic failure context
- Использовать правила planning builders при будущих изменениях operation planning boundaries
- После Java runtime compatibility flow усилить integration coverage для launch planning и Java runtime foundation

---

## Доменные направления

- [Путь развития библиотек](libraries-roadmap.md)
- [Путь развития Java runtime](java-runtime-roadmap.md)

---

## Перспективные направления

- [Перспективы развития проекта](potential-roadmap.md)

---

## Выполнено

- Правила безопасности `ResourceEntry.path` зафиксированы в [ADR-0016](../decisions/ADR-0016-resource-path-safety.md) и реализованы через общий `ResourcePathResolver`
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
