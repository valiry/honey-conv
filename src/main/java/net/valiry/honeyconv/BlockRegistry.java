package net.valiry.honeyconv;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jglrxavpok.hephaistos.mca.BlockState;

/**
 * Copied from my failed Nylium project (server implementation)
 * github.com/cerus/nylium
 */
public class BlockRegistry {

    private static final Map<CachedState, Integer> STATE_PROTOCOL_MAP = new HashMap<>();

    /**
     * Attempts to load the global palette from the provided input stream
     *
     * @param inputStream Stream to load the palette from
     */
    public static void load(final InputStream inputStream) throws JsonIOException, JsonSyntaxException {
        final JsonObject rootObj = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        for (final String key : rootObj.keySet()) {
            final JsonObject childObj = rootObj.get(key).getAsJsonObject();

            // Parse possible states
            final JsonArray statesArr = childObj.get("states").getAsJsonArray();
            for (final JsonElement stateElem : statesArr) {
                final JsonObject stateObj = stateElem.getAsJsonObject();
                final int id = stateObj.get("id").getAsInt();

                // Parse properties object for this state
                final JsonObject propertiesObj = stateObj.has("properties")
                        ? stateObj.get("properties").getAsJsonObject() : new JsonObject();
                final Map<String, String> propertiesMap = propertiesObj.keySet().stream()
                        .collect(Collectors.toMap(s -> s, s -> propertiesObj.get(s).getAsString()));

                STATE_PROTOCOL_MAP.put(new CachedState(key, propertiesMap), id);
            }
        }
    }

    public static int getProtocolId(final BlockState state) {
        final CachedState cachedState = new CachedState(state.getName(), state.getProperties());
        return STATE_PROTOCOL_MAP.get(cachedState);
    }

}
