package rehdpanda.utilitygolems.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import rehdpanda.utilitygolems.BuildPattern;
import rehdpanda.utilitygolems.GolemInventoryMenu;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;

/**
 * Draws the getInventory() screen for golems
 */
public class GolemInventoryScreen extends AbstractContainerScreen<GolemInventoryMenu> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/dispenser.png");

    public GolemInventoryScreen(GolemInventoryMenu handler, Inventory inventory, Component title) {
        super(handler, getInventory(), title);
        
        UtilityGolem golem = handler.getGolem();
        if (golem != null && (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND || golem.getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD)) {
            this.imageHeight = 206; // 166 (dispenser) + 40
            this.inventory.abelY = 75 + 40;
        }
    }

    private void sendSyncPacket(UtilityGolem golem) {
        ClientPlayNetworking.send(new UGInit.SyncPatternPayload(
                golem.getId(),
                golem.getBuildPattern().ordinal(),
                golem.getWallWidth(),
                golem.getWallLength(),
                golem.getHeldItem(),
                golem.isBuildingStarted(),
                golem.getSchematicName()
        ));
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (imageWidth - font.width(title)) / 2;

        UtilityGolem golem = menu.getGolem();
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
        this.addRenderableWidget(Button.builder(Component.literal("Mode: " + golem.getBuildPattern().getDisplayName()), button -> {
            BuildPattern next = BuildPattern.values()[(golem.getBuildPattern().ordinal() + 1) % BuildPattern.values().length];
            golem.setBuildPattern(next);
            golem.setBuildingStarted(false); // Stop when pattern changes
            button.setMessage(Component.literal("Mode: " + next.getDisplayName()));
            sendSyncPacket(golem);
            this.rebuildWidgets(); // Re-init to show/hide width/length buttons
        }).bounds(leftPos + 7, topPos + 77, 135, 20).build());

        // Start/Stop button
        String startLabel = golem.isBuildingStarted() ? "§cStop" : "§aStart";
        this.addRenderableWidget(Button.builder(Component.literal(startLabel), button -> {
            golem.setBuildingStarted(!golem.isBuildingStarted());
            sendSyncPacket(golem);
            button.setMessage(Component.literal(golem.isBuildingStarted() ? "§cStop" : "§aStart"));
        }).bounds(leftPos + 144, topPos + 77, 25, 20).build());

        if (golem.getBuildPattern() == BuildPattern.PLATFORM) {
            // Width adjustment
            this.addRenderableWidget(Button.builder(Component.literal("W: " + golem.getWallWidth()), button -> {
                int nextWidth = (golem.getWallWidth() % 10) + 1;
                golem.setWallWidth(nextWidth);
                button.setMessage(Component.literal("W: " + nextWidth));
                sendSyncPacket(golem);
            }).bounds(leftPos + 7, topPos + 97, 80, 20).build());

            // Length adjustment
            this.addRenderableWidget(Button.builder(Component.literal("L: " + golem.getWallLength()), button -> {
                int nextLength = (golem.getWallLength() % 10) + 1;
                golem.setWallLength(nextLength);
                button.setMessage(Component.literal("L: " + nextLength));
                sendSyncPacket(golem);
            }).bounds(leftPos + 89, topPos + 97, 80, 20).build());
        } else if (golem.getBuildPattern() == BuildPattern.REPLACE) {
            this.addRenderableWidget(Button.builder(Component.literal("Capture Filter from InteractionHand"), button -> {
                ItemStack handStack = this.minecraft.player.getMainInteractionHandItem();
                if (!handStack.isEmpty() && handStack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                    golem.setHeldItem(handStack.copy());
                    sendSyncPacket(golem);
                    this.minecraft.player.displayClientMessage(Component.literal("Golem filter set to: " + handStack.getHoverName().getString()), true);
                } else {
                    this.minecraft.player.displayClientMessage(Component.literal("Hold a block in your main hand!"), true);
                }
            }).bounds(leftPos + 7, topPos + 97, 162, 20).build());
        } else if (golem.getBuildPattern() == BuildPattern.SCHEMATIC) {
            String current = golem.getSchematicName();
            if (current == null) current = "";
            final String label = current.isEmpty() ? "<none>" : current;

            // Left arrow button to the left of the name
            this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                java.util.List<String> files = listSchematics();
                if (!files.isEmpty()) {
                    int idx = files.indexOf(golem.getSchematicName());
                    if (idx == -1) idx = 0;
                    idx = (idx - 1 + files.getContainerSize()) % files.getContainerSize();
                    golem.setSchematicName(files.get(idx));
                    sendSyncPacket(golem);
                    this.rebuildWidgets();
                }
            }).bounds(leftPos + 7, topPos + 97, 18, 20).build());

            // Schematic name button (clicking it opens the folder)
            this.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                java.nio.file.Path dir = getSchematicDir();
                try {
                    java.nio.file.Files.createDirectories(dir);
                } catch (Exception ignored) {}
                net.minecraft.util.Util.getPlatform().openFile(dir.toFile());
            }).bounds(leftPos + 27, topPos + 97, 110, 20).build());

            // Right arrow button to the right of the name
            this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                java.util.List<String> files = listSchematics();
                if (!files.isEmpty()) {
                    int idx = files.indexOf(golem.getSchematicName());
                    if (idx == -1) idx = 0;
                    idx = (idx + 1) % files.getContainerSize();
                    golem.setSchematicName(files.get(idx));
                    sendSyncPacket(golem);
                    this.rebuildWidgets();
                }
            }).bounds(leftPos + 139, topPos + 97, 18, 20).build());

            // Refresh button next to the right arrow button
            this.addRenderableWidget(Button.builder(Component.literal("\u21BB"), b -> {
                this.rebuildWidgets();
            }).bounds(leftPos + 159, topPos + 97, 20, 20).build());
        }
    }

    private int emeraldScrollOffset = 0;

    private void initEmeraldButtons(UtilityGolem golem) {
        java.util.List<ItemStack> trades = golem.getDiscoveredTrades();
        int maxTrades = 8;
        
        for (int i = 0; i < Math.min(maxTrades, trades.getContainerSize() - emeraldScrollOffset); i++) {
            final int index = i + emeraldScrollOffset;
            ItemStack stack = trades.get(index);
            boolean isSelected = ItemStack.areItemsAndComponentsEqual(stack, golem.getSelectedBuyItem());
            
            this.addRenderableWidget(Button.builder(Component.literal(""), button -> {
                if (isSelected) {
                    golem.setSelectedBuyItem(ItemStack.EMPTY);
                } else {
                    golem.setSelectedBuyItem(stack.copy());
                }
                ClientPlayNetworking.send(new UGInit.SelectBuyItemPayload(golem.getId(), golem.getSelectedBuyItem()));
                this.rebuildWidgets();
            }).bounds(leftPos + 8 + i * 20, topPos + 78, 18, 18).build());
        }

        if (emeraldScrollOffset > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                emeraldScrollOffset--;
                this.rebuildWidgets();
            }).bounds(leftPos + 7, topPos + 98, 20, 18).build());
        }
        if (trades.getContainerSize() > emeraldScrollOffset + maxTrades) {
            this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                emeraldScrollOffset++;
                this.rebuildWidgets();
            }).bounds(leftPos + imageWidth - 27, topPos + 98, 20, 18).build());
        }

        if (!golem.getSelectedBuyItem().isEmpty()) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.utility-golems.clear_selection"), b -> {
                golem.setSelectedBuyItem(ItemStack.EMPTY);
                ClientPlayNetworking.send(new UGInit.SelectBuyItemPayload(golem.getId(), ItemStack.EMPTY));
                this.rebuildWidgets();
            }).bounds(leftPos + 30, topPos + 98, imageWidth - 60, 18).build());
        }
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        UtilityGolem golem = menu.getGolem();
        if (golem != null && (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND || golem.getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD)) {
            // Draw top part (the 3x3 grid and label area): 0 to 71 from texture
            context.blit(TEXTURE, x, y, 0.0f, 0.0f, imageWidth, 75, 256, 256);

            // Draw extra background for buttons (always 40 now)
            int extraHeight = 40;

            // Fill the spacer with a generic background color from the texture (e.g., at 7, 7)
            for (int i = 0; i < extraHeight; i += 5) {
                int h = Math.min(10, extraHeight - i);
                context.blit(TEXTURE, x , y + 75 + i, 0.0f, 7.0f, imageWidth, h, 256, 256);
            }

            // Draw the player getInventory() part (which normally starts at 75 in the dispenser texture)
            // Dispenser texture: top part 0-75, player getInventory() 75-166.
            context.blit(TEXTURE, x, y + 75 + extraHeight, 0.0f, 75.0f, imageWidth, 91, 256, 256);
        } else {
            context.blit(TEXTURE, x, y, 0.0f, 0.0f, imageWidth, imageHeight, 256, 256);
        }

        // Draw held item slot background
        context.blit(TEXTURE, x + 133, y + 34, 61.0f, 16.0f, 18, 18, 256, 256);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        UtilityGolem golem = menu.getGolem();
        if (golem != null) {
            if (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD) {
                drawEmeraldTradeIcons(context, mouseX, mouseY, golem);
            } else if (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.CACTUS || golem.getGolemType() == rehdpanda.utilitygolems.GolemType.HOPPER) {
                drawCactusUI(context, mouseX, mouseY, golem);
            } else if (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.TINTED_GLASS) {
                drawTintedGlassUI(context, mouseX, mouseY, golem);
            }
        }

        // Draw tooltip for held item slot if hovered
        if (mouseX >= leftPos + 133 && mouseX < leftPos + 151 && mouseY >= topPos + 34 && mouseY < topPos + 52) {
            context.renderTooltip(font, Component.translatable("gui.utility-golems.held_item_tooltip"), mouseX, mouseY);
        }

        this.renderTooltip(context, mouseX, mouseY);
    }

    private void drawTintedGlassUI(GuiGraphics context, int mouseX, int mouseY, UtilityGolem golem) {
        String text = String.valueOf(golem.getXpScore());
        int textWidth = font.width(text);
        // Top right of the getInventory(): x + 176 is the right edge, minus padding and text width.
        // The title area is roughly 16 pixels high.
        context.drawString(font, text, leftPos + imageWidth - textWidth - 8, topPos + 6, 0xFF00FF00, true);
    }

    private void drawCactusUI(GuiGraphics context, int mouseX, int mouseY, UtilityGolem golem) {
        boolean isHopper = golem.getGolemType() == rehdpanda.utilitygolems.GolemType.HOPPER;
        // Draw deleted items count in dark red (if cactus), or maybe hide/change if hopper
        if (!isHopper) {
            String text = String.valueOf(golem.getDeletedItemsCount());
            int textWidth = font.width(text);
            context.drawString(font, text, leftPos + imageWidth - textWidth - 8, topPos + 6, 0xFFAA0000, true);
        }

        // Draw overlay over occupied slots
        for (int i = 0; i < 9; i++) {
            ItemStack stack = golem.inventory.getItem(i);
            if (!stack.isEmpty()) {
                int slotX = leftPos + 62 + (i % 3) * 18;
                int slotY = topPos + 17 + (i / 3) * 18;

                if (isHopper) {
                    // Transparent green highlight for hopper
                    context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x4000FF00);
                    // Green check or nothing? Requirement says "highlight them with green instead of red"
                    context.drawCenteredString(font, "+", slotX + 8, slotY + 4, 0xFF00FF00);
                } else {
                    // Transparent red highlight
                    context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x40FF0000);
                    // Red X
                    context.drawCenteredString(font, "X", slotX + 8, slotY + 4, 0xFFFF0000);
                }
            }
        }
    }

    private void drawEmeraldTradeIcons(GuiGraphics context, int mouseX, int mouseY, UtilityGolem golem) {
        java.util.List<ItemStack> trades = golem.getDiscoveredTrades();
        int maxTrades = 8;
        for (int i = 0; i < Math.min(maxTrades, trades.getContainerSize() - emeraldScrollOffset); i++) {
            int index = i + emeraldScrollOffset;
            ItemStack stack = trades.get(index);
            int iconX = leftPos + 9 + i * 20;
            int iconY = topPos + 79;
            context.renderFakeItem(stack, iconX, iconY);
            
            if (ItemStack.areItemsAndComponentsEqual(stack, golem.getSelectedBuyItem())) {
                context.fill(iconX - 1, iconY - 1, iconX + 17, iconY + 17, 0x40FFFFFF);
            }
        }
    }
    private java.nio.file.Path getSchematicDir() {
        java.nio.file.Path configDir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
        return configDir.resolve("utility-golems").resolve("schematics");
    }

    private java.util.List<String> listSchematics() {
        java.util.List<String> result = new java.util.ArrayList<>();
        try {
            java.nio.file.Path dir = getSchematicDir();
            if (!java.nio.file.Files.isDirectory(dir)) return result;
            try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(dir)) {
                stream.filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    return name.endsWith(".schematic") || name.endsWith(".schem");
                }).sorted().forEach(p -> result.add(p.getFileName().toString()));
            }
        } catch (Exception ignored) {}
        return result;
    }
}
