# Глоссарий проекта

## Лаунчер

### LauncherEngine

Главный координатор жизненного цикла `Launcher`

Запускает `LaunchOperation`, управляет переходами `LauncherStateMachine` и преобразует operation-level или lifecycle-level
ошибки в `LaunchFailure`

Возвращает `LaunchResult` с финальным состоянием, флагом успешности и failure context для неуспешного launcher lifecycle

### LauncherUserPaths

Набор пользовательских путей приложения: файл конфигурации и каталог данных по умолчанию

Не создает файл или каталоги и не выбирает явно заданную launcher directory

### LauncherUserPathsResolver

Компонент `launcher-app`, определяющий `LauncherUserPaths` для Windows, Linux и macOS

Не читает manifest URI и не создает `LauncherConfiguration`

### LaunchResult

Модель результата работы launcher lifecycle flow

Содержит

- финальное состояние launcher
- флаг успешности запуска
- optional `LaunchFailure`

Успешный результат не содержит failure context

Неуспешный результат содержит `LaunchFailure`

### LaunchFailure

Generic launch-level failure context неуспешного launcher lifecycle

Если причиной является failed operation, сохраняет `OperationType` и исходный `OperationFailure`

Если ошибка возникает на уровне координации launcher lifecycle, содержит readable message без искусственного `OperationResult`
или `OperationFailedEvent`

Не принимает presentation, retry или recovery decisions

### LauncherResultHandler

Application boundary contract для обработки `LaunchResult`

Не принимает presentation decisions внутри `launcher-core`

Минимальный CLI entrypoint использует no-op реализацию

Для завершения presentation launch request используется отдельный `PresentationLaunchCompletionHandler`

### LaunchRequestResult

Немедленный результат приема launch request через `PresentationLaunchBoundary`

Показывает, был ли запрос принят или отклонен из-за уже выполняющегося launcher lifecycle

Не описывает итог выполнения launcher lifecycle и не заменяет `LaunchResult`

### PresentationLaunchCompletion

Application-level модель терминального исхода принятого launch request

Содержит полученный `LaunchResult` либо фиксирует неожиданный сбой выполнения до его получения

Не создает искусственный `LauncherState` и не заменяет `LaunchResult`

### PresentationLaunchCompletionHandler

Application boundary contract для передачи `PresentationLaunchCompletion` внешнему presentation adapter

### PresentationLaunchPhase

Application-level модель текущего этапа принятого launch request

Формируется из переходов launcher lifecycle и не заменяет `PresentationLaunchState` или терминальный исход запроса

### PresentationLaunchPhaseHandler

Application boundary contract для передачи текущего этапа внешнему presentation adapter

Синхронный сбой обработчика диагностируется, но не изменяет launcher lifecycle и `PresentationLaunchCompletion`

### PresentationLaunchBoundary

Application boundary для приема запроса запуска из presentation layer

Выполняет синхронный launcher lifecycle за пределами presentation thread и допускает не более одного активного launch request

Возвращает `LaunchRequestResult` немедленно, а терминальный исход принятого запроса передает как `PresentationLaunchCompletion`
через `PresentationLaunchCompletionHandler`

Для принятого запроса передает текущие этапы через `PresentationLaunchPhaseHandler` до терминального исхода

Не управляет UI controls и не изменяет внутреннюю execution model `LauncherEngine`

### PresentationLaunchDiagnosticReporter

Application-level контракт локальной диагностики неожиданных сбоев принятого presentation launch request

Получает источник сбоя и исходное исключение

Его отказ не изменяет исход запроса

Production adapter вводит контролируемую запись с источником сбоя и типом исключения без исходного сообщения

Различает неожиданный сбой выполнения launcher lifecycle до получения `LaunchResult`, сбой синхронного вызова
`PresentationLaunchCompletion` и сбой синхронного вызова `PresentationLaunchPhaseHandler`

### PresentationLaunchDiagnosticSource

