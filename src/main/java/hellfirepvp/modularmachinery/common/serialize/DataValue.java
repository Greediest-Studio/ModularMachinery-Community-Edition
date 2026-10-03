package hellfirepvp.modularmachinery.common.serialize;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface DataValue<V> {
    String getName();
    boolean getNullable();
    V getValue();
    DataValueDeserializer<Object, V> getDeserializer();
    DataValue<V> nullable();
    DataValue<V> notnull();
    DataValue<V> def(Supplier<V> provider);
    <R> DataValue<R> map(BiFunction<V, String, R> transform);

    default <R> DataValue<R> map(Function<V, R> transform) {
        return map((value, name) -> transform.apply(value));
    }
}
