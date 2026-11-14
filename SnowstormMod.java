package com.example.snowstorm;

import com.example.snowstorm.network.StormPayload;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SnowstormMod implements ModInitializer {
    public static final String MOD_ID = "snowstorm";
    private static boolean active = false;
    private static int remainingTicks = 0;
    private static int randomCooldown = 20 * 60 * 8;
    private static int stormDurationSeconds = 300;
    private static final Map<UUID, Integer> exposure = new HashMap<>();
    private static final Map<UUID, Integer> damageClock = new HashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(StormPayload.ID, StormPayload.CODEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("snowstorm")
                        .then(literal("start").executes(context -> {
                            start(context.getSource().getServer());
                            context.getSource().sendFeedback(() -> Text.literal("Снежная буря запущена."), true);
                            return 1;
                        }).then(argument("seconds", IntegerArgumentType.integer(30, 3600)).executes(context -> {
                            stormDurationSeconds = IntegerArgumentType.getInteger(context, "seconds");
                            start(context.getSource().getServer());
                            context.getSource().sendFeedback(() -> Text.literal("Снежная буря запущена на " + stormDurationSeconds + " сек."), true);
                            return 1;
                        })))
                        .then(literal("stop").executes(context -> {
                            stop(context.getSource().getServer());
                            context.getSource().sendFeedback(() -> Text.literal("Снежная буря остановлена."), true);
                            return 1;
                        }))
                        .then(literal("status").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal(active ? "Сейчас идёт снежная буря." : "Сейчас бури нет."), false);
                            return 1;
                        }))
        ));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ServerPlayNetworking.send(handler.getPlayer(), new StormPayload(active)));

        ServerTickEvents.END_SERVER_TICK.register(SnowstormMod::tick);
    }

    private static void tick(MinecraftServer server) {
        if (active) {
            remainingTicks--;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                applyCold(player);
            }
            if (remainingTicks <= 0) stop(server);
        } else {
            randomCooldown--;
            if (randomCooldown <= 0) {
                stormDurationSeconds = 300 + server.getOverworld().random.nextInt(301);
                start(server);
            }
        }
    }

    private static void applyCold(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        boolean exposed = world.isSkyVisible(BlockPos.ofFloored(player.getX(), player.getEyeY(), player.getZ()));

        if (exposed) {
            int ticks = exposure.getOrDefault(id, 0) + 1;
            exposure.put(id, ticks);
            player.setFrozenTicks(Math.min(player.getMinFreezeDamageTicks() + 80, player.getFrozenTicks() + 2));
            int clock = damageClock.getOrDefault(id, 0) + 1;
            if (ticks > 100 && clock >= 40) {
                player.damage(world, world.getDamageSources().freeze(), 1.0F);
                clock = 0;
            }
            damageClock.put(id, clock);
        } else {
            exposure.put(id, Math.max(0, exposure.getOrDefault(id, 0) - 3));
            damageClock.put(id, 0);
            player.setFrozenTicks(Math.max(0, player.getFrozenTicks() - 3));
        }
    }

    private static void start(MinecraftServer server) {
        if (active) return;
        active = true;
        remainingTicks = stormDurationSeconds * 20;
        broadcastTitle(server, "Буря начинается");
        sync(server);
    }

    private static void stop(MinecraftServer server) {
        if (!active) return;
        active = false;
        remainingTicks = 0;
        exposure.clear();
        damageClock.clear();
        randomCooldown = 20 * (60 * 15 + server.getOverworld().random.nextInt(60 * 16));
        broadcastTitle(server, "Буря кончилась");
        sync(server);
    }

    private static void sync(MinecraftServer server) {
        StormPayload payload = new StormPayload(active);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private static void broadcastTitle(MinecraftServer server, String message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 60, 20));
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(message)));
        }
    }
}
