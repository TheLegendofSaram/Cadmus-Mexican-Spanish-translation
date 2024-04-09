package earth.terrarium.cadmus.common.claims;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.api.events.CadmusEvents;
import earth.terrarium.cadmus.api.flags.FlagApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.*;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class ClaimApiImpl implements ClaimApi {

    @Override
    public void claim(Level level, UUID id, ChunkPos pos, boolean chunkLoad) {
        if (chunkLoad) {
            level.getChunkSource().updateChunkForced(pos, true);
            Cadmus.FORCE_LOADED_CHUNK_COUNT++;
        }

        var data = ClaimSaveData.read(level);
        data.claims().put(pos, ObjectBooleanPair.of(id, chunkLoad));
        data.claimsById().computeIfAbsent(id, uuid -> new Object2BooleanOpenHashMap<>()).put(pos, chunkLoad);

        if (level instanceof ServerLevel serverLevel) {
            data.setDirty();
            NetworkHandler.sendToAllClientPlayers(new ClientboundAddClaimPacket(id, pos, chunkLoad), serverLevel.getServer());
            TeamApi.API.displayTeamNameToAll(serverLevel.getServer());
            TeamApi.API.syncTeamInfo(serverLevel.getServer(), id, false);
        }
        CadmusEvents.AddClaimsEvent.fire(level, id, Object2BooleanMaps.singleton(pos, chunkLoad));
    }

    @Override
    public void claim(Level level, UUID id, Object2BooleanMap<ChunkPos> positions) {
        positions.forEach((pos, chunkLoad) -> {
            if (chunkLoad) {
                level.getChunkSource().updateChunkForced(pos, true);
                Cadmus.FORCE_LOADED_CHUNK_COUNT++;
            }
        });

        var data = ClaimSaveData.read(level);
        for (var entry : positions.object2BooleanEntrySet()) {
            ChunkPos pos = entry.getKey();
            boolean chunkLoad = entry.getBooleanValue();
            data.claims().put(pos, ObjectBooleanPair.of(id, chunkLoad));
            data.claimsById().computeIfAbsent(id, uuid -> new Object2BooleanOpenHashMap<>()).put(pos, chunkLoad);
        }

        if (level instanceof ServerLevel serverLevel) {
            data.setDirty();
            NetworkHandler.sendToAllClientPlayers(new ClientboundAddClaimsPacket(id, positions), serverLevel.getServer());
            TeamApi.API.displayTeamNameToAll(serverLevel.getServer());
            TeamApi.API.syncTeamInfo(serverLevel.getServer(), id, false);
        }
        CadmusEvents.AddClaimsEvent.fire(level, id, positions);
    }

    @Override
    public void unclaim(Level level, UUID id, ChunkPos pos) {
        if (getClaim(level, pos).map(ObjectBooleanPair::rightBoolean).orElse(false)) {
            level.getChunkSource().updateChunkForced(pos, false);
            Cadmus.FORCE_LOADED_CHUNK_COUNT--;
        }

        var data = ClaimSaveData.read(level);
        data.claims().remove(pos);
        data.claimsById().get(id).removeBoolean(pos);

        if (level instanceof ServerLevel serverLevel) {
            data.setDirty();
            NetworkHandler.sendToAllClientPlayers(new ClientboundRemoveClaimPacket(id, pos), serverLevel.getServer());
            TeamApi.API.displayTeamNameToAll(serverLevel.getServer());
            TeamApi.API.syncTeamInfo(serverLevel.getServer(), id, false);
        }
        CadmusEvents.RemoveClaimsEvent.fire(level, id, Set.of(pos));
    }

    @Override
    public void unclaim(Level level, UUID id, Set<ChunkPos> positions) {
        for (var pos : positions) {
            if (getClaim(level, pos).map(ObjectBooleanPair::rightBoolean).orElse(false)) {
                level.getChunkSource().updateChunkForced(pos, false);
                Cadmus.FORCE_LOADED_CHUNK_COUNT--;
            }
        }

        var data = ClaimSaveData.read(level);
        for (var pos : positions) {
            data.claims().remove(pos);
            data.claimsById().get(id).removeBoolean(pos);
        }
        if (level instanceof ServerLevel serverLevel) {
            data.setDirty();
            NetworkHandler.sendToAllClientPlayers(new ClientboundRemoveClaimsPacket(id, positions), serverLevel.getServer());
            TeamApi.API.displayTeamNameToAll(serverLevel.getServer());
            TeamApi.API.syncTeamInfo(serverLevel.getServer(), id, false);
        }
        CadmusEvents.RemoveClaimsEvent.fire(level, id, positions);
    }

    @Override
    public void clear(Level level, UUID id) {
        getOwnedClaims(level, id).ifPresent(claims -> claims.forEach((pos, chunkLoad) -> {
            if (chunkLoad) {
                level.getChunkSource().updateChunkForced(pos, false);
                Cadmus.FORCE_LOADED_CHUNK_COUNT--;
            }
        }));

        var data = ClaimSaveData.read(level);
        data.claims().values().removeIf(claim -> claim.left().equals(id));
        data.claimsById().remove(id);

        if (level instanceof ServerLevel serverLevel) {
            data.setDirty();
            NetworkHandler.sendToAllClientPlayers(new ClientboundClearClaimsPacket(id), serverLevel.getServer());
            TeamApi.API.displayTeamNameToAll(serverLevel.getServer());
            TeamApi.API.syncTeamInfo(serverLevel.getServer(), id, false);
        }
        CadmusEvents.ClearClaimsEvent.fire(level, id);
    }

    @Override
    public void clearAll(MinecraftServer server) {
        CadmusSaveData.clearAll(server);
        FlagApi.API.clearAll(server);
        server.getAllLevels().forEach(level -> {
            getAllClaimsByOwner(level).forEach((id, claims) -> {
                claims.forEach((pos, chunkLoad) -> {
                    if (chunkLoad) {
                        level.getChunkSource().updateChunkForced(pos, false);
                        Cadmus.FORCE_LOADED_CHUNK_COUNT--;
                    }
                });
                NetworkHandler.sendToAllClientPlayers(new ClientboundClearClaimsPacket(id), server);
                CadmusEvents.ClearClaimsEvent.fire(level, id);
            });

            var data = ClaimSaveData.read(level);
            data.claims().clear();
            data.claimsById().clear();
            data.setDirty();
        });
        TeamApi.API.displayTeamNameToAll(server);
        TeamApi.API.syncAllTeamInfo(server);
    }

    @Override
    public Optional<ObjectBooleanPair<UUID>> getClaim(Level level, ChunkPos pos) {
        var data = ClaimSaveData.read(level);
        return Optional.ofNullable(data.claims().get(pos));
    }

    @Override
    public List<ObjectBooleanPair<UUID>> getClaims(Level level, Collection<ChunkPos> positions) {
        var data = ClaimSaveData.read(level);
        List<ObjectBooleanPair<UUID>> results = new ArrayList<>();

        for (var pos : positions) {
            var claim = data.claims().get(pos);
            if (claim != null) {
                results.add(claim);
            }
        }
        return results;
    }

    @Override
    public Optional<Object2BooleanMap<ChunkPos>> getOwnedClaims(Level level, UUID id) {
        var data = ClaimSaveData.read(level);
        return Optional.ofNullable(data.claimsById().get(id));
    }

    @Override
    public Object2ObjectMap<ChunkPos, ObjectBooleanPair<UUID>> getAllClaims(ServerLevel level) {
        return ClaimSaveData.read(level).claims();
    }

    @Override
    public Object2ObjectMap<UUID, Object2BooleanMap<ChunkPos>> getAllClaimsByOwner(ServerLevel level) {
        return ClaimSaveData.read(level).claimsById();
    }

    @Override
    public Optional<ObjectBooleanPair<UUID>> getClientClaim(ResourceKey<Level> level, ChunkPos pos) {
        return Optional.ofNullable(ClaimSaveData.readClient(level).claims().get(pos));
    }

    @Override
    public Object2ObjectMap<ChunkPos, ObjectBooleanPair<UUID>> getAllClientClaims(ResourceKey<Level> level) {
        return ClaimSaveData.readClient(level).claims();
    }
}
