package rehdpanda.utilitygolems.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import rehdpanda.utilitygolems.GolemFurnaceMenu;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class GolemFurnaceScreen extends AbstractContainerScreen<GolemFurnaceMenu> implements MenuAccess<GolemFurnaceMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/furnace.png");

    public GolemFurnaceScreen(GolemFurnaceMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int i = this.leftPos;
        int j = this.topPos;
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.isLit()) {
            int k = this.menu.getBurnProgress();
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 56, j + 36 + 12 - k, 176.0f, 12.0f - k, 14, k + 1, 256, 256);
        }
        int l = this.menu.getCookProgress();
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 79, j + 34, 176.0f, 14.0f, l + 1, 16, 256, 256);
        super.extractContents(context, mouseX, mouseY, delta);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.extractTransparentBackground(context);
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.text(this.font, this.title, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY, 0xFF404040, false);
        context.text(this.font, this.playerInventoryTitle, this.leftPos + this.inventoryLabelX, this.topPos + this.inventoryLabelY, 0xFF404040, false);
        this.extractTooltip(context, mouseX, mouseY);
    }
}
