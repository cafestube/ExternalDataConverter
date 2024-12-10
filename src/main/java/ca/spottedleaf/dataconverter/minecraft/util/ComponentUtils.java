package ca.spottedleaf.dataconverter.minecraft.util;

import ca.spottedleaf.dataconverter.util.GsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import javax.annotation.Nullable;
import java.io.StringReader;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public final class ComponentUtils {

    public static final String EMPTY = createPlainTextComponent("");

    public static String createPlainTextComponent(final String text) {
        final JsonObject ret = new JsonObject();

        ret.addProperty("text", text);

        return ret.toString();
    }

    public static String createTranslatableComponent(final String key) {
        final JsonObject ret = new JsonObject();

        ret.addProperty("translate", key);

        return ret.toString();
    }

    @Nullable
    public static Component fromJsonLenient(String json) {
        JsonReader jsonreader = new JsonReader(new StringReader(json));

        jsonreader.setLenient(true);
        JsonElement jsonelement = JsonParser.parseReader(jsonreader);

        return jsonelement == null ? null : GsonComponentSerializer.gson().deserializeFromTree(jsonelement);
    }

    public static String retrieveTranslationString(final String possibleJson) {
        if (possibleJson == null) {
            return null;
        }

        try {
            final JsonElement element = JsonParser.parseString(possibleJson);

            if (element instanceof JsonObject object) {
                final JsonElement translation = object.get("translate");
                if (translation instanceof JsonPrimitive primitive) {
                    return primitive.getAsString();
                }
            }

            return null;
        } catch (final Exception ex) {
            return null;
        }
    }

    public static String convertFromLenient(final String input) {
        if (input == null) {
            return input;
        }

        if (input.isEmpty() || input.equals("null")) {
            return EMPTY;
        }

        final char firstCharacter = input.charAt(0);
        final char lastCharacter = input.charAt(input.length() - 1);
        if ((firstCharacter == '"' && lastCharacter == '"')
                || (firstCharacter == '{' && lastCharacter == '}')
                || (firstCharacter == '[' && lastCharacter == ']')) {
            try {
                final JsonElement json = JsonParser.parseString(input);

                if (json.isJsonPrimitive()) {
                    return createPlainTextComponent(json.getAsString());
                }

                return GsonUtil.toStableString(json);
            } catch (final JsonParseException ignored) {
                // fall through to plain text
            }
        }

        return createPlainTextComponent(input);
    }

    private ComponentUtils() {}
}
