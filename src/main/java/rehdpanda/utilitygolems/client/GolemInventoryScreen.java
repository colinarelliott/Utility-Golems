package rehdpanda.utilitygolems.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
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
public class GolemInventoryScreen extends AbstractContainerScreen<GolemInventoryMenu> implements MenuAccess<GolemInventoryMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/dispenser.png");

    public GolemInventoryScreen(GolemInventoryMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, (handler.getGolem() != null && (handler.getGolem().getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND || handler.getGolem().getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD)) ? 206 : 166);
        
        UtilityGolem golem = handler.getGolem();
        if (golem != null && (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND || golem.getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD)) {
            this.inventoryLabelY = 75 + 40;
        }
    }

    private void sendSyncPacket(UtilityGolem golem) {
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.SyncPatternPayload(
                    golem.getId(),
                    golem.getBuildPattern().ordinal(),
                    golem.getWallWidth(),
                    golem.getWallLength(),
                    golem.getHeldItem(),
                    golem.isBuildingStarted(),
                    golem.getSchematicName()
            )));
        }
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (imageWidth - font.width(title)) / 2;
        this.titleLabelY = 6;
        
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
                ItemStack handStack = this.minecraft.player.getMainHandItem();
                if (!handStack.isEmpty() && handStack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                    golem.setHeldItem(handStack.copy());
                    sendSyncPacket(golem);
                    this.minecraft.player.sendSystemMessage(Component.literal("Golem filter set to: " + handStack.getHoverName().getString()));
                } else {
                    this.minecraft.player.sendSystemMessage(Component.literal("Hold a block in your main hand!"));
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
                    idx = (idx - 1 + files.size()) % files.size();
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
                    idx = (idx + 1) % files.size();
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
        
        for (int i = 0; i < Math.min(maxTrades, trades.size() - emeraldScrollOffset); i++) {
            final int index = i + emeraldScrollOffset;
            ItemStack stack = trades.get(index);
            boolean isSelected = ItemStack.matches(stack, golem.getSelectedBuyItem());
            
            this.addRenderableWidget(Button.builder(Component.literal(""), button -> {
                if (isSelected) {
                    golem.setSelectedBuyItem(ItemStack.EMPTY);
                } else {
                    golem.setSelectedBuyItem(stack.copy());
                }
                if (Minecraft.getInstance().getConnection() != null) {
                    Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.SelectBuyItemPayload(golem.getId(), golem.getSelectedBuyItem())));
                }
                this.rebuildWidgets();
            }).bounds(leftPos + 8 + i * 20, topPos + 78, 18, 18).build());
        }

        if (emeraldScrollOffset > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                emeraldScrollOffset--;
                this.rebuildWidgets();
            }).bounds(leftPos + 7, topPos + 98, 20, 18).build());
        }
        if (trades.size() > emeraldScrollOffset + maxTrades) {
            this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                emeraldScrollOffset++;
                this.rebuildWidgets();
            }).bounds(leftPos + imageWidth - 27, topPos + 98, 20, 18).build());
        }

        if (!golem.getSelectedBuyItem().isEmpty()) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.utility-golems.clear_selection"), b -> {
                golem.setSelectedBuyItem(ItemStack.EMPTY);
                if (Minecraft.getInstance().getConnection() != null) {
                    Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.SelectBuyItemPayload(golem.getId(), ItemStack.EMPTY)));
                }
                this.rebuildWidgets();
            }).bounds(leftPos + 30, topPos + 98, imageWidth - 60, 18).build());
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int x = this.leftPos;
        int y = this.topPos;

        UtilityGolem golem = menu.getGolem();
        if (golem != null && (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.DIAMOND || golem.getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD)) {
            // Draw top part (the 3x3 grid and label area): 0 to 71 from texture
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0f, 0.0f, imageWidth, 75, 256, 256);

            // Draw extra background for buttons (always 40 now)
            int extraHeight = 40;

            // Fill the spacer with a generic background color from the texture (e.g., at 7, 7)
            for (int i = 0; i < extraHeight; i += 5) {
                int h = Math.min(10, extraHeight - i);
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x , y + 75 + i, 0.0f, 7.0f, imageWidth, h, 256, 256);
            }

            // Draw the player inventory part (which normally starts at 75 in the dispenser texture)
            // Dispenser texture: top part 0-75, player inventory 75-166.
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 75 + extraHeight, 0.0f, 75.0f, imageWidth, 91, 256, 256);
        } else {
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0f, 0.0f, imageWidth, imageHeight, 256, 256);
        }

        // Draw held item slot background
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 133, y + 34, 61.0f, 16.0f, 18, 18, 256, 256);
        
        // DRAW SLOTS ON TOP OF BACKGROUND
        super.extractContents(context, mouseX, mouseY, delta);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // Only the overlays live here; the dim, the labels and the tooltip are all
        // handled by the framework and AbstractContainerScreen.
        super.extractRenderState(context, mouseX, mouseY, delta);

        UtilityGolem golem = menu.getGolem();
        if (golem != null) {
            // Draw the held item itself
            ItemStack heldItem = golem.getHeldItem();
            if (!heldItem.isEmpty()) {
                context.item(heldItem, this.leftPos + 134, this.topPos + 35);
            }

            if (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.EMERALD) {
                drawEmeraldTradeIcons(context, mouseX, mouseY, golem);
            } else if (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.CACTUS || golem.getGolemType() == rehdpanda.utilitygolems.GolemType.HOPPER) {
                drawCactusUI(context, mouseX, mouseY, golem);
            } else if (golem.getGolemType() == rehdpanda.utilitygolems.GolemType.TINTED_GLASS) {
                drawTintedGlassUI(context, mouseX, mouseY, golem);
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractLabels(context, mouseX, mouseY);
        if (menu.getGolem() != null) {
            context.text(this.font, Component.literal("Holding"), 125, 24, 0xFF404040, false);
        }
    }

    private void drawTintedGlassUI(GuiGraphicsExtractor context, int mouseX, int mouseY, UtilityGolem golem) {
        String text = String.valueOf(golem.getXpScore());
        int textWidth = font.width(text);
        // Top right of the inventory: x + 176 is the right edge, minus padding and text width.
        // The title area is roughly 16 pixels high.
        context.text(font, text, leftPos + imageWidth - textWidth - 8, topPos + 6, 0xFF00FF00, true);
    }

    private void drawCactusUI(GuiGraphicsExtractor context, int mouseX, int mouseY, UtilityGolem golem) {
        boolean isHopper = golem.getGolemType() == rehdpanda.utilitygolems.GolemType.HOPPER;
        // Draw deleted items count in dark red (if cactus), or maybe hide/change if hopper
        if (!isHopper) {
            String text = String.valueOf(golem.getDeletedItemsCount());
            int textWidth = font.width(text);
            context.text(font, text, leftPos + imageWidth - textWidth - 8, topPos + 6, 0xFFAA0000, true);
        }

        // Draw overlay over occupied slots
        for (int i = 0; i < 9; i++) {
            ItemStack stack = golem.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                int slotX = leftPos + 62 + (i % 3) * 18;
                int slotY = topPos + 17 + (i / 3) * 18;

                if (isHopper) {
                    // Transparent green highlight for hopper
                    context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x4000FF00);
                    // Green check or nothing? Requirement says "highlight them with green instead of red"
                    context.centeredText(font, "+", slotX + 8, slotY + 4, 0xFF00FF00);
                } else {
                    // Transparent red highlight
                    context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x40FF0000);
                    // Red X
                    context.centeredText(font, "X", slotX + 8, slotY + 4, 0xFFFF0000);
                }
            }
        }
    }

    private void drawEmeraldTradeIcons(GuiGraphicsExtractor context, int mouseX, int mouseY, UtilityGolem golem) {
        java.util.List<ItemStack> trades = golem.getDiscoveredTrades();
        int maxTrades = 8;
        for (int i = 0; i < Math.min(maxTrades, trades.size() - emeraldScrollOffset); i++) {
            int index = i + emeraldScrollOffset;
            ItemStack stack = trades.get(index);
            int iconX = leftPos + 9 + i * 20;
            int iconY = topPos + 79;
            context.item(stack, iconX, iconY);
            
            if (ItemStack.matches(stack, golem.getSelectedBuyItem())) {
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
