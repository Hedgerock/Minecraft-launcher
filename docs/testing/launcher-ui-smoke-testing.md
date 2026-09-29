# Smoke-тест JavaFX launch flow

## Назначение

Opt-in smoke-тест проверяет прохождение локального launcher lifecycle через собранный JavaFX entrypoint

Тест использует локальный HTTP server, временную launcher directory и fake Java executable. Он не требует внешней сети
или реального Minecraft client

---

## Запуск

Тест запускается только при `KEYSTONE_UI_SMOKE=true` и требует доступного графического сеанса

В PowerShell:

```powershell
$env:KEYSTONE_UI_SMOKE = "true"
.\gradlew :launcher-ui:test --tests "com.launcher.ui.smoke.LauncherUiSmokeTest" --rerun-tasks
Remove-Item Env:KEYSTONE_UI_SMOKE
```

`--rerun-tasks` исключает повторное использование предыдущего результата Gradle при изменении переменной окружения

Обычный `check` не запускает этот сценарий без явно заданной переменной

---

## Проверяемый сценарий

Тест запускает JavaFX application с локальным manifest URI и временной launcher directory, выполняет launch action
и проверяет отображение этапа загрузки манифеста и итогового статуса успешного запуска

Управляемый ответ HTTP server позволяет наблюдать промежуточный этап до завершения загрузки манифеста

Маркер fake process подтверждает, что ветка запуска внешнего процесса была достигнута

---

## Граница проверки

Статус `Game launched` означает успешный старт процесса, а не завершение игры

Тест не проверяет реальный Minecraft client, внешний сетевой сервис, визуальное оформление окна или все возможные launch
phases

Сценарий остается opt-in: он зависит от графической среды и не заменяет обычные application-level integration tests
