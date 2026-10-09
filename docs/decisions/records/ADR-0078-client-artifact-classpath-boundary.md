[← Назад к списку решений](../README.md)

# ADR-0078: Определить источник client artifact для game classpath

## Статус

Accepted

---

## Контекст

На момент принятия решения [ADR-0023](ADR-0023-use-libraries-as-game-classpath-source.md) закрепляет `RuntimeLibrarySelection.libraries`
как источник библиотек для game classpath

`GameClasspathBuilder` использует `launchInfo.classpath` только тогда, когда выбранных библиотек нет

Если библиотеки выбраны, путь к client artifact из `launchInfo.classpath` не попадает в итоговый classpath

Кроме того, одна запись в `launchInfo.classpath` не предоставляет физическую метадату, необходимую для проверки и восстановления
client artifact

Для первого воспроизводимого технического запуска необходимо связать проверяемый файл с его участием в classpath, не представляя
его как библиотеку или native artifact

---

## Решение

Физическая метадата client artifact описывается существующим `FileEntry` в `Manifest.files`

`LaunchInfo` содержит необязательный `clientArtifactPath`, указывающий на соответствующий `FileEntry.path` того же манифеста

Если `clientArtifactPath` указан, он должен ссылаться на ресурс из `Manifest.files`; отсутствие такого ресурса отклоняется
до запуска игрового процесса

`GameClasspathBuilder` строит game classpath из выбранных `RuntimeLibrarySelection.libraries` и указанного client artifact

Пути всех записей разрешаются через существующий `ResourcePathResolver`

`RuntimeLibrarySelection.nativeArtifacts` и compatibility projection `Manifest.libraries` не становятся источником game classpath

При наличии `clientArtifactPath` список `launchInfo.classpath` не требуется для построения classpath и может быть пустым

Если `clientArtifactPath` отсутствует, прежнее поведение сохраняется: выбранные библиотеки остаются основным источником, а
`launchInfo.classpath` используется как fallback при их отсутствии

`ManifestResources` продолжает преобразовывать client `FileEntry` в `ResourceEntry` для существующего verification/download flow

`LauncherEngine` не получает ответственность за выбор записей classpath

---

## Рассмотренные варианты

### Включать client artifact в список runtime libraries

Вариант отклонен

Client artifact не является выбранной runtime library; такое представление смешало бы разные роли ресурсов

### Всегда объединять `launchInfo.classpath` с выбранными библиотеками

Вариант отклонен

Существующий список является fallback и может повторять выбранные библиотеки или содержать записи без метадаты
для восстановления

### Ввести отдельную модель client artifact с собственной физической метадатой

Вариант отложен

На момент принятия решения существующий `FileEntry` уже описывает данные, необходимые для проверки и загрузки файла

Отдельная модель будет оправдана, если client artifact получит самостоятельные правила выбора или жизненный цикл

---

## Последствия

Client artifact участвует в существующем verification/download flow и отдельно определяется как запись game classpath

Манифесты без `clientArtifactPath` сохраняют прежнюю семантику построения classpath

Для манифестов с явным client artifact потребуется проверить соответствие ссылки записи в `Manifest.files`

`LaunchInfo` временно поддерживает два способа задания источника classpath: явный client artifact для нового сценария и
прежний fallback для существующих манифестов

Решение устраняет пробел в составе classpath, но само по себе не подтверждает успешный запуск реальной игры

---

## Не входит в решение

- Полная поддержка формата официальных Minecraft version manifests
- Выбор версии игры или client artifact из удалённых метаданных
- Новые операции verification или download
- Изменение способа передачи classpath в аргументах JVM
- Authentication flow
- Строгие переходы состояния launcher lifecycle

---

## Связанные решения

- [ADR-0014: Использовать ManifestResources как источник verification flow](ADR-0014-manifest-resources-verification-flow.md)
- [ADR-0023: Использовать RuntimeLibrarySelection.libraries как источник game classpath](ADR-0023-use-libraries-as-game-classpath-source.md)
- [ADR-0050: Определить границу launch metadata arguments](ADR-0050-launch-metadata-arguments-boundary.md)
- [ADR-0077: Определить семантику отсутствующего загрузчика в манифесте](ADR-0077-optional-manifest-loader.md)
