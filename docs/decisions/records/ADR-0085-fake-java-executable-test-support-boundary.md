[← Назад к списку решений](../README.md)

# ADR-0085: Определить границу подготовки общего тестового Java executable

## Статус

Accepted

> Примечание: решение реализовано в итерации
> `refactor(test): share fake java executable fixture`

---

## Контекст

На момент принятия решения launcher lifecycle integration tests в launcher-app и UI smoke fixture в launcher-ui повторяют
подготовку fake Java executable

Тестовый executable возвращает контролируемую версию Java по аргументу `-version` и создает marker-файл при обычном запуске

Перед созданием marker процесс выходит из игровой директории, чтобы не удерживать ее при cleanup на Windows

Подготовка файла и обработка различий ОС повторяются, хотя проверяемые сценарии launcher lifecycle и UI различаются

Общая поддержка architecture checks уже выделена согласно [ADR-0083](ADR-0083-architecture-test-support-boundary.md), но имеет
другую ответственность

Необходимо определить владельца общей process fixture без изменения production-зависимостей и без объединения тестовых
сценариев потребителей

---

## Решение

Общая подготовка fake Java executable принадлежит инструментальному модулю `launcher-process-test-support`

Модуль располагается в `tools/process-test-support` и не зависит от production-модулей проекта

Потребители подключают его только через `testImplementation`

Включение инструментального модуля в runtime classpath приложения не допускается

Первым общим компонентом является `FakeJavaExecutableFixture`

Он получает директорию подготовки и путь marker-файла, выбранные потребителем, и возвращает путь подготовленного executable

Для Windows формируется командный файл, для Unix-подобных ОС — shell script с необходимым разрешением на выполнение

Ошибка подготовки не должна приводить к возврату пути непригодного executable

Аргумент `-version` сохраняет контролируемый вывод версии в stderr и не создает marker-файл

Обычный запуск создает marker-файл и завершается с успешным кодом

Выход процесса из игровой директории до создания marker сохраняется

Fixture подготавливает файл, но не запускает процесс и не управляет его ожиданием или завершением

Создание манифеста, HTTP server lifecycle, UI synchronization, ожидание marker и cleanup временных файлов остаются
у потребителей

Существующие launcher lifecycle integration tests и UI smoke scenario сохраняют наблюдаемое поведение

---

## Рассмотренные варианты

### Использовать test fixtures launcher-app

Вариант отклонён

Зависимость launcher-ui от launcher-app допустима, поэтому вариант не нарушает направление production-зависимостей

Для данного сценария выбран независимый инструментальный модуль: подготовка process fixture не является ответственностью
application assembly

### Расширить launcher-architecture-test-support

Вариант отклонён

Подготовка исполняемого файла и различия ОС не относятся к разбору Java source или проверке модульных границ

Объединение этих ответственностей расширило бы назначение существующего инструментального модуля

---

## Последствия

Подготовка общего executable получает одно место реализации и самостоятельное regression coverage

Исправления платформенной подготовки не требуют повторения в launcher-app и launcher-ui

Потребители сохраняют собственные сценарии и управление внешними ресурсами

В Gradle build появляется дополнительный инструментальный модуль и публичный test API, который необходимо сопровождать

Исполнение fixture сохраняет зависимость от средств запуска команд ОС и требует подтверждения на Windows и Linux

Общая fixture не подтверждает запуск реального Minecraft, работу установленного JDK или готовность игрового процесса

Изменения поддержки требуют проверки существующих launcher lifecycle и UI smoke scenarios

---

## Не входит в решение

- Изменение production code, launcher lifecycle или assembly
- Перенос HTTP fixtures и UI synchronization
- Объединение всех process doubles и специализированных version-only scripts
- Создание универсального process runner или test framework
- Настраиваемая модель произвольных команд, версий и exit codes
- Изменение назначения launcher-common
- Расширение architecture test support
- Подготовка реального Java runtime или Minecraft

---

## Связанные решения

- [ADR-0037: Определить политику reserved modules](ADR-0037-reserved-modules-policy.md)
- [ADR-0083: Определить границу общей поддержки архитектурных тестов](ADR-0083-architecture-test-support-boundary.md)
