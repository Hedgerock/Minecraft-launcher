# События загрузки

## Статус

Документ описывает текущий контракт специализированных событий загрузки файлов

На текущем этапе `DOWNLOAD_FILES` публикует как общие события жизненного цикла `Operation`, так и специализированные
события загрузки

### Общие события

- `OperationStartedEvent`
- `OperationCompletedEvent`
- `OperationFailedEvent`

### Специализированные события

- `DownloadStartedEvent`
- `DownloadProgressChangedEvent`
- `DownloadCompletedEvent`

Специализированные события публикуются непосредственно `DownloadFilesTask` через `EventBus`


## Обзор

`DownloadFilesTask` публикует события, описывающие жизненный цикл выполнения загрузки файлов

События являются неизменными фактами и не используются для управления выполнением операции

На текущем этапе `DownloadProgressChangedEvent` не представляет потоковый прогресс. После успешного
завершения `downloadService.download(...)` публикуется одно событие с полностью завершенным прогрессом

Ошибки загрузки не публикуются специализированным событием. Они преобразуются в `Result.failure(...)` и передаются
вызывающему коду

## DownloadStartedEvent

### Источник

`DownloadFilesTask`

### Подписчики

- Launcher UI
- Telemetry

### Данные события

- `TotalFiles`
- `TotalBytes`

### Гарантии

- Публикуется ровно один раз для каждого непустого `DownloadPlan`
- Публикуется перед началом `downloadService.download(...)`
- Для пустого `DownloadPlan` не публикуется

---

## DownloadProgressChangedEvent

### Источник

`DownloadFilesTask`

### Подписчики

- `Launcher UI`
- `Telemetry`

### Данные события

- `DownloadFiles`
- `TotalFiles`
- `DownloadedBytes`
- `TotalBytes`

### Текущее поведение

На текущем этапе публикуется один раз после успешного завершения `downloadService.download(...)`

Событие содержит полностью завершенный прогресс

- `DownloadedFiles == TotalBytes`
- `DownloadedBytes == TotalBytes`

Потоковая публикация промежуточного прогресса пока не реализована

### Гарантии

- Не публикуется при ошибке загрузки
- Не публикуется для пустого `DownloadPlan`
- При успешной загрузке публикуется до `DownloadCompletedEvent`

---

## DownloadCompletedEvent

### Источник

`Download Operation`

### Подписчики

- `Launcher UI`
- `Telemetry`
- `Logger`

### Данные события

- `TotalFiles`
- `TotalBytes`

### Гарантии

- Публикуется не более одного раза для каждого успешного выполнения загрузки
- Публикуется только после успешного завершения `downloadService.download(...)`
- Публикуется после `DownloadProgressChangedEvent`
- Не публикуется при ошибке загрузки
- Не публикуется для пустого `DownloadPlan`

Является завершающим специализированным событием жизненного цикла загрузки

---

## Порядок событий

```text
Для непустого `DownloadPlan` при успешной загрузке:
    DownloadStartedEvent
        -> downloadService.download(...)
            -> DownloadProgressChangedEvent
                -> DownloadCompletedEvent
                
При ошибке загрузки:
    DownloadStartedEvent
        -> downloadService.download(...)
            -> Result.failure(...)          
```

`DownloadProgressChangedEvent` и `DownloadCompletedEvent` после ошибки не публикуются

Для пустого `DownloadPlan` -> Result.success(...)

Специализированные события загрузки не публикуются

---

## Примечания

`DownloadFilesTask` не знает о конкретных подписчиках событий

`DownloadService` не отвечает за публикацию событий и не зависит от `EventBus`

Текущая реализация `DownloadProgressChangedEvent` является промежуточной. При появлении требования
на потоковый прогресс контракт `DownloadService` может быть расширен механизмом listener/callback

Инфраструктурные проблемы могут публиковаться отдельными инфраструктурными событиями и не являются частью
специализированного жизненного цикла загрузки











