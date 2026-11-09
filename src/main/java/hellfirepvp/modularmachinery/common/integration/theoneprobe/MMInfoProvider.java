package hellfirepvp.modularmachinery.common.integration.theoneprobe;

import com.github.bsideup.jabel.Desugar;
import com.mojang.authlib.GameProfile;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.util.EnergyDisplayUtil;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftingStatus;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.helper.RequirementComponents;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluidPerTick;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementGas;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementGasPerTick;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationTOP;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.RecipeThread;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.TileParallelController;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import io.netty.util.internal.ThrowableUtil;
import mekanism.api.gas.GasStack;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class MMInfoProvider implements IProbeInfoProvider {
    private static void processParallelControllerTOP(TileParallelController parallelController, IProbeInfo probeInfo) {
        if (!ModIntegrationTOP.showParallelControllerInfo) {
            return;
        }
        TileParallelController.ParallelControllerProvider provider = parallelController.provideComponent();
        probeInfo.text(TextFormatting.AQUA + "{*top.parallelism*}" + TextFormatting.GREEN + provider.getParallelism());
        probeInfo.text(TextFormatting.GOLD + "{*top.max_parallelism*}" + TextFormatting.YELLOW + provider.getMaxParallelism());
    }

    private static void processMultiblockMachineTOP(TileMultiblockMachineController machine, IProbeInfo probeInfo, EntityPlayer player) {
        IProbeInfo stateBox = newVertical(probeInfo);
        UUID ownerUUID = machine.getOwner();
        if (ownerUUID != null) {
            MinecraftServer server = machine.getWorld().getMinecraftServer();
            if (server != null) {
                if (ownerUUID.equals(player.getGameProfile().getId())) {
                    stateBox.text(TextFormatting.AQUA + "{*top.machine.owner*}" + TextFormatting.GREEN + "{*top.machine.owner.self*}");
                } else {
                    GameProfile ownerProfile = server.getPlayerProfileCache().getProfileByUUID(ownerUUID);
                    if (ownerProfile == null) {
                        stateBox.text(TextFormatting.AQUA + "{*top.machine.owner*}" + TextFormatting.YELLOW + "{*top.machine.owner.unknown*}(UUID: " + ownerUUID + ")");
                    } else {
                        stateBox.text(TextFormatting.AQUA + "{*top.machine.owner*}" + TextFormatting.RED + ownerProfile.getName());
                    }
                }
            }
        }

        //是否形成结构
        if (machine.isStructureFormed()) {
            stateBox.text(TextFormatting.GREEN + "{*top.machine.structure.found*}");
        } else if (machine.getMachineRequiringBlueprint() != null) {
            stateBox.text(TextFormatting.YELLOW + "{*top.machine.structure.requires_blueprint*}");
        } else {
            stateBox.text(TextFormatting.RED + "{*top.machine.structure.none*}");
            return;
        }

        if (machine instanceof TileMachineController) {
            processMachineControllerTOP((TileMachineController) machine, probeInfo, player);
        }
        if (machine instanceof TileFactoryController) {
            processFactoryControllerTOP((TileFactoryController) machine, probeInfo, player);
        }
        IProbeInfo perfBox = newVertical(probeInfo);
        perfBox.text(String.format("%sCPU Usage: %sμs%s, Recipe Search: %sms",
            TextFormatting.AQUA, formatCPUUsage(machine.getTimeRecorder().usedTimeAvg()),
            TextFormatting.AQUA, formatRecipeSearchUsage(machine.getTimeRecorder().recipeSearchUsedTimeAvg())));
        TileMultiblockMachineController.WorkMode workMode = machine.getWorkMode();
        long groupId = machine.getExecuteGroupId();
        if (workMode == TileMultiblockMachineController.WorkMode.ASYNC && groupId != -1) {
            perfBox.text(String.format("%sWorkMode: %s (GroupID: %s)", TextFormatting.AQUA, workMode.getDisplayName(), groupId));
        } else {
            perfBox.text(String.format("%sWorkMode: %s", TextFormatting.AQUA, workMode.getDisplayName()));
        }
        addRecipeOutputs(machine, probeInfo);
    }

    // TODO: Really long...
    private static void processFactoryControllerTOP(TileFactoryController factory, IProbeInfo probeInfo, EntityPlayer player) {
        if (factory.isWorking()) {
            newVertical(probeInfo).text(TextFormatting.GREEN + "{*top.machine.working*}");
        } else {
            if (factory.getMaxThreads() > 0) {
                newVertical(probeInfo).text(TextFormatting.RED + "{*" + factory.getControllerStatus().getUnlocMessage() + "*}");
            }
            if (factory.getCoreRecipeThreads().isEmpty()) {
                return;
            }
        }

        List<FactoryRecipeThread> recipeThreads = factory.getFactoryRecipeThreadList();
        Collection<FactoryRecipeThread> coreRecipeThreads = factory.getCoreRecipeThreads().values();

        final AtomicLong energyConsumeTotal = new AtomicLong();
        final AtomicLong energyGenerateTotal = new AtomicLong();
        collectRequirementEnergy(coreRecipeThreads, energyConsumeTotal, energyGenerateTotal);
        collectRequirementEnergy(recipeThreads, energyConsumeTotal, energyGenerateTotal);
        if (energyConsumeTotal.get() > 0 || energyGenerateTotal.get() > 0) {
            IProbeInfo energyBox = newVertical(probeInfo);
            if (energyConsumeTotal.get() > 0) {
                addEnergyUsageText(energyBox, player, IOType.INPUT, energyConsumeTotal.get());
            }
            if (energyGenerateTotal.get() > 0) {
                addEnergyUsageText(energyBox, player, IOType.OUTPUT, energyGenerateTotal.get());
            }
        }

        IProbeInfo threadBox = null;
        int maxThreads = factory.getMaxThreads();
        if (maxThreads > 0) {
            threadBox = newVertical(probeInfo);
            threadBox.text(
                TextFormatting.GREEN + String.valueOf(recipeThreads.size()) +
                    TextFormatting.AQUA + " {*top.factory.thread.running*}" +
                    TextFormatting.RESET + " / " +
                    TextFormatting.YELLOW + maxThreads +
                    TextFormatting.GOLD + " {*top.factory.thread.max*}"
            );
        }

        int trueMaxParallelism = factory.getMaxParallelism();
        int runningParallelism = factory.getRunningParallelism();
        if (runningParallelism > 1) {
            IProbeInfo parallelismBox = threadBox == null ? newVertical(probeInfo) : threadBox;
            parallelismBox.text(TextFormatting.AQUA + "{*top.parallelism*}" +
                TextFormatting.GREEN + runningParallelism);
            parallelismBox.text(TextFormatting.GOLD + "{*top.max_parallelism*}" +
                TextFormatting.YELLOW + trueMaxParallelism);
        }

        AtomicInteger i = new AtomicInteger();
        List<FactoryRecipeThread> recipeThreadList = new ArrayList<>();
        recipeThreadList.addAll(coreRecipeThreads);
        recipeThreadList.addAll(recipeThreads);

        IProbeInfo threadsProgressBox = newVertical(probeInfo);
        recipeThreadList.stream().limit(6).forEach(thread -> {
            IProbeInfo threadProgressBox = newVertical(threadsProgressBox);
            ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
            CraftingStatus status = thread.getStatus();

            String threadName;
            if (thread.isCoreThread()) {
                threadName = TextFormatting.BLUE + "{*" + thread.getThreadName() + "*}";
                i.getAndIncrement();
            } else {
                threadName = TextFormatting.AQUA + "{*top.factory.thread*}" + i.getAndIncrement();
            }

            int progressBarFilledColor = ModIntegrationTOP.recipeProgressBarFilledColor;
            int progressBarAlternateFilledColor = ModIntegrationTOP.recipeProgressBarAlternateFilledColor;
            int progressBarBorderColor = ModIntegrationTOP.recipeProgressBarBorderColor;
            if (status.isCrafting()) {
                threadProgressBox.text(threadName + ": " + TextFormatting.GREEN + "{*" + status.getUnlocMessage() + "*}");
            } else {
                threadProgressBox.text(threadName + ": " + TextFormatting.RED + "{*" + status.getUnlocMessage() + "*}");
                progressBarFilledColor = ModIntegrationTOP.failureProgressBarFilledColor;
                progressBarAlternateFilledColor = ModIntegrationTOP.failureProgressBarAlternateFilledColor;
                progressBarBorderColor = ModIntegrationTOP.failureProgressBarBorderColor;
            }

            if (activeRecipe == null) {
                return;
            }

            int tick = activeRecipe.getTick();
            int totalTick = activeRecipe.getTotalTick();
            float progress = (float) (tick * 100) / totalTick;

            String progressStr;
            if (player.isSneaking()) {
                //如：20.5 秒 / 40.0 秒
                //Example: 20.5 Sec / 40.0 Sec
                progressStr = String.format("%.1f s / %.1f s", (float) tick / 20, (float) totalTick / 20);
            } else if (ModIntegrationTOP.showRecipeProgressBarDecimalPoints && totalTick >= 1200) {
                //只有当启用了显示小数点且配方耗时超过 1200 tick 才会显示小数点
                progressStr = String.format("%.2f", progress) + "%";
            } else {
                progressStr = String.format("%.0f", progress) + "%";
            }

            IProbeInfo progressLine = threadProgressBox.horizontal(threadProgressBox.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            progressLine.text(TextFormatting.AQUA + "{*top.recipe.progress*}:  ");
            progressLine.progress((int) progress, 100, threadProgressBox.defaultProgressStyle()
                                                                        .prefix(progressStr)
                                                                        .filledColor(progressBarFilledColor)
                                                                        .alternateFilledColor(progressBarAlternateFilledColor)
                                                                        .borderColor(progressBarBorderColor)
                                                                        .backgroundColor(ModIntegrationTOP.recipeProgressBarBackgroundColor)
                                                                        .numberFormat(NumberFormat.NONE));
        });
    }

    private static void collectRequirementEnergy(final Collection<FactoryRecipeThread> recipeThreads, final AtomicLong energyConsumeTotal, final AtomicLong energyGenerateTotal) {
        for (final FactoryRecipeThread thread : recipeThreads) {
            if (thread.isIdle()) {
                continue;
            }
            RequirementEnergy req = getRequirementEnergy(thread, IOType.INPUT);
            if (req != null) {
                long required = getEnergyRequired(thread, req);
                energyConsumeTotal.addAndGet(required);
            }
            req = getRequirementEnergy(thread, IOType.OUTPUT);
            if (req != null) {
                long required = getEnergyRequired(thread, req);
                energyGenerateTotal.addAndGet(required);
            }
        }
    }

    // TODO: Really long...
    private static void processMachineControllerTOP(TileMachineController machine, IProbeInfo probeInfo, EntityPlayer player) {
        IProbeInfo statusBox = newVertical(probeInfo);
        //是否在工作
        if (machine.getActiveRecipe() == null || machine.getFoundMachine() == null) {
            statusBox.text(TextFormatting.RED + "{*" + machine.getControllerStatus().getUnlocMessage() + "*}");
            return;
        }

        int progressBarFilledColor = ModIntegrationTOP.recipeProgressBarFilledColor;
        int progressBarAlternateFilledColor = ModIntegrationTOP.recipeProgressBarAlternateFilledColor;
        int progressBarBorderColor = ModIntegrationTOP.recipeProgressBarBorderColor;

        if (machine.getControllerStatus().isCrafting()) {
            statusBox.text(TextFormatting.GREEN + "{*top.machine.working*}");
        } else {
            statusBox.text(TextFormatting.RED + "{*" + machine.getControllerStatus().getUnlocMessage() + "*}");
            progressBarFilledColor = ModIntegrationTOP.failureProgressBarFilledColor;
            progressBarAlternateFilledColor = ModIntegrationTOP.failureProgressBarAlternateFilledColor;
            progressBarBorderColor = ModIntegrationTOP.failureProgressBarBorderColor;
        }

        RecipeThread thread = machine.getRecipeThreadList()[0];
        RequirementEnergy reqEnergyIn = getRequirementEnergy(thread, IOType.INPUT);
        RequirementEnergy reqEnergyOut = getRequirementEnergy(thread, IOType.OUTPUT);
        if (reqEnergyIn != null || reqEnergyOut != null) {
            IProbeInfo energyBox = newVertical(probeInfo);

            if (reqEnergyIn != null) {
                long energyUsage = getEnergyRequired(thread, reqEnergyIn);
                addEnergyUsageText(energyBox, player, IOType.INPUT, energyUsage);
            }
            if (reqEnergyOut != null) {
                long energyGenerate = getEnergyRequired(thread, reqEnergyOut);
                addEnergyUsageText(energyBox, player, IOType.OUTPUT, energyGenerate);
            }
        }

        ActiveMachineRecipe activeRecipe = machine.getActiveRecipe();
        int tick = activeRecipe.getTick();
        int totalTick = activeRecipe.getTotalTick();
        float progress = (float) (tick * 100) / totalTick;

        if (activeRecipe.getParallelism() > 1) {
            IProbeInfo parallelismBox = newVertical(probeInfo);
            parallelismBox.text(TextFormatting.AQUA + "{*top.parallelism*}" + TextFormatting.GREEN + activeRecipe.getParallelism());
            parallelismBox.text(TextFormatting.GOLD + "{*top.max_parallelism*}" + TextFormatting.YELLOW + activeRecipe.getMaxParallelism());
        }

        String progressStr;
        if (player.isSneaking()) {
            //如：20.5 秒 / 40.0 秒
            //Example: 20.5 Sec / 40.0 Sec
            progressStr = String.format("%.1f s / %.1f s", (float) tick / 20, (float) totalTick / 20);
        } else if (ModIntegrationTOP.showRecipeProgressBarDecimalPoints && totalTick >= 1200) {
            //只有当启用了显示小数点且配方耗时超过 1200 tick 才会显示小数点
            progressStr = String.format("%.2f", progress) + "%";
        } else {
            progressStr = String.format("%.0f", progress) + "%";
        }

        IProbeInfo progressLine = statusBox.horizontal(statusBox.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
        progressLine.text("{*top.recipe.progress*}:  ");
        progressLine.progress((int) progress, 100, statusBox.defaultProgressStyle()
                                                            .prefix(progressStr)
                                                            .filledColor(progressBarFilledColor)
                                                            .alternateFilledColor(progressBarAlternateFilledColor)
                                                            .borderColor(progressBarBorderColor)
                                                            .backgroundColor(ModIntegrationTOP.recipeProgressBarBackgroundColor)
                                                            .numberFormat(NumberFormat.NONE)
        );
    }

    private static void addRecipeOutputs(final TileMultiblockMachineController machine, final IProbeInfo probeInfo) {
        if (!machine.isWorking()) {
            return;
        }
        Map<OutputKey, RecipeOutput> outputs = new LinkedHashMap<>();
        for (RecipeThread thread : machine.getRecipeThreadList()) {
            collectRecipeOutputs(thread, outputs);
        }
        if (outputs.isEmpty()) {
            return;
        }

        // 产出行保持独立，让 TOP 按屏幕高度自动分列。
        probeInfo.text(TextFormatting.AQUA + "{*top.recipe.outputs*}");
        for (RecipeOutput output : outputs.values()) {
            IProbeInfo line = probeInfo.horizontal(probeInfo.defaultLayoutStyle()
                                                          .alignment(ElementAlignment.ALIGN_CENTER)
                                                          .borderColor(null));
            output.label.accept(line);
            String amount = output.minAmount == output.maxAmount ? MiscUtils.formatNumber(output.maxAmount)
                : MiscUtils.formatNumber(output.minAmount) + "–" + MiscUtils.formatNumber(output.maxAmount);
            line.text(" ×" + amount + output.unit);
        }
    }

    private static void collectRecipeOutputs(final RecipeThread thread, final Map<OutputKey, RecipeOutput> outputs) {
        RecipeCraftingContext context = thread.getContext();
        if (context == null || thread.getActiveRecipe() == null || !thread.getStatus().isCrafting()) {
            return;
        }

        List<RequirementComponents> components = context.getCurrentComponents();
        if (components == null) {
            return;
        }

        for (RequirementComponents component : components) {
            ComponentRequirement<?, ?> requirement = component.requirement();
            if (requirement.getActionType() != IOType.OUTPUT) {
                continue;
            }
            if (requirement instanceof RequirementItem item) {
                collectItemOutput(outputs, context, item);
            } else if (requirement instanceof RequirementFluid fluid) {
                collectFluidOutput(outputs, fluid.required, getOutputAmount(context, fluid, fluid.required.amount), false);
            } else if (requirement instanceof RequirementFluidPerTick fluid) {
                collectFluidOutput(outputs, fluid.required, getOutputAmount(context, fluid, fluid.required.amount), true);
            } else if (requirement instanceof RequirementGas gas) {
                collectGasOutput(outputs, gas.required, getOutputAmount(context, gas, gas.required.amount), false);
            } else if (requirement instanceof RequirementGasPerTick gas) {
                collectGasOutput(outputs, gas.required, getOutputAmount(context, gas, gas.required.amount), true);
            }
        }
        for (ItemStack stack : context.getActiveRecipe().getDynamicOutputs()) {
            long amount = Math.round(RecipeModifier.applyModifiers(context, RequirementTypesMM.REQUIREMENT_ITEM,
                IOType.OUTPUT, (float) stack.getCount(), false));
            collectItemStackOutput(outputs, stack, amount, amount);
        }
    }

    private static void collectItemOutput(final Map<OutputKey, RecipeOutput> outputs, final RecipeCraftingContext context, final RequirementItem item) {
        long minAmount = (long) Math.round(RecipeModifier.applyModifiers(context, item, (float) item.minAmount, false)) * item.getParallelism();
        long maxAmount = (long) Math.round(RecipeModifier.applyModifiers(context, item, (float) item.maxAmount, false)) * item.getParallelism();
        if (maxAmount <= 0) {
            return;
        }

        ItemStack stack = item.getOutputStack(context);
        if (stack.isEmpty()) {
            return;
        }
        minAmount *= stack.getCount();
        maxAmount *= stack.getCount();
        collectItemStackOutput(outputs, stack, minAmount, maxAmount);
    }

    private static void collectItemStackOutput(final Map<OutputKey, RecipeOutput> outputs, final ItemStack output,
                                               final long minAmount, final long maxAmount) {
        ItemStack stack = output.copy();
        stack.setCount(1);
        mergeOutput(outputs, new OutputKey(stack.getItem(), stack.getMetadata(), stack.getTagCompound(), false),
            minAmount, maxAmount, "", line -> line.item(stack).itemLabel(stack));
    }

    private static long getOutputAmount(final RecipeCraftingContext context, final ComponentRequirement<?, ?> requirement, final int amount) {
        double modified = RecipeModifier.applyModifiers(context,
            requirement instanceof RequirementFluidPerTick ? RequirementTypesMM.REQUIREMENT_FLUID : requirement.getRequirementType(),
            IOType.OUTPUT, (double) amount, false);
        return Math.round(modified) * ((ComponentRequirement.Parallelizable) requirement).getParallelism();
    }

    private static void collectFluidOutput(final Map<OutputKey, RecipeOutput> outputs, final FluidStack fluid, final long amount, final boolean perTick) {
        FluidStack stack = fluid.copy();
        mergeOutput(outputs, new OutputKey(stack.getFluid(), 0, stack.tag, perTick), amount, amount, perTick ? " mB/t" : " mB",
            line -> line.element(new ElementOutputIcon(stack.getFluid().getStill(stack), stack.getFluid().getColor(stack)))
                        .text("{*" + stack.getUnlocalizedName() + "*}"));
    }

    private static void collectGasOutput(final Map<OutputKey, RecipeOutput> outputs, final GasStack gas, final long amount, final boolean perTick) {
        mergeOutput(outputs, new OutputKey(gas.getGas(), 0, null, perTick), amount, amount, perTick ? " mB/t" : " mB",
            line -> line.element(new ElementOutputIcon(gas.getGas().getIcon(), gas.getGas().getTint()))
                        .text("{*" + gas.getGas().getTranslationKey() + "*}"));
    }

    private static void mergeOutput(final Map<OutputKey, RecipeOutput> outputs, final OutputKey key,
                                    final long minAmount, final long maxAmount, final String unit, final Consumer<IProbeInfo> label) {
        if (maxAmount <= 0) {
            return;
        }
        RecipeOutput output = outputs.get(key);
        if (output == null) {
            outputs.put(key, new RecipeOutput(Math.max(0, minAmount), maxAmount, unit, label));
        } else {
            output.minAmount += Math.max(0, minAmount);
            output.maxAmount += maxAmount;
        }
    }

    @Desugar
    private record OutputKey(Object material, int metadata, NBTTagCompound tag, boolean perTick) {
    }

    private static class RecipeOutput {
        private long minAmount;
        private long maxAmount;
        private final String unit;
        private final Consumer<IProbeInfo> label;

        private RecipeOutput(final long minAmount, final long maxAmount, final String unit, final Consumer<IProbeInfo> label) {
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.unit = unit;
            this.label = label;
        }
    }

    private static long getEnergyRequired(RecipeThread thread, RequirementEnergy energy) {
        RecipeCraftingContext context = thread.getContext();
        if (context == null) {
            return 0;
        }

        long reqPerTick = energy.getRequiredEnergyPerTick();
        int parallelism = energy.getParallelism();
        float durationMul = context.getDurationMultiplier();

        return Math.round((RecipeModifier.applyModifiers(context, energy, (double) reqPerTick, false) *
            durationMul *
            parallelism)
        );
    }

    private static RequirementEnergy getRequirementEnergy(RecipeThread thread, IOType ioType) {
        RecipeCraftingContext context = thread.getContext();
        if (context == null) {
            return null;
        }
        List<ComponentRequirement<?, ?>> energyRequirements = context.getRequirementBy(RequirementTypesMM.REQUIREMENT_ENERGY, ioType);
        if (energyRequirements.isEmpty()) {
            return null;
        }

        return (RequirementEnergy) energyRequirements.get(0);
    }

    private static void addEnergyUsageText(final IProbeInfo probe, final EntityPlayer player, final IOType ioType, final long usagePerTick) {
        if (ioType == IOType.INPUT) {
            probe.text(TextFormatting.AQUA + "{*top.energy.input*}" + TextFormatting.RED +
                formatEnergyUsage(player, usagePerTick) + " {*" + EnergyDisplayUtil.type.getUnlocalizedFormat() + "*}/t");
        } else if (ioType == IOType.OUTPUT) {
            probe.text(TextFormatting.AQUA + "{*top.energy.output*}" + TextFormatting.RED +
                formatEnergyUsage(player, usagePerTick) + " {*" + EnergyDisplayUtil.type.getUnlocalizedFormat() + "*}/t");
        }
    }

    private static String formatEnergyUsage(final EntityPlayer player, final long usagePerTick) {
        return player.isSneaking()
            ? MiscUtils.formatNumber(EnergyDisplayUtil.type.formatEnergyForDisplay(usagePerTick))
            : MiscUtils.formatDecimal(EnergyDisplayUtil.type.formatEnergyForDisplay(usagePerTick));
    }

    private static String formatCPUUsage(final int time) {
        String prefix;
        if (time <= 100) {
            prefix = TextFormatting.GREEN.toString();
        } else if (time <= 150) {
            prefix = TextFormatting.YELLOW.toString();
        } else if (time <= 250) {
            prefix = TextFormatting.GOLD.toString();
        } else {
            prefix = TextFormatting.RED.toString();
        }
        return prefix + "~" + MiscUtils.formatDecimal(time);
    }

    private static String formatRecipeSearchUsage(final float time) {
        float convertedMs = time / 1000;
        String prefix;
        if (convertedMs <= 1F) {
            prefix = TextFormatting.GREEN.toString();
        } else if (convertedMs <= 2.5F) {
            prefix = TextFormatting.YELLOW.toString();
        } else if (convertedMs <= 5F) {
            prefix = TextFormatting.GOLD.toString();
        } else {
            prefix = TextFormatting.RED.toString();
        }
        return prefix + "~" + MiscUtils.formatFloat(convertedMs, 2);
    }

    private static IProbeInfo newVertical(final IProbeInfo info) {
        return info.vertical(info.defaultLayoutStyle()
                                 .spacing(0)
                                 .borderColor(0x801E90FF)
        );
    }

    @Override
    public String getID() {
        return "modularmachinery:dynamic_machine_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        if (!blockState.getBlock().hasTileEntity(blockState)) {
            return;
        }

        //获取方块实体
        TileEntity tileEntity = world.getTileEntity(data.getPos());
        if (tileEntity == null) {
            return;
        }
        //判断是否为机械控制器
        if (tileEntity instanceof TileMultiblockMachineController machineController) {
            try {
                processMultiblockMachineTOP(machineController, probeInfo, player);
            } catch (Exception e) {
                ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
            }
        } else if (tileEntity instanceof TileParallelController parallelController) {
            processParallelControllerTOP(parallelController, probeInfo);
        }
    }

}
