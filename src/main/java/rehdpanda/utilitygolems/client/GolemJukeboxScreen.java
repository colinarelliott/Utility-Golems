package rehdpanda.utilitygolems.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.JukeboxSong;
import rehdpanda.utilitygolems.GolemJukeboxMenu;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;

public class GolemJukeboxScreen extends AbstractContainerScreen<GolemJukeboxMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private static final int TEXTURE_SIZE = 256;

    // Source regions inside generic_54.png (the GUI itself is 176x222 in the top-left of the file):
    //   v   0..34  border/title strip plus one row of slots
    //   v 125..138 blank panel, tiled to fill the transport control area
    //   v 126..221 the player inventory panel
    private static final int FILLER_V = 125;
    private static final int FILLER_HEIGHT = 14;
    private static final int PLAYER_PANEL_V = 126;
    private static final int PLAYER_PANEL_HEIGHT = 96;

    private static final int NOW_PLAYING_Y = 39;
    private static final int BUTTON_Y = GolemJukeboxMenu.PLAYER_INVENTORY_Y - 30;
    private static final int BUTTON_WIDTH = 52;
    private static final int BUTTON_HEIGHT = 20;

    private Button playStopButton;
    private Button shuffleButton;
    private Button repeatButton;

    public GolemJukeboxScreen(GolemJukeboxMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, GolemJukeboxMenu.PLAYER_INVENTORY_Y + PLAYER_PANEL_HEIGHT);
        this.titleLabelY = 6;
        this.inventoryLabelY = GolemJukeboxMenu.PLAYER_INVENTORY_Y + 3;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        this.playStopButton = this.addRenderableWidget(Button.builder(Component.empty(), b -> sendAction(0))
                .bounds(this.leftPos + 8, this.topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        this.shuffleButton = this.addRenderableWidget(Button.builder(Component.empty(), b -> sendAction(1))
                .bounds(this.leftPos + 62, this.topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        this.repeatButton = this.addRenderableWidget(Button.builder(Component.empty(), b -> sendAction(2))
                .bounds(this.leftPos + 116, this.topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        refreshButtons();
    }

    private void sendAction(int actionId) {
        UtilityGolem golem = this.menu.getGolem();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (golem == null || connection == null) return;
        connection.send(new ServerboundCustomPayloadPacket(new UGInit.JukeboxActionPayload(golem.getId(), actionId)));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // The golem's play/shuffle/repeat flags are tracked entity data, so the button
        // labels have to follow the server rather than the last click.
        refreshButtons();
    }

    private void refreshButtons() {
        UtilityGolem golem = this.menu.getGolem();
        boolean available = golem != null;

        this.playStopButton.setMessage(available && golem.isJukeboxPlaying()
                ? Component.literal("■ Stop").withStyle(ChatFormatting.RED)
                : Component.literal("▶ Play").withStyle(ChatFormatting.GREEN));
        this.shuffleButton.setMessage(Component.literal("Shuffle")
                .withStyle(available && golem.isJukeboxShuffle() ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        this.repeatButton.setMessage(Component.literal("Repeat")
                .withStyle(available && golem.isJukeboxRepeat() ? ChatFormatting.GREEN : ChatFormatting.GRAY));

        this.playStopButton.active = available;
        this.shuffleButton.active = available;
        this.repeatButton.active = available;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int x = this.leftPos;
        int y = this.topPos;

        // Title strip plus the row of nine playlist slots.
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, GolemJukeboxMenu.TOP_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Blank panel behind the transport controls.
        for (int drawn = 0; drawn < GolemJukeboxMenu.CONTROLS_HEIGHT; drawn += FILLER_HEIGHT) {
            int height = Math.min(FILLER_HEIGHT, GolemJukeboxMenu.CONTROLS_HEIGHT - drawn);
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + GolemJukeboxMenu.TOP_HEIGHT + drawn, 0.0F, FILLER_V, this.imageWidth, height, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Player inventory panel.
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + GolemJukeboxMenu.PLAYER_INVENTORY_Y, 0.0F, PLAYER_PANEL_V, this.imageWidth, PLAYER_PANEL_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Draws the widgets, then the labels and slots on top of them.
        super.extractContents(context, mouseX, mouseY, delta);

        UtilityGolem golem = this.menu.getGolem();
        if (golem != null) {
            if (golem.isJukeboxPlaying()) outlineActive(context, this.playStopButton);
            if (golem.isJukeboxShuffle()) outlineActive(context, this.shuffleButton);
            if (golem.isJukeboxRepeat()) outlineActive(context, this.repeatButton);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractLabels(context, mouseX, mouseY);
        context.text(this.font, nowPlayingText(), 8, NOW_PLAYING_Y, 0xFF404040, false);
    }

    private Component nowPlayingText() {
        UtilityGolem golem = this.menu.getGolem();
        if (golem == null) {
            return Component.literal("Golem unavailable").withStyle(ChatFormatting.DARK_RED);
        }
        if (!golem.isJukeboxPlaying()) {
            return Component.literal("Stopped");
        }
        return JukeboxSong.fromStack(golem.getHeldItem())
                .filter(Holder::isBound)
                .<Component>map(song -> Component.literal("Playing: ").append(song.value().description()))
                .orElse(Component.literal("Playing..."));
    }

    /** Green frame marking a toggle that is currently on. Uses screen coordinates. */
    private void outlineActive(GuiGraphicsExtractor context, Button button) {
        if (button == null) return;
        int x1 = button.getX() - 1;
        int y1 = button.getY() - 1;
        int x2 = button.getX() + button.getWidth() + 1;
        int y2 = button.getY() + button.getHeight() + 1;
        int color = 0xFF55FF55;
        context.fill(x1, y1, x2, y1 + 1, color);
        context.fill(x1, y2 - 1, x2, y2, color);
        context.fill(x1, y1, x1 + 1, y2, color);
        context.fill(x2 - 1, y1, x2, y2, color);
    }
}
