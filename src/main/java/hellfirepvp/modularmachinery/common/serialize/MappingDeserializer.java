package hellfirepvp.modularmachinery.common.serialize;

import java.util.function.BiFunction;

final class MappingDeserializer<I, O, R> implements DataValueDeserializer<I, R> {
    private final DataValueDeserializer<I, O> prev;
    private final BiFunction<O, String, R> transform;

    MappingDeserializer(DataValueDeserializer<I, O> prev, BiFunction<O, String, R> transform) {
        this.prev = prev;
        this.transform = transform;
    }

    public R deserialize(I input, String name) { return transform.apply(prev.deserialize(input, name), name); }
}
