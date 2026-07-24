# Verification Flow

## Цель 

Описать последовательность проверки локального состояния игровых файлов 
перед выполнением DownloadOperation

## Предусловия

- Manifest успешно загружен
- OperationContext создан
- VerificationOperation инициализирован

## Последовательность

LauncherEngine
|
▼
OperationManager
|
▼
VerificationOperation
|
▼
VerificationService
|
▼
FileVerifier
|
├────────────────┐
|                |
▼                ▼               
HashVerifier    FileStorage
|                |
└──────┬─────────┘
       ▼
VerificationReport
       |
       ▼
TelemetryReport
       | 
       ▼
    Result

## Этапы

### 1.Инициализация

- Создается OperationContext
- Публикуется VerificationStarted

### 2.Проверка файлов (для каждого файла)

- Существует ли
- Соответствует ли размер
- Совпадает ли хеш

### 3.Формирование отчета

Создается immutable VerificationReport

В отчет попадают только игровые результаты проверки

### 4.Телеметрия

Создается TelemetryReport

Фиксируется:

- Количество проверенных файлов
- Длительность
- Число повреждений
- Число отсутствующих файлов

### 5.Завершение

- Публикуется VerificationCompleted
- Возвращается Result.success(...) или Result.failure(...)

## Компоненты

- OperationManager
- VerificationOperation
- VerificationService
- FileVerifier
- FileStorage
- VerificationReport
- TelemetryReport

## Результат

Если все файлы корректны, DownloadOperation может быть пропущена

Если обнаружены проблемы, DownloadOperation получает список файлов для восстановления

## Инварианты

V-1
Проверка не изменяет локальные файлы

V-2
VerificationReport является неизменяемым

V-3
Telemetry не влияет на результат проверки

V-4
VerificationOperation завершается только после формирования полного отчета
























