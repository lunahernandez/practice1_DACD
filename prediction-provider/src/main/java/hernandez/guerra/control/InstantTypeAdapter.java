package hernandez.guerra.control;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;

public class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

    @Override
    public JsonElement serialize(Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(instant.toString());
    }

    @Override
    public Instant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Instant.parse(jsonElement.getAsString());
    }
}
