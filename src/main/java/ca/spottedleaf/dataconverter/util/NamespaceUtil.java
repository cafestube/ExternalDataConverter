package ca.spottedleaf.dataconverter.util;

import ca.spottedleaf.dataconverter.types.MapType;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;

public final class NamespaceUtil {

    private NamespaceUtil() {}

    public static void enforceForPath(final MapType data, final String path) {
        if (data == null) {
            return;
        }

        final String id = data.getString(path);
        if (id != null) {
            final String replace = NamespaceUtil.correctNamespaceOrNull(id);
            if (replace != null) {
                data.setString(path, replace);
            }
        }
    }

    public static String correctNamespace(final String value) {
        if (value == null) {
            return null;
        }
        final Key resourceLocation = tryParse(value);
        return resourceLocation != null ? resourceLocation.toString() : value;
    }

    private static Key tryParse(String value) {
        try {
            return Key.key(value);
        } catch (final InvalidKeyException ignored) {
            return null;
        }
    }

    public static String correctNamespaceOrNull(final String value) {
        if (value == null) {
            return null;
        }
        final String correct = correctNamespace(value);
        return correct.equals(value) ? null : correct;
    }
}
