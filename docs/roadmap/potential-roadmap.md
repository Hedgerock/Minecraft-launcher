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

Вернуться к теме стоит, когда появится необходимость в version catalog, dependency locking или более строгом dependency
analysis

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
- local Markdown links
- formatting JSON resources
- formatting GitHub Actions YAML
- formatting PlantUML diagrams
- wildcard imports
- `System.err.println`
- `System.out.println`
- `printStackTrace`
- `@SuppressWarnings("all")`
- некорректная комбинированная секция `//given && when`

Дальнейшее усиление quality gate остается отложенным

Вернуться к теме стоит во время следующей stabilization iteration, если ручные review снова начнут
находить повторяющиеся проблемы

---

## Test style stabilization

Зафиксированы [правила написания тестов](../rules/test-guidelines.md)

Начата ручная полировка тестового стиля на уровне `launcher-model`

Дальнейшее выравнивание тестов переносится в следующую stabilization iteration

---

## Project foundation stabilization — итог

Foundation stabilization завершена

В рамках итерации

- Уточнены границы `launcher-core`
- Зафиксирована политика reserved modules
- Добавлен минимальный Github Actions CI pipeline
- Централизована общая Gradle configuration
- Усилен project quality gate

Дальнейшая stabilization не является активным milestone

Возвращаться к теме стоит при появлении повторяющихся проблем, которые выгодно закрепить автоматическими проверками

Исходная политика reserved modules зафиксирована в [ADR-0037](../decisions/records/ADR-0037-reserved-modules-policy.md)

- `launcher-auth` зарезервирован для будущего authentication flow
- `launcher-ui` активирован как минимальный presentation module после реализации presentation launch boundary
- `launcher-common` остается строго ограниченным reserved shared primitives module

Добавлять код в `launcher-common` можно только после подтвержденного cross-module сценария или отдельного
architecture/design decision

Минимальный CI pipeline добавлен и проверяет

- Java 21
- `./gradlew clean check`

Расширение CI за пределы минимального quality gate откладывается до появления повторяющихся проблем,
которые выгодно проверять автоматически

---

## Логирование и эксплуатационная диагностика

Launcher пока не имеет общей политики структурированного логирования, хранения диагностических данных и корреляции событий
одного launcher lifecycle

Локальное логирование может понадобиться перед дальнейшим развитием технической диагностики, recovery behavior и сценариев
технической поддержки

До реализации необходимо определить

- Границу между пользовательской ошибкой и технической диагностикой
- Уровни логирования
- Правила исключения credentials, tokens и персональных данных
- Формат и место хранения локальных логов
- Политику ротации и очистки
- Идентификатор корреляции одного launcher lifecycle
- Ответственность модулей за создание диагностических событий

Централизованный monitoring, remote telemetry и crash reporting пока не вводятся

Вернуться к теме стоит при появлении backend integration, support workflow или подтвержденной необходимости собирать
диагностику за пределами локального launcher process

---

## Error model

Для лаунчера очень важно, чтобы ошибки были не просто failed, а имели понятный контекст:

- что сломалось
- на каком operation step
- какой файл, URL или library затронуты
- можно ли повторить операцию
- нужно ли удалить поврежденный файл
- к какому типу относится ошибка: network, manifest, hash, storage, permission или runtime

Минимальное presentation-safe отображение launch failure перенесено в активный [Путь развития presentation layer](presentation-roadmap.md)

Расширенная классификация ошибок, recovery behavior и retry policy остаются перспективными направлениями

Вернуться к расширенной error model стоит перед добавлением recovery behavior, retry policy или более детализированных
пользовательских сообщений об ошибках

---

## Launch workflow & Launcher workflow framework

Перед добавлением auth, profile или version selection лучше не перегружать `LauncherEngine`

Можно заранее подумать о модели workflow

``` text
LaunchWorkflow
LaunchStep
ConditionalLaunchStep
LaunchFailurePolicy
```

Также к этому направлению относится потенциальное введение concurrent execution model на уровне workflow,
но только после появления независимых шагов, которые действительно можно выполнять параллельно без нарушения lifecycle
invariants

Вернуться к теме стоит, когда количество условных шагов запуска начнет усложнять `LauncherEngine` или потребует параллельного выполнения

---

## Расширение presentation layer

Активное развитие presentation flow вынесено в [Пути развития presentation layer](presentation-roadmap.md)

Electron client, backend split и shared UX system остаются перспективными направлениями

Возвращаться к этим перспективным направлениям стоит после стабилизации technology-neutral application boundaries и
появления подтвержденного multi-client сценария

---

## Основное правило

Внедрение новых приоритетов не должно быть преждевременным

Каждое направление должно стать активным только после появления конкретного сценария, архитектурного
решения или понятной пользы для следующего этапа проекта
