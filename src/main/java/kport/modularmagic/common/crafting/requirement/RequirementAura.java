package kport.modularmagic.common.crafting.requirement;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import kport.modularmagic.common.crafting.component.ComponentAura;
import kport.modularmagic.common.crafting.helper.AuraProviderCopy;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeAura;
import kport.modularmagic.common.integration.jei.component.JEIComponentAura;
import kport.modularmagic.common.integration.jei.ingredient.Aura;
import kport.modularmagic.common.tile.TileAuraProvider;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentAuraProvider;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequirementAura extends ComponentRequirement.MultiCompParallelizable<Aura, RequirementTypeAura>
    implements Asyncable, ComponentRequirement.Parallelizable {

    public Aura aura;
    public int  max;
    public int  min;

    public RequirementAura(IOType type, Aura aura, int max, int min) {
        super((RequirementTypeAura) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_AURA), type);
        this.aura = aura;
        this.max = max;
        this.min = min;
    }

    private static Map<ChunkPos, AuraProviderCopy> buildAccounts(List<ProcessingComponent<?>> components) {
        Map<ChunkPos, AuraProviderCopy> accounts = new HashMap<>();
        for (ProcessingComponent<?> component : components) {
            Object provided = component.getProvidedComponent();
            if (provided instanceof AuraProviderCopy copy) {
                accounts.putIfAbsent(copy.getOriginal().getChunkPos(), copy);
            } else {
                TileAuraProvider provider = (TileAuraProvider) provided;
                accounts.computeIfAbsent(provider.getChunkPos(), key -> new AuraProviderCopy(provider));
            }
        }
        return accounts;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentAura &&
            cmp instanceof MachineComponentAuraProvider &&
            cmp.ioType == getActionType();
    }

    @Nonnull
    @Override
    public Object getComponentCopyKey(ProcessingComponent<?> component) {
        TileAuraProvider provider = (TileAuraProvider) component.getComponent().getContainerProvider();
        return java.util.Arrays.asList(provider.getWorld(), provider.getChunkPos());
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        return ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> {
            Map<ChunkPos, AuraProviderCopy> accounts = buildAccounts(components);
            List<ProcessingComponent<?>> copies = new ArrayList<>();
            for (ProcessingComponent<?> component : components) {
                TileAuraProvider provider = (TileAuraProvider) component.getComponent().getContainerProvider();
                copies.add(new ProcessingComponent<>((MachineComponent<Object>) component.component(),
                    accounts.get(provider.getChunkPos()), component.tag()));
            }
            return copies;
        });
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        if (actionType == IOType.OUTPUT && ignoreOutputCheck) {
            return CraftCheck.success();
        }
        return transferAll(components, context, parallelism, true) >= parallelism
            ? CraftCheck.success()
            : CraftCheck.failure(actionType == IOType.INPUT
                ? "error.modularmachinery.requirement.aura.less" : "error.modularmachinery.requirement.aura.more");
    }

    @Override
    public void startCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        startCraftingChecked(components, context, chance);
    }

    @Override
    public boolean startCraftingChecked(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        return actionType != IOType.INPUT || transferAll(components, context, parallelism, false) >= parallelism;
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (actionType == IOType.OUTPUT) {
            transferAll(components, context, parallelism, false);
        }
    }

    @Override
    public int getMaxParallelism(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        if (parallelizeUnaffected) {
            return transferAll(components, context, 1, true) >= 1 ? maxParallelism : 0;
        }
        return transferAll(components, context, maxParallelism, true);
    }

    private int transferAll(List<ProcessingComponent<?>> components, RecipeCraftingContext context,
                            int multiplier, boolean simulate) {
        return ModularMachinery.EXECUTE_MANAGER.callOnMainThread(() -> {
            int perRecipe = (int) Math.max(0L, Math.min(Integer.MAX_VALUE,
                Math.round(RecipeModifier.applyModifiers(context, this, (double) aura.getAmount(), false))));
            if (perRecipe == 0) {
                return multiplier;
            }
            long requested = (long) perRecipe * multiplier;
            int bound = actionType == IOType.INPUT ? min : max;
            Map<ChunkPos, AuraProviderCopy> accounts = buildAccounts(components);
            long available = accounts.values().stream()
                .mapToLong(copy -> copy.available(aura.getType(), actionType, bound)).sum();
            int supported = (int) Math.min(multiplier, available / perRecipe);
            // 实际不足时不扣费；检查和转移在同一次主线程调用内完成。
            if (!simulate && available < requested) {
                return supported;
            }
            long transferable = (long) supported * perRecipe;
            long remaining = transferable;
            Map<AuraProviderCopy, Long> transferred = simulate ? Collections.emptyMap() : new HashMap<>();
            for (AuraProviderCopy copy : accounts.values()) {
                while (remaining > 0) {
                    int moved = copy.transfer(aura.getType(), actionType, bound,
                        (int) Math.min(Integer.MAX_VALUE, remaining), simulate);
                    if (moved <= 0) {
                        break;
                    }
                    remaining -= moved;
                    if (!simulate) {
                        transferred.merge(copy, (long) moved, Long::sum);
                    }
                }
                if (remaining == 0) {
                    break;
                }
            }
            if (!simulate && remaining != 0) {
                // 外部 API 未足额转移时，退回本次已经转移的资源。
                IOType reverse = actionType == IOType.INPUT ? IOType.OUTPUT : IOType.INPUT;
                transferred.forEach((copy, amount) -> {
                    while (amount > 0) {
                        int restored = copy.getOriginal().transferAura(
                            new Aura((int) Math.min(Integer.MAX_VALUE, amount), aura.getType()), reverse);
                        if (restored <= 0) {
                            throw new IllegalStateException("Aura transfer rollback failed");
                        }
                        amount -= restored;
                    }
                });
                return 0;
            }
            return (int) ((transferable - remaining) / perRecipe);
        });
    }

    @Override
    public ComponentRequirement<Aura, RequirementTypeAura> deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public ComponentRequirement<Aura, RequirementTypeAura> deepCopyModified(List<RecipeModifier> modifiers) {
        Aura aura = new Aura(Math.round(
            RecipeModifier.applyModifiers(modifiers, this, this.aura.getAmount(), false)),
            this.aura.getType()
        );
        return new RequirementAura(actionType, aura, this.max, this.min);
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public JEIComponent<Aura> provideJEIComponent() {
        return new JEIComponentAura(this);
    }
}
