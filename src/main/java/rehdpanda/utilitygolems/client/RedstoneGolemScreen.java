package rehdpanda.utilitygolems.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import rehdpanda.utilitygolems.RedstoneGolemScreenHandler;
import rehdpanda.utilitygolems.UGInit;
import rehdpanda.utilitygolems.UtilityGolem;

import java.util.ArrayList;
import java.util.List;

public class RedstoneGolemScreen extends HandledScreen<RedstoneGolemScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/generic_54.png");
    private final List<BlockPos> nearbyInteractables = new ArrayList<>();
    private int scrollOffset = 0;
    private TextFieldWidget intervalField;

    public RedstoneGolemScreen(RedstoneGolemScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 300;
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.playerInventoryTitleX = (this.backgroundWidth - 176) / 2 + 8;
    }

    @Override
    protected void init() {
        super.init();
        
        intervalField = new TextFieldWidget(this.textRenderer, this.x + 230, this.y + 115, 30, 12, Text.literal("20"));
        intervalField.setText("20");
        intervalField.setChangedListener(s -> {});
        
        scanNearby();
        refreshButtons();
    }

    private void scanNearby() {
        nearbyInteractables.clear();
        UtilityGolem golem = handler.getGolem();
        if (golem == null) return;

        BlockPos pos = golem.getBlockPos();
        int range = 8;
        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos p = pos.add(x, y, z);
                    BlockState state = golem.getEntityWorld().getBlockState(p);
                    Block block = state.getBlock();
                    if (block instanceof ButtonBlock || block instanceof LeverBlock || block instanceof DoorBlock || block instanceof TrapdoorBlock || block instanceof FenceGateBlock) {
                        nearbyInteractables.add(p);
                    }
                }
            }
        }
    }

    private void refreshButtons() {
        this.clearChildren();
        this.addSelectableChild(intervalField);
        UtilityGolem golem = handler.getGolem();
        if (golem == null) return;

        // Start/Stop button
        String startLabel = golem.isRedstoneProgramStarted() ? "§cStop" : "§aStart";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(startLabel), button -> {
            ClientPlayNetworking.send(new UGInit.RedstoneActionPayload(golem.getId(), 0));
            refreshButtons();
        }).dimensions(this.x + 7, this.y + 17, 90, 20).build());

        // Reset button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            ClientPlayNetworking.send(new UGInit.RedstoneActionPayload(golem.getId(), 1));
            refreshButtons();
        }).dimensions(this.x + 105, this.y + 17, 90, 20).build());

        // Scan button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Scan"), button -> {
            scanNearby();
            refreshButtons();
        }).dimensions(this.x + 203, this.y + 17, 90, 20).build());

        // Program list (left side)
        List<UtilityGolem.RedstoneInteraction> program = golem.getRedstoneProgram();
        for (int i = 0; i < 5 && i < program.size(); i++) {
            final int index = i;
            UtilityGolem.RedstoneInteraction inter = program.get(index);
            BlockState state = golem.getEntityWorld().getBlockState(inter.pos());
            String blockName = state.getBlock().getName().getString();
            String label = blockName + " (" + inter.interval() + "t)";
            
            this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> {
                program.remove(index);
                sendUpdate();
                refreshButtons();
            }).dimensions(this.x + 7, this.y + 40 + i * 18, 15, 15).build());
            
            Text txt = Text.literal(label);
            if (golem.isRedstoneProgramStarted() && golem.getCurrentInteractionIndex() == index) {
                txt = Text.literal("§e> " + label);
            }
            this.addDrawableChild(ButtonWidget.builder(txt, b -> {}).dimensions(this.x + 44, this.y + 40 + i * 18, 100, 15).build());
        }

        // Nearby list (right side)
        for (int i = 0; i < 5 && (i + scrollOffset) < nearbyInteractables.size(); i++) {
            final BlockPos p = nearbyInteractables.get(i + scrollOffset);
            BlockState state = golem.getEntityWorld().getBlockState(p);
            String blockName = state.getBlock().getName().getString();
            
            this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
                int interval = 20;
                try { interval = Integer.parseInt(intervalField.getText()); } catch (Exception ignored) {}
                program.add(new UtilityGolem.RedstoneInteraction(p, interval));
                sendUpdate();
                refreshButtons();
            }).dimensions(this.x + 150, this.y + 40 + i * 18, 15, 15).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal(blockName), b -> {}).dimensions(this.x + 187, this.y + 40 + i * 18, 90, 15).build());
        }
        
        if (nearbyInteractables.size() > 5) {
             this.addDrawableChild(ButtonWidget.builder(Text.literal("▲"), button -> {
                scrollOffset = Math.max(0, scrollOffset - 1);
                refreshButtons();
            }).dimensions(this.x + 281, this.y + 40, 12, 12).build());
             this.addDrawableChild(ButtonWidget.builder(Text.literal("▼"), button -> {
                scrollOffset = Math.min(nearbyInteractables.size() - 5, scrollOffset + 1);
                refreshButtons();
            }).dimensions(this.x + 281, this.y + 40 + 4 * 18 + 3, 12, 12).build());
        }
    }
    
    private void sendUpdate() {
        UtilityGolem golem = handler.getGolem();
        if (golem != null) {
            ClientPlayNetworking.send(new UGInit.SyncRedstoneProgramPayload(golem.getId(), new ArrayList<>(golem.getRedstoneProgram())));
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // No background
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, "Program", this.x + 7, this.y + 38, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Nearby", this.x + 150, this.y + 38, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Interval:", this.x + 180, this.y + 117, 0xFFFFFF, false);
        
        UtilityGolem golem = handler.getGolem();
        if (golem != null) {
            // Draw item icons for program
            List<UtilityGolem.RedstoneInteraction> program = golem.getRedstoneProgram();
            for (int i = 0; i < 5 && i < program.size(); i++) {
                BlockState state = golem.getEntityWorld().getBlockState(program.get(i).pos());
                ItemStack stack = new ItemStack(state.getBlock());
                int itemX = this.x + 24;
                int itemY = this.y + 40 + i * 18;
                // Draw slot background (using the first slot in generic_54.png as a source)
                context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, itemX, itemY, 7, 17, 18, 18, 256, 256);
                context.drawItem(stack, itemX + 1, itemY + 1);
            }

            // Draw item icons for nearby
            for (int i = 0; i < 5 && (i + scrollOffset) < nearbyInteractables.size(); i++) {
                BlockPos p = nearbyInteractables.get(i + scrollOffset);
                BlockState state = golem.getEntityWorld().getBlockState(p);
                ItemStack stack = new ItemStack(state.getBlock());
                int itemX = this.x + 167;
                int itemY = this.y + 40 + i * 18;
                context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, TEXTURE, itemX, itemY, 7, 17, 18, 18, 256, 256);
                context.drawItem(stack, itemX + 1, itemY + 1);
            }
        }

        intervalField.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
