package ca.spottedleaf.dataconverter.minecraft.versions;

import ca.spottedleaf.dataconverter.converters.DataConverter;
import ca.spottedleaf.dataconverter.minecraft.MCVersions;
import ca.spottedleaf.dataconverter.minecraft.converters.blockname.ConverterAbstractBlockRename;
import ca.spottedleaf.dataconverter.minecraft.converters.itemname.ConverterAbstractItemRename;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry;
import ca.spottedleaf.dataconverter.minecraft.walkers.generic.DataWalkerTypePaths;
import ca.spottedleaf.dataconverter.types.ListType;
import ca.spottedleaf.dataconverter.types.MapType;
import ca.spottedleaf.dataconverter.types.ObjectType;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;

public final class V1488 {

    private static final int VERSION = MCVersions.V18W19B + 3;

    private static boolean isIglooPiece(final MapType piece) {
        return "Iglu".equals(piece.getString("id"));
    }

    public static void register() {
        ConverterAbstractBlockRename.register(VERSION, new HashMap<>(
                ImmutableMap.of(
                        "minecraft:kelp_top", "minecraft:kelp",
                        "minecraft:kelp", "minecraft:kelp_plant"
                )
        )::get);
        ConverterAbstractItemRename.register(VERSION, new HashMap<>(
                ImmutableMap.of(
                        "minecraft:kelp_top", "minecraft:kelp"
                )
        )::get);

        // Don't ask me why in V1458 they wrote the converter to NOT do command blocks and THEN in THIS version
        // to ONLY do command blocks. I don't know.

        MCTypeRegistry.TILE_ENTITY.addConverterForId("minecraft:command_block", new DataConverter<>(VERSION) {
            @Override
            public MapType convert(final MapType data, final long sourceVersion, final long toVersion) {
                return V1458.updateCustomName(data);
            }
        });

        MCTypeRegistry.ENTITY.addConverterForId("minecraft:commandblock_minecart", new DataConverter<>(VERSION) {
            @Override
            public MapType convert(final MapType data, final long sourceVersion, final long toVersion) {
                return V1458.updateCustomName(data);
            }
        });

        MCTypeRegistry.STRUCTURE_FEATURE.addStructureConverter(new DataConverter<>(VERSION) {
            @Override
            public MapType convert(final MapType data, final long sourceVersion, final long toVersion) {
                final ListType children = data.getList("Children", ObjectType.MAP);
                boolean isIgloo;
                if (children != null) {
                    isIgloo = true;
                    for (int i = 0, len = children.size(); i < len; ++i) {
                        if (!isIglooPiece(children.getMap(i))) {
                            isIgloo = false;
                            break;
                        }
                    }
                } else {
                    isIgloo = false;
                }

                if (isIgloo) {
                    data.remove("Children");
                    data.setString("id", "Igloo");
                    return null;
                }

                if (children != null) {
                    for (int i = 0; i < children.size();) {
                        final MapType child = children.getMap(i);
                        if (isIglooPiece(child)) {
                            children.remove(i);
                            continue;
                        }
                        ++i;
                    }
                }

                return null;
            }
        });

        MCTypeRegistry.TILE_ENTITY.addWalker(VERSION, "minecraft:command_block",
            new DataWalkerTypePaths<>(MCTypeRegistry.DATACONVERTER_CUSTOM_TYPE_COMMAND, "Command")
        );
        MCTypeRegistry.TILE_ENTITY.addWalker(VERSION, "minecraft:command_block", new DataWalkerTypePaths<>(MCTypeRegistry.TEXT_COMPONENT, "CustomName", "LastOutput"));
    }

    private V1488() {}
}
