package earth.terrarium.cadmus.api;

import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

@ApiStatus.Internal
public final class ApiHelper {

    @ApiStatus.Internal
    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load api for " + clazz.getName()));
    }
}