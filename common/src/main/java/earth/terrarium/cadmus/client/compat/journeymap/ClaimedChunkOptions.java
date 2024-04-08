package earth.terrarium.cadmus.client.compat.journeymap;

import earth.terrarium.cadmus.Cadmus;
import journeymap.client.api.option.BooleanOption;
import journeymap.client.api.option.OptionCategory;

public class ClaimedChunkOptions {

    private final OptionCategory category = new OptionCategory(Cadmus.MOD_ID, "Cadmus", "Cadmus Options");

    public final BooleanOption showClaimedChunks = new BooleanOption(
        category,
        "showClaimedChunks",
        "Show Claimed Chunks",
        true
    );
}
