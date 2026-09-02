# Индекс архитектурных решений

## Назначение

Документ помогает быстро найти архитектурные решения проекта

Подробный формат ADR описан в [Правилах написания ADR](../rules/adr-guidelines.md)

---

## Решения

| ADR                                                              | Решение                                                           | Область            |
|------------------------------------------------------------------|-------------------------------------------------------------------|--------------------|
| [ADR-0001](ADR-0001-launch-context-responsibility.md)            | Ответственность `LaunchContext`                                   | launch context     |
| [ADR-0002](ADR-0002-launcher-lifecycle.md)                       | Жизненный цикл launcher                                           | launcher lifecycle |
| [ADR-0003](ADR-0003-launcher-engine-responsibility.md)           | Ответственность `LauncherEngine`                                  | launcher engine    |
| [ADR-0004](ADR-0004-file-verification.md)                        | Проверка файлов                                                   | verification       |
| [ADR-0005](ADR-0005-immutable-domain-model.md)                   | Immutable domain model                                            | model              |
| [ADR-0006](ADR-0006-authentication-boundaries.md)                | Границы аутентификации                                            | authentication     |
| [ADR-0007](ADR-0007-context-ownership.md)                        | Владения контекста                                                | context            |
| [ADR-0008](ADR-0008-operation-resolution-strategy.md)            | Operation resolution strategy                                     | operation          |
| [ADR-0009](ADR-0009-download-planning.md)                        | Планирование загрузки                                             | download           |
| [ADR-0010](ADR-0010-execution-strategy-selection.md)             | Execution strategy selection                                      | execution          |
| [ADR-0011](ADR-0011-failure-policy-extraction.md)                | Failure policy extraction                                         | failure policy     |
| [ADR-0012](ADR-0012-launcher-engine-uses-operation-manager.md)   | LauncherEngine использует `OperationManager`                      | operation          |
| [ADR-0013](ADR-0013-core-owns-orchestration-ports.md)            | Порты оркестрации принадлежат launcher-core                       | module boundaries  |
| [ADR-0014](ADR-0014-manifest-resources-verification-flow.md)     | `ManifestResources` как source для verification flow              | verification       |
| [ADR-0015](ADR-0015-minimal-library-metadata-scope.md)           | Минимальный scope library metadata                                | libraries          |
| [ADR-0016](ADR-0016-resource-path-safety.md)                     | Безопасность resource path                                        | resource paths     |
| [ADR-0017](ADR-0017-library-runtime-metadata-boundary.md)        | Граница runtime metadata для libraries                            | libraries          |
| [ADR-0018](ADR-0018-runtime-environment-boundary.md)             | Runtime environment boundary                                      | runtime            |
| [ADR-0019](ADR-0019-os-specific-library-rules.md)                | OS-specific rules для libraries                                   | libraries          |
| [ADR-0020](ADR-0020-library-classifiers-and-natives-boundary.md) | Classifiers и natives metadata boundary                           | libraries          |
| [ADR-0021](ADR-0021-native-artifact-processing-boundary.md)      | Граница обработки native artifacts                                | natives            |
| [ADR-0022](ADR-0022-manifest-load-result-boundary.md)            | `ManifestLoadResult` boundary                                     | manifest           |
| [ADR-0023](ADR-0023-use-libraries-as-game-classpath-source.md)   | `RuntimeLibrarySelection.libraries` как source для game classpath | classpath          |
| [ADR-0024](ADR-0024-native-extraction-operation-boundary.md)     | Native extraction operation boundary                              | natives            |
| [ADR-0025](ADR-0025-native-extraction-service-implementation.md) | Реализация native extraction service                              | natives            |
| [ADR-0026](ADR-0026-native-extraction-exclude-rules.md)          | Extract exclude rules для native artifacts                        | natives            |
| [ADR-0027](ADR-0027-natives-directory-launch-argument.md)        | Директория natives в launch arguments                             | launch arguments   |
| [ADR-0028](ADR-0028-native-extraction-output-policy.md)          | Output policy распакованных native artifacts                      | natives            |
| [ADR-0029](ADR-0029-java-runtime-selection-boundary.md)          | Граница выбора Java runtime                                       | java runtime       |
| [ADR-0030](ADR-0030-java-executable-readiness-boundary.md)       | Граница проверки Java executable                                  | java runtime       |
| [ADR-0031](ADR-0031-java-executable-reference-boundary.md)       | Граница Java executable reference                                 | java runtime       |
