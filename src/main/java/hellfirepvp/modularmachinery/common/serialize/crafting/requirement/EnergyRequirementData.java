package hellfirepvp.modularmachinery.common.serialize.crafting.requirement;

import hellfirepvp.modularmachinery.common.serialize.DataStructure;
import hellfirepvp.modularmachinery.common.serialize.DataValue;

public final class EnergyRequirementData extends DataStructure {
    private final DataValue<Long> energyPerTick = longValue("energyPerTick");
    public long getEnergyPerTick() { return energyPerTick.getValue(); }
}
