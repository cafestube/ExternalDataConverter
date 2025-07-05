package ca.spottedleaf.dataconverter.types.nbt;

import ca.spottedleaf.dataconverter.types.ObjectType;
import ca.spottedleaf.dataconverter.types.ListType;
import ca.spottedleaf.dataconverter.types.MapType;
import ca.spottedleaf.dataconverter.types.TypeUtil;
import ca.spottedleaf.dataconverter.types.Types;
import ca.spottedleaf.dataconverter.util.nbt.MutableNBTList;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class NBTListType implements ListType, NBTAdapter {

    private final List<Object> list;
    private BinaryTagType<?> type;

    public NBTListType() {
        this.list = new ArrayList<>();
        this.type = BinaryTagTypes.END;
    }

    public NBTListType(final ListBinaryTag tag) {
        this.type = tag.elementType();
        this.list = tag.stream().map(binaryTag -> {
            if (binaryTag instanceof ListBinaryTag listBinaryTag) {
                return new NBTListType(listBinaryTag);
            } else if (binaryTag instanceof CompoundBinaryTag compoundBinaryTag) {
                return new NBTMapType(compoundBinaryTag);
            }
            return binaryTag;
        }).collect(Collectors.toList());
    }

    @Override
    public TypeUtil<BinaryTag> getTypeUtil() {
        return Types.NBT;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != NBTListType.class) {
            return false;
        }

        return this.list.equals(((NBTListType)obj).list);
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public String toString() {
        return "NBTListType{" +
                "list=" + this.list +
                '}';
    }

    public ListBinaryTag getTag() {
        return ListBinaryTag.listBinaryTag(this.type, this.list.stream().map(o -> switch (o) {
            case NBTAdapter map -> map.getTag();
            case BinaryTag binaryTag -> binaryTag;
            case null, default -> throw new IllegalStateException();
        }).toList());
    }

    @Override
    public BinaryTagType<?> getNBTType() {
        return BinaryTagTypes.COMPOUND;
    }

    @Override
    public ListType copy() {
        return new NBTListType(this.getTag());
    }

    static ObjectType getType(final byte id) {

        if (id == BinaryTagTypes.END.id()) {
            return ObjectType.NONE;
        } else if (id == BinaryTagTypes.BYTE.id()) {
            return ObjectType.BYTE;
        } else if (id == BinaryTagTypes.SHORT.id()) {
            return ObjectType.SHORT;
        } else if (id == BinaryTagTypes.INT.id()) {
            return ObjectType.INT;
        } else if (id == BinaryTagTypes.LONG.id()) {
            return ObjectType.LONG;
        } else if (id == BinaryTagTypes.FLOAT.id()) {
            return ObjectType.FLOAT;
        } else if (id == BinaryTagTypes.DOUBLE.id()) {
            return ObjectType.DOUBLE;
        } else if (id == BinaryTagTypes.BYTE_ARRAY.id()) {
            return ObjectType.BYTE_ARRAY;
        } else if (id == BinaryTagTypes.STRING.id()) {
            return ObjectType.STRING;
        } else if (id == BinaryTagTypes.LIST.id()) {
            return ObjectType.LIST;
        } else if (id == BinaryTagTypes.COMPOUND.id()) {
            return ObjectType.MAP;
        } else if (id == BinaryTagTypes.INT_ARRAY.id()) {
            return ObjectType.INT_ARRAY;
        } else if (id == BinaryTagTypes.LONG_ARRAY.id()) {
            return ObjectType.LONG_ARRAY;
        }
        throw new IllegalStateException("Unknown type: " + id);
    }

    private BinaryTagType<?> getTypeOf(int index) {
        Object obj = this.list.get(index);

        return switch (obj) {
            case NBTAdapter adapter -> adapter.getNBTType();
            case BinaryTag binaryTag -> binaryTag.type();
            case null, default -> throw new IllegalStateException();
        };
    }

    @Override
    public ObjectType getUniformType() {
        ObjectType curr = null;

        for (int i = 0, len = this.list.size(); i < len; ++i) {
            final BinaryTagType<?> type = this.getTypeOf(i);
            final ObjectType tagType = getType(type.id());
            if (curr == null) {
                curr = tagType;
            } else if (tagType != curr) {
                return ObjectType.MIXED;
            }
        }

        return curr == null ? ObjectType.NONE : curr;
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public void remove(final int index) {
        this.list.remove(index);
    }

    private BinaryTag getBinaryTag(int index) {
        Object obj = this.list.get(index);

        return switch (obj) {
            case NBTAdapter adapter -> adapter.getTag();
            case BinaryTag binaryTag -> binaryTag;
            case null, default -> throw new IllegalStateException();
        };
    }

    private void addBinaryTag(Object tag) {
        if(tag instanceof NBTAdapter adapter) {
            if(this.updateType(adapter.getNBTType())) {
                this.list.add(adapter);
            }
        } else if(tag instanceof BinaryTag binaryTag) {
            if(this.updateType(binaryTag.type())) {
                this.list.add(binaryTag);
            }
        } else {
            throw new IllegalArgumentException("Invalid tag type: " + tag);
        }
    }

    private void addBinaryTag(int index, Object tag) {
        if(tag instanceof NBTAdapter adapter) {
            if(this.updateType(adapter.getNBTType())) {
                this.list.add(index, adapter);
            }
        } else if(tag instanceof BinaryTag binaryTag) {
            if(this.updateType(binaryTag.type())) {
                this.list.add(index, binaryTag);
            }
        } else {
            throw new IllegalArgumentException("Invalid tag type: " + tag);
        }
    }

    private void setBinaryTag(int index, Object tag) {
        if(tag instanceof NBTAdapter adapter) {
            if(this.updateType(adapter.getNBTType())) {
                this.list.set(index, adapter);
            }
        } else if(tag instanceof BinaryTag binaryTag) {
            if(this.updateType(binaryTag.type())) {
                this.list.set(index, binaryTag);
            }
        } else {
            throw new IllegalArgumentException("Invalid tag type: " + tag);
        }
    }

    private boolean updateType(BinaryTagType<?> type) {
        if(type == BinaryTagTypes.END) {
            return false;
        } else if (this.type == BinaryTagTypes.END) {
            this.type = type;
            return true;
        } else if(this.type != type && this.type != BinaryTagTypes.LIST_WILDCARD) {
            this.type = BinaryTagTypes.LIST_WILDCARD;
            return true;
        }
        return true;
    }

    @Override
    public Object getGeneric(final int index) {
        return Types.NBT.baseToGeneric(this.getBinaryTag(index));
    }

    @Override
    public Number getNumber(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            throw new IllegalStateException();
        }
        return NBTUtil.getNumber(number);
    }

    @Override
    public Number getNumber(final int index, final Number dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            return dfl;
        }
        return NBTUtil.getNumber(number);
    }

    @Override
    public byte getByte(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            throw new IllegalStateException();
        }
        return number.byteValue();
    }

    @Override
    public byte getByte(final int index, final byte dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            return dfl;
        }
        return number.byteValue();
    }

    @Override
    public void setByte(final int index, final byte to) {
        setBinaryTag(index, ByteBinaryTag.byteBinaryTag(to));
    }

    @Override
    public short getShort(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            throw new IllegalStateException();
        }
        return number.shortValue();
    }

    @Override
    public short getShort(final int index, final short dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            return dfl;
        }
        return number.shortValue();
    }


    @Override
    public void setShort(final int index, final short to) {
        setBinaryTag(index, ShortBinaryTag.shortBinaryTag(to));
    }

    @Override
    public int getInt(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            throw new IllegalStateException();
        }
        return number.intValue();
    }

    @Override
    public int getInt(final int index, final int dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            return dfl;
        }
        return number.intValue();
    }

    @Override
    public void setInt(final int index, final int to) {
        setBinaryTag(index, IntBinaryTag.intBinaryTag(to));
    }

    @Override
    public long getLong(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            throw new IllegalStateException();
        }
        return number.longValue();
    }

    @Override
    public long getLong(final int index, final long dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            return dfl;
        }
        return number.longValue();
    }

    @Override
    public void setLong(final int index, final long to) {
        setBinaryTag(index, LongBinaryTag.longBinaryTag(to));
    }

    @Override
    public float getFloat(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            throw new IllegalStateException();
        }
        return number.floatValue();
    }

    @Override
    public float getFloat(final int index, final float dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            return dfl;
        }
        return number.floatValue();
    }

    @Override
    public void setFloat(final int index, final float to) {
        setBinaryTag(index, FloatBinaryTag.floatBinaryTag(to));
    }

    @Override
    public double getDouble(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            throw new IllegalStateException();
        }
        return number.doubleValue();
    }

    @Override
    public double getDouble(final int index, final double dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof NumberBinaryTag number)) {
            return dfl;
        }
        return number.doubleValue();
    }

    @Override
    public void setDouble(final int index, final double to) {
        setBinaryTag(index, DoubleBinaryTag.doubleBinaryTag(to));
    }

    @Override
    public byte[] getBytes(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof ByteArrayBinaryTag bytes)) {
            throw new IllegalStateException();
        }
        return bytes.value();
    }

    @Override
    public byte[] getBytes(final int index, final byte[] dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof ByteArrayBinaryTag bytes)) {
            return dfl;
        }
        return bytes.value();
    }

    @Override
    public void setBytes(final int index, final byte[] to) {
        setBinaryTag(index, ByteArrayBinaryTag.byteArrayBinaryTag(to));
    }

    @Override
    public short[] getShorts(final int index) {
        // NBT does not support shorts
        throw new UnsupportedOperationException();
    }

    @Override
    public short[] getShorts(final int index, final short[] dfl) {
        // NBT does not support shorts
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShorts(final int index, final short[] to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] getInts(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof IntArrayBinaryTag ints)) {
            throw new IllegalStateException();
        }
        return ints.value();
    }

    @Override
    public int[] getInts(final int index, final int[] dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof IntArrayBinaryTag ints)) {
            return dfl;
        }
        return ints.value();
    }

    @Override
    public void setInts(final int index, final int[] to) {
        setBinaryTag(index, IntArrayBinaryTag.intArrayBinaryTag(to));
    }

    @Override
    public long[] getLongs(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof LongArrayBinaryTag longs)) {
            throw new IllegalStateException();
        }
        return longs.value();
    }

    @Override
    public long[] getLongs(final int index, final long[] dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof LongArrayBinaryTag longs)) {
            return dfl;
        }
        return longs.value();
    }

    @Override
    public void setLongs(final int index, final long[] to) {
        setBinaryTag(index, LongArrayBinaryTag.longArrayBinaryTag(to));
    }

    @Override
    public ListType getList(final int index) {
        final Object tag = this.list.get(index); // does bound checking for us
        switch (tag) {
            case NBTListType nbtListType -> {
                return nbtListType;
            }
            case ListBinaryTag listBinaryTag -> {
                return new NBTListType(listBinaryTag);
            }
            default -> throw new IllegalStateException();
        }
    }

    @Override
    public ListType getList(final int index, final ListType dfl) {
        final Object tag = this.list.get(index); // does bound checking for us
        switch (tag) {
            case NBTListType nbtListType -> {
                return nbtListType;
            }
            case ListBinaryTag listBinaryTag -> {
                return new NBTListType(listBinaryTag);
            }
            default -> {
                return dfl;
            }
        }
    }

    @Override
    public void setList(final int index, final ListType list) {
        if(!(list instanceof NBTListType)) {
            throw new IllegalArgumentException("Invalid list type: " + list);
        }
        setBinaryTag(index, list);
    }

    @Override
    public MapType getMap(final int index) {
        final Object tag = this.list.get(index); // does bound checking for us
        switch (tag) {
            case NBTMapType map -> {
                return map;
            }
            case CompoundBinaryTag compoundBinaryTag -> {
                return new NBTMapType(compoundBinaryTag);
            }
            default -> throw new IllegalStateException("Invalid tag type: " + tag);
        }
    }

    @Override
    public MapType getMap(final int index, final MapType dfl) {
        final Object tag = this.list.get(index); // does bound checking for us
        switch (tag) {
            case NBTMapType map -> {
                return map;
            }
            case CompoundBinaryTag compoundBinaryTag -> {
                return new NBTMapType(compoundBinaryTag);
            }
            default -> {
                return dfl;
            }
        }
    }

    @Override
    public void setMap(final int index, final MapType to) {
        if(!(to instanceof NBTMapType)) {
            throw new IllegalArgumentException("Invalid map type: " + to);
        }
        this.setBinaryTag(index, to);
    }

    @Override
    public String getString(final int index) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof StringBinaryTag stringTag)) {
            throw new IllegalStateException();
        }
        return stringTag.value();
    }

    @Override
    public String getString(final int index, final String dfl) {
        final BinaryTag tag = this.getBinaryTag(index); // does bound checking for us
        if (!(tag instanceof StringBinaryTag stringTag)) {
            return dfl;
        }
        return stringTag.value();
    }

    @Override
    public void setString(final int index, final String to) {
        setBinaryTag(index, StringBinaryTag.stringBinaryTag(to));
    }

    @Override
    public void addByte(final byte b) {
        addBinaryTag(ByteBinaryTag.byteBinaryTag(b));
    }

    @Override
    public void addByte(final int index, final byte b) {
        addBinaryTag(index, ByteBinaryTag.byteBinaryTag(b));
    }

    @Override
    public void addShort(final short s) {
        addBinaryTag(ShortBinaryTag.shortBinaryTag(s));
    }

    @Override
    public void addShort(final int index, final short s) {
        addBinaryTag(index, ShortBinaryTag.shortBinaryTag(s));
    }

    @Override
    public void addInt(final int i) {
        addBinaryTag(IntBinaryTag.intBinaryTag(i));
    }

    @Override
    public void addInt(final int index, final int i) {
        addBinaryTag(index, IntBinaryTag.intBinaryTag(i));
    }

    @Override
    public void addLong(final long l) {
        addBinaryTag(LongBinaryTag.longBinaryTag(l));
    }

    @Override
    public void addLong(final int index, final long l) {
        addBinaryTag(index, LongBinaryTag.longBinaryTag(l));
    }

    @Override
    public void addFloat(final float f) {
        addBinaryTag(FloatBinaryTag.floatBinaryTag(f));
    }

    @Override
    public void addFloat(final int index, final float f) {
        addBinaryTag(index, FloatBinaryTag.floatBinaryTag(f));
    }

    @Override
    public void addDouble(final double d) {
        addBinaryTag(DoubleBinaryTag.doubleBinaryTag(d));
    }

    @Override
    public void addDouble(final int index, final double d) {
        addBinaryTag(index, DoubleBinaryTag.doubleBinaryTag(d));
    }

    @Override
    public void addByteArray(final byte[] arr) {
        addBinaryTag(ByteArrayBinaryTag.byteArrayBinaryTag(arr));
    }

    @Override
    public void addByteArray(final int index, final byte[] arr) {
        addBinaryTag(index, ByteArrayBinaryTag.byteArrayBinaryTag(arr));
    }

    @Override
    public void addShortArray(final short[] arr) {
        // NBT does not support short[]
        throw new UnsupportedOperationException();
    }

    @Override
    public void addShortArray(final int index, final short[] arr) {
        // NBT does not support short[]
        throw new UnsupportedOperationException();
    }

    @Override
    public void addIntArray(final int[] arr) {
        addBinaryTag(IntArrayBinaryTag.intArrayBinaryTag(arr));
    }

    @Override
    public void addIntArray(final int index, final int[] arr) {
        addBinaryTag(index, IntArrayBinaryTag.intArrayBinaryTag(arr));
    }

    @Override
    public void addLongArray(final long[] arr) {
        addBinaryTag(LongArrayBinaryTag.longArrayBinaryTag(arr));
    }

    @Override
    public void addLongArray(final int index, final long[] arr) {
        addBinaryTag(index, LongArrayBinaryTag.longArrayBinaryTag(arr));
    }

    @Override
    public void addList(final ListType list) {
        if(list instanceof NBTListType nbtListType) {
            this.addBinaryTag(nbtListType);
        } else {
            throw new IllegalArgumentException("Invalid list type: " + list);
        }
    }

    @Override
    public void addList(final int index, final ListType list) {
        if(list instanceof NBTListType nbtListType) {
            this.addBinaryTag(index, nbtListType);
        } else {
            throw new IllegalArgumentException("Invalid list type: " + list);
        }
    }

    @Override
    public void addMap(final MapType map) {
        if(map instanceof NBTMapType nbtMapType) {
            this.addBinaryTag(nbtMapType);
        } else {
            throw new IllegalArgumentException("Invalid map type: " + map);
        }
    }

    @Override
    public void addMap(final int index, final MapType map) {
        if(map instanceof NBTMapType nbtMapType) {
            this.addBinaryTag(index, nbtMapType);
        } else {
            throw new IllegalArgumentException("Invalid map type: " + map);
        }
    }

    @Override
    public void addString(final String string) {
        addBinaryTag(StringBinaryTag.stringBinaryTag(string));
    }

    @Override
    public void addString(final int index, final String string) {
        addBinaryTag(index, StringBinaryTag.stringBinaryTag(string));
    }
}
