package rehdpanda.utilitygolems.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rehdpanda.utilitygolems.GolemJukeboxScreenHandler;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;

public class GolemJukeboxScreen extends HandledScreen<GolemJukeboxScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/generic_54.png");
    private ButtonWidget shuffleButton;
    private ButtonWidget repeatButton;
    private ButtonWidget playStopButton;

    public GolemJukeboxScreen(GolemJukeboxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        UtilityGolem golem = handler.getGolem();
        if (golem == null) return;

        // Play/Stop button
        this.playStopButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("▶/■"), button -> {
            ClientPlayNetworking.send(new UGInit.JukeboxActionPayload(golem.getId(), 0));
        }).dimensions(this.x + 38, this.y + 42, 30, 20).build());

        // Shuffle button
        this.shuffleButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("🔀"), button -> {
            ClientPlayNetworking.send(new UGInit.JukeboxActionPayload(golem.getId(), 1));
        }).dimensions(this.x + 73, this.y + 42, 30, 20).build());

        // Repeat button
        this.repeatButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("🔁"), button -> {
            ClientPlayNetworking.send(new UGInit.JukeboxActionPayload(golem.getId(), 2));
        }).dimensions(this.x + 108, this.y + 42, 30, 20).build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        
        // Draw generic_54 top part (title area and first row of slots)
        // Texture generic_54.png:
        // Title area (0, 0) to (176, 17)
        // One row of slots (0, 17) to (176, 35)
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0f, 0.0f, this.backgroundWidth, 35, 256, 256);
        
        // Filler for buttons (between 35 and 75) using the generic GUI color from the texture
        for (int g = 0; g < 9; g++) {
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, i, g*5 + 70, 0.0f, 6.0f, this.backgroundWidth, 5, 256, 256);
        }
        
        // Player inventory part from dispenser.png
        Identifier DISPENSER_TEXTURE = Identifier.of("minecraft", "textures/gui/container/dispenser.png");
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, DISPENSER_TEXTURE, i, j + 75, 0.0f, 75.0f, this.backgroundWidth, 91, 256, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        UtilityGolem golem = handler.getGolem();
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

            context.drawText(this.textRenderer, "Play: " + (golem.isJukeboxPlaying() ? "ON" : "OFF"), this.x + 8, this.y + 68, 0x404040, false);
            context.drawText(this.textRenderer, "Shuffle: " + (golem.isJukeboxShuffle() ? "ON" : "OFF"), this.x + 140, this.y + 40, 0x404040, false);
            context.drawText(this.textRenderer, "Repeat: " + (golem.isJukeboxRepeat() ? "ON" : "OFF"), this.x + 140, this.y + 55, 0x404040, false);
        }
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void drawButtonHighlight(DrawContext context, ButtonWidget button) {
        if (button == null) return;
        // Draw a light blue semi-transparent overlay to show it's "selected"
        context.fill(button.getX() - 1, button.getY() - 1, button.getX() + button.getWidth() + 1, button.getY() + button.getHeight() + 1, 0x4000FFFF);
    }
}
