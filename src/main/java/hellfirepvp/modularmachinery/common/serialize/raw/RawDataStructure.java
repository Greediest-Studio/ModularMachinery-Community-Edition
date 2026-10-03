package hellfirepvp.modularmachinery.common.serialize.raw;

import java.util.List;
import java.util.Set;

public interface RawDataStructure extends RawData {
    RawData get(String name);
    Set<String> keySet();
    String getString(String name);
    Boolean getBoolean(String name);
    Integer getInt(String name);
    Long getLong(String name);
    Float getFloat(String name);
    Double getDouble(String name);
    RawDataStructure getStructure(String name);
    List<RawData> getList(String name);
}
