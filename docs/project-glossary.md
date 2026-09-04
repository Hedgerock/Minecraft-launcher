# Глоссарий проекта

## Лаунчер

### LauncherEngine

Главный координатор жизненного цикла `Launcher`

Запускает `LaunchOperation` и управляет переходами `LauncherStateMachine`

### LauncherState

Состояние приложения `Launcher` в текущий момент времени

### LauncherStateMachine

Компонент, отвечающий за изменение состояний `Launcher`

### LaunchContext

Runtime-контекст текущего запуска лаунчера

Передается в `LauncherTask` исключительно во время выполнения

`LauncherTask` не хранит `LaunchContext`, а использует его как входной параметр

### LauncherTask

Минимальная исполняемая единица внутри `LaunchOperation`

`LauncherTask` не владеет `LaunchContext`

`LaunchContext` передается только во время выполнения `execute(...)`

### LaunchInfo

Метаданные запуска игры из `Manifest`

Содержит `mainClass`, `javaExecutable`, `jvmArgs`, `gameArgs` и `classpath`

Используется `GameLaunchPlanBuilder` для построения команды запуска

### Исполняемый файл Java

Первый элемент команды запуска игрового процесса

На текущем этапе значение выбирается через `ManifestJavaRuntimeSelector`, который использует
`LaunchInfo.javaExecutable` и не выполняет автоматический поиск Java runtime

### LaunchVariables

Набор значений, доступных при построении команды запуска

На текущем этапе содержит версию игры, путь к игровой директории, отформатированный `classpath`
и путь к директории natives

### LaunchArgumentResolver

Компонент, преобразующий аргументы запуска с подстановками в итоговые аргументы команды

Неизвестные подстановки сохраняются без изменений

### Manifest

Описание сборки, полученное перед `verification flow`

Содержит список файлов, список libraries и `launch metadata`

`files` является обязательным неизменяемым списком

Связанный внешний контракт описан в [Контракт manifest JSON](contracts/manifest-json.md)


### FileEntry

Описание одного файла из `Manifest`

Содержит поля

- `path`
- `sha256`
- `size`
- `url`

Используется `verification flow` и `download flow`

### LibraryEntry

Минимальное описание library из `Manifest`

На текущем этапе содержит поля `path`, `sha256`, `size` и `url` для работы с physical metadata

Может быть расширен physical metadata для восстановления library-файла, но остается отдельной моделью от
`FileEntry`

### ResourceEntry

Общая resource-level модель physical metadata ресурса из `Manifest`

Содержит `path`, `sha256`, `size` и `url`

Не содержит статуса проверки, плана загрузки или lifecycle-решений

### ManifestResources

Компонент доменной модели, который строит список `ResourceEntry` из `Manifest.files` и
`Manifest.libraries`

Сохраняет семантику исходных моделей: `FileEntry` и `LibraryEntry` продолжают использоваться в своих
runtime-сценариях

---

## Runtime

### JavaExecutableReference

Модель смысловой ссылки на Java executable

Разделяет два сценария

- command name
- explicit filesystem path

`ManifestJavaRuntimeSelector` делегирует интерпретацию `LaunchInfo.javaExecutable` в 
`ManifestJavaExecutableReferenceResolver`

`JavaExecutableReadinessChecker` получает `JavaExecutableReference`, а не raw `Path`

`GameLaunchCommandBuilder` использует `JavaExecutableReference.value()` как первый элемент launch command

### JavaExecutableReferenceResolver

Контракт преобразования raw Java executable metadata в `JavaExecutableReference`

`ManifestJavaExecutableReferenceResolver` интерпретирует manifest-provided `javaExecutable` перед
созданием `JavaExecutableReference`

Значение без path separator считается command name

Значение с path separator считается explicit filesystem path

Resolver не выполняет PATH resolution, не проверяет существование файла и не выбирает Java version

### JavaCommandPathResolver

Контракт преобразования `JavaExecutableReference` типа command name в `JavaExecutableReference` типа explicit
filesystem path

Resolver выполняет PATH-oriented lookup отдельно от manifest mapping, runtime selection, command building и process
launch

`DefaultJavaCommandPathResolver` использует `JavaCommandPathEnvironment`, который содержит директории и executable
extensions

На текущем этапе `GameLaunchPlanBuilder` уже вызывает `JavaCommandPathResolver` перед readiness check

Application assembly использует `DefaultJavaCommandPathResolver`, поэтому command name из manifest metadata разрешается
в explicit filesystem path до readiness check

### JavaCommandPathEnvironmentProvider

Контракт получения `JavaCommandPathEnvironment` из runtime окружения

