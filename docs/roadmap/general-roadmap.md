# Общий путь развития проекта

## Текущий фокус

- Определить следующий runtime milestone после завершения Java executable flow
- Развить Java runtime flow без преждевременного Java installation discovery

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
- Стабилизирована Gradle build foundation: common module configuration, test dependencies, repositories, group
  version централизованы в root build script
