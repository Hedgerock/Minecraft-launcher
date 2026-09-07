# Общий путь развития проекта

## Текущий фокус

- Провести foundation stabilization перед следующим runtime milestone
- Уточнить границы `launcher-core` после переноса concrete adapters
- Зафиксировать статус reserved modules
- Добавить минимальный CI pipeline для GitHub

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
