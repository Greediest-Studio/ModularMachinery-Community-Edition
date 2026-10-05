package hellfirepvp.modularmachinery.common.machine.assembly;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.network.PktAssemblyReport;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import hellfirepvp.modularmachinery.common.util.FluidUtils;
import hellfirepvp.modularmachinery.common.util.StructureIngredient;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MachineAssembly implements BuildTask {
    protected final World               world;
    protected final BlockPos            ctrlPos;
    protected final EntityPlayer        player;
    private final TileEntity controller;
    private       StructureIngredient ingredient;

    protected final MissingMaterialsReport missingMaterials = new MissingMaterialsReport();
    protected final AssemblyOptions options;
    protected MaterialPayment payment;
    private boolean cancelled;

    public MachineAssembly(World world, BlockPos pos, EntityPlayer player, StructureIngredient ingredient, AssemblyOptions options) {
        this.world = world;
        this.ctrlPos = pos;
        this.player = player;
        this.controller = world.getTileEntity(pos);
        this.ingredient = ingredient;
        this.options = options;
    }

    public MachineAssembly(final World world, final BlockPos ctrlPos, final EntityPlayer player, final StructureIngredient ingredient) {
        this(world, ctrlPos, player, ingredient, AssemblyOptions.simple(0));
    }

    public static List<StructureIngredient.FluidIngredient> buildFluidIngredients(final List<ItemStack> inventory,
                                                                                  final List<StructureIngredient.FluidIngredient> fluidIngredients) {
        List<StructureIngredient.FluidIngredient> result = new ArrayList<>();
        List<IFluidHandlerItem> fluidHandlers = getFluidHandlerItems(inventory);

        Iterator<StructureIngredient.FluidIngredient> fluidIngredientIter = fluidIngredients.iterator();
        fluidIngredient:
        while (fluidIngredientIter.hasNext()) {
            final StructureIngredient.FluidIngredient fluidIngredient = fluidIngredientIter.next();
            final BlockPos pos = fluidIngredient.pos();

            for (final Tuple<FluidStack, IBlockState> tuple : fluidIngredient.ingredientList()) {
                FluidStack required = tuple.getFirst();
                IBlockState state = tuple.getSecond();

                if (!consumeInventoryFluid(required, fluidHandlers)) {
                    continue;
                }

                result.add(new StructureIngredient.FluidIngredient(
                    pos, Collections.singletonList(new Tuple<>(required, state)))
                );
                fluidIngredientIter.remove();
                continue fluidIngredient;
            }
        }

        return result;
    }

    private static List<IFluidHandlerItem> getFluidHandlerItems(final List<ItemStack> inventory) {
        List<IFluidHandlerItem> fluidHandlers = new ArrayList<>();
        for (final ItemStack invStack : inventory) {
            IFluidHandlerItem fluidHandler = getSupportedFluidHandler(invStack);
            if (fluidHandler != null) {
                fluidHandlers.add(fluidHandler);
            }
        }
        return fluidHandlers;
    }

    private static IFluidHandlerItem getSupportedFluidHandler(ItemStack stack) {
        Item item = stack.getItem();
        // Buckets remain unsupported by automatic assembly.
        if (item instanceof UniversalBucket || item == Items.LAVA_BUCKET || item == Items.WATER_BUCKET
            || !FluidUtils.isFluidHandler(stack)) return null;
        return FluidUtil.getFluidHandler(stack);
    }

    public static List<StructureIngredient.ItemIngredient> buildItemIngredients(final List<ItemStack> inventory,
                                                                                final List<StructureIngredient.ItemIngredient> itemIngredients) {
        List<StructureIngredient.ItemIngredient> result = new ArrayList<>();

        Iterator<StructureIngredient.ItemIngredient> iterator = itemIngredients.iterator();
        itemIngredient:
        while (iterator.hasNext()) {
            StructureIngredient.ItemIngredient ingredient = iterator.next();

            for (final Tuple<ItemStack, IBlockState> tuple : ingredient.ingredientList()) {
                ItemStack required = tuple.getFirst();
                if (!consumeInventoryItem(required, inventory)) {
                    continue;
                }

                result.add(new StructureIngredient.ItemIngredient(
                    ingredient.pos(), Collections.singletonList(tuple), ingredient.nbt())
                );

                iterator.remove();
                continue itemIngredient;
            }
        }

        return result;
    }

    public static void searchAndRemoveContainItem(final List<ItemStack> inventory,
                                                  final List<StructureIngredient.ItemIngredient> itemIngredients) {
        Iterator<StructureIngredient.ItemIngredient> itemIngredientIter = itemIngredients.iterator();
        itemIngredient:
        while (itemIngredientIter.hasNext()) {
            final StructureIngredient.ItemIngredient ingredient = itemIngredientIter.next();

            for (final Tuple<ItemStack, IBlockState> tuple : ingredient.ingredientList()) {
                ItemStack required = tuple.getFirst();
                if (required.isEmpty() || consumeInventoryItem(required, inventory)) {
                    itemIngredientIter.remove();
                    continue itemIngredient;
                }
            }
        }
    }

    public static void searchAndRemoveContainFluid(final List<ItemStack> inventory,
                                                   final List<StructureIngredient.FluidIngredient> fluidIngredients) {
        List<IFluidHandlerItem> fluidHandlers = getFluidHandlerItems(inventory);

        Iterator<StructureIngredient.FluidIngredient> fluidIngredientIter = fluidIngredients.iterator();
        fluidIngredient:
        while (fluidIngredientIter.hasNext()) {
            final StructureIngredient.FluidIngredient fluidIngredient = fluidIngredientIter.next();

            for (final Tuple<FluidStack, IBlockState> tuple : fluidIngredient.ingredientList()) {
                FluidStack required = tuple.getFirst();

                if (consumeInventoryFluid(required, fluidHandlers)) {
                    fluidIngredientIter.remove();
                    continue fluidIngredient;
                }
            }
        }
    }

    public static boolean checkAllItems(EntityPlayer player, StructureIngredient ingredient) {
        List<ItemStack> inventory = player.inventory.mainInventory.stream()
                                                                  .map(ItemStack::copy)
                                                                  .collect(Collectors.toList());

        List<StructureIngredient.ItemIngredient> itemIngredientList = ingredient.itemIngredient();
        List<StructureIngredient.FluidIngredient> fluidIngredientList = ingredient.fluidIngredient();
        searchAndRemoveContainItem(inventory, itemIngredientList);
        searchAndRemoveContainFluid(inventory, fluidIngredientList);

        if (itemIngredientList.isEmpty() && fluidIngredientList.isEmpty()) {
            return true;
        }
        List<List<ItemStack>> itemStackIngList = getItemStackIngList(itemIngredientList);
        List<List<FluidStack>> fluidStackIngList = getFluidStackIngList(fluidIngredientList);

        PktAssemblyReport pkt = new PktAssemblyReport(
            itemStackIngList,
            fluidStackIngList);

        if (player instanceof EntityPlayerMP) {
            ModularMachinery.NET_CHANNEL.sendTo(pkt, (EntityPlayerMP) player);
        }

        return false;
    }

    private static List<List<FluidStack>> getFluidStackIngList(final List<StructureIngredient.FluidIngredient> fluidIngredientList) {
        List<List<FluidStack>> fluidStackIngList = new ArrayList<>();

        fluidIng:
        for (final StructureIngredient.FluidIngredient ingredient : fluidIngredientList) {
            if (ingredient.ingredientList().isEmpty()) {
                continue;
            }

            List<FluidStack> stackIngList = ingredient.ingredientList()
                                                      .stream()
                                                      .map(tuple -> tuple.getFirst().copy())
                                                      .collect(Collectors.toList());

            if (stackIngList.size() == 1) {
                FluidStack ing = stackIngList.get(0);

                for (final List<FluidStack> fluidStackList : fluidStackIngList) {
                    if (fluidStackList.size() != 1) {
                        continue;
                    }

                    FluidStack another = fluidStackList.get(0);
                    if (ing.isFluidEqual(another)) {
                        another.amount += 1000;
                        continue fluidIng;
                    }
                }
            }

            fluidStackIngList.add(stackIngList);
        }

        return fluidStackIngList;
    }

    private static List<List<ItemStack>> getItemStackIngList(final List<StructureIngredient.ItemIngredient> itemIngredientList) {
        List<List<ItemStack>> stackList = new ArrayList<>();

        itemIng:
        for (final StructureIngredient.ItemIngredient itemIng : itemIngredientList) {
            if (itemIng.ingredientList().isEmpty()) {
                continue;
            }

            @SuppressWarnings("SimplifyStreamApiCallChains")
            List<ItemStack> stackIngList = itemIng.ingredientList()
                                                  .stream()
                                                  .map(tuple -> tuple.getFirst().copy())
                                                  .collect(Collectors.toList());
            if (stackIngList.size() == 1) {
                ItemStack ing = stackIngList.get(0);

                for (final List<ItemStack> itemStackList : stackList) {
                    if (itemStackList.size() != 1) {
                        continue;
                    }

                    ItemStack anotherInput = itemStackList.get(0);
                    if (ItemUtils.matchStacks(ing, anotherInput)) {
                        anotherInput.grow(1);
                        continue itemIng;
                    }
                }
            }

            List<ItemStack> filteredStackIngList = new ArrayList<>();

            filter:
            for (final ItemStack stack : stackIngList) {
                for (final ItemStack filtered : filteredStackIngList) {
                    if (ItemUtils.matchStacks(stack, filtered)) {
                        continue filter;
                    }
                }

                filteredStackIngList.add(stack);
            }

            stackList.add(filteredStackIngList);
        }

        return stackList;
    }

    public static boolean consumeInventoryItem(final ItemStack required, final List<ItemStack> inventory) {
        int available = inventory.stream().filter(stack -> ItemUtils.matchStacks(stack, required)).mapToInt(ItemStack::getCount).sum();
        if (available < required.getCount()) return false;
        int remaining = required.getCount();
        for (ItemStack stack : inventory) {
            if (!ItemUtils.matchStacks(stack, required)) continue;
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken); remaining -= taken;
            if (remaining == 0) return true;
        }
        return false;
    }

    public static boolean fillInventoryFluid(final FluidStack required, final List<IFluidHandlerItem> fluidHandlers) {
        for (final IFluidHandlerItem fluidHandler : fluidHandlers) {
            if (fluidHandler.fill(required.copy(), false) >= required.amount) {
                return fluidHandler.fill(required.copy(), true) == required.amount;
            }
        }
        return false;
    }

    public static boolean consumeInventoryFluid(final FluidStack required, final List<IFluidHandlerItem> fluidHandlers) {
        for (final IFluidHandlerItem fluidHandler : fluidHandlers) {
            FluidStack drained = fluidHandler.drain(required.copy(), false);
            if (drained == null || !drained.containsFluid(required)) {
                continue;
            }

            fluidHandler.drain(required.copy(), true);
            return true;
        }
        return false;
    }

    public World getWorld() { return world; }
    public BlockPos getCtrlPos() { return ctrlPos; }
    public EntityPlayer getPlayer() { return player; }
    public StructureIngredient getIngredient() { return ingredient; }
    public int getTickInterval() { return options.tickInterval(); }
    public int getOperationsPerTick() { return options.operationsPerTick(); }
    public boolean isCompleted() { return ingredient.itemIngredient().isEmpty() && ingredient.fluidIngredient().isEmpty(); }
    public boolean isCancelled() { return cancelled; }
    public void cancel() { cancelled = true; }
    public boolean isControllerInvalid() {
        return !world.isBlockLoaded(ctrlPos) || !(controller instanceof TileMultiblockMachineController)
            || world.getTileEntity(ctrlPos) != controller;
    }
    public void tick() { assembly(!player.isCreative()); }
    public void report() { missingMaterials.report(player, isCompleted()); }
    public String getCancelledMessageKey() { return "message.modularmachinery.builder.cancelled"; }
    public String getSuccessMessageKey() {
        return missingMaterials.hasSkipped() ? "message.modularmachinery.builder.partial" : "message.modularmachinery.builder.success";
    }

    public void buildIngredients(boolean consumeInventory) {
        List<ItemStack> inventory = player.inventory.mainInventory;
        if (!consumeInventory) inventory = inventory.stream().map(ItemStack::copy).collect(Collectors.toList());
        ingredient = new StructureIngredient(buildItemIngredients(inventory, ingredient.itemIngredient()),
            buildFluidIngredients(inventory, ingredient.fluidIngredient()));
    }

    public void assembly(boolean consumeInventory) {
        if (!ingredient.itemIngredient().isEmpty()) {
            StructureIngredient.ItemIngredient entry = ingredient.itemIngredient().remove(0);
            BlockPos pos = ctrlPos.add(entry.pos());
            if (!replaceCheck(pos)) return;
            if (!options.advanced && AssemblyConfig.skipBlockContainNBT && entry.nbt() != null) {
                missingMaterials.markSkipped();
                return;
            }
            for (Tuple<ItemStack, IBlockState> candidate : selectItemCandidatesForPosition(entry.pos(), entry.ingredientList())) {
                payment = MaterialPayment.FREE;
                if (consumeInventory && !consumeItem(entry.pos(), candidate.getFirst())) continue;
                if (placeAssemblyBlock(pos, candidate.getSecond())) applyTileNbt(world, pos, entry.nbt());
                else { payment.refund(); missingMaterials.markSkipped(); }
                return;
            }
            if (entry.ingredientList().isEmpty()) { missingMaterials.markSkipped(); return; }
            ItemStack required = selectRequirementItemStack(entry.pos(), entry.ingredientList());
            if (shouldWaitForItemCraft(entry.pos(), required)) ingredient.itemIngredient().add(entry);
            else { missingMaterials.addItem(required); missingMaterials.markSkipped(); }
        } else if (!ingredient.fluidIngredient().isEmpty()) {
            StructureIngredient.FluidIngredient entry = ingredient.fluidIngredient().remove(0);
            BlockPos pos = ctrlPos.add(entry.pos());
            if (!replaceCheck(pos)) return;
            for (Tuple<FluidStack, IBlockState> candidate : selectFluidCandidatesForPosition(entry.pos(), entry.ingredientList())) {
                payment = MaterialPayment.FREE;
                if (consumeInventory && !consumeFluid(entry.pos(), candidate.getFirst())) continue;
                if (!placeAssemblyBlock(pos, candidate.getSecond())) { payment.refund(); missingMaterials.markSkipped(); }
                return;
            }
            if (entry.ingredientList().isEmpty()) { missingMaterials.markSkipped(); return; }
            FluidStack required = selectRequirementFluidStack(entry.pos(), entry.ingredientList());
            if (shouldWaitForFluidCraft(entry.pos(), required)) ingredient.fluidIngredient().add(entry);
            else { missingMaterials.addFluid(required); missingMaterials.markSkipped(); }
        }
    }

    protected List<Tuple<ItemStack, IBlockState>> selectItemCandidatesForPosition(BlockPos pos, List<Tuple<ItemStack, IBlockState>> candidates) { return candidates; }
    protected List<Tuple<FluidStack, IBlockState>> selectFluidCandidatesForPosition(BlockPos pos, List<Tuple<FluidStack, IBlockState>> candidates) { return candidates; }
    protected ItemStack selectRequirementItemStack(BlockPos pos, List<Tuple<ItemStack, IBlockState>> candidates) { return candidates.get(0).getFirst(); }
    protected FluidStack selectRequirementFluidStack(BlockPos pos, List<Tuple<FluidStack, IBlockState>> candidates) { return candidates.get(0).getFirst(); }
    protected boolean shouldWaitForItemCraft(BlockPos pos, ItemStack item) { return false; }
    protected boolean shouldWaitForFluidCraft(BlockPos pos, FluidStack fluid) { return false; }
    protected boolean consumeItem(BlockPos pos, ItemStack required) { return takeInventoryItem(required); }
    protected boolean consumeFluid(BlockPos pos, FluidStack required) { return takeInventoryFluid(required); }
    protected boolean takeInventoryItem(ItemStack required) {
        payment = InventoryMaterials.takeItem(player, required);
        return payment != null;
    }
    protected boolean takeInventoryFluid(FluidStack required) {
        payment = InventoryMaterials.takeFluid(player, required);
        return payment != null;
    }

    private boolean replaceCheck(BlockPos pos) {
        if (pos.equals(ctrlPos)) return false;
        if (!world.isBlockLoaded(pos) || world.isOutsideBuildHeight(pos) || !AssemblyUtils.isReplaceableForAssembly(world, pos)
            || !world.isBlockModifiable(player, pos) || !player.canPlayerEdit(pos, net.minecraft.util.EnumFacing.UP, player.getHeldItem(options.hand))) {
            missingMaterials.markSkipped();
            return false;
        }
        return true;
    }

    /** 使用真实原方块快照触发 Forge 放置事件；取消时还原方块和 TE。 */
    protected boolean placeAssemblyBlock(BlockPos pos, IBlockState state) {
        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, pos);
        IBlockState original = world.getBlockState(pos);
        if (!world.setBlockState(pos, state)) return false;
        BlockEvent.PlaceEvent event = new BlockEvent.PlaceEvent(snapshot, original, player, options.hand);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) { snapshot.restore(true, false); return false; }
        world.playSound(null, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1F, 1F);
        return true;
    }

    public static void applyTileNbt(World world, BlockPos pos, NBTTagCompound nbt) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile == null || nbt == null) return;
        NBTTagCompound data = tile.writeToNBT(new NBTTagCompound());
        String id = data.getString("id");
        data.merge(nbt.copy());
        data.setString("id", id);
        data.setInteger("x", pos.getX()); data.setInteger("y", pos.getY()); data.setInteger("z", pos.getZ());
        tile.readFromNBT(data);
        tile.setPos(pos);
        tile.markDirty();
    }
}
