package rehdpanda.utilitygolems.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import rehdpanda.utilitygolems.GolemJukeboxMenu;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;

public class GolemJukeboxScreen extends AbstractContainerScreen<GolemJukeboxMenu> implements MenuAccess<GolemJukeboxMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/dispenser.png");
    private static final Identifier GENERIC_54_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private Button shuffleButton;
    private Button repeatButton;
    private Button playStopButton;

    public GolemJukeboxScreen(GolemJukeboxMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 166 + 48);
        this.inventoryLabelY = 75 + 48;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (imageWidth - font.width(title)) / 2;
        this.titleLabelY = 6;
        
        UtilityGolem golem = menu.getGolem();
        if (golem == null) return;

        // Play/Stop button
        this.playStopButton = this.addRenderableWidget(Button.builder(Component.literal("▶/■"), button -> {
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.JukeboxActionPayload(golem.getId(), 0)));
            }
        }).bounds(this.leftPos + 38, this.topPos + 77, 30, 20).build());

        // Shuffle button
        this.shuffleButton = this.addRenderableWidget(Button.builder(Component.literal("🔀"), button -> {
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.JukeboxActionPayload(golem.getId(), 1)));
            }
        }).bounds(this.leftPos + 73, this.topPos + 77, 30, 20).build());

        // Repeat button
        this.repeatButton = this.addRenderableWidget(Button.builder(Component.literal("🔁"), button -> {
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.JukeboxActionPayload(golem.getId(), 2)));
            }
        }).bounds(this.leftPos + 108, this.topPos + 77, 30, 20).build());
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        
        // Draw top part (title and first row) from generic_54.png
        context.blit(RenderPipelines.GUI_TEXTURED, GENERIC_54_TEXTURE, i, j, 0.0f, 0.0f, this.imageWidth, 35, 256, 256);
        
        // Filler for buttons and extra height (now 88 to compensate for smaller top part)
        int extraHeight = 88;
        for (int k = 0; k < extraHeight; k += 5) {
            int h = Math.min(5, extraHeight - k);
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j + 35 + k, 0.0f, 7.0f, this.imageWidth, h, 256, 256);
        }
        
        // Draw the player inventory part from dispenser.png (starts at 75 in texture)
        // Its absolute position remains the same: j + 35 + 88 = j + 123
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j + 35 + extraHeight, 0.0f, 75.0f, this.imageWidth, 91, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.extractTransparentBackground(context);
        super.extractRenderState(context, mouseX, mouseY, delta);
        
        // Draw the container title
        context.text(this.font, this.title, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY, 0xFF404040, false);
        
        // Draw the player inventory title
        context.text(this.font, this.playerInventoryTitle, this.leftPos + this.inventoryLabelX, this.topPos + this.inventoryLabelY, 0xFF404040, false);

        UtilityGolem golem = menu.getGolem();
        if (golem != null) {
            // Draw highlights for active buttons
            if (golem.isJukeboxPlaying()) {
                drawButtonHighlight(context, this.playStopButton);
            }
            if (golem.isJukeboxShuffle()) {
                drawButtonHighlight(context, this.shuffleButton);
            }
            if (golem.isJukeboxRepeat()) {
                drawButtonHighlight(context, this.repeatButton);
            }

            context.text(this.font, "Play: " + (golem.isJukeboxPlaying() ? "ON" : "OFF"), this.leftPos + 8, this.topPos + 102, 0xFF404040, false);
            context.text(this.font, "Shuffle: " + (golem.isJukeboxShuffle() ? "ON" : "OFF"), this.leftPos + 140, this.topPos + 75, 0xFF404040, false);
            context.text(this.font, "Repeat: " + (golem.isJukeboxRepeat() ? "ON" : "OFF"), this.leftPos + 140, this.topPos + 90, 0xFF404040, false);
        }
        this.extractTooltip(context, mouseX, mouseY);
    }

    private void drawButtonHighlight(GuiGraphicsExtractor context, Button button) {
        if (button == null) return;
        // Draw a light blue semi-transparent overlay to show it's "selected"
        context.fill(button.getX() - 1, button.getY() - 1, button.getX() + button.getWidth() + 1, button.getY() + button.getHeight() + 1, 0x4000FFFF);
    }
}
