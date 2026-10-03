package hellfirepvp.modularmachinery.common.serialize.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.serialize.raw.RawData;
import hellfirepvp.modularmachinery.common.serialize.raw.RawDataStructure;
import java.util.ArrayList;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class JsonRawDataStructure implements RawDataStructure {
    private final JsonObject element;

    public JsonRawDataStructure(JsonObject element) { this.element = element; }
    public JsonObject getElement() { return element; }
    public Object get() { return element.toString(); }
    public RawData get(String name) { return element.has(name) ? new JsonRawData(element.get(name)) : null; }
    public Set<String> keySet() {
        // Minecraft 1.12.2 自带 Gson 2.8.0，没有 JsonObject.keySet()。
        return new AbstractSet<String>() {
            public int size() { return element.entrySet().size(); }
            public Iterator<String> iterator() {
                Iterator<Map.Entry<String, JsonElement>> entries = element.entrySet().iterator();
                return new Iterator<String>() {
                    public boolean hasNext() { return entries.hasNext(); }
                    public String next() { return entries.next().getKey(); }
                };
            }
        };
    }
    // 保留 JSON 文本语义：字符串包含引号，显式 null 返回 "null"。
    public String getString(String name) { return element.has(name) ? element.get(name).toString() : null; }

    private <T> T primitive(String name, Function<String, T> parse) {
        JsonElement value = element.get(name);
        if (value == null || !(value.isJsonPrimitive() || value.isJsonNull())) { return null; }
        return parse.apply(value.isJsonNull() ? "null" : value.getAsString());
    }

    public Boolean getBoolean(String name) {
        return primitive(name, value -> {
            if ("true".equals(value)) { return true; }
            if ("false".equals(value)) { return false; }
            throw new IllegalStateException("Value '" + value + "' is not a boolean");
        });
    }
    public Integer getInt(String name) { return primitive(name, Integer::parseInt); }
    public Long getLong(String name) { return primitive(name, Long::parseLong); }
    public Float getFloat(String name) { return primitive(name, Float::parseFloat); }
    public Double getDouble(String name) { return primitive(name, Double::parseDouble); }
    public RawDataStructure getStructure(String name) {
        JsonElement value = element.get(name);
        return value != null && value.isJsonObject() ? new JsonRawDataStructure(value.getAsJsonObject()) : null;
    }
    public List<RawData> getList(String name) {
        JsonElement value = element.get(name);
        if (value == null || !value.isJsonArray()) { return null; }
        List<RawData> result = new ArrayList<>();
        for (JsonElement child : value.getAsJsonArray()) { result.add(new JsonRawData(child)); }
        return result;
    }
}
