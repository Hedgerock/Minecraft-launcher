# Общий путь развития проекта

## Текущий фокус

- Завершить ревизию Java runtime flow после подключения production PATH resolution
- Определить следующий шаг Java executable failure diagnostics

---

## Доменные направления

- [Путь развития библиотек](libraries-roadmap.md)

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
