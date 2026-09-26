# Eblan Visuals

Клиентский мод визуальных улучшений и кастомного Discord Rich Presence для
Minecraft Fabric 1.21.1.

## Возможности

- интерактивная ватермарка `Eblan Visuals`, подключённая через `HudRenderCallback`;
- меню по ПКМ с переключателями сервера, пинга, ника, времени и координат;
- окно настройки DRPC с переменными `{server}`, `{username}`, `{ping}` и `{coords}`;
- сохранение настроек в `config/eblan_visuals.json` и
  `config/eblan_visuals_drp.json`;
- плавный HUD-индикатор здоровья с интерполяцией `MathHelper.lerp`;
- частицы попаданий и след за стрелами, элитрами и игроком в спринте;
- встроенный Discord IPC-клиент без обязательных внешних библиотек.

## Сборка

Нужна Java 21 и установленный Gradle:

```bash
cd eblan-visuals
gradle build
```

Готовый JAR находится в `build/libs/eblan-visuals-1.0.0.jar`.

## Настройка Discord

1. Создайте приложение в Discord Developer Portal.
2. Скопируйте его Application ID в поле `applicationId` файла
   `config/eblan_visuals_drp.json`.
3. Запустите Minecraft с открытым Discord.
4. Изменения текста можно применять в игре через пункт
   `Настройка DRPC...`, перезапуск не требуется.

Если Discord закрыт или Application ID оставлен демонстрационным, мод продолжает
работать, а DRPC просто ожидает следующего подключения.

## Лицензия

MIT