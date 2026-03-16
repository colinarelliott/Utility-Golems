package rehdpanda.utilitygolems.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rehdpanda.utilitygolems.GolemChestScreenHandler;

public class GolemChestScreen extends HandledScreen<GolemChestScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/generic_54.png");

    public GolemChestScreen(GolemChestScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        int rows = handler.getRows();
        this.backgroundHeight = 114 + rows * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        if (this.handler.isGolemDead()) {
            int xPos = this.x + this.backgroundWidth - 20;
            int yPos = this.y + 6;
            if (mouseX >= xPos && mouseX < xPos + 9 && mouseY >= yPos && mouseY < yPos + 9) {
                context.drawTooltip(this.textRenderer, Text.translatable("gui.utility-golems.golem_dead"), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        if (this.handler.isGolemDead()) {
            // Position it to the right of the title
            int xPos = this.backgroundWidth - 20;
            // Skull icon using Unicode
            context.drawText(this.textRenderer, "☠", xPos, 6, 0xFFAA0000, false);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        int rows = this.handler.getRows();
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0, 0, this.backgroundWidth, rows * 18 + 17, 256, 256);
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, i, j + rows * 18 + 17, 0, 126, this.backgroundWidth, 96, 256, 256);
    }
}
