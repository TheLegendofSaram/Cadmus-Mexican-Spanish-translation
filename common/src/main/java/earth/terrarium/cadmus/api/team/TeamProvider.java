package earth.terrarium.cadmus.api.team;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface TeamProvider {
    /**
     * Gets the team members of the creator
     *
     * @param server  the server
     * @param creator the creator
     * @return the team members, returns a list with just the creator if no team is found
     */
    @NotNull
    Set<GameProfile> getTeamMembers(MinecraftServer server, GameProfile creator);

    /**
     * Gets the name of the team of the creator
     *
     * @param server  the server
     * @param creator the creator
     * @return the name of the team, or the creator's name if no team is found
     */
    @NotNull
    String getTeamName(MinecraftServer server, GameProfile creator);
}
