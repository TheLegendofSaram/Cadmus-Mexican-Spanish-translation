package earth.terrarium.cadmus.common.network.packets;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.client.CadmusClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.Set;
import java.util.UUID;

public record ClientboundRemoveClaimsPacket(
    UUID id,
    Set<ChunkPos> positions
) implements Packet<ClientboundRemoveClaimsPacket> {

    public static final ClientboundPacketType<ClientboundRemoveClaimsPacket> TYPE = new Type();

    @Override
    public PacketType<ClientboundRemoveClaimsPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundRemoveClaimsPacket> implements ClientboundPacketType<ClientboundRemoveClaimsPacket> {

        public Type() {
            super(
                ClientboundRemoveClaimsPacket.class,
                new ResourceLocation(Cadmus.MOD_ID, "remove_claims"),
                ObjectByteCodec.create(
                    ByteCodec.UUID.fieldOf(ClientboundRemoveClaimsPacket::id),
                    ExtraByteCodecs.CHUNK_POS.setOf().fieldOf(ClientboundRemoveClaimsPacket::positions),
                    ClientboundRemoveClaimsPacket::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundRemoveClaimsPacket packet) {
            return () -> ClaimApi.API.unclaim(CadmusClient.level(), packet.id, packet.positions);
        }
    }
}
