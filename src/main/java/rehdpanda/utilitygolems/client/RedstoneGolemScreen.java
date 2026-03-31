package rehdpanda.utilitygolems.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.entity.player.Inventory;
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

public class RedstoneGolemScreen extends AbstractContainerScreen<RedstoneGolemMenu> implements MenuAccess<RedstoneGolemMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private final List<BlockPos> nearbyInteractables = new ArrayList<>();
    private int scrollOffset = 0;
    private EditBox intervalField;

    public RedstoneGolemScreen(RedstoneGolemMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 300, 222);
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = (this.imageWidth - 176) / 2 + 8;
    }

    @Override
    protected void init() {
        super.init();
        
        intervalField = new EditBox(this.font, this.leftPos + 230, this.topPos + 115, 30, 12, Component.literal("20"));
        intervalField.setValue("20");
        intervalField.setResponder(s -> {});
        
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
        String startLabel = golem.isRedstoneProgramStarted() ? "§cStop" : "§aStart";
        this.addRenderableWidget(Button.builder(Component.literal(startLabel), button -> {
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.RedstoneActionPayload(golem.getId(), 0)));
            }
            refreshButtons();
        }).bounds(this.leftPos + 7, this.topPos + 17, 90, 20).build());

        // Reset button
        this.addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.RedstoneActionPayload(golem.getId(), 1)));
            }
            refreshButtons();
        }).bounds(this.leftPos + 105, this.topPos + 17, 90, 20).build());

        // Scan button
        this.addRenderableWidget(Button.builder(Component.literal("Scan"), button -> {
            scanNearby();
            refreshButtons();
        }).bounds(this.leftPos + 203, this.topPos + 17, 90, 20).build());

        // Program list (left side)
        List<UtilityGolem.RedstoneInteraction> program = golem.getRedstoneProgram();
        for (int i = 0; i < 5 && i < program.size(); i++) {
            final int index = i;
            UtilityGolem.RedstoneInteraction inter = program.get(index);
            BlockState state = golem.level().getBlockState(inter.pos());
            String blockName = state.getBlock().getDescriptionId();
            String label = blockName + " (" + inter.interval() + "t)";
            
            this.addRenderableWidget(Button.builder(Component.literal("X"), button -> {
                program.remove(index);
                sendUpdate();
                refreshButtons();
            }).bounds(this.leftPos + 7, this.topPos + 40 + i * 18, 15, 15).build());
            
            Component txt = Component.literal(label);
            if (golem.isRedstoneProgramStarted() && golem.getCurrentInteractionIndex() == index) {
                txt = Component.literal("§e> " + label);
            }
            this.addRenderableWidget(Button.builder(txt, b -> {}).bounds(this.leftPos + 44, this.topPos + 40 + i * 18, 100, 15).build());
        }

        // Nearby list (right side)
        for (int i = 0; i < 5 && (i + scrollOffset) < nearbyInteractables.size(); i++) {
            final BlockPos p = nearbyInteractables.get(i + scrollOffset);
            BlockState state = golem.level().getBlockState(p);
            String blockName = state.getBlock().getDescriptionId();
            
            this.addRenderableWidget(Button.builder(Component.literal("+"), button -> {
                int interval = 20;
                try { interval = Integer.parseInt(intervalField.getValue()); } catch (Exception ignored) {}
                program.add(new UtilityGolem.RedstoneInteraction(p, interval));
                sendUpdate();
                refreshButtons();
            }).bounds(this.leftPos + 150, this.topPos + 40 + i * 18, 15, 15).build());
            this.addRenderableWidget(Button.builder(Component.literal(blockName), b -> {}).bounds(this.leftPos + 187, this.topPos + 40 + i * 18, 90, 15).build());
        }
        
        if (nearbyInteractables.size() > 5) {
             this.addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                scrollOffset = Math.max(0, scrollOffset - 1);
                refreshButtons();
            }).bounds(this.leftPos + 281, this.topPos + 40, 12, 12).build());
             this.addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                scrollOffset = Math.min(nearbyInteractables.size() - 5, scrollOffset + 1);
                refreshButtons();
            }).bounds(this.leftPos + 281, this.topPos + 40 + 4 * 18 + 3, 12, 12).build());
        }
    }
    
    private void sendUpdate() {
        UtilityGolem golem = menu.getGolem();
        if (golem != null) {
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new UGInit.SyncRedstoneProgramPayload(golem.getId(), new ArrayList<>(golem.getRedstoneProgram()))));
            }
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // No background
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.extractTransparentBackground(context);
        super.extractRenderState(context, mouseX, mouseY, delta);
        
        // Draw the container title
        context.text(this.font, this.title, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY, 0xFF404040, false);
        
        // Draw the player inventory title
        context.text(this.font, this.playerInventoryTitle, this.leftPos + this.inventoryLabelX, this.topPos + this.inventoryLabelY, 0xFF404040, false);
        
        context.text(this.font, "Program", this.leftPos + 7, this.topPos + 38, 0xFFFFFFFF, false);
        context.text(this.font, "Nearby", this.leftPos + 150, this.topPos + 38, 0xFFFFFFFF, false);
        context.text(this.font, "Interval:", this.leftPos + 180, this.topPos + 117, 0xFFFFFFFF, false);
        
        UtilityGolem golem = menu.getGolem();
        if (golem != null) {
            // Draw item icons for program
            List<UtilityGolem.RedstoneInteraction> program = golem.getRedstoneProgram();
            for (int i = 0; i < 5 && i < program.size(); i++) {
                BlockState state = golem.level().getBlockState(program.get(i).pos());
                ItemStack stack = new ItemStack(state.getBlock());
                int itemX = this.leftPos + 24;
                int itemY = this.topPos + 40 + i * 18;
                // Draw slot background (using the first slot in generic_54.png as a source)
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, itemX, itemY, 7, 17, 18, 18, 256, 256);
                context.item(stack, itemX + 1, itemY + 1);
            }

            // Draw item icons for nearby
            for (int i = 0; i < 5 && (i + scrollOffset) < nearbyInteractables.size(); i++) {
                BlockPos p = nearbyInteractables.get(i + scrollOffset);
                BlockState state = golem.level().getBlockState(p);
                ItemStack stack = new ItemStack(state.getBlock());
                int itemX = this.leftPos + 167;
                int itemY = this.topPos + 40 + i * 18;
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, itemX, itemY, 7, 17, 18, 18, 256, 256);
                context.item(stack, itemX + 1, itemY + 1);
            }
        }

        intervalField.extractWidgetRenderState(context, mouseX, mouseY, delta);
        this.extractTooltip(context, mouseX, mouseY);
    }
}
