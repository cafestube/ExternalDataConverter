package ca.spottedleaf.dataconverter.types.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;

public interface NBTAdapter {

    BinaryTag getTag();

    BinaryTagType<?> getNBTType();

}
