# Project Glossary

## Launcher

### LauncherEngine

Главный координатор жизненного цикла Launcher

Запускает LaunchOperation и управляет переходами LauncherStateMachine

### LauncherState

Состояние приложения Launcher в текущий момент времени

### LauncherStateMachine

Компонент, отвечающий за изменение состояний Launcher

### LaunchContext

Runtime-контекст текущего запуска Launcher

Передается в LauncherTask исключительно во время выполнения

LauncherTask не хранит LaunchContext, а использует его как входной параметр

### LauncherTask

Минимальная исполняемая единица внутри LaunchOperation

LauncherTask не владеет LaunchContext

LaunchContext передается только во время выполнения execute(...)

---

## Operations

### LaunchOperation

Минимальная завершенная операция Launcher, имеющая собственный жизненный цикл, LauncherContext
и набор LauncherTask

### LauncherTask

Минимальная единица работы внутри LaunchOperation

### LaunchContext

Контекст выполнения одной LaunchOperation

Содержит исключительно данные, необходимые данной операции

Создается отдельно для каждой новой Operation

### OperationManager

Компонент, управляющий жизненным циклом LaunchOperation

Не создает операции

Не содержит бизнес-логики операций

---

## Authentication

### Session

Неизменяемый объект, описывающий активную пользовательскую сессию

### SessionHandle

Временный объект владения Session

Представляет безопасный доступ к Session во время выполнения LauncherTask

### AuthenticationProvider

Компонент, создающий новую Session

Не отвечает за ее хранение

---

## Execution

### Execution Strategy

Компонент, определяющий способ выполнения набора LauncherTask

### Sequential Execution

Последовательное выполнение LauncherTask

### Parallel Execution

Одновременное выполнение независимых LauncherTask

### Independent Task

LauncherTask, выполнение которой не зависит от результата другой LauncherTask

### Operation Completion

Момент, когда LaunchOperation достигла согласованного архитектурного состояния, включающего
завершения LauncherTask, построение результата, публикацию событий и изменение состояния
Launcher

---

## Planning

### DownloadPlan

Описание порядка загрузки ресурсов

Не выполняет загрузку самостоятельно

### VerificationReport

Результат выполнения проверки файлов

Не содержит логики проверки

---

## Documentation

### ADR (Architecture Decision Record)

Документирует принятое архитектурное решение

### RFC (Request For Comments)

Документирует архитектурное правило или соглашение проекта

---

## Concrete Operation

Конкретная реализация LaunchOperation, содержит только зависимости, необходимые для
выполнения собственного сценария. Общая инфраструктура жизненного цикла наследуется от
LaunchOperation

---

## Event

Свершившийся факт, произошедший в системе, который может представлять интерес для других
компонентов, но не требует знания о конкретных получателях

---

## Architecture Evolution Rule

Каждый новый уровень архитектуры сначала должен использовать уже существующие модели

Новая модель вводится только тогда, когда существующая перестает выражать необходимое поведение