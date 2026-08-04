# Launcher Lifecycle

## Цель 

Описать жизненный цикл приложения Launcher, основные состояния системы и
взаимодействие между ключевыми компонентами

## Основная идея

Launcher представляет собой управляемую систему состояний

Каждый этап жизненного цикла приложения имеет четко определенную ответственность и не может
самостоятельно нарушать границы соседних этапов

## Таблица состояний

| State            | Responsibility                      |
|------------------|-------------------------------------|
| IDLE             | Launcher запущен и ожидает действия |
| CHECKING_UPDATES | Проверка доступности новой версии   |
| LOADING_MANIFEST | Получение описания сборки           |
| VERIFYING_FILES  | Проверка локального состояния       |
| DOWNLOADING      | Получение недостающих ресурсов      |
| PREPARING_GAME   | Подготовка окружения                |
| LAUNCHING        | Запуск игрового процесса            |
| RUNNING          | Игра запущена                       |
| FAILED           | Не удалось выполнять операцию       |

## Инварианты

L-1

В один момент времени Launcher выполняет не более одной Operation для нового профиля

L-2

Каждая Operation использует собственный LaunchContext

L-3

LauncherEngine не содержит бизнес-логики отдельных Operation

L-4

Все переходы между состояниями происходят исключительно через LauncherStateMachine

L-5

Завершение Operation всегда приводит систему в согласованное состояние 
(RUNNING, FAILED или IDLE в зависимости от сценария)

После успешной загрузки manifest launcher переходит к VERIFYING_FILES

До появления DownloadOperation невалидный VerificationPlan переводит launcher в FAILED

## Обоснование Проекта

- Bootstrap отделен от LauncherEngine для изоляции процесса сборки зависимостей
- LauncherEngine не знает деталей конкретных Operation
- LaunchContext создается отдельно для каждой Operation
- LauncherStateMachine централизует изменение состояния приложения
- OperationManager управляет запуском Operation
- ExecutionStrategy определяет способ выполнения LauncherTask внутри Operation

Связанные документы

- ADR-0003 Launcher Engine Responsibility
- ADR-0005 Context Ownership (Deferred)
- RFC-0002 Session Lifecycle Management
- RFC-0003 Task Cancellation

Диаграммы

- launcher-lifecycle.puml
- launcher-user-journey.puml
- launcher-startup.puml
- launch-sequence.puml











