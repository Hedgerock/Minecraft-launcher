# Ретроспектива: manifest runtime flow

## Контекст

Данный milestone развил manifest resources и launch metadata flow после `v0.6.1-operation-lifecycle-boundary`

До начала этапа launcher уже умел загружать manifest, выбирать runtime libraries, восстанавливать ресурсы, распаковывать
native artifacts и строить команду запуска

Однако assets еще не имели собственной domain model, launch metadata не содержала auth arguments, а общий набор ресурсов
не проверялся на согласованность локальных назначений

Целью этапа было расширить manifest metadata и укрепить существующий verification/download flow без добавления новых
launcher operations, полноценной авторизации или resource cache policy

---

## Что было сделано

- Зафиксирована граница assets index flow
- Добавлены модели `AssetEntry` и `AssetsIndex`
- Добавлен optional `assets` в manifest JSON mapping
- Assets включены в `ManifestResources`
- Зафиксирована граница launch metadata arguments
- Добавлены `LaunchInfo.authArgs`, JSON mapping и включение аргументов в launch command
- Добавлено интеграционное покрытие manifest JSON mapping и launch planning
- Зафиксирована граница согласованности manifest resources
- Добавлены `ResourceSetPlanner`, `PlannedResource` и `ResourceSetPlan`
- Добавлена структурированная диагностика конфликтующих назначений
- Подготовка ресурсов подключена перед verification и download
- Добавлены интеграционные сценарии восстановления совместимых назначений и отклонения конфликтующего download plan
- Quality gate расширен проверкой локальных Markdown-ссылок и форматирования JSON resources
- Quality checks вынесены в buildSrc precompiled script plugin
- Архитектурные решения перенесены в отдельную директорию records с сохранением индекса решений

---

## Что подтвердилось

`ManifestResources` остается подходящей границей для подключения новых типов восстанавливаемых ресурсов

Assets смогли войти в существующий verification/download lifecycle без отдельной operation и без изменения ответственности
`LauncherEngine`

Auth arguments могут развиваться как manifest metadata без введения authentication flow

При этом наличие `authArgs` не означает, что launcher уже получает токены или подставляет `${access_token}`

После расширения resource projection потребовалась общая проверка согласованности

Сравнения исходных строк `path` недостаточно, потому что разные строки могут разрешаться в одно локальное назначение

---

## Что было улучшено архитектурно

Manifest-specific модели сохраняют собственную семантику

`ManifestResources` отвечает за projection в `ResourceEntry`, но не определяет локальные назначения

`ResourceSetPlanner` отвечает за подготовку согласованного набора без файлового или сетевого доступа

Verification и download используют одинаковые правила совместимости ресурсов

Совместимые назначения объединяются, а конфликт отклоняет весь набор до начала проверки файлов или загрузки

Граница стала явной

```text
Manifest resources / DownloadPlan.resources
    -> ResourceSetPlanner
        -> ResourceSetPlan
            -> verification / download
```

Интеграционные тесты подтвердили связку production services, а не только поведение отдельных моделей и planner

---

## Что осталось отложенным

- Minecraft asset index compatibility и asset object layout
- Authentication flow и подстановки auth runtime values
- Profile/version-specific metadata
- Resource cache versioning и cleanup policy
- Mirror selection и URL fallback policy
- Physical file identity через symbolic links или hard links
- Транзакционная загрузка и rollback
- Parallel downloads и retry policy

---

## Технический долг

Технический долг в рамках данного milestone остается контролируемым

Согласованность определяется по нормализованным путям, а не по физической идентичности файлов

Разные URL для одного назначения считаются конфликтом даже при одинаковых `sha256` и `size`

Эти ограничения сохраняют однозначное поведение до появления отдельного сценария physical identity или mirror selection

Download progress рассчитывается из исходного `DownloadPlan`

Для вручную созданного плана с совместимыми дублями итоговые значения могут превышать объем фактической загрузки

Ограничение зафиксировано в документации и не проявляется в обычной цепочке verification → download planning, где
совместимые назначения уже объединены

---

## Главный вывод

Milestone подтвердил, что manifest metadata можно расширять без усложнения launcher-orchestration

Assets и auth arguments получили собственные границы, а verification/download flow — общую подготовку согласованного
набора ресурсов

Следующее направление должно выбираться после подготовки релиза и отдельной ревизии, а не через автоматическое расширение
scope текущего milestone