`SystemJavaCommandPathEnvironmentProvider` читает `PATH` и `PATHEXT`, преобразует директории поиска и executable
extensions в модель, которую использует `DefaultJavaCommandPathResolver`

Application assembly использует `SystemJavaCommandPathEnvironmentProvider` для построения production
`JavaCommandPathEnvironment`

Некоторые entries из `PATH`, которые нельзя преобразовать в `Path`, игнорируются provider-ом и не должен ломать
application assembly

### JavaExecutableReadinessChecker

Контракт проверки готовности выбранного Java executable перед построением `GameLaunchPlan`

На текущем этапе `GameLaunchPlanBuilder` вызывает checker после `JavaCommandPathResolver` и
до `GameLaunchCommandBuilder`

`DefaultJavaExecutableReadinessChecker` проверяет существование файла и то, что путь указывает на regular file

`DefaultJavaExecutableReadinessChecker` используется в application assembly после PATH resolution и проверяет
уже resolved explicit filesystem path

`NoOpJavaExecutableReadinessChecker` остается полезным для тестов и изолированных сценариев, где filesystem readiness
не является предметом проверки

### JavaRuntimeSelector

Контракт выбора Java executable для построения `GameLaunchPlan`

На текущем этапе минимальная реализация `ManifestJavaRuntimeSelector` использует `JavaExecutableReferenceResolver` для
преобразования `LaunchInfo.javaExecutable` в `JavaExecutableReference`

Selector не проверяет существование Java executable и не ищет Java installations

### RuntimeLibrarySelection

Модель результата runtime library selection

Содержит отдельно обычные selected libraries и selected native artifacts

Используется после загрузки manifest для сохранения результата selection в `LaunchContext`
и построения `NativeExtractionPlan`

### RuntimeLibraryMetadata

Промежуточная модель library metadata, полученная из manifest JSON до выбора runtime-compatible `LibraryEntry`

На текущем этапе содержит основной `LibraryArtifactMetadata`, список `LibraryRule`, `LibraryClassifiersMetadata`,
`LibraryNativesMetadata` и `NativeExtractionRules`

Описывает metadata library до выбора итогового runtime-compatible `LibraryEntry`

### LibraryArtifactMetadata

Модель downloadable artifact metadata для library

Содержит `path`, `sha256`, `size` и `url`

Используется `RuntimeLibrarySelector` для формирования выбранного `LibraryEntry`

### LibraryClassifiersMetadata

Модель metadata, описывающая classifier artifacts library до выбора runtime-compatible `LibraryEntry`

Содержит mapping между `classifierName` и `LibraryArtifactMetadata`

### LibraryNativesMetadata

Модель metadata, описывающая соответствие `OperatingSystem` и имени classifier для native artifact

Используется при выборе native artifact для текущей `OperatingSystem`

### SelectedNativeArtifact

Модель выбранного native artifact после runtime library selection

Содержит `LibraryEntry` и `NativeExtractionRules`

Используется как входная модель для native extraction flow

### NativeExtractionRules

Правила распаковки selected native artifact

На текущем этапе содержит список archive entries или префиксов, которые должны быть исключены при распаковке

### LibraryRule

Правило выбора library для конкретной операционной системы

Содержит действие выбора и `OperatingSystem`

### LibraryRuleAction

Действие правила выбора library

На текущем этапе содержит значения `ALLOW` и `DISALLOW`

### RuntimeLibrarySelector

Компонент, формирующий `RuntimeLibrarySelection` из `RuntimeLibraryMetadata` для заданного `RuntimeEnvironment`

На текущем этапе учитывает минимальные OS-specific library rules

Если rules отсутствуют, library считается доступной для любого runtime environment

Если rules есть, selector выбирает library на основе последней rule, совпадающей с текущей `OperatingSystem`

### OperatingSystem

Доменное перечисление поддерживаемых операционных систем для runtime selection

На текущем этапе содержит минимальный набор значений, необходимый для OS-specific library и native artifact selection

### RuntimeEnvironment

Доменная модель runtime environment, для которого выполняется выбор runtime-compatible libraries

На текущем этапе содержит `OperatingSystem`

Не определяет текущую OS самостоятельно и не зависит от системных API

### RuntimeEnvironmentProvider

Компонент, предоставляющий текущий `RuntimeEnvironment`

Не выбирает libraries самостоятельно, а только предоставляет окружение runtime selection

### SystemRuntimeEnvironmentProvider

Реализация `RuntimeEnvironmentProvider`, определяющая `OperatingSystem` на основе системного
свойства `os.name`

Используется composition root для передачи текущего runtime environment в
manifest mapping и runtime library selection

---

