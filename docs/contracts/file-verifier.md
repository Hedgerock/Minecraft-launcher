# FileVerifier Contract

## Responsibility

Проверка одного файла относительно ManifestFile

## Input

ManifestFile.

## Output

FileVerificationResult.

## Guarantees

- Не изменяет файловую систему.
- Не скачивает данные.
- Использует только инфраструктурные сервисы.
- Не хранит состояние.

## Failure

Может завершиться IOException при невозможности чтения файла.