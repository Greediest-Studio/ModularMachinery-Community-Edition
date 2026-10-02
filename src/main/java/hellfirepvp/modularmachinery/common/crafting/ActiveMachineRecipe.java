/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftingStatus;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.RecipeFailureActions;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenSetter;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ActiveMachineRecipe
 * Created by HellFirePvP
 * Date: 29.06.2017 / 15:50
 */
@ZenRegister
@ZenClass("mods.modularmachinery.ActiveMachineRecipe")
public class ActiveMachineRecipe {
    private final MachineRecipe  recipe;
    private       NBTTagCompound data = new NBTTagCompound();
    private       int            tick = 0, totalTick;
    private int baseMaxParallelism, maxParallelism, parallelism = 1;
    private boolean started;
    private Long craftingGroupId;
    private Long craftingSeed;
    private int startRequirementIndex;
    private int[] activeCatalystIndices = new int[0];

    public ActiveMachineRecipe(MachineRecipe recipe, int maxParallelism) {
        this.recipe = recipe;
        this.totalTick = recipe.getRecipeTotalTickTime();
        this.baseMaxParallelism = maxParallelism;
        this.maxParallelism = maxParallelism;
    }

    public ActiveMachineRecipe(NBTTagCompound serialized) {
        // Older saves also contain an already-paid, active recipe.
        this.started = !serialized.hasKey("started") || serialized.getBoolean("started");
        if (serialized.hasKey("craftingGroupId")) {
            this.craftingGroupId = serialized.getLong("craftingGroupId");
        }
        if (serialized.hasKey("craftingSeed")) {
            this.craftingSeed = serialized.getLong("craftingSeed");
        }
        this.startRequirementIndex = serialized.getInteger("startRequirementIndex");
        this.activeCatalystIndices = serialized.getIntArray("activeCatalystIndices");
        this.recipe = RecipeRegistry.getRecipe(new ResourceLocation(serialized.getString("recipeName")));
        this.tick = serialized.getInteger("tick");
        this.totalTick = serialized.getInteger("totalTick");
        if (serialized.hasKey("data", Constants.NBT.TAG_COMPOUND)) {
            data = serialized.getCompoundTag("data");
        }
        if (serialized.hasKey("baseMaxParallelism")) {
            baseMaxParallelism = serialized.getInteger("baseMaxParallelism");
        }
        if (serialized.hasKey("maxParallelism")) {
            maxParallelism = serialized.getInteger("maxParallelism");
        }
        if (baseMaxParallelism <= 0) {
            baseMaxParallelism = Math.max(1, maxParallelism);
        }
        if (serialized.hasKey("parallelism")) {
            parallelism = serialized.getInteger("parallelism");
        }
    }

    public void reset() {
        this.started = false;
        this.craftingGroupId = null;
        this.craftingSeed = null;
        this.startRequirementIndex = 0;
        this.activeCatalystIndices = new int[0];
        this.tick = 0;
        this.parallelism = 1;
        this.baseMaxParallelism = 1;
        this.maxParallelism = 1;
        this.data = new NBTTagCompound();
    }

    public MachineRecipe getRecipe() {
        return recipe;
    }

    @Nonnull
    public CraftingStatus tick(TileMultiblockMachineController ctrl, RecipeCraftingContext context) {
        if (!started) {
            start(context);
            if (!started) {
                return CraftingStatus.failure("craftcheck.failure.start_retry");
            }
        }
        float rawTotalTick = RecipeModifier.applyModifiers(
            context, RequirementTypesMM.REQUIREMENT_DURATION, null, this.recipe.getRecipeTotalTickTime(), false);
        this.totalTick = rawTotalTick < 1F ? 1 : Math.round(rawTotalTick);

        //Skip per-tick logic until the controller can finish the recipe
        if (this.isCompleted()) {
            return CraftingStatus.working();
        }

        RecipeCraftingContext.CraftingCheckResult check;
        if ((check = context.ioTick(tick)).isFailure()) {
            //On Failure
            DynamicMachine machine = ctrl.getFoundMachine();
            //Some Actions
            if (machine != null) {
                RecipeFailureActions action = machine.getFailureAction();
                doFailureAction(action);
            } else {
                doFailureAction(RecipeFailureActions.getDefaultAction());
            }
            return CraftingStatus.failure(check.getFirstErrorMessage(""));
        } else {
            //Success
            this.tick++;
            return CraftingStatus.working();
        }
    }

    public void doFailureAction(RecipeFailureActions action) {
        switch (action) {
            case RESET -> this.tick = 0;
            case DECREASE -> {
                if (this.tick > 0) {
                    this.tick--;
                }
            }
        }
    }

    public boolean isCompleted() {
        return started && this.tick >= totalTick;
    }

