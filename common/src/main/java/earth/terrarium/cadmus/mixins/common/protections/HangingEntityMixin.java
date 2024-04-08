package earth.terrarium.cadmus.mixins.common.protections;

import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.protections.Protections;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ArmorStand.class, ItemFrame.class, HangingEntity.class})
public abstract class HangingEntityMixin extends Entity {

    public HangingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // Prevent armor stands and hanging entities from being damaged by projectiles and explosions in protected chunks
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void cadmus$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof Projectile projectile) {
            if (projectile.getOwner() instanceof Player player) {
                if (!Protections.ENTITY_DAMAGE.canDamageEntity(player, this)) {
                    cir.setReturnValue(false);
                }
            } else if (projectile.getOwner() != null && !Protections.MOB_GRIEFING.canMobGrief(projectile.getOwner())) {
                cir.setReturnValue(false);
            }
        } else if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            if (!ClaimApi.API.isClaimed(level(), chunkPosition())) {
                cir.setReturnValue(false);
            }
        }
    }
}
