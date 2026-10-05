// 移植自 MMCE-AdvancedBuilderTool，按 GPL-3.0 许可融合。
package hellfirepvp.modularmachinery.common.integration.ae2.builder;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerCraftConfirm;
import com.google.common.collect.ImmutableSet;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.machine.assembly.AssemblyUtils;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.assembly.*;
import hellfirepvp.modularmachinery.common.machine.assembly.MachineAssembly;
import hellfirepvp.modularmachinery.common.util.StructureIngredient;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;
import java.util.List;

/** 共用本体放置流程，仅扩展 AE 取料、合成确认和任务产物结算。 */
public class AeMachineAssembly extends MachineAssembly implements CraftingRequester {

    private static final int MAX_MISSING_REPORTS = 8;
    private static final int CRAFTING_BUILD_INTERVAL_TICKS = 20;
    private static final int CRAFT_REQUEST_LOST_GRACE_TICKS = 100;

    private final boolean useAeItems;
    private final boolean useAeFluids;
    private final boolean craftMissing;

    private final List<CraftableMissingEntry> craftableMissingEntries = new ArrayList<>();
    private Ae2GridAccess.CraftingGuiRequest activeCraftRequest;
    private CraftableMissingEntry activeCraftGuiEntry;
    private CraftableMissingEntry activeCraftAmountEntry;
    private long lastCraftStateCheckTick = -1;
    private boolean cancelled;
    private boolean allCraftingGuisOffered;
    private int nextCraftableIndex;
    private int submittedCraftCount;

    public AeMachineAssembly(World world, BlockPos pos, EntityPlayer player, StructureIngredient ingredient, AssemblyOptions options) {
        super(world, pos, player, ingredient, options);
        this.useAeItems = options.useAeItems;
        this.useAeFluids = options.useAeFluids;
        this.craftMissing = options.craftMissing;
        cacheInitialCraftingShortages();
    }

    @Override
    public void tick() {
        if (cancelled) {
            return;
        }
        updateCraftingFlow();
        if (isCancelled()) {
            return;
        }
        openCraftingGuiIfNeeded();
        if (shouldThrottleForCrafting() && getWorld().getTotalWorldTime() % CRAFTING_BUILD_INTERVAL_TICKS != 0) {
            return;
        }
        assembly(!getPlayer().isCreative());
    }

    @Override
    public boolean isCancelled() {
        return cancelled || (!isCompleted() && allSubmittedCraftsCancelled());
    }

