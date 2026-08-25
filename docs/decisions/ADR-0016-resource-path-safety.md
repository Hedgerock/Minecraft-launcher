# ADR-0016: Зафиксировать правила безопасности resource path

## Статус

Accepted

---

## Контекст

`ResourceEntry.path` приходит из manifest metadata и используется в runtime flow для построения
локального пути ресурса

На текущем этапе один и тот же `ResourceEntry` участвует в нескольких сценариях

- verification flow
- download flow
- построение `VerificationPlan`
- построение `DownloadPlan`

Verification и download используют `ResourceEntry.path` для построения локального пути относительно
`game directory`

Если `ResourceEntry.path` содержит небезопасное значение, launcher может попытаться обратиться к файлу
за пределами `game directory`

### Примеры небезопасных значений

- Абсолютный путь
- Path traversal через `..`
- Пустой путь
- Путь, который после normalize выходит за пределы `game directory`

---

## Решение

`ResourceEntry.path` должен описывать только относительный путь ресурса внутри `game directory`

Запрещены:

- Absolute paths
- Path traversal через `..`
- Пустые и blank paths
- Пути, которые после normalize выходят за пределы `game directory`

Проверка blank path остается частью `ResourceEntry`

Проверка выхода за пределы `game directory` должна выполняться в момент построения локального
пути ресурса, потому что для нее нужен base directory

Для этого должен быть выделен отдельный механизм safe path resolution

Verification/download flow не должны напрямую выполнять

```java
gameDirectory.resolve(resource.path())
```

Вместо этого они должны использовать общий resolver, который

- Принимает `gameDirectory`
- Принимает `ResourceEntry.path`
- Строит нормализованный локальный путь
- Проверяет, что результат остается внутри `gameDirectory`
- Возвращает безопасный `Path` или завершает операцию ошибкой

---

## Последствия

Защита от path traversal не смешивается с download или verification алгоритмами

ResourceEntry остается model-level объектом и не зависит от filesystem base path

Verification и download получают единый способ построения локального пути

Ошибки небезопасного path должны быть явно представлены в runtime flow

В будущем тот же resolver можно будет использовать для asset, libraries, files и других
recoverable resources

---

## Не входит в решение

- Sandbox на уровне ОС
- Проверка существования файла
- Проверка прав доступа
- Canonical path resolution через real path
- Символические ссылки
- Нормализация URL
- Maven coordinate validation