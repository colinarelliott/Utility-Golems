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
        if (golem != null && (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND || golem.getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD)) {
            this.backgroundHeight = 206; // 166 (dispenser) + 40
            this.playerInventoryTitleY = 75 + 40; 
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
        if (golem != null) {
            if (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND) {
                // ... (Diamond golem buttons)
                initDiamondButtons(golem);
            } else if (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD) {
                initEmeraldButtons(golem);
            }
        }
    }

    private void initDiamondButtons(UtilityGolem golem) {
        // Pattern cycle button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Mode: " + golem.getBuildPattern().getDisplayName()), button -> {
            BuildPattern next = BuildPattern.values()[(golem.getBuildPattern().ordinal() + 1) % BuildPattern.values().length];
            golem.setBuildPattern(next);
            golem.setBuildingStarted(false); // Stop when pattern changes
            button.setMessage(Text.literal("Mode: " + next.getDisplayName()));
            sendSyncPacket(golem);
            this.clearAndInit(); // Re-init to show/hide width/length buttons
        }).dimensions(x + 7, y + 77, 135, 20).build());

        // Start/Stop button
        String startLabel = golem.isBuildingStarted() ? "§cStop" : "§aStart";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(startLabel), button -> {
            golem.setBuildingStarted(!golem.isBuildingStarted());
            sendSyncPacket(golem);
            button.setMessage(Text.literal(golem.isBuildingStarted() ? "§cStop" : "§aStart"));
        }).dimensions(x + 144, y + 77, 25, 20).build());

        if (golem.getBuildPattern() == BuildPattern.WALL) {
            // Width adjustment
            this.addDrawableChild(ButtonWidget.builder(Text.literal("W: " + golem.getWallWidth()), button -> {
                int nextWidth = (golem.getWallWidth() % 10) + 1;
                golem.setWallWidth(nextWidth);
                button.setMessage(Text.literal("W: " + nextWidth));
                sendSyncPacket(golem);
            }).dimensions(x + 7, y + 97, 80, 20).build());

            // Length adjustment
            this.addDrawableChild(ButtonWidget.builder(Text.literal("L: " + golem.getWallLength()), button -> {
                int nextLength = (golem.getWallLength() % 10) + 1;
                golem.setWallLength(nextLength);
                button.setMessage(Text.literal("L: " + nextLength));
                sendSyncPacket(golem);
            }).dimensions(x + 89, y + 97, 80, 20).build());
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
            }).dimensions(x + 7, y + 97, 162, 20).build());
        }
    }

    private int emeraldScrollOffset = 0;

    private void initEmeraldButtons(UtilityGolem golem) {
        java.util.List<ItemStack> trades = golem.getDiscoveredTrades();
        int maxTrades = 8;
        
        for (int i = 0; i < Math.min(maxTrades, trades.size() - emeraldScrollOffset); i++) {
            final int index = i + emeraldScrollOffset;
            ItemStack stack = trades.get(index);
            boolean isSelected = ItemStack.areItemsEqual(stack, golem.getSelectedBuyItem());
            
            this.addDrawableChild(ButtonWidget.builder(Text.literal(""), button -> {
                if (isSelected) {
                    golem.setSelectedBuyItem(ItemStack.EMPTY);
                } else {
                    golem.setSelectedBuyItem(stack.copy());
                }
                ClientPlayNetworking.send(new UGInit.SelectBuyItemPayload(golem.getId(), golem.getSelectedBuyItem()));
                this.clearAndInit();
            }).dimensions(x + 8 + i * 20, y + 78, 18, 18).build());
        }

        if (emeraldScrollOffset > 0) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> {
                emeraldScrollOffset--;
                this.clearAndInit();
            }).dimensions(x + 7, y + 98, 20, 18).build());
        }
        if (trades.size() > emeraldScrollOffset + maxTrades) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> {
                emeraldScrollOffset++;
                this.clearAndInit();
            }).dimensions(x + backgroundWidth - 27, y + 98, 20, 18).build());
        }

        if (!golem.getSelectedBuyItem().isEmpty()) {
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.utility-golems.clear_selection"), b -> {
                golem.setSelectedBuyItem(ItemStack.EMPTY);
                ClientPlayNetworking.send(new UGInit.SelectBuyItemPayload(golem.getId(), ItemStack.EMPTY));
                this.clearAndInit();
            }).dimensions(x + 30, y + 98, backgroundWidth - 60, 18).build());
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        
        UtilityGolem golem = handler.getGolem();
        if (golem != null && (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND || golem.getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD)) {
            // Draw top part (the 3x3 grid and label area): 0 to 71 from texture
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0f, 0.0f, backgroundWidth, 75, 256, 256);
            
            // Draw extra background for buttons (always 40 now)
            int extraHeight = 40;
            
            // Fill the spacer with a generic background color from the texture (e.g., at 7, 7)
            for (int i = 0; i < extraHeight; i += 5) {
                int h = Math.min(10, extraHeight - i);
                context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, x , y + 75 + i, 0.0f, 7.0f, backgroundWidth, h, 256, 256);
            }
            
            // Draw the player inventory part (which normally starts at 75 in the dispenser texture)
            // Dispenser texture: top part 0-75, player inventory 75-166.
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 75 + extraHeight, 0.0f, 75.0f, backgroundWidth, 91, 256, 256);
        } else {
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0f, 0.0f, backgroundWidth, backgroundHeight, 256, 256);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        UtilityGolem golem = handler.getGolem();
        if (golem != null && golem.getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD) {
            drawEmeraldTradeIcons(context, mouseX, mouseY, golem);
        }
        
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void drawEmeraldTradeIcons(DrawContext context, int mouseX, int mouseY, UtilityGolem golem) {
        java.util.List<ItemStack> trades = golem.getDiscoveredTrades();
        int maxTrades = 8;
        for (int i = 0; i < Math.min(maxTrades, trades.size() - emeraldScrollOffset); i++) {
            int index = i + emeraldScrollOffset;
            ItemStack stack = trades.get(index);
            int iconX = x + 9 + i * 20;
            int iconY = y + 79;
            context.drawItem(stack, iconX, iconY);
            
            if (ItemStack.areItemsEqual(stack, golem.getSelectedBuyItem())) {
                context.fill(iconX - 1, iconY - 1, iconX + 17, iconY + 17, 0x40FFFFFF);
            }
        }
    }
}
