package earth.terrarium.cadmus.client;

import com.mojang.blaze3d.platform.InputConstants;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.compat.prometheus.PrometheusClientCompat;
import earth.terrarium.cadmus.common.claims.ClaimSaveData;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import it.unimi.dsi.fastutil.objects.ObjectCharPair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CadmusClient {

    public static final Map<UUID, ObjectCharPair<String>> TEAM_INFO = new HashMap<>();

    public static final KeyMapping KEY_OPEN_CLAIM_MAP = new KeyMapping(
        ConstantComponents.OPEN_CLAIM_MAP_KEY.getString(),
        InputConstants.KEY_M,
        ConstantComponents.PROJECT_ODYSSEY_CATEGORY.getString());

    public static void init() {
        if (Cadmus.IS_PROMETHEUS_LOADED) {
            PrometheusClientCompat.init();
        }
    }

    public static void onClientTick() {
        if (KEY_OPEN_CLAIM_MAP.consumeClick()) {
            openClaimMap();
        }
    }


    public static void onPlayerLoggedOut() {
        ClaimSaveData.clearClientClaims();
        TEAM_INFO.clear();
    }

    public static void openClaimMap() {
        // TODO
    }

    public static void onEnterSection() {
        // TODO
    }

    @NotNull
    public static Level level() {
        return Objects.requireNonNull(Minecraft.getInstance().level);
    }

    public static void sendCommand(String command) {
        Objects.requireNonNull(Minecraft.getInstance().getConnection()).sendUnsignedCommand(command);
    }
}
