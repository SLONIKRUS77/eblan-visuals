package ru.eblan.visuals.config;

/**
 * Данные, которые используются для Discord Rich Presence.
 */
public final class DrpcConfig {
    public String applicationId = "123456789012345678";
    public boolean enabled = true;
    public String details = "Играю на {server}";
    public String state = "Игрок: {username} | Пинг: {ping} ms";
    public String button1Name = "Мой профиль";
    public String button1Url = "https://discord.com";
    public String button2Name = "";
    public String button2Url = "";
}