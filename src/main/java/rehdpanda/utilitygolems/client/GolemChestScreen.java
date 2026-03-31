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
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.extractTransparentBackground(context);
        super.extractRenderState(context, mouseX, mouseY, delta);
        
        // Draw the title
        context.text(this.font, this.title, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY, 0xFF404040, false);

        // Draw the inventory label
        context.text(this.font, this.playerInventoryTitle, this.leftPos + this.inventoryLabelX, this.topPos + this.inventoryLabelY, 0xFF404040, false);

        if (this.menu.isGolemDead()) {
            int xPos = this.leftPos + this.imageWidth - 20;
            int yPos = this.topPos + 6;
            // Skull icon using Unicode
            context.text(this.font, "☠", xPos, yPos, 0xFFAA0000, false);
        }
        
        this.extractTooltip(context, mouseX, mouseY);
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
