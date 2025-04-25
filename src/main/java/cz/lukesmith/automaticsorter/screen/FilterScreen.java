package cz.lukesmith.automaticsorter.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FilterScreen extends HandledScreen<FilterScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(AutomaticSorter.MOD_ID, "textures/gui/filter.png");
    private ButtonWidget receiveItemsButton;
    private static final ItemStack CHEST_BLOCK = new ItemStack(Blocks.CHEST);
    private static final ItemStack FILTER_BLOCK = new ItemStack(ModBlocks.FILTER_BLOCK);
    private static final ItemStack REJECTS = new ItemStack(Blocks.BARRIER);


    public FilterScreen(FilterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleY = 1000;
        playerInventoryTitleY = 1000;

        receiveItemsButton = ButtonWidget.builder(Text.of(""), button -> {
            int value = handler.toggleFilterType();
            sendFilterTypeUpdate(value);
        }).dimensions(this.x + 6, this.y + 14, 18, 18).build();

        this.addDrawableChild(receiveItemsButton);
    }

    private Text getButtonText() {
        return Text.of(FilterBlockEntity.FilterTypeEnum.getName(handler.getFilterType()));
    }

    private void sendFilterTypeUpdate(int value) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(value);
        ClientPlayNetworking.send(new Identifier(AutomaticSorter.MOD_ID, "update_receive_items"), buf);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);

        if (receiveItemsButton.isMouseOver(mouseX, mouseY)) {
            renderTooltip(matrices, getButtonText(), mouseX, mouseY);
        }

        int filterType = handler.getFilterType();
        ItemStack renderButtonBlock = getDisplayedItemStack();
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        switch (FilterBlockEntity.FilterTypeEnum.fromValue(filterType)) {
            case WHITELIST:
                itemRenderer.renderInGui(renderButtonBlock, this.x + 6, this.y + 15);
                break;
            case IN_INVENTORY:
            case REJECTS:
                itemRenderer.renderGuiItemIcon(renderButtonBlock, this.x + 6, this.y + 15);
                int x = this.x + 25;
                int y = this.y + 14;
                int width = 8 * 18;
                int height = 3 * 18;
                int color = 0x80000000;
                fill(matrices, x, y, x + width, y + height, color);
                break;
        }
    }

    private ItemStack getDisplayedItemStack() {
        int filterType = handler.getFilterType();
        if (filterType == FilterBlockEntity.FilterTypeEnum.WHITELIST.getValue()) {
            return FILTER_BLOCK;
        } else if (filterType == FilterBlockEntity.FilterTypeEnum.IN_INVENTORY.getValue()) {
            return CHEST_BLOCK;
        } else {
            return REJECTS;
        }
    }
}
