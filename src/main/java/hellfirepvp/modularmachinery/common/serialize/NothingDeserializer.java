package hellfirepvp.modularmachinery.common.serialize;

final class NothingDeserializer implements DataValueDeserializer<Object, Void> {
    static final NothingDeserializer INSTANCE = new NothingDeserializer();
    private NothingDeserializer() { }
    public Void deserialize(Object input, String name) { return null; }
}
