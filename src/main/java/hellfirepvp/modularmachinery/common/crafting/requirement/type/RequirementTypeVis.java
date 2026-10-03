// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementVis;
import hellfirepvp.modularmachinery.common.tiles.TileVisProvider;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.util.RequiresMod;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Vis;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.RequirementUtils;

@RequiresMod(Mods.THAUMCRAFT_ID)
public class RequirementTypeVis extends BaseRequirementType<Vis, RequirementVis> {

    @Override
    public ComponentRequirement<Vis, ? extends RequirementType<Vis, RequirementVis>> createRequirement(IOType type, JsonObject jsonObject) {
        float amount = RequirementUtils.getRequiredFloat(jsonObject, "amount", RequirementTypesMM.KEY_REQUIREMENT_VIS.toString());
        int chunkRange = RequirementUtils.getOptionalInt(jsonObject, "chunkRange", 0); // Only the chunk the machine is in
        float minPerChunk = RequirementUtils.getOptionalFloat(jsonObject, "minPerChunk", 0);
        float maxPerChunk = RequirementUtils.getOptionalFloat(jsonObject, "maxPerChunk", TileVisProvider.Output.MAXIMUM_AMOUNT_IN_CHUNK);

        return RequirementVis.from(type, chunkRange, amount, minPerChunk, maxPerChunk);
    }
}
