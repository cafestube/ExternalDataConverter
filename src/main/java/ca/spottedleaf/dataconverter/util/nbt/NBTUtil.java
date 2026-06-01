package ca.spottedleaf.dataconverter.util.nbt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NBTUtil {

    public static CompoundBinaryTag parseCompoundSNBTString(String string) {
        try {
            return TagStringIO.tagStringIO().asCompound(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, BinaryTag> toMap(CompoundBinaryTag compound) {
        Map<String, BinaryTag> map = new HashMap<>();

        for (String key : compound.keySet()) {
            map.put(key, compound.get(key));
        }
        return map;
    }

    public static Number getNumber(NumberBinaryTag number) {
        //TODO(CafeStube): https://github.com/KyoriPowered/adventure/issues/1087
        switch (number) {
            case ByteBinaryTag tag -> {
                return tag.value();
            }
            case ShortBinaryTag tag -> {
                return tag.value();
            }
            case IntBinaryTag tag -> {
                return tag.value();
            }
            case LongBinaryTag tag -> {
                return tag.value();
            }
            case FloatBinaryTag tag -> {
                return tag.value();
            }
            case DoubleBinaryTag tag -> {
                return tag.value();
            }
            default -> throw new IllegalStateException("Unexpected value: " + number);
        }
    }

    public static boolean contains(CompoundBinaryTag compound, String tag, BinaryTagType<?> type) {
        return compound.keySet().contains(tag) && Objects.requireNonNull(compound.get(tag)).type() == type;
    }

    public static boolean contains(Map<String, BinaryTag> compound, String tag, BinaryTagType<?> type) {
        return compound.containsKey(tag) && Objects.requireNonNull(compound.get(tag)).type() == type;
    }


    public static JsonElement convertToJson(final BinaryTag tag) {
        switch (tag) {
            case CompoundBinaryTag entries -> {
                JsonObject object = new JsonObject();
                for (String key : entries.keySet()) {
                    object.add(key, convertToJson(Objects.requireNonNull(entries.get(key))));
                }
                return object;
            }
            case ListBinaryTag list -> {
                JsonArray array = new JsonArray();
                for (BinaryTag i : list) {
                    array.add(convertToJson(i));
                }
                return array;
            }
            case IntBinaryTag nbtInt -> {
                return new JsonPrimitive(nbtInt.value());
            }
            case IntArrayBinaryTag integers -> {
                JsonArray array = new JsonArray();
                for (int i : integers.value()) {
                    array.add(new JsonPrimitive(i));
                }
                return array;
            }
            case ByteBinaryTag nbtByte -> {
                return new JsonPrimitive(nbtByte.value());
            }
            case ByteArrayBinaryTag bytes -> {
                JsonArray array = new JsonArray();
                for (byte i : bytes.value()) {
                    array.add(new JsonPrimitive(i));
                }
                return array;
            }
            case LongBinaryTag nbtLong -> {
                return new JsonPrimitive(nbtLong.value());
            }
            case LongArrayBinaryTag longs -> {
                JsonArray array = new JsonArray();
                for (long i : longs.value()) {
                    array.add(new JsonPrimitive(i));
                }
                return array;
            }
            case FloatBinaryTag nbtFloat -> {
                return new JsonPrimitive(nbtFloat.value());
            }
            case DoubleBinaryTag nbtDouble -> {
                return new JsonPrimitive(nbtDouble.value());
            }
            case ShortBinaryTag nbtShort -> {
                return new JsonPrimitive(nbtShort.value());
            }
            case StringBinaryTag nbtString -> {
                return new JsonPrimitive(nbtString.value());
            }
            default -> throw new UnsupportedOperationException("Unsupported NBT type: " + tag.getClass().getSimpleName());
        }
    }


}
