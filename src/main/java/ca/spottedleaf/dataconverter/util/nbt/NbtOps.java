package ca.spottedleaf.dataconverter.util.nbt;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NbtOps implements DynamicOps<BinaryTag> {
    public static DynamicOps<BinaryTag> INSTANCE = new NbtOps();

    @Override
    public BinaryTag empty() {
        return CompoundBinaryTag.empty();
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, BinaryTag tag) {
        if(tag instanceof ByteBinaryTag byteTag) {
            return outOps.createByte(byteTag.value());
        } else if(tag instanceof ShortBinaryTag shortTag) {
            return outOps.createShort(shortTag.value());
        } else if(tag instanceof IntBinaryTag intTag) {
            return outOps.createInt(intTag.value());
        } else if(tag instanceof LongBinaryTag longTag) {
            return outOps.createLong(longTag.value());
        } else if(tag instanceof FloatBinaryTag floatTag) {
            return outOps.createFloat(floatTag.value());
        } else if(tag instanceof DoubleBinaryTag doubleTag) {
            return outOps.createDouble(doubleTag.value());
        } else if(tag instanceof ByteArrayBinaryTag byteArrayTag) {
            return outOps.createByteList(ByteBuffer.wrap(byteArrayTag.value().clone()));
        } else if(tag instanceof StringBinaryTag stringTag) {
            return outOps.createString(stringTag.value());
        } else if(tag instanceof ListBinaryTag listTag) {
            return this.convertList(outOps, listTag);
        } else if(tag instanceof CompoundBinaryTag compoundTag) {
            return this.convertMap(outOps, compoundTag);
        } else if(tag instanceof IntArrayBinaryTag intArrayTag) {
            return outOps.createIntList(Arrays.stream(intArrayTag.value().clone()));
        } else if(tag instanceof LongArrayBinaryTag longArrayTag) {
            return outOps.createLongList(Arrays.stream(longArrayTag.value().clone()));
        }

        throw new IllegalStateException("Unexpected value: " + tag);
    }

    @Override
    public DataResult<Number> getNumberValue(BinaryTag input) {
        if(input instanceof NumberBinaryTag numberTag) {
            return DataResult.success(NBTUtil.getNumber(numberTag));
        }
        return DataResult.error(() -> "Not a number");
    }

    @Override
    public BinaryTag createNumeric(Number i) {
        return DoubleBinaryTag.doubleBinaryTag(i.doubleValue());
    }

    @Override
    public BinaryTag createBoolean(boolean value) {
        return ByteBinaryTag.byteBinaryTag((byte)(value ? 1 : 0));
    }

    @Override
    public BinaryTag createInt(int value) {
        return IntBinaryTag.intBinaryTag(value);
    }

    @Override
    public BinaryTag createByte(byte value) {
        return ByteBinaryTag.byteBinaryTag(value);
    }

    @Override
    public BinaryTag createShort(short value) {
        return ShortBinaryTag.shortBinaryTag(value);
    }

    @Override
    public BinaryTag createLong(long value) {
        return LongBinaryTag.longBinaryTag(value);
    }

    @Override
    public BinaryTag createFloat(float value) {
        return FloatBinaryTag.floatBinaryTag(value);
    }

    @Override
    public BinaryTag createDouble(double value) {
        return DoubleBinaryTag.doubleBinaryTag(value);
    }

    @Override
    public DataResult<String> getStringValue(BinaryTag input) {
        if (input instanceof StringBinaryTag stringTag) {
            return DataResult.success(stringTag.value());
        } else {
            return DataResult.error(() -> "Not a string");
        }
    }

    @Override
    public BinaryTag createString(String value) {
        return StringBinaryTag.stringBinaryTag(value);
    }

    @Override
    public DataResult<BinaryTag> mergeToList(BinaryTag list, BinaryTag value) {
        if(!(list instanceof ListBinaryTag nbtList)) {
            return DataResult.error(() -> "Not a list");
        }
        if(nbtList.type() != value.type()) {
            return DataResult.error(() -> "List type mismatch");
        }

        List<BinaryTag> newData = new ArrayList<>(nbtList.stream().toList());
        newData.add(value);

        return DataResult.success(ListBinaryTag.listBinaryTag(nbtList.elementType(), newData));
    }

    @Override
    public DataResult<BinaryTag> mergeToList(BinaryTag list, List<BinaryTag> values) {
        if(!(list instanceof ListBinaryTag nbtList)) {
            return DataResult.error(() -> "Not a list");
        }

        //TODO: check if all values are of the same type

        List<BinaryTag> newData = new ArrayList<>(nbtList.stream().toList());
        newData.addAll(values);

        return DataResult.success(ListBinaryTag.listBinaryTag(nbtList.elementType(), newData));
    }

    @Override
    public DataResult<BinaryTag> mergeToMap(BinaryTag map, BinaryTag key, BinaryTag value) {
        if(!(map instanceof CompoundBinaryTag) && !(map instanceof EndBinaryTag)) {
            return DataResult.error(() -> "Not a map");
        } else if(!(key instanceof StringBinaryTag)) {
            return DataResult.error(() -> "Key is not a string");
        } else {
            CompoundBinaryTag.Builder newMap = CompoundBinaryTag.builder();
            if(map instanceof CompoundBinaryTag compoundTag) {
                newMap.put(compoundTag);
            }

            newMap.put(((StringBinaryTag)key).value(), value);
            return DataResult.success(newMap.build());
        }
    }

    @Override
    public DataResult<BinaryTag> mergeToMap(BinaryTag map, MapLike<BinaryTag> values) {
        if(!(map instanceof CompoundBinaryTag) && !(map instanceof EndBinaryTag)) {
            return DataResult.error(() -> "Not a map");
        } else {
            CompoundBinaryTag.Builder newMap = CompoundBinaryTag.builder();

            if(map instanceof CompoundBinaryTag compoundTag) {
                newMap.put(compoundTag);
            }

            List<BinaryTag> errors = Lists.newArrayList();
            values.entries().forEach((pair) -> {
                BinaryTag key = pair.getFirst();
                if (!(key instanceof StringBinaryTag)) {
                    errors.add(key);
                } else {
                    newMap.put(((StringBinaryTag) key).value(), pair.getSecond());
                }
            });
            return !errors.isEmpty() ? DataResult.error(() -> {
                return "following keys are not strings: " + errors;
            }, newMap.build()) : DataResult.success(newMap.build());
        }
    }

    @Override
    public DataResult<Stream<Pair<BinaryTag, BinaryTag>>> getMapValues(BinaryTag tag) {
        if (tag instanceof CompoundBinaryTag compoundTag) {
            return DataResult.success(compoundTag.keySet().stream().map((key) -> {
                return Pair.of(this.createString(key), compoundTag.get(key));
            }));
        } else {
            return DataResult.error(() -> "Invalid tag provided: " + tag);
        }
    }

    @Override
    public DataResult<Consumer<BiConsumer<BinaryTag, BinaryTag>>> getMapEntries(BinaryTag tag) {
        if (tag instanceof CompoundBinaryTag compoundTag) {
            return DataResult.success((entryConsumer) -> compoundTag.keySet().forEach((key) -> entryConsumer.accept(this.createString(key), compoundTag.get(key))));
        } else {
            return DataResult.error(() -> "Invalid tag provided: " + tag);
        }
    }

    @Override
    public DataResult<MapLike<BinaryTag>> getMap(BinaryTag tag) {
        if (tag instanceof final CompoundBinaryTag compoundTag) {
            return DataResult.success(new MapLike<BinaryTag>() {
                @Nullable
                public BinaryTag get(BinaryTag tag) {
                    return compoundTag.get(((StringBinaryTag)tag).value());
                }

                @Nullable
                public BinaryTag get(String string) {
                    return compoundTag.get(string);
                }

                public Stream<Pair<BinaryTag, BinaryTag>> entries() {
                    return compoundTag.keySet().stream().map((key) -> Pair.of(NbtOps.this.createString(key), compoundTag.get(key)));
                }

                @Override
                public String toString() {
                    return "MapLike[" + compoundTag + "]";
                }
            });
        } else {
            return DataResult.error(() -> "Invalid tag provided: " + tag);
        }
    }

    @Override
    public BinaryTag createMap(Stream<Pair<BinaryTag, BinaryTag>> map) {
        CompoundBinaryTag.Builder compoundTag = CompoundBinaryTag.builder();
        map.forEach((entry) -> compoundTag.put(((StringBinaryTag) entry.getFirst()).value(), entry.getSecond()));
        return compoundTag.build();
    }

    private static BinaryTag unwrap(CompoundBinaryTag nbt) {
        if (nbt.size() == 1) {
            BinaryTag tag = nbt.get("");
            if (tag != null) {
                return tag;
            }
        }

        return nbt;
    }

    @SuppressWarnings("unchecked")
    public DataResult<Stream<BinaryTag>> getStream(BinaryTag tag) {
        if (tag instanceof ListBinaryTag listTag) {
            return listTag.elementType() == BinaryTagTypes.COMPOUND ? DataResult.success(listTag.stream()
                .map((nbt) -> unwrap((CompoundBinaryTag) nbt))) : DataResult.success(listTag.stream());
        } else if(tag instanceof LongArrayBinaryTag array) {
            return DataResult.success(Arrays.stream(array.value()).mapToObj(LongBinaryTag::longBinaryTag));
        } else if(tag instanceof IntArrayBinaryTag array) {
            return DataResult.success(Arrays.stream(array.value()).mapToObj(IntBinaryTag::intBinaryTag));
        } else if(tag instanceof ByteArrayBinaryTag array) {
            return DataResult.success(Arrays.stream(ArrayUtils.toObject(array.value())).map(ByteBinaryTag::byteBinaryTag));
        } else {
            return DataResult.error(() -> "Invalid tag provided: " + tag);
        }
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(BinaryTag input) {
        if(input instanceof ByteArrayBinaryTag array) {
            return DataResult.success(ByteBuffer.wrap(array.value().clone()));
        }
        return DynamicOps.super.getByteBuffer(input);
    }

    @Override
    public BinaryTag createByteList(ByteBuffer input) {
        ByteBuffer byteBuffer2 = input.duplicate().clear();
        byte[] bs = new byte[input.capacity()];
        byteBuffer2.get(0, bs, 0, bs.length);
        return ByteArrayBinaryTag.byteArrayBinaryTag(bs);
    }

    @Override
    public DataResult<IntStream> getIntStream(BinaryTag input) {
        if(input instanceof IntArrayBinaryTag array) {
            return DataResult.success(Arrays.stream(array.value().clone()));
        }
        return DynamicOps.super.getIntStream(input);
    }

    @Override
    public BinaryTag createIntList(IntStream input) {
        return IntArrayBinaryTag.intArrayBinaryTag(input.toArray());
    }

    @Override
    public DataResult<LongStream> getLongStream(BinaryTag input) {
        if(input instanceof LongArrayBinaryTag array) {
            return DataResult.success(Arrays.stream(array.value().clone()));
        }
        return DynamicOps.super.getLongStream(input);
    }

    @Override
    public BinaryTag createLongList(LongStream input) {
        return LongArrayBinaryTag.longArrayBinaryTag(input.toArray());
    }

    @Override
    public BinaryTag createList(Stream<BinaryTag> input) {
        Iterator<BinaryTag> it = input.iterator();

        BinaryTag firstElement = it.next();
        if(firstElement instanceof CompoundBinaryTag) {
            return createListHeterogenous(List.of(firstElement), it);
        } else if(firstElement instanceof ByteBinaryTag) {
            return createListBytes(List.of(firstElement), it);
        } else if(firstElement instanceof IntBinaryTag) {
            return createListInts(List.of(firstElement), it);
        } else if(firstElement instanceof LongBinaryTag) {
            return createListLongs(List.of(firstElement), it);
        } else {
            return createListHomogenous(List.of(firstElement), it);
        }
    }

    private BinaryTag createListBytes(List<BinaryTag> previous, Iterator<BinaryTag> input) {
        ByteArrayList list = new ByteArrayList(previous.size());

        for (BinaryTag nbt : previous) {
            if(!(nbt instanceof ByteBinaryTag)) {
                return createListHomogenous(previous, input);
            }

            list.add(((ByteBinaryTag) nbt).value());
        }

        while (input.hasNext()) {
            BinaryTag nbt = input.next();

            if(!(nbt instanceof ByteBinaryTag)) {
                List<BinaryTag> newPrevious = new ArrayList<>(list.size());
                for (Byte b : list) {
                    newPrevious.add(ByteBinaryTag.byteBinaryTag(b));
                }
                return createListHomogenous(newPrevious, input);
            }

            list.add(((ByteBinaryTag) nbt).value());
        }

        return ByteArrayBinaryTag.byteArrayBinaryTag(list.toByteArray());
    }

    private BinaryTag createListInts(List<BinaryTag> previous, Iterator<BinaryTag> input) {
        IntArrayList list = new IntArrayList(previous.size());

        for (BinaryTag nbt : previous) {
            if(!(nbt instanceof IntBinaryTag)) {
                return createListHomogenous(previous, input);
            }

            list.add(((IntBinaryTag) nbt).value());
        }

        while (input.hasNext()) {
            BinaryTag nbt = input.next();

            if(!(nbt instanceof IntBinaryTag)) {
                List<BinaryTag> newPrevious = new ArrayList<>(list.size());
                for (int b : list) {
                    newPrevious.add(IntBinaryTag.intBinaryTag(b));
                }
                return createListHomogenous(newPrevious, input);
            }

            list.add(((IntBinaryTag) nbt).value());
        }

        return IntArrayBinaryTag.intArrayBinaryTag(list.toIntArray());
    }

    private BinaryTag createListLongs(List<BinaryTag> previous, Iterator<BinaryTag> input) {
        LongArrayList list = new LongArrayList(previous.size());

        for (BinaryTag nbt : previous) {
            if(!(nbt instanceof LongBinaryTag)) {
                return createListHomogenous(previous, input);
            }

            list.add(((LongBinaryTag) nbt).value());
        }

        while (input.hasNext()) {
            BinaryTag nbt = input.next();

            if(!(nbt instanceof LongBinaryTag)) {
                List<BinaryTag> newPrevious = new ArrayList<>(list.size());
                for (long b : list) {
                    newPrevious.add(LongBinaryTag.longBinaryTag(b));
                }
                return createListHomogenous(newPrevious, input);
            }

            list.add(((LongBinaryTag) nbt).value());
        }

        return LongArrayBinaryTag.longArrayBinaryTag(list.toLongArray());
    }

    private BinaryTag createListHeterogenous(List<BinaryTag> previous, Iterator<BinaryTag> input) {
        MutableNBTList list = new MutableNBTList();
        previous.forEach(nbt -> list.add(wrapIfNeeded(nbt)));

        while (input.hasNext()) {
            BinaryTag nbt = input.next();

            list.add(wrapIfNeeded(nbt));
        }

        return ListBinaryTag.listBinaryTag(list.getType(), list.getList());
    }

    private static boolean isWrapper(CompoundBinaryTag nbt) {
        return nbt.size() == 1 && nbt.keySet().contains("");
    }

    private static BinaryTag wrapIfNeeded(BinaryTag value) {
        if (value instanceof CompoundBinaryTag compoundTag) {
            if (!isWrapper(compoundTag)) {
                return compoundTag;
            }
        }

        return wrapElement(value);
    }

    private static CompoundBinaryTag wrapElement(BinaryTag value) {
        return CompoundBinaryTag.builder().put("", value).build();
    }

    private BinaryTag createListHomogenous(List<BinaryTag> previous, Iterator<BinaryTag> input) {
        MutableNBTList list = new MutableNBTList();
        previous.forEach(list::add);

        while (input.hasNext()) {
            BinaryTag nbt = input.next();

            if(list.getType() != BinaryTagTypes.END && list.getType() != nbt.type()) {
                return createListHomogenous(list.getList(), input);
            } else {
                list.add(nbt);
            }

        }

        return ListBinaryTag.listBinaryTag(list.getType(), list.getList());
    }

    @Override
    public BinaryTag remove(BinaryTag input, String key) {
        if(input instanceof CompoundBinaryTag inp) {
            CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
            inp.keySet().stream().filter((k) -> !Objects.equals(k, key)).forEach((k) -> builder.put(k, Objects.requireNonNull(inp.get(k))));
            return builder.build();
        }
        return input;
    }

}