## HTTP

### LauncherHttpClient

Контракт HTTP-клиента внутри `launcher-api`

Используется адаптерами API для получения внешних данных

### JavaLauncherHttpClient

Реализация `LauncherHttpClient` на основе стандартного Java HTTP Client

Выполняет HTTP GET и возвращает тело ответа

### HttpManifestClient

Адаптер загрузки Manifest JSON по `manifestUri`

Не преобразует JSON в доменную модель самостоятельно

---

## Операции

### LaunchOperation

Минимальная завершенная операция `Launcher`, имеющая собственный жизненный цикл, `LaunchContext`
и набор `LauncherTask`

### LauncherTask

Минимальная единица работы внутри `LaunchOperation`

### LaunchContext

Контекст выполнения одной `LaunchOperation`

Содержит исключительно данные, необходимые данной операции

Создается отдельно для каждой новой Operation

### OperationManager

Компонент, управляющий жизненным циклом `LaunchOperation`

Не создает операции

Не содержит бизнес-логики операций

---

## Аутентификация

### Session

Неизменяемый объект, описывающий активную пользовательскую сессию

### SessionHandle

Временный объект владения `Session`

Представляет безопасный доступ к `Session` во время выполнения `LauncherTask`

### AuthenticationProvider

Компонент, создающий новую `Session`

Не отвечает за ее хранение

---

## Выполнение

### Execution Strategy

Компонент, определяющий способ выполнения набора `LauncherTask`

### Sequential Execution

Последовательное выполнение `LauncherTask`

### Parallel Execution

Одновременное выполнение независимых `LauncherTask`

### Independent Task

`LauncherTask`, выполнение которой не зависит от результата другой `LauncherTask`

### Operation Completion

Момент, когда `LaunchOperation` достигла согласованного архитектурного состояния, включающего
завершения `LauncherTask`, построение результата, публикацию событий и изменение состояния
`Launcher`

---

## Планирование

### GameLaunchPlan

Описание входных данных для запуска игры

Не запускает игровой процесс самостоятельно

Создается перед `LAUNCH_GAME` и передается в `GameService`

Содержит путь к `gameDirectory` и список `command`

### GameClasspath

Неизменяемая модель classpath для запуска игры

Содержит список локальных путей, которые должны попасть в classpath команды запуска

### GameClasspathBuilder

Компонент, строящий `GameClasspath` из `RuntimeLibrarySelection.libraries`

Если `RuntimeLibrarySelection.libraries` пустой, использует `launchInfo.classpath` как fallback для минимальных
сценариев

`Manifest.libraries` больше не принимает участия в построении classpath

При построении локальных classpath paths использует `ResourcePathResolver`, чтобы не дублировать правила
безопасного разрешения manifest paths

### ResourcePathResolver

Общий компонент безопасного разрешения пути `Manifest` относительно базовой директории

Используется verification, download и classpath building, когда путь `Manifest` нужно преобразовать
в локальный `Path`

### ClasspathFormatter

Компонент, преобразующий `GameClasspath` в строку classpath для команды запуска

Использует системный разделитель путей

### GameProcessLauncher

Адаптер запуска игрового процесса на основе `GameLaunchPlan`

Не строит команду запуска самостоятельно, а исполняет уже подготовленный план

### DownloadPlan

Описание неизменяемого порядка загрузки ресурсов

Не выполняет загрузку самостоятельно

### VerificationPlan

Результат проверки локальных ресурсов относительно `Manifest`

Содержит неизменяемый результат проверки ресурсов

Используется `LauncherEngine` для принятия решения о следующем шаге

- Перейти в `RUNNING`, если все ресурсы корректны
- Построить `DownloadPlan`, если обнаружены ресурсы, требующие восстановления
- Завершить запуск ошибкой, если результат проверки не может быть использован

### VerificationReport

Исторический термин ранней архитектуры

В текущей реализации вместо него используется `VerificationPlan`

---

## Документация

### ADR (Architecture Decision Record)

Документирует принятое архитектурное решение

### RFC (Request For Comments)

Документирует архитектурное правило или соглашение проекта

---

## Конкретная операция

Конкретная реализация `LaunchOperation`, содержит только зависимости, необходимые для
выполнения собственного сценария. Общая инфраструктура жизненного цикла наследуется от
`LaunchOperation`

---

## Событие

Свершившийся факт, произошедший в системе, который может представлять интерес для других
компонентов, но не требует знания о конкретных получателях

---

## Правило эволюции архитектуры

Каждый новый уровень архитектуры сначала должен использовать уже существующие модели

Новая модель вводится только тогда, когда существующая перестает выражать необходимое поведение