package earth.terrarium.cadmus.common.claims;

import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.utils.SaveHandler;
import earth.terrarium.cadmus.Cadmus;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClaimHandler extends SaveHandler {

    public static final String PLAYER_PREFIX = "p:";
    public static final String TEAM_PREFIX = "t:";
    public static final String ADMIN_PREFIX = "a:";

    private final Map<ChunkPos, Pair<String, ClaimType>> claims = new HashMap<>();
    private final Map<String, Map<ChunkPos, ClaimType>> claimsById = new HashMap<>();

    private final ClaimListenHandler listenHandler;

    private ClaimHandler(ResourceKey<Level> dimension) {
        this.listenHandler = new ClaimListenHandler(dimension);
    }

    private void loadLegacyTeam(String id, CompoundTag tag) {
        Map<ChunkPos, ClaimType> claimData = new HashMap<>();
        tag.getAllKeys().forEach(chunkPos -> {
            ChunkPos pos = new ChunkPos(Long.parseLong(chunkPos));
            ClaimType type = ClaimType.values()[tag.getByte(chunkPos)];
            claimData.put(pos, type);
        });
        claimsById.put(id, claimData);
    }

    private void loadTeam(String id, long[] values) {
        Map<ChunkPos, ClaimType> claimData = new HashMap<>();
        for (long value : values) {
            int x = BlockPos.getX(value);
            int z = BlockPos.getZ(value);
            int type = BlockPos.getY(value);
            claimData.put(new ChunkPos(x, z), ClaimType.values()[type]);
        }
        claimsById.put(id, claimData);
    }

    @Override
    public void loadData(CompoundTag tag) {
        tag.getAllKeys().forEach(id -> {
            if (id.startsWith(PLAYER_PREFIX) || id.startsWith(TEAM_PREFIX) || id.startsWith(ADMIN_PREFIX)) {
                if (tag.getTagType(id) == Tag.TAG_LONG_ARRAY) {
                    loadTeam(id, tag.getLongArray(id));
                } else {
                    loadLegacyTeam(id, tag.getCompound(id));
                }
            }
        });

        updateInternal();
    }

    @Override
    public void saveData(CompoundTag tag) {
        claimsById.forEach((id, claimData) -> {
            long[] values = new long[claimData.size()];
            int i = 0;
            for (Map.Entry<ChunkPos, ClaimType> entry : claimData.entrySet()) {
                ChunkPos pos = entry.getKey();
                ClaimType type = entry.getValue();
                values[i] = BlockPos.asLong(pos.x, type.ordinal(), pos.z);
                i++;
            }
            tag.put(id, new LongArrayTag(values));
        });
    }

    public static ClaimHandler read(ServerLevel level) {
        return read(level.getDataStorage(), () -> new ClaimHandler(level.dimension()), "cadmus_claims");
    }

    public static void claim(ServerLevel level, String id, ChunkPos pos, ClaimType type) {
        var data = read(level);
        if (data.claims.containsKey(pos) && !data.claims.get(pos).getFirst().equals(id)) return;
        if (type == ClaimType.CHUNK_LOADED) {
            level.getChunkSource().updateChunkForced(pos, true);
            Cadmus.FORCE_LOADED_CHUNK_COUNT++;
        }

        data.listenHandler.addClaims(level, id, Set.of(pos));
        data.claims.put(pos, Pair.of(id, type));
        var currentClaims = data.claimsById.getOrDefault(id, new HashMap<>());
        currentClaims.put(pos, type);
        data.claimsById.put(id, currentClaims);
    }

    public static void unclaim(ServerLevel level, String id, ChunkPos pos) {
        var data = read(level);
        var claim = data.claims.get(pos);
        if (claim == null) return;
        if (!claim.getFirst().equals(id)) return;
        if (claim.getSecond() == ClaimType.CHUNK_LOADED) {
            level.getChunkSource().updateChunkForced(pos, false);
            Cadmus.FORCE_LOADED_CHUNK_COUNT--;
        }

        data.listenHandler.removeClaims(level, id, Set.of(pos));
        data.claims.remove(pos);
        data.claimsById.get(id).remove(pos);
    }

    public static void clear(ServerLevel level, String id) {
        var data = read(level);
        if (data.claimsById.containsKey(id)) {
            data.listenHandler.removeClaims(level, id, data.claimsById.get(id).keySet());
        }
        data.claimsById.remove(id);
        data.updateInternal();
    }

    public static void clearAll(ServerLevel level) {
        var data = read(level);
        var teams = new HashSet<>(data.claimsById.keySet());
        teams.forEach(id -> clear(level, id));
    }

    @Nullable
    public static Pair<String, ClaimType> getClaim(ServerLevel level, ChunkPos pos) {
        return read(level).claims.get(pos);
    }

    @Nullable
    public static Map<ChunkPos, ClaimType> getTeamClaims(ServerLevel level, String id) {
        return read(level).claimsById.get(id);
    }

    public static Map<String, Map<ChunkPos, ClaimType>> getAllTeamClaims(ServerLevel level) {
        return read(level).claimsById;
    }

    public static ClaimListenHandler getListener(ServerLevel level) {
        return read(level).listenHandler;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    private void updateInternal() {
        claims.clear();
        claimsById.forEach((id, claimData) -> claimData.forEach((pos, type) -> claims.put(pos, Pair.of(id, type))));
    }
}
