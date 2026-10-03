package hellfirepvp.modularmachinery.common.serialize.json;

import com.google.gson.JsonElement;
import hellfirepvp.modularmachinery.common.serialize.raw.RawData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonRawData implements RawData {
    private final JsonElement element;

    public JsonRawData(JsonElement element) { this.element = element; }
    public JsonElement getElement() { return element; }

    public Object get() {
        // Kotlin JsonPrimitive.content 对数字、布尔和 null 也返回字符串。
        if (element.isJsonNull()) { return "null"; }
        if (element.isJsonPrimitive()) { return element.getAsString(); }
        if (element.isJsonArray()) {
            List<Object> values = new ArrayList<>();
            for (JsonElement child : element.getAsJsonArray()) { values.add(new JsonRawData(child).get()); }
            return values;
        }
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            values.put(entry.getKey(), new JsonRawData(entry.getValue()).get());
        }
        return values;
    }

    public String toString() { return element.toString(); }
}
