[← Назад к списку решений](README.md)

# ADR-0037: Определить политику reserved modules

## Статус

Accepted

---

## Контекст

В проекте существуют модули, которые были добавлены в общий Gradle build заранее

- `launcher-auth`
- `launcher-common`
- `launcher-ui`

На момент принятия решения эти модули находятся в разном состоянии готовности

`launcher-auth` подключен к build, но пока не содержит production code

`launcher-common` подключен к build, но пока не содержит production code

`launcher-ui` содержит минимальный UI skeleton, но еще не участвует в основном launcher flow

Эти модули появились не как случайные директории, а как будущие границы развития проекта

Однако если не зафиксировать их статус, они создают неоднозначность

- Непонятно, являются ли пустые модули техническим долгом
- Непонятно, можно ли добавлять туда код без отдельного решения
- Непонятно, нужно ли удалять их из Gradle build до появления реализации
- `launcher-common` может превратиться в хранилище для несвязанных utility classes

После foundation stabilization перед следующим runtime milestone нужно явно определить статус таких модулей

---

## Решение

`launcher-auth`, `launcher-common` и `launcher-ui` признаются reserved modules

Reserved module — это модуль, который подключен к build, но может временно не содержать полноценной
production реализации

Reserved module допустим, если он представляет будущую архитектурную границу или явно ограниченный shared boundary

`launcher-auth` остается reserved module для будущего authentication flow

Этот модуль связан с уже существующими authentication documents и будущими сценариями login/session/token lifecycle

`launcher-ui` остается reserved module для будущего presentation layer

Этот модуль может развиваться позже, когда launcher lifecycle, runtime, download, verification, native extraction и
game launch flow будут достаточно стабильны

`launcher-common` остается reserved module только как строго ограниченный shared primitives boundary

`launcher-common` не должен использоваться как место для удобных utility classes

Код может быть добавлен в `launcher-common`, только если выполняется хотя бы одно условие

- Компонент нужен двум или более независимым модулям
- Компонент представляет стабильный shared primitive
- Добавление компонента явно подтверждено отдельным ADR или design decision

Если таких условий нет, код должен оставаться в более конкретном модуле

Граница выглядит так

```text
launcher-auth
    -> reserved authentication boundary

launcher-ui
    -> reserved presentation boundary

launcher-common
    -> reserved shared primitives boundary
    -> no generic utility dumping
```

Reserved modules не считаются проблемой сами по себе, пока их назначение явно зафиксировано и они проходят общий
build/quality gate

---

## Последствия

Пустые или почти пустые модули перестают быть неявным техническим долгом

Причина существования `launcher-auth`, `launcher-common` и `launcher-ui` становится явной

`launcher-common` получает строгую политику использования и не должен превращаться в общий utility module

Gradle build может продолжать включать reserved modules

Будущий CI pipeline сможет проверять эти модули как часть общего проекта

Если reserved module долго не получает подтвержденного сценария или начинает мешать build, его статус нужно
будет пересмотреть

---

## Не входит в решение

- Реализация authentication flow
- Реализация UI flow
- Создание новых shared primitives
- Удаление `launcher-auth` из Gradle build
- Удаление `launcher-common` из Gradle build
- Удаление `launcher-ui` из Gradle build
- Перенос существующей логики между модулями
- Изменение Gradle build structure
- Добавление CI pipeline

---

## Связанные решения

- [ADR-0006: Определить границы аутентификации](ADR-0006-authentication-boundaries.md)
- [ADR-0013: Порты оркестрации принадлежат launcher-core](ADR-0013-core-owns-orchestration-ports.md)
- [ADR-0034: Определить классификацию границ launcher-core](ADR-0034-core-boundary-classification.md)
