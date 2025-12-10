package tk.jasonho.tally.api.models.helpers;

import com.google.gson.JsonElement;

/**
 * @param <T> Represents the current class's type.
 */
public interface ModelDeserializable<T> {
    public T deserialize(JsonElement json);
}
