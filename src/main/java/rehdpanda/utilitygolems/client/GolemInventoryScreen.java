package rehdpanda.utilitygolems.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rehdpanda.utilitygolems.BuildPattern;
import rehdpanda.utilitygolems.GolemInventoryScreenHandler;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;

/// DRAWS THE INVENTORY SCREEN FOR GOLEMS

public class GolemInventoryScreen extends HandledScreen<GolemInventoryScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/dispenser.png");

    public GolemInventoryScreen(GolemInventoryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        
        UtilityGolem golem = handler.getGolem();
        if (golem != null && golem.getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND) {
            this.backgroundHeight += 40; // Always use max height to avoid layout issues
        }
    }

    private void sendSyncPacket(UtilityGolem golem) {
        ClientPlayNetworking.send(new UGInit.SyncPatternPayload(
                golem.getId(),
                golem.getBuildPattern().ordinal(),
                golem.getWallWidth(),
                golem.getWallLength(),
                golem.getHeldItem(),
                golem.isBuildingStarted()
        ));
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;

        UtilityGolem golem = handler.getGolem();
        if (golem != null && golem.getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND) {
            // Pattern cycle button
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Mode: " + golem.getBuildPattern().getDisplayName()), button -> {
                BuildPattern next = BuildPattern.values()[(golem.getBuildPattern().ordinal() + 1) % BuildPattern.values().length];
                golem.setBuildPattern(next);
                golem.setBuildingStarted(false); // Stop when pattern changes
                button.setMessage(Text.literal("Mode: " + next.getDisplayName()));
                sendSyncPacket(golem);
                this.clearAndInit(); // Re-init to show/hide width/length buttons
            }).dimensions(x + 7, y + 71, 135, 20).build());

            // Start/Stop button
            String startLabel = golem.isBuildingStarted() ? "§cStop" : "§aStart";
            this.addDrawableChild(ButtonWidget.builder(Text.literal(startLabel), button -> {
                golem.setBuildingStarted(!golem.isBuildingStarted());
                sendSyncPacket(golem);
                button.setMessage(Text.literal(golem.isBuildingStarted() ? "§cStop" : "§aStart"));
            }).dimensions(x + 144, y + 71, 25, 20).build());

            if (golem.getBuildPattern() == BuildPattern.WALL) {
                // Width adjustment
                this.addDrawableChild(ButtonWidget.builder(Text.literal("W: " + golem.getWallWidth()), button -> {
                    int nextWidth = (golem.getWallWidth() % 10) + 1;
                    golem.setWallWidth(nextWidth);
                    button.setMessage(Text.literal("W: " + nextWidth));
                    sendSyncPacket(golem);
                }).dimensions(x + 7, y + 91, 80, 20).build());

                // Length adjustment
                this.addDrawableChild(ButtonWidget.builder(Text.literal("L: " + golem.getWallLength()), button -> {
                    int nextLength = (golem.getWallLength() % 10) + 1;
                    golem.setWallLength(nextLength);
                    button.setMessage(Text.literal("L: " + nextLength));
                    sendSyncPacket(golem);
                }).dimensions(x + 89, y + 91, 80, 20).build());
            } else if (golem.getBuildPattern() == BuildPattern.REPLACE) {
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Capture Filter from Hand"), button -> {
                    ItemStack handStack = this.client.player.getMainHandStack();
                    if (!handStack.isEmpty() && handStack.getItem() instanceof net.minecraft.item.BlockItem) {
                        golem.setHeldItem(handStack.copy());
                        sendSyncPacket(golem);
                        this.client.player.sendMessage(Text.literal("Golem filter set to: " + handStack.getName().getString()), true);
                    } else {
                        this.client.player.sendMessage(Text.literal("Hold a block in your main hand!"), true);
                    }
                }).dimensions(x + 7, y + 91, 162, 20).build());
            }
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        
        UtilityGolem golem = handler.getGolem();
        if (golem != null && golem.getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND) {
            // Draw top part (the 3x3 grid and label area)
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0f, 0.0f, backgroundWidth, 71, 256, 256);
            
            // Draw extra background for buttons (always 40 now)
            int extraHeight = 40;
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 71, 0.0f, 71.0f, backgroundWidth, extraHeight, 256, 256);
            
            // Draw the player inventory part
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 71 + extraHeight, 0.0f, 71.0f, backgroundWidth, 95, 256, 256);
        } else {
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0f, 0.0f, backgroundWidth, backgroundHeight, 256, 256);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
