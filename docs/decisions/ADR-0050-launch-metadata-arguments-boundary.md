[← Назад к списку решений](README.md)

# ADR-0050: Определить границу launch metadata arguments

## Статус

Accepted

> Примечание: решение реализовано в итерациях
> `feat(model): add auth launch argument metadata`
> `feat(api): map auth launch arguments from manifest json`
> `feat(core): include auth arguments in launch command`

---

## Контекст

На момент принятия решения launcher уже умеет строить `GameLaunchPlan` на основе manifest metadata

`LaunchInfo` содержит минимальную информацию для запуска игры

- `javaExecutable`
- `mainClass`
- `gameArgs`
- `jvmArgs`
- `javaVersionRequirement`

`GameLaunchPlanBuilder` преобразует `LaunchInfo`, runtime libraries, natives directory и Java runtime selection в итоговую
launch command

После завершения assets index flow следующим естественным направлением становится развитие launch metadata

Manifest contract должен постепенно приближаться к реальному описанию запуска, но без преждевременного введения auth flow,
profile system, account/session model или UI

Первым подтвержденным сценарием является расширение launch metadata аргументами, которые должны попасть в итоговую launch
command

---

## Решение

Launch metadata arguments должны принадлежать manifest domain model

`LaunchInfo` является source-of-truth для manifest-driven launch metadata

Manifest JSON mapping должен преобразовывать внешнее описание launch metadata в доменную модель `LaunchInfo`

`GameLaunchPlanBuilder` отвечает за преобразование launch metadata в итоговую `GameLaunchPlan.command`

Поток данных должен оставаться линейным

```text
Manifest JSON
    -> LaunchInfo
        -> GameLaunchPlanBuilder
            -> GameLaunchPlan.command
```

`LauncherEngine` не должен знать о конкретных launch metadata arguments

`LauncherEngine` продолжает запускать operation lifecycle и не получает ответственность за интерпретацию manifest arguments

Auth-related launch arguments допускаются как manifest metadata, если они являются частью launch command contract

При этом они не означают появление полноценного authentication flow

---

## Последствия

Launch metadata получает явную границу развития

Новые аргументы запуска должны добавляться через LaunchInfo, manifest JSON mapping и `GameLaunchPlanBuilder`

`GameLaunchPlanBuilder` становится основной точкой применения manifest-driven launch arguments

`LauncherEngine` остается orchestration layer и не разрастается деталями command building

Решение приближает manifest contract к более реалистичному запуску игры

Trade-off решения в том, что auth-related arguments могут появиться раньше полноценного auth flow

Поэтому такие аргументы должны рассматриваться только как metadata для command building, а не как модель пользователя,
сессии или авторизации

Если позже появится полноценный authentication flow, он должен быть оформлен отдельным решением

---

## Не входит в решение

- Реализация authentication flow
- Login/logout lifecycle
- Account model
- Session model
- Secure token storage
- UI для авторизации
- Profile management
- Dynamic argument rules engine
- Loader-specific conditional rules
- Изменение `LauncherEngine` lifecycle
- Изменение operation sequence

---

## Связанные решения

- [ADR-0023: Использовать RuntimeLibrarySelection.libraries как источник game classpath](ADR-0023-use-libraries-as-game-classpath-source.md)
- [ADR-0027: Определить передачу директории natives в launch arguments](ADR-0027-natives-directory-launch-argument.md)
- [ADR-0038: Определить границу configured Java override](ADR-0038-configured-java-override-boundary.md)
- [ADR-0049: Определить границу assets index flow](ADR-0049-assets-index-flow-boundary.md)
