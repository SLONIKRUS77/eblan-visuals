# Eblan Visuals

Клиентский мод визуальных улучшений и кастомного Discord Rich Presence для
Minecraft Fabric 1.21.1.

## Возможности

- интерактивная ватермарка `Eblan Visuals`, подключённая через `HudRenderCallback`;
- меню по ПКМ с переключателями сервера, пинга, ника, времени и координат;
- окно настройки DRPC с режимами «Обычный Minecraft» и «Кастомный игровой статус»;
- подмена названия игры, деталей и описания с переменными `{server}`, `{username}`,
  `{ping}`, `{coords}` и `{time}`;
- настройка Large Image / Small Image, текста подсказки и двух Discord-кнопок;
- сохранение настроек в `config/eblan_visuals.json` и
  `config/eblan_visuals_drp.json`;
- плавный HUD-индикатор здоровья с интерполяцией `MathHelper.lerp`;
- частицы попаданий и след за стрелами, элитрами и игроком в спринте;
- встроенный Discord IPC-клиент без обязательных внешних библиотек.

## Сборка

Нужна Java 21. Удобнее всего использовать Gradle Wrapper:

```bash
cd eblan-visuals
./gradlew build
```

Готовый JAR находится в `build/libs/eblan-visuals-1.0.0.jar`.

## Настройка Discord

1. Создайте приложение в Discord Developer Portal.
2. Скопируйте его Application ID в поле `applicationId` файла
   `config/eblan_visuals_drp.json`.
3. Запустите Minecraft с открытым Discord.
4. Выберите нужный режим и заполните поля через пункт
   `Настройка DRPC...`, затем нажмите `Применить и Сохранить`.

Если Discord закрыт или Application ID оставлен демонстрационным, мод продолжает
работать, а DRPC просто ожидает следующего подключения.

### Важное ограничение Discord

Discord определяет название приложения в блоке «Играет в ...» из Discord
Developer Portal. Мод отправляет поле «Название игры» через IPC и дополнительно
использует его в содержимом статуса. Если клиент Discord игнорирует переданное
имя приложения, точное название можно задать в настройках самого Discord-приложения.

## Автоматическая сборка и релизы GitHub

Workflow находится в `.github/workflows/build-release.yml`. Он:

- собирает мод на Java 21;
- запускается автоматически при отправке тега `v*`;
- запускается вручную через GitHub Actions → `Сборка и релиз Eblan Visuals` →
  `Run workflow`;
- создаёт GitHub Release и прикрепляет JAR из `build/libs/eblan-visuals-*.jar`.

Чтобы выпустить новую версию:

```bash
cd eblan-visuals
git add .
git commit -m "Подготовить релиз v1.0.0"
git tag v1.0.0
git push origin main
git push origin v1.0.0
```

После завершения workflow готовый файл можно скачать на странице репозитория в
разделе **Releases**. Для ручного запуска GitHub создаст релиз с тегом вида
`manual-номер-запуска`.

## Лицензия

MIT