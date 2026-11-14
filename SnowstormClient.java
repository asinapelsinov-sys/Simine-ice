package com.example.snowstorm.client;

import com.example.snowstorm.network.StormPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;

public class SnowstormClient implements ClientModInitializer {
    private static boolean active = false;
    private static int particleClock = 0;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(StormPayload.ID, (payload, context) ->
                context.client().execute(() -> active = payload.active()));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!active || client.world == null || client.player == null) return;
            particleClock++;
            if (particleClock % 1 != 0) return;

            var player = client.player;
            var world = client.world;
            for (int i = 0; i < 22; i++) {
                double x = player.getX() + (world.random.nextDouble() - 0.5) * 28.0;
                double y = player.getY() + world.random.nextDouble() * 16.0 - 3.0;
                double z = player.getZ() + (world.random.nextDouble() - 0.5) * 28.0;
                world.addParticle(ParticleTypes.SNOWFLAKE, x, y, z,
                        -0.10 + world.random.nextDouble() * 0.05,
                        -0.02 - world.random.nextDouble() * 0.06,
                        0.08 + world.random.nextDouble() * 0.10);
            }
        });

        // A subtle full-screen storm veil. The title and normal HUD remain readable.
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!active || client.world == null) return;
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            drawContext.fill(0, 0, width, height, 0x33090F18);
        });
    }
}
