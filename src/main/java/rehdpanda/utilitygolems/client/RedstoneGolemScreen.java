package rehdpanda.utilitygolems.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import rehdpanda.utilitygolems.RedstoneGolemMenu;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;

import java.util.ArrayList;
import java.util.List;

public class RedstoneGolemScreen extends AbstractContainerScreen<RedstoneGolemMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private static final int TEXTURE_SIZE = 256;

    // generic_54.png is only 176 wide, so the 300-wide panel is nine-sliced out of its frame:
    // a 3px border, the flat body strip at v=125..138, and the bottom frame at v=219..221.
    private static final int BORDER = 3;
    private static final int BODY_U = 3;
    private static final int BODY_V = 125;
    private static final int BODY_TILE_W = 170;
    private static final int BODY_TILE_H = 14;
    private static final int RIGHT_U = 173;
    private static final int BOTTOM_V = 219;
    /** Top-left corner of a slot cell in the texture; the 16x16 interior starts one pixel in. */
    private static final int SLOT_CELL_U = 7;
    private static final int SLOT_CELL_V = 17;

    private static final int CONTROLS_Y = 40;
    private static final int CONTROLS_HEIGHT = 20;
    private static final int HEADER_Y = 65;
    private static final int LIST_Y = 76;
    private static final int LIST_ROWS = 5;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_WIDGET_SIZE = 16;

    private static final int PROGRAM_X = 8;
    private static final int NEARBY_X = 156;

    private final List<BlockPos> nearbyInteractables = new ArrayList<>();
    private int scrollOffset = 0;
    private EditBox intervalField;

    public RedstoneGolemScreen(RedstoneGolemMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, RedstoneGolemMenu.PANEL_WIDTH, RedstoneGolemMenu.PANEL_HEIGHT);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = RedstoneGolemMenu.SLOT_X;
        this.inventoryLabelY = RedstoneGolemMenu.PLAYER_INVENTORY_Y - 11;
    }

    @Override
    protected void init() {
        super.init();

        // init() runs again on resize, so carry the typed interval across.
        String interval = intervalField != null ? intervalField.getValue() : "20";
        intervalField = new EditBox(this.font, this.leftPos + 252, this.topPos + CONTROLS_Y + 2, 40, 16, Component.literal("20"));
        intervalField.setMaxLength(4);
        intervalField.setValue(interval);

        scanNearby();
        refreshButtons();
    }

    private void scanNearby() {
        nearbyInteractables.clear();
        UtilityGolem golem = menu.getGolem();
        if (golem == null) return;

        BlockPos pos = golem.blockPosition();
        int range = 8;
        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos p = pos.offset(x, y, z);
                    BlockState state = golem.level().getBlockState(p);
                    Block block = state.getBlock();
                    if (block instanceof ButtonBlock || block instanceof LeverBlock || block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock) {
                        nearbyInteractables.add(p);
                    }
                }
            }
        }
    }

    private void refreshButtons() {
        this.clearWidgets();
        this.addWidget(intervalField);
        UtilityGolem golem = menu.getGolem();
        if (golem == null) return;

        // Start/Stop button
        Component startLabel = golem.isRedstoneProgramStarted()
                ? Component.literal("Stop").withStyle(ChatFormatting.RED)
                : Component.literal("Start").withStyle(ChatFormatting.GREEN);
        this.addRenderableWidget(Button.builder(startLabel, button -> {
            sendAction(golem, 0);
            refreshButtons();
        }).bounds(this.leftPos + 8, this.topPos + CONTROLS_Y, 60, CONTROLS_HEIGHT).build());

        // Reset button
        this.addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
            sendAction(golem, 1);
            refreshButtons();
        }).bounds(this.leftPos + 72, this.topPos + CONTROLS_Y, 60, CONTROLS_HEIGHT).build());

        // Scan button
        this.addRenderableWidget(Button.builder(Component.literal("Scan"), button -> {
            scanNearby();
            refreshButtons();
        }).bounds(this.leftPos + 136, this.topPos + CONTROLS_Y, 60, CONTROLS_HEIGHT).build());

        // Program list (left column)
        List<UtilityGolem.RedstoneInteraction> program = golem.getRedstoneProgram();
        for (int i = 0; i < LIST_ROWS && i < program.size(); i++) {
            final int index = i;
            int rowY = this.topPos + LIST_Y + i * ROW_HEIGHT;
            UtilityGolem.RedstoneInteraction inter = program.get(index);
            String label = blockName(golem, inter.pos()) + " (" + inter.interval() + "t)";

            this.addRenderableWidget(Button.builder(Component.literal("X").withStyle(ChatFormatting.RED), button -> {
                program.remove(index);
                sendUpdate();
                refreshButtons();
            }).bounds(this.leftPos + PROGRAM_X, rowY, ROW_WIDGET_SIZE, ROW_WIDGET_SIZE).build());

            Component txt = golem.isRedstoneProgramStarted() && golem.getCurrentInteractionIndex() == index
                    ? Component.literal("> " + label).withStyle(ChatFormatting.YELLOW)
                    : Component.literal(label);
            this.addRenderableWidget(Button.builder(txt, b -> {})
                    .bounds(this.leftPos + PROGRAM_X + 38, rowY, 96, ROW_WIDGET_SIZE).build());
        }

        // Nearby list (right column)
        for (int i = 0; i < LIST_ROWS && (i + scrollOffset) < nearbyInteractables.size(); i++) {
            final BlockPos p = nearbyInteractables.get(i + scrollOffset);
            int rowY = this.topPos + LIST_Y + i * ROW_HEIGHT;

            this.addRenderableWidget(Button.builder(Component.literal("+").withStyle(ChatFormatting.GREEN), button -> {
                int interval = 20;
                try {
                    interval = Math.max(1, Integer.parseInt(intervalField.getValue().trim()));
                } catch (NumberFormatException ignored) {}
                golem.getRedstoneProgram().add(new UtilityGolem.RedstoneInteraction(p, interval));
                sendUpdate();
                refreshButtons();
            }).bounds(this.leftPos + NEARBY_X, rowY, ROW_WIDGET_SIZE, ROW_WIDGET_SIZE).build());

            this.addRenderableWidget(Button.builder(Component.literal(blockName(golem, p)), b -> {})
                    .bounds(this.leftPos + NEARBY_X + 38, rowY, 82, ROW_WIDGET_SIZE).build());
        }

        if (nearbyInteractables.size() > LIST_ROWS) {
            this.addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                scrollOffset = Math.max(0, scrollOffset - 1);
                refreshButtons();
            }).bounds(this.leftPos + 280, this.topPos + LIST_Y, 12, 12).build());
            this.addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                scrollOffset = Math.min(nearbyInteractables.size() - LIST_ROWS, scrollOffset + 1);
                refreshButtons();
            }).bounds(this.leftPos + 280, this.topPos + LIST_Y + (LIST_ROWS - 1) * ROW_HEIGHT + 4, 12, 12).build());
        }
    }

    private static String blockName(UtilityGolem golem, BlockPos pos) {
        return golem.level().getBlockState(pos).getBlock().getName().getString();
    }

    private void sendAction(UtilityGolem golem, int actionId) {
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.RedstoneActionPayload(golem.getId(), actionId)));
        }
    }

    private void sendUpdate() {
        UtilityGolem golem = menu.getGolem();
        if (golem != null && Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.SyncRedstoneProgramPayload(golem.getId(), new ArrayList<>(golem.getRedstoneProgram()))));
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        drawPanel(context, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        for (Slot slot : this.menu.slots) {
            drawSlotCell(context, this.leftPos + slot.x, this.topPos + slot.y);
        }
        // Cells for the two list columns' block icons.
        UtilityGolem golem = menu.getGolem();
        if (golem != null) {
            int programRows = Math.min(LIST_ROWS, golem.getRedstoneProgram().size());
            for (int i = 0; i < programRows; i++) {
                drawSlotCell(context, this.leftPos + PROGRAM_X + 20, this.topPos + LIST_Y + i * ROW_HEIGHT + 1);
            }
            int nearbyRows = Math.min(LIST_ROWS, nearbyInteractables.size() - scrollOffset);
            for (int i = 0; i < nearbyRows; i++) {
                drawSlotCell(context, this.leftPos + NEARBY_X + 20, this.topPos + LIST_Y + i * ROW_HEIGHT + 1);
            }
        }

        super.extractContents(context, mouseX, mouseY, delta);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractLabels(context, mouseX, mouseY);

        String wip = "W.I.P";
        context.text(this.font, wip, this.imageWidth - this.font.width(wip) - 8, 6, 0xFFFFAA00, false);

        context.text(this.font, "Program", PROGRAM_X, HEADER_Y, 0xFF404040, false);
        context.text(this.font, "Nearby", NEARBY_X, HEADER_Y, 0xFF404040, false);
        context.text(this.font, "Interval:", 200, CONTROLS_Y + 6, 0xFF404040, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // Only the overlays live here; the dim, the labels and the tooltip are all
        // handled by the framework and AbstractContainerScreen.
        super.extractRenderState(context, mouseX, mouseY, delta);

        UtilityGolem golem = menu.getGolem();
        if (golem != null) {
            List<UtilityGolem.RedstoneInteraction> program = golem.getRedstoneProgram();
            for (int i = 0; i < LIST_ROWS && i < program.size(); i++) {
                drawBlockIcon(context, golem, program.get(i).pos(), this.leftPos + PROGRAM_X + 20, this.topPos + LIST_Y + i * ROW_HEIGHT + 1);
            }
            for (int i = 0; i < LIST_ROWS && (i + scrollOffset) < nearbyInteractables.size(); i++) {
                drawBlockIcon(context, golem, nearbyInteractables.get(i + scrollOffset), this.leftPos + NEARBY_X + 20, this.topPos + LIST_Y + i * ROW_HEIGHT + 1);
            }
        }

        // Added with addWidget rather than addRenderableWidget, so it is not drawn for us.
        intervalField.extractWidgetRenderState(context, mouseX, mouseY, delta);
    }

    private void drawBlockIcon(GuiGraphicsExtractor context, UtilityGolem golem, BlockPos pos, int x, int y) {
        ItemStack stack = new ItemStack(golem.level().getBlockState(pos).getBlock());
        if (!stack.isEmpty()) {
            context.item(stack, x, y);
        }
    }

    private void drawSlotCell(GuiGraphicsExtractor context, int x, int y) {
        blit(context, x - 1, y - 1, SLOT_CELL_U, SLOT_CELL_V, 18, 18);
    }

    /** Nine-slices the vanilla container frame out to an arbitrary size. */
    private void drawPanel(GuiGraphicsExtractor context, int x, int y, int width, int height) {
        int innerW = width - 2 * BORDER;
        int innerH = height - 2 * BORDER;

        blit(context, x, y, 0, 0, BORDER, BORDER);
        blit(context, x + width - BORDER, y, RIGHT_U, 0, BORDER, BORDER);
        blit(context, x, y + height - BORDER, 0, BOTTOM_V, BORDER, BORDER);
        blit(context, x + width - BORDER, y + height - BORDER, RIGHT_U, BOTTOM_V, BORDER, BORDER);

        for (int dx = 0; dx < innerW; dx += BODY_TILE_W) {
            int w = Math.min(BODY_TILE_W, innerW - dx);
            blit(context, x + BORDER + dx, y, BODY_U, 0, w, BORDER);
            blit(context, x + BORDER + dx, y + height - BORDER, BODY_U, BOTTOM_V, w, BORDER);
            for (int dy = 0; dy < innerH; dy += BODY_TILE_H) {
                int h = Math.min(BODY_TILE_H, innerH - dy);
                blit(context, x + BORDER + dx, y + BORDER + dy, BODY_U, BODY_V, w, h);
            }
        }

        for (int dy = 0; dy < innerH; dy += BODY_TILE_H) {
            int h = Math.min(BODY_TILE_H, innerH - dy);
            blit(context, x, y + BORDER + dy, 0, BODY_V, BORDER, h);
            blit(context, x + width - BORDER, y + BORDER + dy, RIGHT_U, BODY_V, BORDER, h);
        }
    }

    private void blit(GuiGraphicsExtractor context, int x, int y, int u, int v, int width, int height) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, (float) u, (float) v, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
