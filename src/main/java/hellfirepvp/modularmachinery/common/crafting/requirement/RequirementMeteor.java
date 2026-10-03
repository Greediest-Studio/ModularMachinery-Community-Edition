// 部分实现来自 MMCE-Addons (Alecsio)，按 GPL-3.0 许可并入本体。
package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.crafting.component.ComponentMeteor;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeMeteor;
import hellfirepvp.modularmachinery.common.machine.component.MachineComponentMeteorProvider;

import WayofTime.bloodmagic.meteor.MeteorRegistry;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.util.RequirementValidator;
import hellfirepvp.modularmachinery.common.integration.jei.component.JEIComponentMeteor;
import hellfirepvp.modularmachinery.common.integration.jei.ingredient.Meteor;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class RequirementMeteor extends AbstractMultiComponentRequirement<Meteor, RequirementTypeMeteor> {

    private static final RequirementValidator requirementValidator = RequirementValidator.getInstance();

    private final Meteor meteor;

    public static RequirementMeteor from(String catalystItem) {
        Item item = Item.getByNameOrId(catalystItem);
        requirementValidator.validateNotNull(item, String.format("Failed to load recipe for %s. Item %s doesn't exist!", RequirementMeteor.class, catalystItem));

        var catalystStack = new ItemStack(item);
        var meteor = MeteorRegistry.getMeteorForItem(catalystStack);
        requirementValidator.validateNotNull(meteor, String.format("Failed to find meteor with catalyst: %s", item.getRegistryName()));

        return new RequirementMeteor(IOType.OUTPUT, new Meteor(catalystStack, meteor.getComponents(), meteor.getExplosionStrength(), meteor.getRadius()));
    }

    private RequirementMeteor(IOType actionType, Meteor meteor) {
        super((RequirementTypeMeteor) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(RequirementTypesMM.KEY_REQUIREMENT_METEOR), actionType);
        this.meteor = meteor;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cpn = component.getComponent();
        return cpn instanceof MachineComponentMeteorProvider &&
                cpn.getComponentType() instanceof ComponentMeteor &&
                cpn.ioType == getActionType();
    }

    @Override
    public ComponentRequirement<Meteor, RequirementTypeMeteor> deepCopy() {
        // I know, the components, but whatever
        return new RequirementMeteor(actionType, new Meteor(meteor.getCatalystStack(), meteor.getComponents(), meteor.getExplosionStrength(), meteor.getRadius()));
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid.meteor";
    }

    @Override
    public JEIComponent<Meteor> provideJEIComponent() {
        return new JEIComponentMeteor(this.meteor);
    }

    public Meteor getMeteor() {
        return meteor;
    }
}
