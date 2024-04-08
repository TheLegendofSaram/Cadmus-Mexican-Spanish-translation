package earth.terrarium.cadmus.common.network.packets;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.client.CadmusClient;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;

public record ClientboundSyncClaimsPacket(
    ResourceKey<Level> dimension,
    Map<UUID, Object2BooleanMap<ChunkPos>> claims
) implements Packet<ClientboundSyncClaimsPacket> {

    public static final ClientboundPacketType<ClientboundSyncClaimsPacket> TYPE = new Type();

    @Override
    public PacketType<ClientboundSyncClaimsPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundSyncClaimsPacket> implements ClientboundPacketType<ClientboundSyncClaimsPacket> {

        public Type() {
            super(
                ClientboundSyncClaimsPacket.class,
                new ResourceLocation(Cadmus.MOD_ID, "sync_claims"),
                ObjectByteCodec.create(
                    ExtraByteCodecs.resourceKey(Registries.DIMENSION).fieldOf(ClientboundSyncClaimsPacket::dimension),
                    new MapCodec<>(ByteCodec.UUID,
                        new MapCodec<>(ExtraByteCodecs.CHUNK_POS, ByteCodec.BOOLEAN)
                            .map(map -> (Object2BooleanMap<ChunkPos>) new Object2BooleanOpenHashMap<>(map), map -> map
                            )).fieldOf(ClientboundSyncClaimsPacket::claims),
                    ClientboundSyncClaimsPacket::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundSyncClaimsPacket packet) {
            return () -> packet.claims.forEach((id, claims) ->
                ClaimApi.API.claim(CadmusClient.level(), id, claims));
        }
    }
}
