# Authentication Model

## Цель

Определить архитектурную модель аутентификации пользователя,
границы ответственности компонентов и жизненный цикл пользовательской сессии

Документ не описывает тели реализации конкретного протокола авторизации (JWT, OAuth, Basic etc)

Он определяет архитектурніе роли компонентов

## Основные компоненты

- Profile - кто такой пользователь
- AuthenticationProvider - кто умеет подтвердить личность пользователя
- Session - контекст успешной авторизации
- SessionManager - владелец жизненного цикла Session
- SessionHandle - временное право пользователя Session
- SessionToken - подтверждение действительности Session

## Архитектурные отношения

 Profile
    |
    ▼
 AuthenticationProvider
    |
    ▼
  Session
    |
    ▼
 SessionManager
    |
    ▼
 SessionHandle

## Инварианты

A-1

Session является неизменяемым объектом

A-2

SessionManager - единственный владелец Session

A-3

SessionHandle никогда не продлевает жизнь Session

A-4

AUthenticationProvider не хранит Session

A-5

Logout никогда не прерывает уже выполняющиеся операции принудительно

## Границы ответственности

| Компонент              | Отвечает                                  | Не отвечает                 |
|------------------------|-------------------------------------------|-----------------------------|
| AuthenticationProvider | Проверка учетных данных, создание Session | Хранение Session            |
| SessionManager         | Жизненный цикл Session                    | Проверка пароля, HTTP       |
| Session                | Контекст авторизации                      | Бизнес-логика               |
| SessionHandle          | Временный доствуп к Session               | Управление жизненным циклом |
| SessionToken           | Проверка срока действия                   | Обновление токена           |

## Authentication Boundary

AuthenticationProvider является единственной точкой, через которую система получает новую Session

Остальная часть приложения не зависит от конкретного механизма аутентификации

## Архитектурные принципы

1. Authentication является единственной подсистемой, которая создает Session
2. Session никогда самостоятельно не управляет собственным жизненным циклом
3. Execution Layer получает Session исключительно через SessionHandle
4. Authentication расширяется только посредством новых AuthenticationProvider

## Design Rationale

- Session является immutable для упрощения многопоточности
- SessionManager является единственным владельцем Session
- AuthenticationProvider изолирует механизм авторизации
- SessionHandle предотвращает прямую зависимость Execution Layer от SessionManager
