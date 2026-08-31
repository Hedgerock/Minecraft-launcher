# Перспективы развития проекта

## Приоритет 1 — привести Gradle в порядок

Можно унифицировать:

- group;
- version;
- JUnit BOM;
- test configuration;
- common repositories;
- Java toolchain.
Это даст более чистую основу перед дальнейшим ростом проекта.

---

## Приоритет 2 — усилить quality gate

Текущий qualityCheck полезен, но минимален.

Можно постепенно добавить:

- Checkstyle;
- SpotBugs;
- PMD;
- JaCoCo;
- dependency analysis;
- архитектурные тесты на зависимости между модулями.

Особенно полезно было бы формализовать правило:

``` text
launcher-core не должен зависеть от инфраструктурных модулей
launcher-app является composition root
```
---

## Приоритет 3 — стабилизировать error model

Для лаунчера очень важно, чтобы ошибки были не просто failed, а имели понятный контекст:

- что сломалось;
- на каком operation step;
- какой файл/URL/library;
- можно ли повторить;
- нужно ли удалить файл;
- это ошибка сети, manifest, hash, storage, permission или runtime.

---

## Приоритет 4 — подготовить launch flow к дальнейшему росту

Перед добавлением auth/profile/version selection лучше не перегружать LauncherEngine.
Можно заранее подумать о модели:

``` text
LaunchWorkflow
LaunchStep
ConditionalLaunchStep
LaunchFailurePolicy
```

## Основное правило

Внедрение новых приоритетов не должно быть преждевременным.
Каждое изменение должно быть обосновано решением, текущим состоянием проекта и
понятной пользой для дальнейшего развития