Указывает источник неожиданного сбоя: выполнения launcher lifecycle до получения `LaunchResult` или синхронный вызов
`PresentationLaunchCompletionHandler`, синхронный вызов `PresentationLaunchPhaseHandler` или синхронный вызов
`PresentationLaunchPhaseHandler`

Не классифицирует обычный неуспешный `LaunchResult`

### PresentationLaunchState

Минимальная модель состояния launch interaction в presentation layer

Содержит `READY`, `LAUNCHING`, `LAUNCHED` и `FAILED`

`PresentationLaunchStateMachine` управляет переходами после `LaunchRequestResult` и `PresentationLaunchCompletion` и определяет
доступность launch action

Модель не зависит от JavaFX controls и не копирует внутренние состояния `LauncherStateMachine`

### PresentationLaunchFailure

Минимальная модель безопасного пользовательского сообщения о неуспешном запуске

Хранится в presentation state только при `FAILED` и очищается после принятия нового launch request

### PresentationLaunchFailureMapper

Преобразует `LaunchFailure` в `PresentationLaunchFailure` внутри `launcher-ui`

Выбирает сообщение по `OperationType` или возвращает общее сообщение для lifecycle-level failure

Не использует исходные technical messages и details при формировании пользовательского сообщения

Для неожиданного сбоя выполнения без `LaunchResult` возвращает общее безопасное сообщение

### JavaFxPresentationLaunchCompletionHandler

JavaFX adapter контракта `PresentationLaunchCompletionHandler`

Переносит обработку `PresentationLaunchCompletion` в JavaFX Application Thread

Не определяет presentation state или user-facing message

### JavaFxPresentationLaunchPhaseHandler

JavaFX adapter контракта `PresentationLaunchPhaseHandler`

Переносит обработку `PresentationLaunchPhase` в JavaFX Application Thread

Пользовательский текст этапа определяется в `launcher-ui`

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

Содержит `mainClass`, `javaExecutable`, `javaVersionRequirement`, `jvmArgs`, `gameArgs`, `authArgs` и `classpath`

Используется `GameLaunchPlanBuilder` для построения команды запуска

### Исполняемый файл Java

Первый элемент команды запуска игрового процесса

Значение выбирается через `JavaRuntimeSelector`

Configured Java override имеет приоритет над manifest-provided `LaunchInfo.javaExecutable`

Если override отсутствует, используется manifest-based Java executable selection flow

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

Компонент доменной модели, который строит список `ResourceEntry` из `Manifest.files`,
`Manifest.libraries` и `Manifest.assetsIndex`

Сохраняет семантику исходных моделей: `FileEntry`, `LibraryEntry` и `AssetEntry` продолжают использоваться в своих
runtime-сценариях

### AssetEntry

Manifest-specific модель asset resource metadata

Содержит физическую метадату asset resource

- `path`
- `sha256`
- `size`
- `url`

Не заменяет `FileEntry`, `LibraryEntry` или `ResourceEntry`

### AssetsIndex

Manifest-specific контейнер asset resources

Используется как отдельная semantic boundary для assets metadata внутри manifest model

Подключен к `ManifestResources` и участвует в verification/download flow через общий `ResourceEntry` контракт

---

## Runtime

### JavaRuntimeCompatibilityChecker

Контракт проверки совместимости detected Java runtime version с `JavaVersionRequirement`

Получает уже определенную `JavaRuntimeVersion` и требование версии Java

Не выбирает Java executable, не определяет runtime version, не выполняет Java installation discovery и не
строит команду запуска

Application assembly использует `DefaultJavaRuntimeCompatibilityChecker`, который проверяет, что detected
`JavaRuntimeVersion` удовлетворяет `JavaVersionRequirement`

Если detected Java runtime version ниже requirement, checker возвращает Java runtime failure с причиной
`INCOMPATIBLE_JAVA_VERSION`

### JavaRuntimeCompatibilityRequest

Модель входных данных для проверки Java runtime compatibility

Содержит detected `JavaRuntimeVersion` и `JavaVersionRequirement`

