package earth.terrarium.cadmus.common.protections.types.neoforge;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.protections.Protections;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@Mod.EventBusSubscriber(modid = Cadmus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
final class EntityExplosionProtectionImpl {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onExplode(ExplosionEvent.Detonate event) {
        event.getAffectedEntities().removeIf(entity ->
            !Protections.ENTITY_EXPLOSIONS.canExplodeEntity(entity, event.getExplosion()));
    }
}
