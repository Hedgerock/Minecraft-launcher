# Login Sequence

## Цель

Описать последовательность действий при успешной аутентификации пользователя и 
создании новой пользовательской Session

## Предусловия

- Launcher находится в состоянии WAITING_FOR_AUTHENTICATION
- Пользователь выбрал профиль
- AuthenticationProvider доступен

## Последовательность

User
 |
 |
 ▼
Launcher UI
 |
 | create(AuthenticationRequest)
 ▼
AuthenticationProvider
 |
 | authenticate(request)
 ▼
Session
 |
 | create()
 ▼
SessionManager
 |
 | open(session)
 ▼
LauncherStateMachine
 |
 | transition(AUTHENTICATED)
 ▼
ProfileSelection

## Компоненты

- Launcher UI
- AuthenticationProvider
- SessionManager
- Session
- LauncherStateMachine

## Результат

- Session успешно создана
- SessionManager становится владельцем Session
- Launcher переходит к выбору игрового профиля или сервера

## Альтернативные сценарии

### Неверные учетные данные

AuthenticationProvider
|
▼
AuthenticationFailed
|
▼
LauncherStateMachine
|
▼
WAITING_FOR_AUTHENTICATION

### Пользователь отменил вход

Authentication cancelled
|
▼
Launcher остается

WAITING_FOR_AUTHENTICATION

## Exceptional Scenarios

## Network unavailable

AuthenticationProver
|
▼
HttpClient
|
▼
NetworkUnavailable (Infrastructure Event)
|
▼
Result.failure(NetworkUnavailable)
|
▼
LauncherStateMachine
|
▼
WAITIN_FOR_AUTHENTICATION

## Инварианты

L-1
Session создается только AuthenticationProver

L-2
SessionManager становится единственным владельцем Session

L-3
LauncherStateMachine изменяет состояние только после успешного открытия Session

L-4
При ошибке существующая Session не изменяется













