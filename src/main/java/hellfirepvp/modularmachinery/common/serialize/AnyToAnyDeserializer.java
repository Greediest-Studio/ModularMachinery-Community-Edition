package hellfirepvp.modularmachinery.common.serialize;

final class AnyToAnyDeserializer implements DataValueDeserializer<Object, Object> {
    static final AnyToAnyDeserializer INSTANCE = new AnyToAnyDeserializer();
    private AnyToAnyDeserializer() { }
    public Object deserialize(Object input, String name) { return input; }
}
