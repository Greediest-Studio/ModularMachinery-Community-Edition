package hellfirepvp.modularmachinery.common.serialize;

@FunctionalInterface
public interface DataValueDeserializer<I, O> {
    O deserialize(I input, String name);
}
