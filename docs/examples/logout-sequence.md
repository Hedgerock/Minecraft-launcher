# Logout Sequence

## Цель

Описать последовательность корректного завершения пользовательской Session и безопасного
освобождения ресурсов, принадлежащих текущему пользователю

## Предусловия

- Launcher находится в состоянии AUTHENTICATED или RUNNING
- Существует активная Session
- SessionManager является владельцем текущей Session

## Последовательность

User
|
| Logout
▼
Launcher UI
|
▼
OperationManager
|
| create()
▼
LogoutOperation
|
|
| Stop accepting new SessionHandle
▼
LauncherTask
|
| close(Session)
▼
LauncherStateMachine
|
| transition(WAITING_FOR_AUTHENTICATION)
▼
Launcher UI
|
▼
Login Screen

## Компоненты

- Launcher UI
- OperationManager
- LogoutOperation
- LauncherTask
- SessionManager
- LauncherStateMachine

## Результат

- Все новые операции не могут получить Session
- Уже выполняющиеся операции завершаются согласно RFC-0003
- Session закрыта
- Launcher возвращается в состояние WAITING_FOR_AUTHENTICATION

## Functional Alternatives

### Пользователь отменил Logout

Logout cancelled
|
▼
Launcher продолжает работу
|
▼
Session остается активной

## Exceptional Scenarios

Ошибки инфраструктуры описываются непосредственно в данном документе

В будущем общие инфраструктурные сценарии могут быть вынесены в отдельный документ согласно
Documentation Guidelines

## Инварианты

LO-1
После начала Logout новые SessionHandle не выдаются

LO-2
SessionManager остается единственным владельцем Session до момента закрытия

LO-3
Уже полученные SessionHandle завершают работу самостоятельно

LO-4
LauncherStateMachine изменяет состояние только после завершения LogoutOperation

LO-5
После Logout предыдущая Session никогда не используется повторно

## Связанные документы

- RFC-0002 Session Lifecycle Management
- RFC-0003 Task Lifecycle
- authentication-model.md
- launcher-lifecycle.md























