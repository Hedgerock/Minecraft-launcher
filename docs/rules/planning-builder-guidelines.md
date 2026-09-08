# Правила planning builders

## Назначение

Документ фиксирует правило, когда planning builder может оставаться concrete implementation, а когда
должен быть выделен в отдельный contract

Planning builder строит промежуточную модель запуска, проверки, загрузки или подготовки окружения, но не выполняет
внешнее действие самостоятельно

---

## Основное правило

Planning builder должен становиться contract-ом, если он является collaborator-ом operation или task слоя и выполняется
хотя бы одно условие

- Builder скрывает несколько downstream dependencies
- Builder участвует в orchestration flow
- Builder получает данные из `LaunchContext` или configuration через task слой
- Builder имеет несколько возможных реализаций или ожидаемую вариативность поведения
- Builder нужен как test double для проверки task или operation behavior
- Concrete implementation содержит policy, которую не должен знать вызывающий слой

Planning builder может оставаться concrete implementation, если он является простой локальной transformation
без внешних эффектов и без подтвержденной вариативности поведения

---

## Примеры

`GameLaunchPlanBuilder` является contract-ом, потому что участвует в Java runtime selection flow, получает configured
Java override через task слой и скрывает несколько downstream dependencies

`DefaultGameLaunchPlanBuilder` является concrete implementation этого contract-а

`DownloadPlanBuilder` может оставаться concrete implementation, потому что на момент фиксации правила он является
stateless transformation из `VerificationPlan` в `DownloadPlan`

`NativeExtractionPlanBuilder` может оставаться concrete implementation, пока его ответственность ограничена построением
`NativeExtractionPlan` из `RuntimeLibrarySelection` и директории natives

Если для native extraction появятся versioned natives directory, cleanup policy, platform-specific layout или
необходимость task-level test double, `NativeExtractionPlanBuilder` может быть выделен в contract отдельной итерацией

---

## Чего избегать

Не нужно выделять interface только потому, что класс называется builder

Не нужно оставлять concrete dependency в task или operation слое, если вызывающему слою нужен stable contract, а не
детали реализации

Не нужно создавать общий marker-interface для всех builders

Не нужно переносить builder в другой модуль без отдельного архитектурного решения
