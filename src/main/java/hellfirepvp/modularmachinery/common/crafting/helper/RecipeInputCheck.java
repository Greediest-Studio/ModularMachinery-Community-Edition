package hellfirepvp.modularmachinery.common.crafting.helper;

import github.kasuminova.mmce.common.concurrent.Sync;
import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import github.kasuminova.mmce.common.util.InfItemFluidHandler;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 仅检查必要条件：物品输入为空或所需流体不足时，无需复制库存或检查输出。
 * 不模拟消耗，不缓存库存；通过此检查的分组仍须执行原有精确校验。
 */
final class RecipeInputCheck {
    private final RecipeCraftingContext context;

    private RecipeInputCheck(RecipeCraftingContext context) {
        this.context = context;
    }

    @Nullable
    static RecipeInputCheck create(List<ComponentRequirement<?, ?>> requirements, RecipeCraftingContext context) {
        for (ComponentRequirement<?, ?> requirement : requirements) {
            Class<?> type = requirement.getClass();
            // 自定义需求和回调可能改变后续需求的消耗或组件，保留它们的完整检查流程。
            if (type == RequirementItem.class) {
                RequirementItem item = (RequirementItem) requirement;
                if (item.itemChecker != null || !item.itemModifierList.isEmpty()) {
                    return null;
                }
            } else if (type == RequirementIngredientArray.class) {
                for (ChancedIngredientStack ingredient : ((RequirementIngredientArray) requirement).getIngredients()) {
                    if (ingredient.itemChecker != null || !ingredient.itemModifierList.isEmpty()) {
                        return null;
                    }
                }
            } else if (type != RequirementFluid.class && type != RequirementEnergy.class) {
                return null;
            }
        }
        return new RecipeInputCheck(context);
    }

    @Nullable
    String getMissingInput(List<RequirementComponents> requirements) {
        for (RequirementComponents entry : requirements) {
            ComponentRequirement<?, ?> requirement = entry.requirement();
            if (requirement.getActionType() != IOType.INPUT || requirement.isOptional()) {
                continue;
            }
            if (requirement instanceof RequirementItem item) {
                // 随机数量只检查两个端点，不额外抽取随机数。
                if (Math.round(RecipeModifier.applyModifiers(context, item, item.minAmount, false)) > 0
                    && Math.round(RecipeModifier.applyModifiers(context, item, item.maxAmount, false)) > 0
                    && !hasItems(entry.components())) {
                    return missingInput(requirement, entry.components(), "craftcheck.failure.item.input");
                }
            } else if (requirement instanceof RequirementIngredientArray array) {
                boolean needsItems = !array.getIngredients().isEmpty();
                for (ChancedIngredientStack ingredient : array.getIngredients()) {
                    // GUARANTEED 检查使用 maxCount，与精确检查保持一致。
                    if (Math.round(RecipeModifier.applyModifiers(context, RequirementTypesMM.REQUIREMENT_ITEM,
                        IOType.INPUT, ingredient.maxCount, false)) <= 0) {
                        needsItems = false;
                        break;
                    }
                }
                if (needsItems && !hasItems(entry.components())) {
                    return missingInput(requirement, entry.components(), "craftcheck.failure.item.input");
                }
            } else if (requirement instanceof RequirementFluid fluid) {
                long amount = Math.round(RecipeModifier.applyModifiers(context, fluid, (double) fluid.required.amount, false));
                if (amount > 0 && !hasFluids(entry.components(), fluid.required, amount)) {
                    return missingInput(requirement, entry.components(), "craftcheck.failure.fluid.input");
                }
            }
        }
        return null;
    }

    private static String missingInput(ComponentRequirement<?, ?> requirement,
                                       List<ProcessingComponent<?>> components, String message) {
        return components.isEmpty() ? requirement.getMissingComponentErrorMessage(IOType.INPUT) : message;
    }

    private static boolean hasItems(List<ProcessingComponent<?>> components) {
        for (ProcessingComponent<?> component : components) {
            IItemHandlerModifiable handler = (IItemHandlerModifiable) component.getProvidedComponent();
            boolean[] found = {false};
            Sync.executeSyncIfPresent(handler, () -> {
                // 完整能力接口包含共用/手动输入，不能使用 InfItemFluidHandler.isEmpty()。
                int slots = handler.getSlots();
                for (int slot = 0; slot < slots; slot++) {
                    if (!handler.getStackInSlot(slot).isEmpty()) {
                        found[0] = true;
                        break;
                    }
                }
            });
            if (found[0]) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasFluids(List<ProcessingComponent<?>> components, FluidStack required, long amount) {
        long available = 0;
        for (ProcessingComponent<?> component : components) {
            available += getFluidAmount((IFluidHandler) component.getProvidedComponent(), required, amount - available);
            if (available >= amount) {
                return true;
            }
        }
        return false;
    }

    private static long getFluidAmount(IFluidHandler handler, FluidStack required, long minimum) {
        long[] amount = {0};
        Sync.executeSyncIfPresent(handler, () -> {
            if (handler.getClass() == InfItemFluidHandler.class) {
                InfItemFluidHandler input = (InfItemFluidHandler) handler;
                // 空隔离分组无需分配属性数组；子处理器仍参与判断。
                for (FluidStack fluid : input.getFluidStackList()) {
                    if (fluid != null && fluid.amount > 0 && fluid.isFluidEqual(required)) {
                        amount[0] += fluid.amount;
                        if (amount[0] >= minimum) return;
                    }
                }
                IFluidHandler shared = input.getSubFluidHandler();
                if (shared != null) amount[0] += getFluidAmount(shared, required, minimum - amount[0]);
            } else {
                for (IFluidTankProperties tank : handler.getTankProperties()) {
                    FluidStack fluid = tank.getContents();
                    if (fluid != null && fluid.amount > 0 && fluid.isFluidEqual(required)) {
                        amount[0] += fluid.amount;
                        if (amount[0] >= minimum) return;
                    }
                }
            }
        });
        return amount[0];
    }
}