    @Override
    public void cancel() {
        cancelled = true;
        activeCraftRequest = null;
        activeCraftGuiEntry = null;
        if (activeCraftAmountEntry != null) {
            activeCraftAmountEntry.cancelAmountProbe();
            activeCraftAmountEntry = null;
        }
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            entry.cancelAmountProbe();
            entry.cancelLink();
        }
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        ImmutableSet.Builder<ICraftingLink> builder = ImmutableSet.builder();
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.link != null && !entry.link.isDone() && !entry.link.isCanceled()) {
                builder.add(entry.link);
            }
        }
        return builder.build();
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack stack, Actionable mode) {
        if (stack == null || stack.getStackSize() <= 0) {
            return null;
        }
        CraftableMissingEntry entry = findSubmittedCraft(link);
        if (entry == null || !stack.isSameType(entry.request)) {
            return stack;
        }
        if (mode == Actionable.SIMULATE) {
            return null;
        }
        long leftover = entry.insertCraftedOutput(stack);
        if (leftover <= 0) {
            return null;
        }
        IAEItemStack remaining = stack.copy();
        remaining.setStackSize(leftover);
        return remaining;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        CraftableMissingEntry entry = findSubmittedCraft(link);
        if (entry == null) {
            return;
        }
        if (link.isCanceled()) {
            entry.markCancelled(getWorld().getTotalWorldTime());
        } else if (link.isDone()) {
            entry.markDone(getWorld().getTotalWorldTime());
        }
    }

    @Override
    public void mmce$setCraftingLink(ICraftingLink link) {
        if (!mmce$canSubmitCraft()) {
            if (link != null) link.cancel();
            return;
        }
        if (link == null) {
            activeCraftGuiEntry.markSkipped();
        } else {
            activeCraftGuiEntry.markSubmitted(link, activeCraftRequest, getWorld().getTotalWorldTime());
            submittedCraftCount++;
        }
        activeCraftRequest = null;
        activeCraftGuiEntry = null;
    }

    @Override
    public boolean mmce$canSubmitCraft() { return !cancelled && !isCompleted() && activeCraftGuiEntry != null; }

    @Override
    public IGridNode getActionableNode() {
        if (activeCraftRequest != null) {
            return activeCraftRequest.getNode();
        }
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.link != null && !entry.link.isDone() && !entry.link.isCanceled() && entry.node != null) {
                return entry.node;
            }
        }
        return null;
    }

    @Override
    public void report() {
        if (isCompleted()) {
            cancelUnfinishedCrafts();
        }
        super.report();
    }

    protected List<Tuple<ItemStack, IBlockState>> selectItemCandidatesForPosition(BlockPos relativePos, List<Tuple<ItemStack, IBlockState>> candidates) {
        for (Tuple<ItemStack, IBlockState> candidate : candidates) {
            if (getPlayerItemAmount(candidate.getFirst()) >= candidate.getFirst().getCount()) return singleCandidate(candidate);
        }
        Tuple<ItemStack, IBlockState> craftReservedCandidate = findCraftReservedItemCandidate(relativePos, candidates);
        if (craftReservedCandidate != null) {
            return singleCandidate(craftReservedCandidate);
        }
        return candidates;
    }

    protected List<Tuple<FluidStack, IBlockState>> selectFluidCandidatesForPosition(BlockPos relativePos, List<Tuple<FluidStack, IBlockState>> candidates) {
        Tuple<FluidStack, IBlockState> craftReservedCandidate = findCraftReservedFluidCandidate(relativePos, candidates);
        if (craftReservedCandidate != null) {
            return singleCandidate(craftReservedCandidate);
        }
        return candidates;
    }

    private Tuple<ItemStack, IBlockState> findCraftReservedItemCandidate(BlockPos relativePos, List<Tuple<ItemStack, IBlockState>> candidates) {
        for (Tuple<ItemStack, IBlockState> tuple : candidates) {
            CraftableMissingEntry entry = findManagedCraftableMissing(relativePos, tuple.getFirst());
            if (entry != null && entry.shouldReserveCandidate()) {
                return tuple;
            }
        }
        return null;
    }

    private Tuple<FluidStack, IBlockState> findCraftReservedFluidCandidate(BlockPos relativePos, List<Tuple<FluidStack, IBlockState>> candidates) {
        for (Tuple<FluidStack, IBlockState> tuple : candidates) {
            CraftableMissingEntry entry = findManagedCraftableMissing(relativePos, tuple.getFirst());
            if (entry != null && entry.shouldReserveCandidate()) {
                return tuple;
            }
        }
        return null;
    }

    private <T> List<Tuple<T, IBlockState>> singleCandidate(Tuple<T, IBlockState> candidate) {
        List<Tuple<T, IBlockState>> selected = new ArrayList<>(1);
        selected.add(candidate);
        return selected;
    }

    protected boolean consumeItem(BlockPos relativePos, ItemStack required) {
        CraftableMissingEntry managedEntry = findManagedCraftableMissing(relativePos, required);
        if (managedEntry != null) {
            return consumeManagedItem(managedEntry, required);
        }
        if (takeInventoryItem(required)) {
            return true;
        }
        return useAeItems && Mods.AE2.isPresent() && takeAeItem(required, false);
    }

    private boolean consumeManagedItem(CraftableMissingEntry entry, ItemStack required) {
        CraftableMissingEntry.CraftReservation direct = entry.reserveDirect(required.getCount());
        if (takeInventoryItem(required)) return true;
        if (direct != null) {
            if (useAeItems && takeAeItem(required, false)) return true;
            entry.restoreAvailable(direct);
        }
        CraftableMissingEntry.CraftReservation crafted = entry.reserveCrafted(required.getCount());
        if (crafted != null) {
            if (takeAeItem(required, true)) return true;
            entry.restoreAvailable(crafted);
        }
        return false;
    }

    protected boolean consumeFluid(BlockPos relativePos, FluidStack required) {
        CraftableMissingEntry managedEntry = findManagedCraftableMissing(relativePos, required);
        if (managedEntry != null) {
            return consumeManagedFluid(managedEntry, required);
        }
        if (takeInventoryFluid(required)) {
            return true;
        }
        return useAeFluids && Mods.AE2.isPresent() && takeAeFluid(required, false);
    }

    private boolean consumeManagedFluid(CraftableMissingEntry entry, FluidStack required) {
        CraftableMissingEntry.CraftReservation direct = entry.reserveDirect(required.amount);
        if (takeInventoryFluid(required)) return true;
        if (direct != null) {
            if (useAeFluids && takeAeFluid(required, false)) return true;
            entry.restoreAvailable(direct);
        }
        CraftableMissingEntry.CraftReservation crafted = entry.reserveCrafted(required.amount);
        if (crafted != null) {
            if (takeAeFluid(required, true)) return true;
            entry.restoreAvailable(crafted);
        }
        return false;
    }

    public void openCraftingGuiIfNeeded() {
        if (!craftMissing || !Mods.AE2.isPresent() || activeCraftGuiEntry != null || allCraftingGuisOffered) {
            return;
        }
        if (activeCraftAmountEntry != null) {
            if (!activeCraftAmountEntry.tickAmountProbe()) {
                return;
            }
            CraftableMissingEntry entry = activeCraftAmountEntry;
            activeCraftAmountEntry = null;
            long amount = entry.getProbedAmount();
            if (amount <= 0) {
                entry.markSkipped();
                return;
            }
            openCraftingGui(entry, amount);
            return;
        }
        while (nextCraftableIndex < craftableMissingEntries.size()) {
            CraftableMissingEntry entry = craftableMissingEntries.get(nextCraftableIndex++);
            if (!entry.canOfferGui()) {
                continue;
            }
            entry.startAmountProbe();
            activeCraftAmountEntry = entry;
            if (!entry.tickAmountProbe()) {
                return;
            }
            activeCraftAmountEntry = null;
            long amount = entry.getProbedAmount();
            if (amount <= 0) {
                entry.markSkipped();
                continue;
            }
            openCraftingGui(entry, amount);
            return;
        }
        allCraftingGuisOffered = true;
    }

    private void openCraftingGui(CraftableMissingEntry entry, long amount) {
        entry.prepareRequest(amount);
        Ae2GridAccess.CraftingGuiRequest request = Ae2GridAccess.openCraftConfirmGui(getPlayer(), entry.request.copy(), this);
        if (request != null) {
            activeCraftGuiEntry = entry;
            activeCraftRequest = request;
            entry.markOffered(request, getWorld().getTotalWorldTime());
            return;
        }
        entry.markSkipped();
    }

    private void cacheInitialCraftingShortages() {
        if (!craftMissing || !Mods.AE2.isPresent()) {
            allCraftingGuisOffered = true;
            return;
        }
        cacheInitialItemCraftingShortages();
        cacheInitialFluidCraftingShortages();
        allCraftingGuisOffered = craftableMissingEntries.isEmpty();
    }

    private void cacheInitialItemCraftingShortages() {
        List<RequiredItemEntry> requiredItems = collectRequiredItemEntries();
        for (RequiredItemEntry entry : requiredItems) {
            if (entry.amount <= 0) {
                continue;
            }
            IAEItemStack request = Ae2GridAccess.toAeItemRequest(entry.stack, entry.amount);
            if (request != null) {
                craftableMissingEntries.add(new CraftableMissingEntry(this, entry.stack, null, request, entry.amount, entry.directAmount, entry.positions));
            }
        }
    }

    private void cacheInitialFluidCraftingShortages() {
        List<RequiredFluidEntry> requiredFluids = collectRequiredFluidEntries();
        subtractPlayerFluids(requiredFluids);
        for (RequiredFluidEntry entry : requiredFluids) {
            if (entry.amount <= 0) {
                continue;
            }
            long afterPlayer = entry.amount;
            long stored = useAeFluids ? Ae2GridAccess.getStoredFluidAmount(getPlayer(), entry.fluid) : 0;
            long storedUsed = Math.min(afterPlayer, stored);
            long shortage = afterPlayer - storedUsed;
            if (shortage <= 0) {
                continue;
            }
            IAEItemStack request = Ae2GridAccess.toAeFluidRequest(entry.fluid, shortage);
            if (request != null) {
                long playerUsed = entry.totalAmount - afterPlayer;
                craftableMissingEntries.add(new CraftableMissingEntry(this, ItemStack.EMPTY, entry.fluid, request, shortage, playerUsed + storedUsed, entry.positions));
            }
        }
    }

    private List<RequiredItemEntry> collectRequiredItemEntries() {
        List<RequiredItemEntry> requiredItems = new ArrayList<>();
        List<ItemAvailabilityEntry> availableItems = new ArrayList<>();
        for (StructureIngredient.ItemIngredient ingredient : getIngredient().itemIngredient()) {
            if (ingredient.ingredientList().isEmpty()) {
                continue;
            }
            ItemStack selected = selectRequirementItemStack(ingredient.pos(), ingredient.ingredientList(), availableItems);
            if (selected.isEmpty()) {
                continue;
            }
            addItemShortageIfNeeded(requiredItems, availableItems, selected, ingredient.pos());
        }
        return requiredItems;
    }

    private List<RequiredFluidEntry> collectRequiredFluidEntries() {
        List<RequiredFluidEntry> requiredFluids = new ArrayList<>();
        for (StructureIngredient.FluidIngredient ingredient : getIngredient().fluidIngredient()) {
            if (ingredient.ingredientList().isEmpty()) {
                continue;
            }
            FluidStack fluid = selectRequirementFluidStack(ingredient.pos(), ingredient.ingredientList());
            if (fluid == null || fluid.amount <= 0) {
                continue;
            }
            addRequiredFluid(requiredFluids, fluid, fluid.amount, ingredient.pos());
        }
        return requiredFluids;
    }

    protected ItemStack selectRequirementItemStack(BlockPos relativePos, List<Tuple<ItemStack, IBlockState>> candidates) {
        return selectRequirementItemStack(relativePos, candidates, null);
    }

    protected ItemStack selectRequirementItemStack(BlockPos relativePos, List<Tuple<ItemStack, IBlockState>> candidates, List<ItemAvailabilityEntry> availableItems) {
        for (Tuple<ItemStack, IBlockState> candidate : candidates) {
            ItemStack stack = candidate.getFirst();
            if (getPlayerItemAmount(stack) >= stack.getCount()
                && (availableItems == null || getAvailableItemAmount(availableItems, stack) >= stack.getCount())) return stack;
        }
        for (Tuple<ItemStack, IBlockState> candidate : candidates) {
            ItemStack stack = candidate.getFirst();
            if (stack.isEmpty()) {
                continue;
            }
            long available = availableItems == null ? getStoredAvailableItemAmount(stack) : getAvailableItemAmount(availableItems, stack);
            if (available >= stack.getCount()) {
                return stack;
            }
        }
        if (useAeItems && Mods.AE2.isPresent()) {
            for (Tuple<ItemStack, IBlockState> candidate : candidates) {
                ItemStack stack = candidate.getFirst();
                IAEItemStack request = Ae2GridAccess.toAeItemRequest(stack, stack.isEmpty() ? 0 : stack.getCount());
                if (request != null && Ae2GridAccess.canCraftAeItem(getPlayer(), request)) {
                    return stack;
                }
            }
        }
        if (relativePos != null) {
            for (Tuple<ItemStack, IBlockState> candidate : candidates) {
                CraftableMissingEntry entry = findManagedCraftableMissing(relativePos, candidate.getFirst());
                if (entry != null && entry.shouldReserveCandidate()) {
                    return candidate.getFirst();
                }
            }
        }
        return candidates.isEmpty() ? ItemStack.EMPTY : candidates.get(0).getFirst();
    }

    protected FluidStack selectRequirementFluidStack(BlockPos relativePos, List<Tuple<FluidStack, IBlockState>> candidates) {
        for (Tuple<FluidStack, IBlockState> candidate : candidates) {
            FluidStack fluid = candidate.getFirst();
            if (fluid != null && fluid.amount > 0 && getStoredAvailableFluidAmount(fluid) >= fluid.amount) {
                return fluid;
            }
        }
        if (useAeFluids && Mods.AE2.isPresent()) {
            for (Tuple<FluidStack, IBlockState> candidate : candidates) {
                FluidStack fluid = candidate.getFirst();
                IAEItemStack request = Ae2GridAccess.toAeFluidRequest(fluid, fluid == null ? 0 : fluid.amount);
                if (request != null && Ae2GridAccess.canCraftAeItem(getPlayer(), request)) {
                    return fluid;
                }
            }
        }
        if (relativePos != null) {
            for (Tuple<FluidStack, IBlockState> candidate : candidates) {
                CraftableMissingEntry entry = findManagedCraftableMissing(relativePos, candidate.getFirst());
                if (entry != null && entry.shouldReserveCandidate()) {
                    return candidate.getFirst();
                }
            }
        }
        return candidates.isEmpty() ? null : candidates.get(0).getFirst();
    }

    private void addItemShortageIfNeeded(List<RequiredItemEntry> requiredItems, List<ItemAvailabilityEntry> availableItems, ItemStack stack, BlockPos relativePos) {
        if (stack.isEmpty()) {
            return;
        }
        long directAmount = reserveItemAmount(availableItems, stack, stack.getCount());
        long shortage = stack.getCount() - directAmount;
        if (shortage > 0) {
            addRequiredItem(requiredItems, stack, shortage, directAmount, relativePos);
        }
    }

    private long getAvailableItemAmount(List<ItemAvailabilityEntry> availableItems, ItemStack stack) {
        return getItemAvailability(availableItems, stack).amount;
    }

    private long reserveItemAmount(List<ItemAvailabilityEntry> availableItems, ItemStack stack, long requestedAmount) {
        ItemAvailabilityEntry availability = getItemAvailability(availableItems, stack);
        long reserved = Math.min(availability.amount, requestedAmount);
        availability.amount -= reserved;
        return reserved;
    }

    private ItemAvailabilityEntry getItemAvailability(List<ItemAvailabilityEntry> availableItems, ItemStack stack) {
        for (ItemAvailabilityEntry entry : availableItems) {
            if (AssemblyUtils.areItemStacksEqual(entry.stack, stack)) {
                return entry;
            }
        }
        ItemAvailabilityEntry entry = new ItemAvailabilityEntry(stack, getStoredAvailableItemAmount(stack));
        availableItems.add(entry);
        return entry;
    }

    private long getStoredAvailableItemAmount(ItemStack stack) {
        return getPlayerItemAmount(stack) + (useAeItems ? Ae2GridAccess.getStoredItemAmount(getPlayer(), stack) : 0);
    }

    private long getStoredAvailableFluidAmount(FluidStack fluid) {
        return getPlayerFluidAmount(fluid) + (useAeFluids ? Ae2GridAccess.getStoredFluidAmount(getPlayer(), fluid) : 0);
    }

    private long getPlayerItemAmount(ItemStack stack) {
        long amount = 0;
        for (ItemStack inventoryStack : getPlayer().inventory.mainInventory) {
            if (AssemblyUtils.areItemStacksEqual(inventoryStack, stack)) {
                amount += inventoryStack.getCount();
            }
        }
        return amount;
    }

    private long getPlayerFluidAmount(FluidStack fluid) {
        if (fluid == null || fluid.amount <= 0) {
            return 0;
        }
        long amount = 0;
        for (IFluidHandlerItem handler : AssemblyUtils.getFluidHandlerItems(getPlayer().inventory.mainInventory)) {
            for (IFluidTankProperties property : handler.getTankProperties()) {
                FluidStack contained = property.getContents();
                if (AssemblyUtils.areFluidsEqual(contained, fluid)) {
                    amount += contained.amount;
                }
            }
        }
        return amount;
    }

    private void subtractPlayerFluids(List<RequiredFluidEntry> requiredFluids) {
        for (IFluidHandlerItem handler : AssemblyUtils.getFluidHandlerItems(getPlayer().inventory.mainInventory)) {
            for (IFluidTankProperties property : handler.getTankProperties()) {
                FluidStack contained = property.getContents();
                if (contained == null || contained.amount <= 0) {
                    continue;
                }
                int remaining = contained.amount;
                for (RequiredFluidEntry entry : requiredFluids) {
                    if (remaining <= 0) {
                        break;
                    }
                    if (!AssemblyUtils.areFluidsEqual(contained, entry.fluid)) {
                        continue;
                    }
                    long consumed = Math.min(entry.amount, remaining);
                    entry.amount -= consumed;
                    remaining -= consumed;
                }
            }
        }
    }

    private void addRequiredItem(List<RequiredItemEntry> requiredItems, ItemStack stack, long amount, long directAmount, BlockPos relativePos) {
        for (RequiredItemEntry entry : requiredItems) {
            if (AssemblyUtils.areItemStacksEqual(entry.stack, stack)) {
                entry.amount += amount;
                entry.directAmount += directAmount;
                entry.positions.add(relativePos);
                return;
            }
        }
        requiredItems.add(new RequiredItemEntry(stack, amount, directAmount, relativePos));
    }

    private void addRequiredFluid(List<RequiredFluidEntry> requiredFluids, FluidStack fluid, long amount, BlockPos relativePos) {
        for (RequiredFluidEntry entry : requiredFluids) {
            if (AssemblyUtils.areFluidsEqual(entry.fluid, fluid)) {
                entry.totalAmount += amount;
                entry.amount += amount;
                entry.positions.add(relativePos);
                return;
            }
        }
        requiredFluids.add(new RequiredFluidEntry(fluid, amount, relativePos));
    }

    protected boolean shouldWaitForItemCraft(BlockPos relativePos, ItemStack required) {
        CraftableMissingEntry entry = findManagedCraftableMissing(relativePos, required);
        if (entry == null) {
            return false;
        }
        openCraftingGuiIfNeeded();
        return entry.shouldWait(getWorld().getTotalWorldTime());
    }

    protected boolean shouldWaitForFluidCraft(BlockPos relativePos, FluidStack required) {
        CraftableMissingEntry entry = findManagedCraftableMissing(relativePos, required);
        if (entry == null) {
            return false;
        }
        openCraftingGuiIfNeeded();
        return entry.shouldWait(getWorld().getTotalWorldTime());
    }

    private CraftableMissingEntry findManagedCraftableMissing(BlockPos relativePos, ItemStack required) {
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.matches(relativePos, required)) {
                return entry;
            }
        }
        return null;
    }

    private CraftableMissingEntry findManagedCraftableMissing(BlockPos relativePos, FluidStack required) {
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.matches(relativePos, required)) {
                return entry;
            }
        }
        return null;
    }

    private CraftableMissingEntry findSubmittedCraft(ICraftingLink link) {
        if (link == null) {
            return null;
        }
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.link == link || (entry.link != null && entry.link.getCraftingID().equals(link.getCraftingID()))) {
                return entry;
            }
        }
        return null;
    }

    private void updateCraftingFlow() {
        long now = getWorld().getTotalWorldTime();
        if (lastCraftStateCheckTick == now) {
            return;
        }
        lastCraftStateCheckTick = now;
        updateActiveCraftGui(now);
        updateSubmittedCrafts(now);
    }

    private void updateActiveCraftGui(long now) {
        if (activeCraftGuiEntry == null) {
            return;
        }
        if (activeCraftGuiEntry.link != null) {
            activeCraftRequest = null;
            activeCraftGuiEntry = null;
            return;
        }
        boolean confirmOpen = getPlayer().openContainer instanceof ContainerCraftConfirm;
        if (confirmOpen) {
            ContainerCraftConfirm confirm = (ContainerCraftConfirm) getPlayer().openContainer;
            if (confirm.isSimulation() && confirm.getUsedBytes() > 0 && now - activeCraftGuiEntry.offeredTick > 20) {
                activeCraftGuiEntry.markSkipped();
                activeCraftRequest = null;
                activeCraftGuiEntry = null;
            }
            return;
        }
        if (activeCraftRequest != null && activeCraftRequest.isRequesting()) {
            activeCraftGuiEntry.markSubmitted(null, activeCraftRequest, now);
            submittedCraftCount++;
            activeCraftRequest = null;
            activeCraftGuiEntry = null;
            return;
        }
        activeCraftGuiEntry.markSkipped();
        activeCraftRequest = null;
        activeCraftGuiEntry = null;
    }

    private void updateSubmittedCrafts(long now) {
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.done || entry.cancelled) {
                continue;
            }
            if (entry.link == null) {
                entry.updateLinklessRequestState(now);
                continue;
            }
            if (entry.link.isCanceled()) {
                entry.markCancelled(now);
                continue;
            }
            if (entry.link.isDone()) {
                entry.markDone(now);
                continue;
            }
            if (entry.isRequestLost(now)) {
                entry.markCancelled(now);
            }
        }
    }

    private boolean shouldThrottleForCrafting() {
        if (activeCraftGuiEntry != null || activeCraftAmountEntry != null) {
            return true;
        }
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.shouldThrottle()) {
                return true;
            }
        }
        return false;
    }

    private boolean allSubmittedCraftsCancelled() {
        if (submittedCraftCount <= 0 || !allCraftingGuisOffered || activeCraftGuiEntry != null) {
            return false;
        }
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            if (entry.submitted && entry.link == null && !entry.cancelled) {
                return false;
            }
            if (entry.link != null && !entry.cancelled) {
                return false;
            }
        }
        return true;
    }

    private void cancelUnfinishedCrafts() {
        activeCraftRequest = null;
        activeCraftGuiEntry = null;
        if (activeCraftAmountEntry != null) {
            activeCraftAmountEntry.cancelAmountProbe();
            activeCraftAmountEntry = null;
        }
        for (CraftableMissingEntry entry : craftableMissingEntries) {
            entry.cancelAmountProbe();
            entry.cancelLink();
        }
    }

    static void applyTileNbt(World world, BlockPos realPos, IBlockState state, NBTTagCompound nbt) {
        if (nbt == null) {
            return;
        }
        TileEntity te = world.getTileEntity(realPos);
        if (te == null) {
            return;
        }
        try {
            te.readFromNBT(nbt);
        } catch (Exception | LinkageError e) {
            ModularMachinery.log.warn("Failed to apply NBT to TileEntity!", e);
            world.removeTileEntity(realPos);
            world.setTileEntity(realPos, state.getBlock().createTileEntity(world, state));
        }
    }

    private static final class RequiredItemEntry {
        private final ItemStack stack;
        private final List<BlockPos> positions = new ArrayList<>();
        private long amount;
        private long directAmount;

        private RequiredItemEntry(ItemStack stack, long amount, long directAmount, BlockPos relativePos) {
            this.stack = stack.copy();
            this.stack.setCount(1);
            this.amount = amount;
            this.directAmount = directAmount;
            this.positions.add(relativePos);
        }
    }

    private static final class ItemAvailabilityEntry {
        private final ItemStack stack;
        private long amount;

        private ItemAvailabilityEntry(ItemStack stack, long amount) {
            this.stack = stack.copy();
            this.stack.setCount(1);
            this.amount = amount;
        }
    }

    private static final class RequiredFluidEntry {
        private final FluidStack fluid;
        private final List<BlockPos> positions = new ArrayList<>();
        private long totalAmount;
        private long amount;

        private RequiredFluidEntry(FluidStack fluid, long amount, BlockPos relativePos) {
            this.fluid = fluid.copy();
            this.totalAmount = amount;
            this.amount = amount;
            this.positions.add(relativePos);
        }
    }

    static int craftRequestLostGraceTicks() {
        return CRAFT_REQUEST_LOST_GRACE_TICKS;
    }

    boolean isActiveCraftEntry(CraftableMissingEntry entry) {
        return activeCraftGuiEntry == entry || activeCraftAmountEntry == entry;
    }
    private boolean takeAeItem(ItemStack required, boolean crafted) {
        if (!(crafted ? Ae2GridAccess.extractCraftedItem(getPlayer(), required) : Ae2GridAccess.extractItemSilently(getPlayer(), required))) return false;
        payment = new MaterialPayment(() -> AssemblyUtils.giveOrDrop(getPlayer(), Ae2GridAccess.insertItem(getPlayer(), required.copy())));
        return true;
    }

    private boolean takeAeFluid(FluidStack required, boolean crafted) {
        if (!(crafted ? Ae2GridAccess.extractCraftedFluid(getPlayer(), required) : Ae2GridAccess.extractFluidSilently(getPlayer(), required))) return false;
        payment = new MaterialPayment(() -> {
            FluidStack remaining = Ae2GridAccess.insertFluid(getPlayer(), required.copy());
            if (remaining != null && remaining.amount > 0) {
                AssemblyUtils.returnFluid(getPlayer(), remaining);
            }
        });
        return true;
    }
}
