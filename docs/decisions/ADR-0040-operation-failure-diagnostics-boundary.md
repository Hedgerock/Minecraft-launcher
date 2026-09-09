[← Назад к списку решений](README.md)

# ADR-0040: Определить границу operation failure diagnostics

## Статус

Accepted

---

## Контекст

После реализации Java runtime failure model ошибки Java runtime flow получили явную классификацию причин

Java runtime boundary теперь может различать

- Некорректное raw Java executable value
- Невозможность разрешить command name
- Некорректный explicit filesystem path
- Отсутствующий Java executable
- Java executable, который не является regular file

На момент принятия решения `LaunchOperation` преобразует exception в `OperationResult.failure(...)` через текстовое
сообщение

Такой подход сохраняет простой operation lifecycle, но не передает наверх structured failure context

Если напрямую добавить `JavaRuntimeFailureReason` в `OperationResult`, operation layer начнет зависеть от Java
runtime-specific модели ошибок

Это нарушит границу между общим operation lifecycle и конкретным runtime flow

Перед развитием UI diagnostics, recovery behavior или fallback policy нужно определить, как structured failure
information может подниматься выше domain-specific boundary

---

## Решение

`OperationResult` не должен зависеть от Java runtime-specific failure reason

`JavaRuntimeFailureReason` остается моделью Java runtime boundary

`LaunchOperation` может преобразовывать exception в readable failure message, сохраняя текущий operation lifecycle

Если structured diagnostics потребуется на уровне operation lifecycle, она должна появиться через generic operation
failure context

Например

```text
OperationFailure
    -> failure code
    -> readable message
    -> optional source
    -> optional details
```

Generic operation failure context может ссылаться на источник ошибки, но не должен напрямую встраивать domain-specific
enum конкретного flow

Граница выглядит так

```text
Java runtime boundary
    -> JavaRuntimeFailureException(...)
        -> LaunchOperation
            -> OperationResult.failure(...)

Future operation diagnostics
    -> OperationFailure
        -> generic failure code
        -> source-specific details
```

На момент принятия решения текущего `OperationResult.failure(...)` достаточно для lifecycle behavior

Расширение `OperationResult` должно быть отдельной итерацией после появления подтвержденного сценария

---

## Последствия

Operation lifecycle остается общим и не становится Java-runtime-specific

Java runtime failure model сохраняет свою пользу внутри runtime boundary

Будущий UI сможет получить structured diagnostics через generic failure context, а не через привязку к
Java runtime

Recovery behavior и fallback policy не появляются преждевременно

Текущие operation tests и event flow могут продолжать проверять readable failure message

Появляется ясное правило для будущих error model изменений

---

## Не входит в решение

- Немедленное изменение `OperationResult`
- Добавление `OperationFailure`
- Добавление generic `FailureCode`
- UI отображение ошибок
- Recovery behavior
- Fallback policy
- Retry policy
- Изменение operation lifecycle
- Изменение event model
- Проброс `JavaRuntimeFailureReason` в operation layer

---

## Связанные решения

- [ADR-0011: Failure Policy Extraction](ADR-0011-failure-policy-extraction.md)
- [ADR-0012: LauncherEngine uses OperationManager](ADR-0012-launcher-engine-uses-operation-manager.md)
- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0039: Определить модель ошибок Java runtime](ADR-0039-java-runtime-failure-model.md)
