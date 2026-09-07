[← Назад к общему пути](general-roadmap.md)

# Перспективы развития проекта

## Назначение

Документ содержит направления, которые могут стать активными итерациями позже

Перспективы не считаются обязательством к реализации, пока они не перенесены в текущий план
или отдельный domain roadmap

---

## Gradle и build foundation

Централизованы:

- group
- version
- repositories
- Java toolchain
- JUnit test dependencies
- test platform configuration

`docs` не является Gradle module, но остается в области root-level `qualityCheck`

Дальнейшее развитие build foundation пока отложено

Вернуться к теме стоит, когда появится необходимость в convention plugins, version catalog, dependency looking или
более строгом dependency analysis

---

## Quality gate

Текущий qualityCheck полезен, но минимален.

Можно постепенно добавить:

- Checkstyle
- SpotBugs
- PMD
- JaCoCo
- dependency analysis
- архитектурные тесты на зависимости между модулями

Вернуться к теме стоит, когда ручные review начнут регулярно находить однотипные проблемы,
которые можно проверять автоматически

Базовое усиление quality gate выполнено

Добавлены проверки

- missing final newline
- empty catch blocks
- `System.err.println`
- некорректная комбинированная секция `//given && when`

Дальнейшее усиление quality gate остается отложенным

Вернутся к теме стоит во время следующей stabilization iteration, если ручные review снова начнут
находить повторяющиеся проблемы

---

## Test style stabilization

Зафиксированы [правила написания тестов](../rules/test-guidelines.md)

Начата ручная полировка тестового стиля на уровне `launcher-model`

Дальнейшее выравнивание тестов переносится в следующую stabilization iteration

---

## Project foundation stabilization

Перед следующим runtime milestone нужно завершить короткую foundation stabilization итерацию

Цель итерации

- Уточнить оставшиеся спорные границы `launcher-core`
- Поддерживать зафиксированную политику reserved modules
- Поддерживать минимальный CI pipeline для GitHub
- Не развивать новые launcher features до завершения foundation pass

Reserved modules зафиксированы в [ADR-0037](../decisions/ADR-0037-reserved-modules-policy.md)

- `launcher-auth` зарезервирован для будущего authentication flow
- `launcher-ui` зарезервирован для будущего presentation layer
- `launcher-common` остается строго ограниченным reserved shared primitives module

Добавлять код в `launcher-common` можно только после подтвержденного cross-module сценария или отдельного
architecture/design decision

Минимальный CI pipeline добавлен и проверяет

- Java 21
- `./gradlew clean check`

Расширение CI за пределы минимального quality gate откладывается до появления повторяющихся проблем,
которые выгодно проверять автоматически

---

## Error model

Для лаунчера очень важно, чтобы ошибки были не просто failed, а имели понятный контекст:

- что сломалось
- на каком operation step
- какой файл, URL или library затронуты
- можно ли повторить операцию
- нужно ли удалить поврежденный файл
- к какому типу относится ошибка: network, manifest, hash, storage, permission или runtime

Вернуться к теме стоит перед расширением recovery behavior, retry policy или пользовательских сообщений
об ошибках

---

## Launch workflow

Перед добавлением auth, profile или version selection лучше не перегружать `LauncherEngine`

Можно заранее подумать о модели

``` text
LaunchWorkflow
LaunchStep
ConditionalLaunchStep
LaunchFailurePolicy
```

Вернуться к теме стоит, когда количество условных шагов запуска начнет усложнять `LauncherEngine`

## Основное правило

Внедрение новых приоритетов не должно быть преждевременным

Каждое направление должно стать активным только после появления конкретного сценария, архитектурного
решения или понятной пользы для следующего этапа проекта
