package com.example.snowstorm.network;

import com.example.snowstorm.SnowstormMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StormPayload(boolean active) implements CustomPayload {
    public static final Id<StormPayload> ID = new Id<>(Identifier.of(SnowstormMod.MOD_ID, "storm_state"));
    public static final PacketCodec<RegistryByteBuf, StormPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value.active()),
            buf -> new StormPayload(buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
