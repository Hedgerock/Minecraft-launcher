[← Назад к списку решений](README.md)

# ADR-0009 Download Planning

## Статус
Accepted

---

## Контекст

Если первичная проверка файлов обнаруживает ресурсы со статусами `MISSING`, `OUTDATED` или `CORRUPTED`, launcher
должен построить явный план восстановления локального состояния

До появления `DownloadPlan` решение о том, какие файлы нужно скачать, могло смешиваться с проверкой файлов или с самим
выполнением загрузки

Такое смешение усложняет жизненный цикл `LauncherEngine`, тестирование и дальнейшее развитие загрузки

---

## Решение

Ввести отдельную модель `DownloadPlan`

`DownloadPlan` описывает список файлов, которые требуют восстановления, но не выполняет загрузку самостоятельно

`DownloadPlanBuilder` строит `DownloadPlan` на основе `VerificationPlan`

В `DownloadPlan` попадают файлы со статусами:

- `MISSING`
- `OUTDATED`
- `CORRUPTED`

Файлы со статусом `VALID` не попадают в `DownloadPlan`

`BuildDownloadPlanTask` сохраняет построенный `DownloadPlan` в `LaunchContext`

`DownloadFilesTask` получает уже построенный `DownloadPlan` из `LaunchContext` и передает его в `DownloadService`

`DownloadService` отвечает за выполнение загрузки, но не принимает решение о составе плана

После успешного выполнения `DOWNLOAD_FILES` launcher повторно запускает `VERIFY_FILES`, потому что факт загрузки сам
по себе не доказывает корректность локального состояния

---

## Последствия

- `VerificationService` не отвечает за загрузку файлов
- `DownloadService` не отвечает за принятие решения, какие файлы нужно скачать
- `LauncherEngine` работает с явными этапами: `VERIFY_FILES`, `BUILD_DOWNLOAD_PLAN`, `DOWNLOAD_FILES`, повторный
  `VERIFY_FILES`
- `DownloadPlan` становится стабильной моделью orchestration-слоя
- Повторная проверка после загрузки остается обязательной частью жизненного цикла
- Будущая параллельная загрузка, приоритеты и очереди могут быть добавлены поверх `DownloadPlan`, не меняя
  ответственность verification-слоя

---

## Альтернативы

1. Строить список файлов для загрузки внутри `DownloadService`

    > Отклонено, потому что `DownloadService` начал бы принимать orchestration-решения и
    > зависел бы от `VerificationPlan`

2. Скачивать файлы сразу внутри `VerificationService`

    > Отклонено, потому что verification должна оставаться read-only относительно локального состояния

3. Не вводить отдельный `DownloadPlan` и передавать `VerificationPlan` напрямую в загрузку

    > Отклонено, потому что это смешивает результат проверки и намерение восстановления

---

## Связанные документы

- [`download-flow.md`](../examples/download-flow.md)
- [`verification-flow.md`](../examples/verification-flow.md)
- [`launcher-lifecycle.md`](../architecture/launcher/launcher-lifecycle.md)
- [`module-boundaries.md`](../architecture/module-boundaries.md)
