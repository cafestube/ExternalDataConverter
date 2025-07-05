package ca.spottedleaf.dataconverter.types.nbt;

import ca.spottedleaf.dataconverter.types.ListType;
import ca.spottedleaf.dataconverter.types.MapType;
import ca.spottedleaf.dataconverter.types.ObjectType;
import ca.spottedleaf.dataconverter.types.TypeUtil;
import ca.spottedleaf.dataconverter.types.Types;
import ca.spottedleaf.dataconverter.util.nbt.NBTUtil;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class NBTMapType implements MapType, NBTAdapter {

    private final Map<String, Object> map;

    public NBTMapType() {
        this.map = new HashMap<>();
    }

    public NBTMapType(final CompoundBinaryTag tag) {
        this.map = new HashMap<>();
        for (final String key : tag.keySet()) {
            BinaryTag value = tag.get(key);
            if(value instanceof CompoundBinaryTag compoundTag) {
                this.map.put(key, new NBTMapType(compoundTag));
            } else if(value instanceof ListBinaryTag listTag) {
                this.map.put(key, new NBTListType(listTag));
            } else {
                this.map.put(key, value);
            }
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != NBTMapType.class) {
            return false;
        }

        return this.map.equals(((NBTMapType)obj).map);
    }

    @Override
    public TypeUtil<BinaryTag> getTypeUtil() {
        return Types.NBT;
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public String toString() {
        return "NBTMapType{" +
                "map=" + this.map +
                '}';
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<String> keys() {
        return this.map.keySet();
    }

    public CompoundBinaryTag getTag() {
        return CompoundBinaryTag.from(this.map.entrySet().stream().map(stringObjectEntry -> {
            if(stringObjectEntry.getValue() instanceof NBTMapType mapType) {
                return Map.entry(stringObjectEntry.getKey(), mapType.getTag());
            } else if(stringObjectEntry.getValue() instanceof NBTListType listType) {
                return Map.entry(stringObjectEntry.getKey(), listType.getTag());
            } else {
                return Map.entry(stringObjectEntry.getKey(), (BinaryTag) stringObjectEntry.getValue());
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public BinaryTagType<?> getNBTType() {
        return BinaryTagTypes.COMPOUND;
    }

    @Override
    public MapType copy() {
        return new NBTMapType(getTag());
    }

    @Override
    public boolean hasKey(final String key) {
        return this.map.get(key) != null;
    }

    @Override
    public boolean hasKey(final String key, final ObjectType type) {
        final Object tag = this.map.get(key);
        if (tag == null) {
            return false;
        }

        BinaryTagType<?> tagType;

        if(tag instanceof BinaryTag binaryTag) {
            tagType = binaryTag.type();
        } else if(tag instanceof NBTAdapter adapter) {
            tagType = adapter.getNBTType();
        } else {
            throw new UnsupportedOperationException("Unsupported tag type: " + tag.getClass());
        }


        final ObjectType valueType = NBTListType.getType(tagType.id());
        return valueType == type || (type == ObjectType.NUMBER && valueType.isNumber());
    }

    @Override
    public void remove(final String key) {
        this.map.remove(key);
    }

    @Override
    public Object getGeneric(final String key) {
        final Object tag = this.map.get(key);
        if (tag == null) {
            return null;
        }

        switch (tag) {
            case ByteBinaryTag byteTag -> {
                return byteTag.value();
            }
            case ShortBinaryTag shortTag -> {
                return shortTag.value();
            }
            case IntBinaryTag intTag -> {
                return intTag.value();
            }
            case LongBinaryTag longTag -> {
                return longTag.value();
            }
            case FloatBinaryTag floatTag -> {
                return floatTag.value();
            }
            case DoubleBinaryTag doubleTag -> {
                return doubleTag.value();
            }
            case CompoundBinaryTag compoundTag -> {
                return new NBTMapType(compoundTag);
            }
            case ListBinaryTag listTag -> {
                return new NBTListType(listTag);
            }
            case NBTListType listType -> {
                return listType;
            }
            case NBTMapType mapType -> {
                return mapType;
            }
            case StringBinaryTag stringTag -> {
                return stringTag.value();
            }
            case ByteArrayBinaryTag byteArrayTag -> {
                return byteArrayTag.value();
            }
            // Note: No short array tag!
            case IntArrayBinaryTag intArrayTag -> {
                return intArrayTag.value();
            }
            case LongArrayBinaryTag longTag -> {
                return longTag.value();
            }
            default -> {
                BinaryTagType<?> tagType;
                if(tag instanceof BinaryTag binaryTag) {
                    tagType = binaryTag.type();
                } else if(tag instanceof NBTAdapter adapter) {
                    tagType = adapter.getNBTType();
                } else {
                    throw new UnsupportedOperationException("Unsupported tag type: " + tag.getClass());
                }

                throw new UnsupportedOperationException("Unsupported tag type: " + tagType);
            }
        }
    }

    @Override
    public Number getNumber(final String key) {
        return this.getNumber(key, null);
    }

    @Override
    public Number getNumber(final String key, final Number dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return NBTUtil.getNumber(number);
        }
        return dfl;
    }

    @Override
    public boolean getBoolean(final String key) {
        return this.getByte(key) != 0;
    }

    @Override
    public boolean getBoolean(final String key, final boolean dfl) {
        return this.getByte(key, dfl ? (byte)1 : (byte)0) != 0;
    }

    @Override
    public void setBoolean(final String key, final boolean val) {
        this.setByte(key, val ? (byte)1 : (byte)0);
    }

    @Override
    public byte getByte(final String key) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.byteValue();
        }
        return 0;
    }

    @Override
    public byte getByte(final String key, final byte dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.byteValue();
        }
        return dfl;
    }

    @Override
    public void setByte(final String key, final byte val) {
        this.map.put(key, ByteBinaryTag.byteBinaryTag(val));
    }

    @Override
    public short getShort(final String key) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.shortValue();
        }
        return 0;
    }

    @Override
    public short getShort(final String key, final short dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.shortValue();
        }
        return dfl;
    }

    @Override
    public void setShort(final String key, final short val) {
        this.map.put(key, ShortBinaryTag.shortBinaryTag(val));
    }

    @Override
    public int getInt(final String key) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.intValue();
        }
        return 0;
    }

    @Override
    public int getInt(final String key, final int dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.intValue();
        }
        return dfl;
    }

    @Override
    public void setInt(final String key, final int val) {
        this.map.put(key, IntBinaryTag.intBinaryTag(val));
    }

    @Override
    public long getLong(final String key) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.longValue();
        }
        return 0;
    }

    @Override
    public long getLong(final String key, final long dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.longValue();
        }
        return dfl;
    }

    @Override
    public void setLong(final String key, final long val) {
        this.map.put(key, LongBinaryTag.longBinaryTag(val));
    }

    @Override
    public float getFloat(final String key) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.floatValue();
        }
        return 0;
    }

    @Override
    public float getFloat(final String key, final float dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.floatValue();
        }
        return dfl;
    }

    @Override
    public void setFloat(final String key, final float val) {
        this.map.put(key, FloatBinaryTag.floatBinaryTag(val));
    }

    @Override
    public double getDouble(final String key) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.doubleValue();
        }
        return 0;
    }

    @Override
    public double getDouble(final String key, final double dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof NumberBinaryTag number) {
            return number.doubleValue();
        }
        return dfl;
    }

    @Override
    public void setDouble(final String key, final double val) {
        this.map.put(key, DoubleBinaryTag.doubleBinaryTag(val));
    }

    @Override
    public byte[] getBytes(final String key) {
        return this.getBytes(key, null);
    }

    @Override
    public byte[] getBytes(final String key, final byte[] dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof ByteArrayBinaryTag arrayBinaryTag) {
            return arrayBinaryTag.value();
        }
        return dfl;
    }

    @Override
    public void setBytes(final String key, final byte[] val) {
        this.map.put(key, ByteArrayBinaryTag.byteArrayBinaryTag(val));
    }

    @Override
    public short[] getShorts(final String key) {
        return this.getShorts(key, null);
    }

    @Override
    public short[] getShorts(final String key, final short[] dfl) {
        // NBT does not support short array
        return dfl;
    }

    @Override
    public void setShorts(final String key, final short[] val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] getInts(final String key) {
        return this.getInts(key, null);
    }

    @Override
    public int[] getInts(final String key, final int[] dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof IntArrayBinaryTag integers) {
            return integers.value();
        }
        return dfl;
    }

    @Override
    public void setInts(final String key, final int[] val) {
        this.map.put(key, IntArrayBinaryTag.intArrayBinaryTag(val));
    }

    @Override
    public long[] getLongs(final String key) {
        return this.getLongs(key, null);
    }

    @Override
    public long[] getLongs(final String key, final long[] dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof LongArrayBinaryTag) {
            return ((LongArrayBinaryTag)tag).value();
        }
        return dfl;
    }

    @Override
    public void setLongs(final String key, final long[] val) {
        this.map.put(key, LongArrayBinaryTag.longArrayBinaryTag(val));
    }

    @Override
    public ListType getListUnchecked(final String key) {
        return this.getListUnchecked(key, null);
    }

    @Override
    public ListType getListUnchecked(final String key, final ListType dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof ListBinaryTag list) {
            return new NBTListType(list);
        } else if(tag instanceof NBTListType nbtListType) {
            return nbtListType;
        }
        return dfl;
    }

    @Override
    public void setList(final String key, final ListType val) {
        if (val instanceof NBTListType nbtListType) {
            this.map.put(key, nbtListType);
        } else {
            throw new UnsupportedOperationException("Unsupported list type: " + val.getClass());
        }
    }

    @Override
    public MapType getMap(final String key) {
        return this.getMap(key, null);
    }

    @Override
    public MapType getMap(final String key, final MapType dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof CompoundBinaryTag) {
            return new NBTMapType(((CompoundBinaryTag) tag));
        } else if(tag instanceof NBTMapType nbtMapType) {
            return nbtMapType;
        }
        return dfl;
    }

    @Override
    public void setMap(final String key, final MapType val) {
        if(val instanceof NBTMapType nbtMapType) {
            this.map.put(key, nbtMapType);
        } else {
            throw new UnsupportedOperationException("Unsupported map type: " + val.getClass());
        }
    }

    @Override
    public String getString(final String key) {
        return this.getString(key, null);
    }

    @Override
    public String getString(final String key, final String dfl) {
        final Object tag = this.map.get(key);
        if (tag instanceof StringBinaryTag) {
            return ((StringBinaryTag)tag).value();
        }
        return dfl;
    }

    @Override
    public void setString(final String key, final String val) {
        this.map.put(key, StringBinaryTag.stringBinaryTag(val));
    }
}
