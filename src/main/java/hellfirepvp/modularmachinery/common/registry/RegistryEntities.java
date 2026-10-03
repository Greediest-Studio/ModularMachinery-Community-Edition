package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.entity.EntityImprovedMeteor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

public final class RegistryEntities {
    private RegistryEntities() {}

    public static void register(IForgeRegistry<EntityEntry> registry) {
        if (Mods.BM2.isPresent()) {
            registry.register(EntityEntryBuilder.create()
                .id(new ResourceLocation(ModularMachinery.MODID, "improved_meteor"), 0)
                .entity(EntityImprovedMeteor.class).name("improved_meteor")
                .tracker(64, 3, true).build());
        }
    }
}
