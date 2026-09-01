# ADR-0028: Определить политику записи распакованных native artifacts

## Статус

Accepted

---

## Контекст

После развития native extraction flow launcher умеет выбирать native artifacts для текущей `OperatingSystem`,
загружать их как обычные ресурсы и распаковывать в директорию `LauncherDirectories.nativesDirectory`

Распаковка выполняется отдельной operation `EXTRACT_NATIVES` после `PREPARE_DIRECTORIES` и до `BUILD_GAME_LAUNCH_PLAN`

`DefaultNativeExtractionService` распаковывает archive entries из selected native artifacts и применяет
`extract.exclude` rules

Директория natives передается в launch command через подстановку `${natives_directory}`

Однако текущая политика записи распакованных файлов не определяет поведение при повторном запуске launcher,
когда целевой файл уже существует в директории natives

Без явной output policy повторная распаковка может завершиться ошибкой из-за уже существующего файла

На текущем этапе в проекте еще нет стабильной runtime identity, которая могла бы быть использована как ключ
для версионирования директории natives

Например

```text
profile id
game version
loader version
manifest hash
installation id
runtime environment
```

Также в проекте еще нет cleanup policy для устаревших runtime directories

---

## Решение

`DefaultNativeExtractionService` должен использовать простую идемпотентную output policy для текущей директории natives

Если archive entry уже существует в target directory, он должен быть заменен новой версией из native artifact

Перед записью файла проверка безопасности target path остается обязательной

`extract.exclude` rules применяются до записи файла и продолжают исключать archive entries из распаковки

Граница выглядит так

```text
SelectedNativeArtifact
    -> NativeExtractionRules
    -> DefaultNativeExtractionService
        -> resolve target path
        -> apply exclude rules
        -> replace existing extracted file
```

`DefaultNativeExtractionService` не должен очищать всю директорию natives перед распаковкой

`DefaultNativeExtractionService` не должен создавать версионированные директории natives

Версионирование директории natives остается отложенным решением до появления стабильной runtime identity

---

## Последствия

Повторный запуск launcher становится устойчивым к уже существующим распакованным native files

Native extraction остается простой и не требует отдельной модели runtime directory identity

Поведение распаковки становится идемпотентным для одинакового набора selected native artifacts

`EXTRACT_NATIVES` продолжает отвечать только за подготовку natives, а не за управление версиями runtime directories

Zip Slip защита остается обязательной частью `NativeExtractionService` и не заменяется output policy

Если в будущем появится поддержка нескольких game profiles, versions или installations, текущая
политика может быть заменена или расширена отдельным решением

---

## Не входит в решение

- Версионирование директории natives
- Cleanup policy для старых natives directories
- Параллельная поддержка нескольких runtime native sets
- Разделение natives по profile, version, manifest hash или installation id
- Проверка, используется ли native file уже запущенным игровым процессом
- Очистка всей директории natives перед распаковкой
- Изменение verification/download flow
- Изменение `RuntimeLibrarySelection`
- Изменение `${natives_directory}` launch variable

---

## Связанные решения

- [ADR-0021: Определить границу обработки native artifacts](ADR-0021-native-artifact-processing-boundary.md)
- [ADR-0024: Определить границу native extraction operation](ADR-0024-native-extraction-operation-boundary.md)
- [ADR-0025: Определить реализацию native extraction service](ADR-0025-native-extraction-service-implementation.md)
- [ADR-0026: Определить правила исключения при распаковке native artifacts](ADR-0026-native-extraction-exclude-rules.md)
- [ADR-0027: Определить передачу директории natives в launch arguments](ADR-0027-natives-directory-launch-argument.md)
