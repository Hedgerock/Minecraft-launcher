# ADR-0014: Использовать ManifestResources как источник verification flow

## Статус

Accepted

> Примечание: решение реализовано в итерации
> `feat(verification): verify manifest resources`

---

## Контекст

`Manifest` содержит несколько manifest-specific коллекций, которые описывают физические ресурсы:

- `files`
- `libraries`

`FileEntry` и `LibraryEntry` имеют общую физическую метадату:

- `path`
- `sha256`
- `size`
- `url`

Было принято решение ввести модель `ResourceEntry`

Также была введена projection-модель `ManifestResources`, которая строит список `ResourceEntry` из
`Manifest.files` и `Manifest.libraries`

На текущем этапе `DefaultVerificationService` все еще использует только `Manifest.files` и преобразует
`FileEntry` в `ResourceEntry`

Это означает, что библиотеки уже участвуют в построении classpath, но еще не участвуют в
verification/download lifecycle

---

## Решение

Verification flow должен использовать `ManifestResources.from(...)` как источник проверяемых
ресурсов

`DefaultVerificationService` не должен самостоятельно выбирать только `Manifest.files`

Проверяемой единицей verification flow становится `ResourceEntry`

`VerificationPlan` должен содержать результаты проверки ресурсов, а не manifest-specific entries

`DownloadPlan` должен строиться из невалидных `ResourceEntry`, полученных через `VerificationPlan`

---

## Последствия

`Manifest.files` и `Manifest.libraries` будут участвовать в едином verification/download lifecycle

Библиотеки, отсутствующие локально или имеющие неверный размер/hash, смогут попадать в `DownloadPlan`

`FileEntry` и `LibraryEntry` остаются manifest-specific моделями

`ResourceEntry` остается общей resource-level моделью для verification/download flow

Подключение `ManifestResources` меняет фактическое поведение launcher lifecycle, поэтому должно выполняться
отдельной кодовой итерацией после этого ADR

## Не входит в решение

- Полная поддержка Maven metadata
- OS-specific natives
- Rules/classifiers для выбора platform-specific библиотек
- Параллельная загрузка
- Retry/backoff downloader