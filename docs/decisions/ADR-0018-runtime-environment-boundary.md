# ADR-0018: Определить границу runtime environment

## Статус

Accepted

## Контекст

Для дальнейшего развития runtime library selection launcher должен учитывать окружение, под которое
выбираются runtime-compatible libraries

В будущих итерациях это потребуется для поддержки

- OS-specific rules
- Classifiers
- Natives
- Выбора platform-specific артефактов
- Правил распаковки natives

На текущем этапе `RuntimeLibrarySelector` выполняет минимальное преобразование `RuntimeLibraryMetadata`
в выбранный `LibraryEntry`

Следующий уровень выбора не должен зависеть от системных API, текущей JVM или деталей запуска приложения

Если `RuntimeLibrarySelector` будет самостоятельно определять текущую операционную систему, он смешает
две ответственности

- Определение runtime environment
- Выбор подходящих library artifacts

---

## Решение

Ввести отдельную доменную модель runtime environment

Runtime environment описывает окружение, для которого выполняется выбор runtime-compatible libraries

На текущем этапе минимальная модель должна содержать операционную систему

```text
RuntimeEnvironment
    -> OperatingSystem
```

Модель `RuntimeEnvironment` должна быть независима от способа определения текущего окружения

`RuntimeLibrarySelector` должен получать runtime environment как входные данные, а не определять его
самостоятельно

```text
RuntimeEnvironmentProvider
    -> RuntimeEnvironment
        -> RuntimeLibrarySelector
            -> LibraryEntry
```

Определение текущего окружения должно быть вынесено в отдельный компонент

Этот компонент может использовать системные свойства JVM runtime или configuration, но результатом
его работы должна быть доменная модель `RuntimeEnvironment`

---

## Последствия

`RuntimeLibrarySelector` остается детерминированным и тестируемым

Выбор library artifacts можно будет тестировать для разных OS без зависимости от текущей машины разработчика

Модель selection flow становится готовой для будущих OS-specific rules, classifiers и natives

`RuntimeLibraryMetadata` не получает ответственность за определение текущего окружения

`LibraryEntry` остается моделью уже выбранного artifact

---

## Не входит в решение

- Реализация определения текущей OS
- Реализация OS-specific rules
- Реализация classifiers
- Реализация natives
- Распаковка natives
- Изменение текущего поведения `DefaultRuntimeLibrarySelector`
- Поддержка architecture-specific выбора, например `x86_64` или `aarch64`

Architecture/CPU может быть добавлена позже, когда появится подтвержденный сценарий

---

## Связанные решения

- [ADR-0015: Зафиксировать минимальный scope library metadata](ADR-0015-minimal-library-metadata-scope.md)
- [ADR-0017: Определить границу runtime metadata для libraries](ADR-0017-library-runtime-metadata-boundary.md)
