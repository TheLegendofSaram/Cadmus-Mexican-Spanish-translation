package earth.terrarium.cadmus.api.claims;

import earth.terrarium.cadmus.api.ApiHelper;
import earth.terrarium.cadmus.api.teams.TeamApi;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface ClaimApi {

    ClaimApi API = ApiHelper.load(ClaimApi.class);

    /**
     * Claims a chunk.
     *
     * @param level     The level to claim.
     * @param id        The team ID.
     * @param pos       The chunk position to claim.
     * @param chunkLoad If the chunk should be chunk loaded.
     */
    void claim(Level level, UUID id, ChunkPos pos, boolean chunkLoad);

    /**
     * Claims a set of chunks.
     *
     * @param level     The level to claim.
     * @param id        The team ID.
     * @param positions The positions mapped to chunk load status.
     */
    void claim(Level level, UUID id, Object2BooleanMap<ChunkPos> positions);

    /**
     * Claims a chunk. If the player is on a team, use the team ID, otherwise use the player's UUID.
     *
     * @param player    The claimer.
     * @param pos       The chunk position to claim.
     * @param chunkLoad If the chunk should be chunk loaded.
     */
    default void claim(@NotNull ServerPlayer player, ChunkPos pos, boolean chunkLoad) {
        this.claim(player.serverLevel(), TeamApi.API.getId(player), pos, chunkLoad);
    }

    /**
     * Claims a set of chunks. If the player is on a team, use the team ID, otherwise use the player's UUID.
     *
     * @param player    The claimer.
     * @param positions The positions mapped to chunk load status.
     */
    default void claim(@NotNull ServerPlayer player, Object2BooleanMap<ChunkPos> positions) {
        this.claim(player.serverLevel(), TeamApi.API.getId(player), positions);
    }

    /**
     * Unclaims a chunk.
     *
     * @param level The level.
     * @param id    The team ID.
     * @param pos   The chunk position to unclaim.
     */
    void unclaim(Level level, UUID id, ChunkPos pos);

    /**
     * Unclaims a set of chunks.
     *
     * @param level     The level.
     * @param id        The team ID.
     * @param positions The chunk position to unclaim.
     */
    void unclaim(Level level, UUID id, Set<ChunkPos> positions);

    /**
     * Unclaims a chunk. If the player is on a team, use the team ID, otherwise use the player's UUID.
     *
     * @param player The unclaimer.
     * @param pos    The chunk position to unclaim.
     */
    default void unclaim(@NotNull ServerPlayer player, ChunkPos pos) {
        this.unclaim(player.serverLevel(), TeamApi.API.getId(player), pos);
    }

    /**
     * Unclaims a set of chunks. If the player is on a team, use the team ID, otherwise use the player's UUID.
     *
     * @param player    The unclaimer.
     * @param positions The chunk positions to unclaim.
     */
    default void unclaim(@NotNull ServerPlayer player, Set<ChunkPos> positions) {
        this.unclaim(player.serverLevel(), TeamApi.API.getId(player), positions);
    }

    /**
     * Clears all claims in the level for the given team.
     *
     * @param level The level.
     * @param id    The team ID.
     */
    void clear(Level level, UUID id);

    /**
     * Completely clears all claims in every level.
     *
     * @param server The server.
     */
    void clearAll(MinecraftServer server);

    /**
     * Clears all claims in the level for the given player or their team if they're in one.
     *
     * @param player The team member.
     */
    default void clear(@NotNull Player player) {
        this.clear(player.level(), TeamApi.API.getId(player));
    }

    /**
     * Gets a claim.
     *
     * @param level The level to get the claim from.
     * @param pos   The chunk position to get the claim from.
     * @return The claim ID and chunk load status if the chunk is claimed, empty otherwise.
     */
    Optional<ObjectBooleanPair<UUID>> getClaim(Level level, ChunkPos pos);

    /**
     * Checks if a chunk is claimed.
     *
     * @param level The level to check the claim from.
     * @param pos   The chunk position to check the claim from.
     * @return True if the chunk is claimed, false otherwise.
     */
    default boolean isClaimed(Level level, ChunkPos pos) {
        return this.getClaim(level, pos).isPresent();
    }

    /**
     * Checks if a block is claimed.
     *
     * @param level The level to check the claim from.
     * @param pos   The block position to check the claim from.
     * @return True if the chunk is claimed, false otherwise.
     */
    default boolean isClaimed(Level level, BlockPos pos) {
        return isClaimed(level, new ChunkPos(pos));
    }

    /**
     * Checks if the chunk the player is in is claimed.
     *
     * @param player The player to check the claim from.
     * @return True if the chunk is claimed, false otherwise.
     */
    default boolean isClaimed(Player player) {
        return this.isClaimed(player.level(), player.chunkPosition());
    }

    /**
     * Gets all claims within the given chunk positions.
     *
     * @param level     The level to get the claims from.
     * @param positions The chunk positions to get the claim from.
     * @return The claim ID and chunk load status for all claimed chunks.
     */
    List<ObjectBooleanPair<UUID>> getClaims(Level level, Collection<ChunkPos> positions);

    /**
     * Gets all claims for the given team iD.
     *
     * @param level The level to get the claims from.
     * @param id    The team ID.
     * @return A map of chunk positions to chunk load status.
     */
    Optional<Object2BooleanMap<ChunkPos>> getOwnedClaims(Level level, UUID id);

    /**
     * Gets all claims for the given player. If the player is on a team, retrieves the team's claims, otherwise retrieves the player's claims.
     *
     * @param player The player to get the claims from.
     * @return A map of chunk positions to chunk load status.
     */
    default Optional<Object2BooleanMap<ChunkPos>> getOwnedClaims(Player player) {
        return this.getOwnedClaims(player.level(), TeamApi.API.getId(player));
    }

    /**
     * Gets all claims for the given level.
     *
     * @param level The level to get the claims from.
     * @return A map of chunk positions to a pair of claim ID and chunk load status.
     */
    Object2ObjectMap<ChunkPos, ObjectBooleanPair<UUID>> getAllClaims(ServerLevel level);

    /**
     * Gets all claims for the given level.
     *
     * @param level The level to get the claims from.
     * @return A map of chunk positions to a pair of claim ID and chunk load status.
     */
    Object2ObjectMap<UUID, Object2BooleanMap<ChunkPos>> getAllClaimsByOwner(ServerLevel level);

    /**
     * Gets a claim that has been synced to the client for the given level.
     *
     * @param level The level to get the claim from.
     * @param pos   The chunk position to get the claim from.
     * @return The claim ID and chunk load status if the chunk is claimed, empty otherwise.
     */
    Optional<ObjectBooleanPair<UUID>> getClientClaim(ResourceKey<Level> level, ChunkPos pos);

    /**
     * Gets all claims that have been synced to the client for the given level.
     *
     * @param level The level to get the claims from.
     * @return A map of chunk positions to a pair of claim ID and chunk load status.
     */
    Object2ObjectMap<ChunkPos, ObjectBooleanPair<UUID>> getAllClientClaims(ResourceKey<Level> level);
}
