/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerController;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiMachineController
 * Created by HellFirePvP
 * Date: 12.07.2017 / 23:34
 */
public class GuiMachineController extends GuiControllerBase<ContainerController> {

    public static final ResourceLocation TEXTURES_CONTROLLER = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guicontroller_large.png");

    private final TileMachineController controller;

    public GuiMachineController(TileMachineController controller, EntityPlayer opening) {
        super(new ContainerController(controller, opening), 7);
        this.controller = controller;
        this.ySize = 213;
    }

    @Override
    protected void addControllerStatus() {
        statusPanel.addLine(I18n.format("gui.controller.status"));
        statusPanel.addLine(I18n.format(controller.getControllerStatus().getUnlocMessage()));
        statusPanel.addParagraphSpacing();

        ActiveMachineRecipe activeRecipe = controller.getActiveRecipe();
        if (activeRecipe != null) {
            if (activeRecipe.getTotalTick() > 0) {
                int progress = (activeRecipe.getTick() * 100) / activeRecipe.getTotalTick();
                statusPanel.addLine(I18n.format("gui.controller.status.crafting.progress", progress + "%"));
                statusPanel.addParagraphSpacing();
            }

            int parallelism = activeRecipe.getParallelism();
            int maxParallelism = activeRecipe.getMaxParallelism();
            if (parallelism > 1) {
                statusPanel.addLine(I18n.format("gui.controller.parallelism", parallelism));
                statusPanel.addLine(I18n.format("gui.controller.max_parallelism", maxParallelism));
                statusPanel.addParagraphSpacing();
            }
        }

        addPerformanceInfo();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_CONTROLLER);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void setWidthHeight() {
    }
}
