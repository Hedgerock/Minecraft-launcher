[← Назад к списку решений](README.md)

# ADR-0039: Определить модель ошибок Java runtime

## Статус

Accepted

---

## Контекст

После `v0.5.0-java-runtime-foundation` launcher получил явный Java executable runtime flow

Java executable проходит через несколько границ

```text
JavaRuntimeSelector
    -> JavaExecutableReferenceResolver
    -> JavaCommandPathResolver
    -> JavaExecutableReadinessChecker
    -> GameLaunchCommandBuilder
```

После добавления configured Java override executable может приходить не только из manifest metadata, но
и из launcher configuration

Это усиливает требования к ошибкам Java runtime flow

На момент принятия решения ошибки Java runtime представлены отдельными runtime exceptions и текстовыми сообщениями

Такой подход достаточен для минимального lifecycle, но становится слабым для будущего UI, diagnostics и fallback policy

Launcher должен различать причины ошибок Java runtime, не смешивая их с process launch, manifest mapping или Java
installation discovery

---

## Решение

Ошибки Java runtime flow должны получить явную классификацию причин

К Java runtime failure относятся ошибки, возникающие до построения или выполнения game process command

- Некорректное raw Java executable value
- Невозможность разрешить command name через PATH-oriented lookup
- Некорректный explicit filesystem path
- Отсутствующий Java executable
- Java executable, который не является regular file

Причина ошибки должна быть выражена отдельной моделью, а не только текстом exception message

Минимальная модель должна описывать причину failure, но не должна принимать recovery-решения

Java runtime failure model не должна запускать fallback policy и не должна выбирать другую Java runtime

Граница выглядит так

```text
Java runtime boundary
    -> detects failure reason
        -> throws classified runtime exception
            -> operation failure
```

`JavaCommandPathResolver` может использовать Java runtime failure model для ошибок PATH resolution

`JavaExecutableReadinessChecker` может использовать Java runtime failure model для ошибок readiness check

`LaunchOperation` продолжает преобразовывать exception в operation failure

---

## Последствия

Java runtime errors становятся понятнее для будущего UI и diagnostics

Configured Java override получает более предсказуемое failure behavior

Manifest-provided Java executable и configured override проходят через одинаковую модель ошибок

Operation lifecycle не меняется: ошибка Java runtime по-прежнему завершает operation failure

Появляется основа для будущего Java runtime diagnostics без добавления Java installation discovery

Fallback policy остается отдельным будущим решением

---

## Не входит в решение

- Java installation discovery
- Java version compatibility
- Автоматический fallback на другую Java runtime
- UI отображение ошибок
- Process launch diagnostics
- Хранение пользовательских настроек
- Изменение manifest JSON contract
- Изменение `GameService`
- Изменение operation lifecycle

---

## Связанные решения

- [ADR-0029: Определить границу выбора Java runtime](ADR-0029-java-runtime-selection-boundary.md)
- [ADR-0030: Определить границу проверки Java executable](ADR-0030-java-executable-readiness-boundary.md)
- [ADR-0031: Определить границу Java executable reference](ADR-0031-java-executable-reference-boundary.md)
- [ADR-0032: Определить границу PATH resolution для Java command name](ADR-0032-java-command-path-resolution-boundary.md)
- [ADR-0035: Определить границу реализации Java executable readiness checker](ADR-0035-java-executable-readiness-adapter-boundary.md)
- [ADR-0036: Определить границу Java command path resolver](ADR-0036-java-command-path-resolver-boundary.md)
- [ADR-0038: Определить границу configured Java override](ADR-0038-configured-java-override-boundary.md)
