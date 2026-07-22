# Цель

Добавить независимую подсистему авторизации

# Требования

- Microsoft OAuth
- Launcher Account
- Offline Mode
- Возможность расширения

# Ограничения

- Core не знает про Http
- Core не знает про JWT
- UI не знает реализации авторизации

# Предлагаемая Архитектура

User
AuthenticationRequest
AuthenticationProvider
AuthenticationRegistry
AuthenticationService