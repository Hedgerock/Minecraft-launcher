[← Назад к общему пути](general-roadmap.md)

# Путь развития presentation layer

## Текущий план

- Не добавлять recovery actions после launch failure или локализацию без подтвержденного сценария
- Не добавлять потоковый progress presentation, cancel behavior и retry policy без отдельного архитектурного решения

---

## Milestone `v0.9.0-local-ui-launch-flow` — Local UI launch flow

Локальный UI launch flow дополнен источником manifest URI из `keystone.properties` и платформенными пользовательскими каталогами

Startup-конфигурация проверяется до принятия launch request

Ожидаемые ошибки отображаются безопасно, а после внешнего исправления локального файла доступна повторная подготовка

UI показывает этапы принятого запроса и сохраняет отдельную диагностику неожиданных сбоев presentation boundary

Opt-in JavaFX smoke-тест подтверждает запуск через явно заданный URI локального HTTP-сервера

Сценарий чтения `keystone.properties` и Retry в этот smoke-тест пока не входит

Подробные итоги зафиксированы в [ретроспективе local UI launch flow](../retrospective/2026-09-local-ui-launch-flow.md)

---

## Milestone `v0.8.0-presentation-launch-boundary` — Presentation launch boundary

Минимальный presentation launch flow доведен до JavaFX entrypoint через application boundary

Presentation state управляет доступностью launch action и отображением статуса

Полученный `LaunchResult` и неожиданный сбой выполнения различаются через `PresentationLaunchCompletion` без изменения смысла
результата launcher lifecycle

Подробные итоги зафиксированы в [ретроспективе presentation launch boundary](../retrospective/2026-09-presentation-launch-boundary.md)

Архитектурные границы зафиксированы в

- [ADR-0052](../decisions/records/ADR-0052-presentation-launch-request-boundary.md)
- [ADR-0053](../decisions/records/ADR-0053-presentation-launch-state-boundary.md)
- [ADR-0055](../decisions/records/ADR-0055-presentation-launch-failure-boundary.md)
- [ADR-0056](../decisions/records/ADR-0056-presentation-launch-completion-boundary.md)

---

## Отложено

- Recovery actions после неуспешного launch request до появления подтвержденного пользовательского сценария
- Не добавлять потоковый progress отдельных операций, cancel behavior и retry policy без отдельных архитектурных решений
- Retry policy для повторного launch request до определения допустимых сценариев повторного запуска
- Game process lifecycle tracking до появления требования отслеживать состояние запущенной игры
- Локализация до появления устойчивого набора пользовательских текстов
- Shared UX system до появления нескольких независимых presentation clients
- Полноценная navigation model до появления нескольких самостоятельных screens
- Electron client до стабилизации technology-neutral application boundaries
- Backend split до появления подтвержденного remote client scenario
- Создание и редактирование `keystone.properties` в UI до определения отдельного пользовательского сценария настройки источника
  манифеста

---

## Возможные следующие направления

### Progress presentation

Передача и отображение текущего этапа принятого presentation launch request реализованы согласно
[ADR-0058](../decisions/records/ADR-0058-presentation-launch-phase-reporting-boundary.md)

Этап не заменяет presentation state или терминальный исход запроса

Потоковый progress verification, download и native extraction остается отложенным до появления промежуточных данных и
отдельного решения об их передаче в presentation layer

`launcher-ui` не подписывается на внутренние события `launcher-core` напрямую

### Presentation error model

Минимальная граница преобразования `LaunchFailure` в безопасное пользовательское представление зафиксирована и реализована
согласно [ADR-0055](../decisions/records/ADR-0055-presentation-launch-failure-boundary.md)

Дальнейшее развитие error categories, recovery actions и localization откладывается до появления отдельных подтвержденных
сценариев

### UI composition

Новые screens, navigation и reusable presentation components стоит вводить после появления нескольких независимых
пользовательских сценариев

Минимальный launch screen пока не подтверждает необходимость общего UI framework

---

## Правило развития

Presentation layer развивается через небольшие подтвержденные пользовательские сценарии

`launcher-ui` использует technology-neutral boundaries из `launcher-app` и не управляет `LauncherEngine`, operation layer или
application assembly напрямую

Новые состояния и interaction policies должны добавляться через отдельные ADR, если они изменяют ответственность, lifecycle
или границы presentation layer
