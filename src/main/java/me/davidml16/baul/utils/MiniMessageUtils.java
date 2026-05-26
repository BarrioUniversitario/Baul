package me.davidml16.baul.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Utility class for handling MiniMessage formatting
 * Converts between MiniMessage format and legacy section format
 */
public class MiniMessageUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    /**
     * Formats a MiniMessage string into a legacy section format string
     * @param message The MiniMessage formatted string
     * @return The formatted legacy section string
     */
    public static String format(String message) {
        if (message == null || message.isEmpty()) return "";
        try {
            return LEGACY.serialize(miniMessage.deserialize(message));
        } catch (Exception e) {
            return message;
        }
    }

    /**
     * Parses a MiniMessage string into an Adventure Component
     * @param message The MiniMessage formatted string
     * @return The parsed Adventure Component
     */
    public static Component parse(String message) {
        if (message == null || message.isEmpty()) return Component.empty();
        try {
            return miniMessage.deserialize(message);
        } catch (Exception e) {
            return Component.text(message);
        }
    }

    /**
     * Serializes an Adventure Component to legacy section format
     * @param component The Adventure Component
     * @return The legacy section format string
     */
    public static String serialize(Component component) {
        if (component == null) return "";
        return LEGACY.serialize(component);
    }
}