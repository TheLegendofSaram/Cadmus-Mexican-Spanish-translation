package earth.terrarium.cadmus.common.util;

import earth.terrarium.cadmus.common.claims.ClaimChunkSaveData;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.LastMessageHolder;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.client.SyncClaimedChunksPacket;
import earth.terrarium.cadmus.common.team.Team;
import earth.terrarium.cadmus.common.team.TeamSaveData;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModUtils {
    public static <T> T generate(Predicate<T> validator, Supplier<T> getter) {
        T value;
        do {
            value = getter.get();
        } while (!validator.test(value));
        return value;
    }

    public static void enterChunkSection(ServerPlayer player) {
        displayTeamName(player);
        var info = ClaimChunkSaveData.get(player);
        if (info == null) return;
        NetworkHandler.CHANNEL.sendToPlayer(new SyncClaimedChunksPacket(player.chunkPosition(), info), player);
    }

    public static void displayTeamName(ServerPlayer player) {
        if (!(player instanceof LastMessageHolder holder)) return;

        var team = Optionull.mapOrDefault(ClaimChunkSaveData.get(player), ClaimInfo::team, new Team(null, null, null, ""));
        String name = team.name();
        String lastMessage = holder.cadmus$getLastMessage();

        if (Objects.equals(name, lastMessage)) return;
        holder.cadmus$setLastMessage(name);
        if (team.creator() == null) {
            player.displayClientMessage(ConstantComponents.WILDERNESS, true);
        } else {
            Team playerTeam = TeamSaveData.getPlayerTeam(player);
            ChatFormatting color = playerTeam == null ? ChatFormatting.DARK_RED : playerTeam.teamId().equals(team.teamId()) ? ChatFormatting.AQUA : ChatFormatting.DARK_RED;
            player.displayClientMessage(Component.nullToEmpty(name).copy().withStyle(color), true);
        }
    }
}
