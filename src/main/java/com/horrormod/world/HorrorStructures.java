package com.horrormod.world;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public final class HorrorStructures {
    public static final StructureType<ObeliskStructure> OBELISK_TYPE = () -> ObeliskStructure.CODEC;
    public static final StructurePieceType OBELISK_PIECE = ObeliskPiece::load;
    public static final StructureType<AbandonedHouseStructure> ABANDONED_HOUSE_TYPE = () -> AbandonedHouseStructure.CODEC;
    public static final StructurePieceType ABANDONED_HOUSE_PIECE = AbandonedHousePiece::load;

    private HorrorStructures() {
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.STRUCTURE_TYPE, HorrorMod.id("obelisk"), OBELISK_TYPE);
        Registry.register(BuiltInRegistries.STRUCTURE_PIECE, HorrorMod.id("obelisk"), OBELISK_PIECE);
        Registry.register(BuiltInRegistries.STRUCTURE_TYPE, HorrorMod.id("abandoned_house"), ABANDONED_HOUSE_TYPE);
        Registry.register(BuiltInRegistries.STRUCTURE_PIECE, HorrorMod.id("abandoned_house"), ABANDONED_HOUSE_PIECE);
    }
}