Не содержит `JavaExecutableReference`, потому что получение фактической версии Java runtime вынесено в
`JavaRuntimeVersionDetector`

### JavaRuntimeVersionDetector

Контракт определения фактической `JavaRuntimeVersion` для already resolved `JavaExecutableReference`

Не выбирает Java executable, не выполняет Java installation discovery и не принимает fallback policy

Application assembly использует `DefaultJavaRuntimeVersionDetector`, который определяет Java runtime version через запуск
resolved Java executable с аргументом версии и parsing process output

### JavaProcessDiagnostic

Adapter-level модель диагностики Java process lifecycle внутри Java runtime version detection flow

Описывает failure-сценарии, возникающие при запуске process для определения фактической `JavaRuntimeVersion`

Минимальные причины диагностики

- process не удалось запустить
- process завершился с non-zero exit code
- process вернул пустой output
- process вернул output, который невозможно распарсить как Java runtime version
- ожидание завершения Java process было interrupted

Не является частью `launcher-core` runtime policy и не поднимается в operation layer как Java-specific reason

Используется внутри `launcher-app` рядом с `DefaultJavaRuntimeVersionDetector`

### JavaExecutableReference

Модель смысловой ссылки на Java executable

Разделяет два сценария

- command name
- explicit filesystem path

`ManifestJavaRuntimeSelector` выбирает raw Java executable value из configured override или manifest-provided
`LaunchInfo.javaExecutable`

Выбранное значение интерпретируется через `JavaExecutableReferenceResolver`

`JavaExecutableReadinessChecker` получает `JavaExecutableReference`, а не raw `Path`

`GameLaunchCommandBuilder` использует `JavaExecutableReference.value()` как первый элемент launch command

### JavaExecutableReferenceResolver

Контракт преобразования raw Java executable metadata в `JavaExecutableReference`

`ManifestJavaExecutableReferenceResolver` интерпретирует выбранное raw Java executable value перед созданием
`JavaExecutableReference`

Выбранное значение может прийти из configured override или manifest-provided `LaunchInfo.javaExecutable`

Значение без path separator считается command name

Значение с path separator считается explicit filesystem path

Resolver не выполняет PATH resolution, не проверяет существование файла и не выбирает Java version

### JavaVersionRequirement

Модель требования к версии Java

В manifest flow требование приходит из manifest launch metadata через `LaunchInfo.javaVersionRequirement`

Содержит минимальную major version, необходимую для будущей проверки совместимости Java runtime

Не описывает Java executable, installation path, способ поиска Java или fallback policy

### JavaRuntimeVersion

Модель фактически обнаруженной версии Java runtime

Содержит major version выбранного Java executable

Не описывает Java executable, installation path, способ определения версии Java или fallback policy

### JavaCommandPathResolver

Контракт преобразования `JavaExecutableReference` типа command name в `JavaExecutableReference` типа explicit
filesystem path

Resolver выполняет PATH-oriented lookup отдельно от manifest mapping, runtime selection, command building и process
launch

`DefaultJavaCommandPathResolver` использует `JavaCommandPathEnvironment`, который содержит директории и executable
extensions

`GameLaunchPlanBuilder` вызывает `JavaCommandPathResolver` перед readiness check

Application assembly использует `DefaultJavaCommandPathResolver`, поэтому command name из manifest metadata разрешается
в explicit filesystem path до readiness check

### JavaCommandPathEnvironmentProvider

Контракт получения `JavaCommandPathEnvironment` из runtime окружения

`SystemJavaCommandPathEnvironmentProvider` читает `PATH` и `PATHEXT`, преобразует директории поиска и executable
extensions в модель, которую использует `DefaultJavaCommandPathResolver`

Application assembly использует `SystemJavaCommandPathEnvironmentProvider` для построения production
`JavaCommandPathEnvironment`

Некоторые entries из `PATH`, которые нельзя преобразовать в `Path`, игнорируются provider-ом и не должны ломать
application assembly

