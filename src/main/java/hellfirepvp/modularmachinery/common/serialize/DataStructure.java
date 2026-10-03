package hellfirepvp.modularmachinery.common.serialize;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.serialize.json.JsonRawDataStructure;
import hellfirepvp.modularmachinery.common.serialize.raw.RawDataStructure;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class DataStructure {
    private final List<DataValue<?>> columns = new ArrayList<>();

    public List<DataValue<?>> getColumns() { return columns; }
    public void loadFrom(JsonObject json) { loadFrom(new JsonRawDataStructure(json)); }

    public void loadFrom(RawDataStructure raw) {
        for (DataValue<?> column : columns) {
            try {
                // 保留原实现将完整结构传给列的行为，迁移不修复取值逻辑。
                ((DataValueImpl<?>) column).solve(raw);
            } catch (Throwable error) {
                throw new IllegalArgumentException("Failed to parse column '" + column.getName() + "'", error);
            }
        }
    }

    protected DataValue<Object> raw(String name) {
        DataValue<Object> value = new DataValueImpl<>(name, true, AnyToAnyDeserializer.INSTANCE, () -> null);
        columns.add(value);
        return value;
    }

    // Java 关键字不能作为方法名，因此原有数字类型辅助方法添加 Value 后缀。
    protected DataValue<Byte> byteValue(String name) {
        return raw(name).notnull().map(value -> value instanceof Number
                ? ((Number) value).byteValue() : Byte.parseByte(value.toString())).def(() -> (byte) 0);
    }
    protected DataValue<Short> shortValue(String name) {
        return raw(name).notnull().map(value -> value instanceof Number
                ? ((Number) value).shortValue() : Short.parseShort(value.toString())).def(() -> (short) 0);
    }
    protected DataValue<Integer> integer(String name) {
        return raw(name).notnull().map(value -> value instanceof Number
                ? ((Number) value).intValue() : Integer.parseInt(value.toString())).def(() -> 0);
    }
    protected DataValue<Long> longValue(String name) {
        return raw(name).notnull().map(value -> value instanceof Number
                ? ((Number) value).longValue() : Long.parseLong(value.toString())).def(() -> 0L);
    }
    protected DataValue<Float> floatValue(String name) {
        return raw(name).notnull().map(value -> value instanceof Number
                ? ((Number) value).floatValue() : Float.parseFloat(value.toString())).def(() -> 0F);
    }
    protected DataValue<Double> doubleValue(String name) {
        return raw(name).notnull().map(value -> value instanceof Number
                ? ((Number) value).doubleValue() : Double.parseDouble(value.toString())).def(() -> 0D);
    }
    protected DataValue<Boolean> booleanValue(String name) {
        return raw(name).notnull().map(value -> value instanceof Boolean
                ? (Boolean) value : Boolean.parseBoolean(value.toString())).def(() -> false);
    }
    protected DataValue<String> string(String name) {
        return raw(name).notnull().map(Object::toString).def(() -> "");
    }
    protected <T> DataValue<List<T>> list(String name, Function<Object, T> mapping) {
        return raw(name).notnull().map(value -> {
            if (!(value instanceof List<?>)) { return Collections.<T>emptyList(); }
            List<T> result = new ArrayList<>();
            for (Object entry : (List<?>) value) { result.add(mapping.apply(Objects.requireNonNull(entry))); }
            return result;
        });
    }
    protected DataValue<RawDataStructure> subData(String name) {
        return raw(name).nullable().map(value -> value instanceof RawDataStructure ? (RawDataStructure) value : null);
    }
    protected DataValue<DataStructure> subStructure(String name, DataStructure structure) {
        return subData(name).map(raw -> {
            if (raw != null) { structure.loadFrom(raw); }
            return structure;
        });
    }
    protected DataValue<Map<String, DataStructure>> multipleStructure(String name, Supplier<DataStructure> provider) {
        return subData(name).map(raw -> {
            RawDataStructure structure = raw == null ? null : raw.getStructure(name);
            if (structure == null) { return null; }
            Map<String, DataStructure> result = new LinkedHashMap<>();
            for (String key : structure.keySet()) {
                DataStructure data = provider.get();
                data.loadFrom(Objects.requireNonNull(structure.getStructure(key)));
                result.put(key, data);
            }
            return result;
        });
    }
    protected <E extends Enum<E>> DataValue<E> enumValue(String name, E[] values) {
        return raw(name).notnull().map(value -> {
            String text = value.toString();
            for (E candidate : values) { if (candidate.name().equals(text)) { return candidate; } }
            throw new IllegalArgumentException("Value '" + text + "' is not valid for enum '"
                    + (values.length == 0 ? null : values[0].getClass().getName()) + "'");
        });
    }
}
