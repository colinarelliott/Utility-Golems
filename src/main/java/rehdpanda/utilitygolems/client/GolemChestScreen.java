package rehdpanda.utilitygolems.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import rehdpanda.utilitygolems.GolemChestMenu;

public class GolemChestScreen extends AbstractContainerScreen<GolemChestMenu> implements MenuAccess<GolemChestMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    public GolemChestScreen(GolemChestMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 114 + handler.getRows() * 18);
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int i = this.leftPos;
        int j = this.topPos;
        int rows = this.menu.getRows();
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0, 0, this.imageWidth, rows * 18 + 17, 256, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j + rows * 18 + 17, 0, 126, this.imageWidth, 96, 256, 256);

        super.extractContents(context, mouseX, mouseY, delta);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractLabels(context, mouseX, mouseY);
        if (this.menu.isGolemDead()) {
            context.text(this.font, "☠", this.imageWidth - 20, 6, 0xFFAA0000, false);
        }
    }
}
