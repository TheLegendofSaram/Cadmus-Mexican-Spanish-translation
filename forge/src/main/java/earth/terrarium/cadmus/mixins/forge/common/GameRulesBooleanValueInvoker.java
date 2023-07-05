package earth.terrarium.cadmus.mixins.forge.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.BiConsumer;

@Mixin(GameRules.BooleanValue.class)

public interface GameRulesBooleanValueInvoker {
    @Invoker
    static GameRules.Type<GameRules.BooleanValue> invokeCreate(boolean bl, BiConsumer<MinecraftServer, GameRules.BooleanValue> biConsumer) {
        throw new UnsupportedOperationException();
    }
}
