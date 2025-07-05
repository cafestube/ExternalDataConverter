package ca.spottedleaf.dataconverter.types.nbt;

import ca.spottedleaf.dataconverter.minecraft.converters.helpers.CopyHelper;
import ca.spottedleaf.dataconverter.types.ListType;
import ca.spottedleaf.dataconverter.types.MapType;
import ca.spottedleaf.dataconverter.types.TypeUtil;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;

public final class NBTTypeUtil implements TypeUtil<BinaryTag> {

    @Override
    public ListType createEmptyList() {
        return new NBTListType();
    }

    @Override
    public MapType createEmptyMap() {
        return new NBTMapType();
    }

    @Override
    public Object convertTo(final Object valueGeneric, final TypeUtil<?> to) {
        if (valueGeneric == null || valueGeneric instanceof String || valueGeneric instanceof Boolean) {
            return valueGeneric;
        }
        if (valueGeneric instanceof Number number) {
            if (CopyHelper.sanitizeNumber(number) == null) {
                throw new IllegalStateException("Unknown type: " + number);
            }
            return number;
        }
        if (valueGeneric instanceof NBTListType listType) {
            return convertNBT(to, listType.getTag());
        }
        if (valueGeneric instanceof NBTMapType mapType) {
            return convertNBT(to, mapType.getTag());
        }
        throw new IllegalStateException("Unknown type: " + valueGeneric);
    }

    @Override
    public Object convertFromBaseToGeneric(final BinaryTag input, final TypeUtil<?> to) {
        return convertNBTToGeneric(to, input);
    }

    @Override
    public Object baseToGeneric(final BinaryTag input) {
        return switch (input) {
            case CompoundBinaryTag ct -> new NBTMapType(ct);
            case ListBinaryTag lt -> new NBTListType(lt);
            case EndBinaryTag ignored -> null;
            case StringBinaryTag st -> st.value();
            case ByteArrayBinaryTag bt -> bt.value();
            case IntArrayBinaryTag it -> it.value();
            case LongArrayBinaryTag lt -> lt.value();
            case NumberBinaryTag nt -> nt.numberValue();
            case null -> null;
            default -> throw new IllegalStateException("Unknown tag: " + input);
        };
    }

    @Override
    public BinaryTag genericToBase(final Object input) {
        return switch (input) {
            case null -> EndBinaryTag.endBinaryTag();
            case NBTMapType mapType -> mapType.getTag();
            case NBTListType listType -> listType.getTag();
            case String string -> StringBinaryTag.stringBinaryTag(string);
            case Boolean bool -> bool.booleanValue() ? ByteBinaryTag.ONE : ByteBinaryTag.ZERO;
            case Byte b -> ByteBinaryTag.byteBinaryTag(b.byteValue());
            case Short s -> ShortBinaryTag.shortBinaryTag(s.shortValue());
            case Integer i -> IntBinaryTag.intBinaryTag(i.intValue());
            case Long l -> LongBinaryTag.longBinaryTag(l.longValue());
            case byte[] bytes -> ByteArrayBinaryTag.byteArrayBinaryTag(bytes);
            case int[] ints -> IntArrayBinaryTag.intArrayBinaryTag(ints);
            case long[] longs -> LongArrayBinaryTag.longArrayBinaryTag(longs);

            default -> throw new IllegalStateException("Unrecognized type " + input);
        };
    }

    private static Object convertNBTToGeneric(final TypeUtil<?> to, final BinaryTag nbt) {
        return switch (nbt) {
            case CompoundBinaryTag ct -> convertNBT(to, ct);
            case ListBinaryTag lt -> convertNBT(to, lt);
            case EndBinaryTag ignored -> null;
            case StringBinaryTag st -> st.value();
            case ByteArrayBinaryTag bt -> bt.value();
            case IntArrayBinaryTag it -> it.value();
            case LongArrayBinaryTag lt -> lt.value();
            case NumberBinaryTag nt -> nt.numberValue();
            case null -> null;
            default -> throw new IllegalStateException("Unknown tag: " + nbt);
        };
    }

    public static MapType convertNBT(final TypeUtil<?> to, final CompoundBinaryTag nbt) {
        final MapType ret = to.createEmptyMap();

        for (final String key : nbt.keySet()) {
            ret.setGeneric(key, convertNBTToGeneric(to, nbt.get(key)));
        }

        return ret;
    }

    public static ListType convertNBT(final TypeUtil<?> to, final ListBinaryTag nbt) {
        final ListType ret = to.createEmptyList();

        for (int i = 0, len = nbt.size(); i < len; ++i) {
            ret.addGeneric(convertNBTToGeneric(to, nbt.get(i)));
        }

        return ret;
    }
}