    public RecipeCraftingContext.CraftingCheckResult canStartCrafting(RecipeCraftingContext context) {
        calculateExtraParallelism(context);
        RecipeCraftingContext.CraftingCheckResult result = context.canStartCrafting();
        if (result.isSuccess()) {
            calculateExtraParallelism(context);
        }
        return result;
    }

    public RecipeCraftingContext.CraftingCheckResult canRestartCrafting(RecipeCraftingContext context) {
        calculateExtraParallelism(context);
        RecipeCraftingContext.CraftingCheckResult result = context.canRestartCrafting();
        if (result.isSuccess()) {
            calculateExtraParallelism(context);
        }
        return result;
    }

    public void calculateExtraParallelism(final RecipeCraftingContext context) {
        float totalTick = RecipeModifier.applyModifiers(
            context, RequirementTypesMM.REQUIREMENT_DURATION, null, this.recipe.getRecipeTotalTickTime(), false);
        if (totalTick < 0F) {
            this.totalTick = 1;
            this.maxParallelism = this.baseMaxParallelism;
        } else if (totalTick < 1) {
            int extraParallelism = (int) (1F / totalTick);
            this.maxParallelism = this.baseMaxParallelism * extraParallelism;
            this.totalTick = 1;
        } else {
            this.totalTick = Math.round(totalTick);
            this.maxParallelism = this.baseMaxParallelism;
        }
    }

    public void start(RecipeCraftingContext context) {
        if (started) return;
        if (craftingGroupId == null) {
            activeCatalystIndices = context.getActiveCatalystIndices();
        }
        this.craftingGroupId = context.getGroupId();
        if (craftingSeed == null) craftingSeed = ThreadLocalRandom.current().nextLong();
        if (!context.tryStartCrafting(craftingSeed)) return;
        this.started = true;
        float rawTotalTick = RecipeModifier.applyModifiers(
            context, RequirementTypesMM.REQUIREMENT_DURATION, null, this.recipe.getRecipeTotalTickTime(), false);
        this.totalTick = rawTotalTick < 1F ? 1 : Math.round(rawTotalTick);
    }

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("tick", this.tick);
        tag.setInteger("totalTick", this.totalTick);
        tag.setString("recipeName", this.recipe.getRegistryName().toString());
        tag.setInteger("baseMaxParallelism", this.baseMaxParallelism);
        tag.setInteger("maxParallelism", this.maxParallelism);
        tag.setInteger("parallelism", this.parallelism);
        tag.setBoolean("started", this.started);
        if (craftingGroupId != null) {
            tag.setLong("craftingGroupId", craftingGroupId);
        }
        if (craftingSeed != null) {
            tag.setLong("craftingSeed", craftingSeed);
        }
        tag.setInteger("startRequirementIndex", startRequirementIndex);
        tag.setIntArray("activeCatalystIndices", activeCatalystIndices);

        if (!data.isEmpty()) {
            tag.setTag("data", data);
        }
        return tag;
    }

    public boolean isStarted() {
        return started;
    }

    public Long getCraftingGroupId() {
        return craftingGroupId;
    }

    public int getStartRequirementIndex() {
        return startRequirementIndex;
    }

    public int[] getActiveCatalystIndices() {
        return activeCatalystIndices;
    }

    public void setStartRequirementIndex(int startRequirementIndex) {
        this.startRequirementIndex = startRequirementIndex;
    }

    @ZenGetter("maxParallelism")
    public int getMaxParallelism() {
        return maxParallelism;
    }

    @ZenSetter("maxParallelism")
    public void setMaxParallelism(int maxParallelism) {
        this.baseMaxParallelism = maxParallelism;
        this.maxParallelism = maxParallelism;
    }

    @ZenGetter("parallelism")
    public int getParallelism() {
        return parallelism;
    }

    @ZenSetter("parallelism")
    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    @ZenGetter("tick")
    public int getTick() {
        return tick;
    }

    @ZenSetter("tick")
    public void setTick(int tick) {
        if (tick >= 0) {
            this.tick = tick;
        }
    }

    @ZenGetter("totalTick")
    public int getTotalTick() {
        return totalTick;
    }

    @ZenSetter("totalTick")
    public void setTotalTick(int totalTick) {
        this.totalTick = totalTick;
    }

    @ZenGetter("registryName")
    public String getRegistryName() {
        return recipe.getRegistryName().getPath();
    }

    @ZenGetter("data")
    public IData getData() {
        return CraftTweakerMC.getIDataModifyable(data);
    }

    @ZenSetter("data")
    public void setData(IData data) {
        this.data = CraftTweakerMC.getNBTCompound(data);
    }

    public NBTTagCompound getDataCompound() {
        return data;
    }
}