### JavaExecutableReadinessChecker

Контракт проверки готовности выбранного Java executable перед построением `GameLaunchPlan`

`GameLaunchPlanBuilder` вызывает checker после `JavaCommandPathResolver` и до `GameLaunchCommandBuilder`

`DefaultJavaExecutableReadinessChecker` проверяет существование файла и то, что путь указывает на regular file

`DefaultJavaExecutableReadinessChecker` используется в application assembly после PATH resolution и проверяет
уже resolved explicit filesystem path

`DefaultJavaExecutableReadinessChecker` является production implementation `JavaExecutableReadinessChecker` и находится
в `launcher-app`

Он проверяет explicit filesystem path после PATH resolution и не принадлежит `launcher-core`, потому что читает состояние
локальной файловой системы

Некорректный explicit filesystem path преобразуется в readiness failure, а не протекает наружу как platform-specific
path parsing error

`NoOpJavaExecutableReadinessChecker` остается полезным для тестов и изолированных сценариев, где filesystem readiness
не является предметом проверки

### JavaRuntimeSelector

Контракт выбора Java executable для построения `GameLaunchPlan`

`ManifestJavaRuntimeSelector` выбирает configured Java override, если он задан, иначе использует
`LaunchInfo.javaExecutable`

Selector не проверяет существование Java executable и не ищет Java installations

### JavaRuntimeSelectionRequest

Модель входных данных для выбора Java executable

Содержит manifest launch metadata и optional configured Java override

Используется `JavaRuntimeSelector`, чтобы выбрать configured override перед manifest-provided
`LaunchInfo.javaExecutable`

Не выполняет runtime selection, PATH lookup, readiness check или Java version validation

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

Контекст одного сценария запуска launcher lifecycle

Содержит конфигурацию и артефакты, которые последовательно создаются и используются операциями одного запуска

Создается один раз в `LauncherEngine.launch(...)` и передается операциям через `OperationManager`

### OperationManager

Компонент, управляющий жизненным циклом `LaunchOperation`

Не создает операции

Не содержит бизнес-логики операций

### OperationResult

Operation-level результат выполнения `LaunchOperation`

Содержит флаг успешности и optional `OperationFailure`

Readable error message является производным представлением failure context

Не описывает outcome всего launcher lifecycle и не заменяет `LaunchResult`

### OperationFailure

Generic operation failure context

Содержит readable message, generic failure code и details

Не зависит от domain-specific failure reasons, UI presentation, retry policy или recovery behavior

### OperationFailureCode

Generic category ошибки на уровне operation lifecycle

На текущем этапе содержит минимальный набор кодов и не заменяет domain-specific failure reasons

### OperationFailureMapper

Компонент, преобразующий exception на operation boundary в `OperationFailure`

Используется `LaunchOperation` для сохранения generic failure context при ошибках lifecycle hooks или execution strategy

### OperationFailedEvent

Событие неуспешного завершения `LaunchOperation`

Публикует `OperationType` и `OperationFailure`

Readable error message остается compatibility view поверх `OperationFailure`

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

### ResourceSetPlanner

Pure planning-компонент подготовки согласованного набора `ResourceEntry`

Разрешает локальные назначения через `ResourcePathResolver`, объединяет совместимые записи и отклоняет конфликтующие назначения

Не выполняет файловый или сетевой доступ

### PlannedResource

Неизменяемая пара исходной метадаты `ResourceEntry` и разрешенного локального `targetPath`

### ResourceSetPlan

Неизменяемый результат подготовки набора ресурсов

Содержит выбранные ресурсы в порядке первого появления уникальных локальных назначений

Не заменяет `VerificationPlan` или `DownloadPlan`

### ResourceSetConflictException

Ошибка подготовки ресурсов с одинаковым локальным назначением и различающимися `sha256`, `size` или `url`

Содержит локальное назначение, первую запись и конфликтующую запись

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

В verification/download flow `ResourcePathResolver` используется через `ResourceSetPlanner`

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
