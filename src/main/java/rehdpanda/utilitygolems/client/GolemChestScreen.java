package rehdpanda.utilitygolems.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import rehdpanda.utilitygolems.GolemChestMenu;

public class GolemChestScreen extends AbstractContainerScreen<GolemChestMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    public GolemChestScreen(GolemChestMenu handler, Inventory inventory, Component title) {
        super(handler, getInventory(), title);
        int rows = handler.getRows();
        this.imageHeight = 114 + rows * 18;
        this.inventory.abelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.renderTooltip(context, mouseX, mouseY);

        if (this.menu.isGolemDead()) {
            int xPos = this.leftPos + this.imageWidth - 20;
            int yPos = this.topPos + 6;
            if (mouseX >= xPos && mouseX < xPos + 9 && mouseY >= yPos && mouseY < yPos + 9) {
                context.renderTooltip(this.font, Component.translatable("gui.utility-golems.golem_dead"), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        super.renderLabels(context, mouseX, mouseY);
        if (this.menu.isGolemDead()) {
            // Position it to the right of the title
            int xPos = this.imageWidth - 20;
            // Skull icon using Unicode
            context.drawString(this.font, "☠", xPos, 6, 0xFFAA0000, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int rows = this.menu.getRows();
        context.blit(TEXTURE, i, j, 0, 0, this.imageWidth, rows * 18 + 17, 256, 256);
        context.blit(TEXTURE, i, j + rows * 18 + 17, 0, 126, this.imageWidth, 96, 256, 256);
    }
}
