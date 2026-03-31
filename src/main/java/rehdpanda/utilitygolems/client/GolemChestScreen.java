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
        int rows = handler.getRows();
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.extractTransparentBackground(context);
        super.extractRenderState(context, mouseX, mouseY, delta);
        this.extractTooltip(context, mouseX, mouseY);

        if (this.menu.isGolemDead()) {
            int xPos = this.leftPos + this.imageWidth - 20;
            int yPos = this.topPos + 6;
            if (mouseX >= xPos && mouseX < xPos + 9 && mouseY >= yPos && mouseY < yPos + 9) {
                this.extractTooltip(context, mouseX, mouseY); // Standard way to handle tooltips in 26.1
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractLabels(context, mouseX, mouseY);
        if (this.menu.isGolemDead()) {
            // Position it to the right of the title
            int xPos = this.imageWidth - 20;
            // Skull icon using Unicode
            context.text(this.font, "☠", xPos, 6, 0xFFAA0000, false);
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int rows = this.menu.getRows();
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0, 0, this.imageWidth, rows * 18 + 17, 256, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j + rows * 18 + 17, 0, 126, this.imageWidth, 96, 256, 256);
    }
}
