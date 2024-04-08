package earth.terrarium.cadmus.common.protections.types;

import earth.terrarium.cadmus.api.flags.types.BooleanFlag;
import earth.terrarium.cadmus.api.protections.Protection;
import earth.terrarium.cadmus.common.flags.Flags;
import earth.terrarium.cadmus.common.protections.ClaimSettings;
import earth.terrarium.cadmus.common.tags.ModItemTags;
import earth.terrarium.cadmus.common.utils.CadmusGameRules;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

public final class ItemPickupProtection implements Protection {

    @Override
    public String setting() {
        return ClaimSettings.CAN_PICKUP_ITEMS;
    }

    @Override
    public String permission() {
        return "cadmus.item_pickup";
    }

    @Override
    public String personalPermission() {
        return "cadmus.personal.item_pickup";
    }

    @Override
    public BooleanFlag flag() {
        return Flags.ITEM_PICKUP;
    }

    @Override
    public GameRules.Key<GameRules.BooleanValue> gameRule() {
        return CadmusGameRules.DO_CLAIMED_ITEM_PICKUP;
    }

    public boolean canPickupItem(Player player, ItemEntity item) {
        if (item.getItem().is(ModItemTags.ALLOWS_CLAIM_PICKUP)) return true;
        return item.getOwner() == player ||
            player.level().isClientSide() ||
            getId(player.level(), item.chunkPosition()).map(id ->
                isPlayerAllowed(player, id)).orElse(true);
    }
}
