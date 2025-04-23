package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.network.FilterTypePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class FilterScreen extends HandledScreen<FilterScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of(AutomaticSorter.MOD_ID, "textures/gui/filter.png");
    private ButtonWidget receiveItemsButton;
    private static final ItemStack CHEST_BLOCK = new ItemStack(Blocks.CHEST);
    private static final ItemStack FILTER_BLOCK = new ItemStack(ModBlocks.FILTER_BLOCK);
    private static final ItemStack NO_FILTER = new ItemStack(Blocks.HOPPER);


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
            BlockPos blockPos = handler.getBlockPos();
            FilterTypePayload payload = new FilterTypePayload(blockPos, value);
            ClientPlayNetworking.send(payload);
        }).dimensions(this.x + 6, this.y + 14, 18, 18).build();

        this.addDrawableChild(receiveItemsButton);
    }

    private Text getButtonText() {
        return Text.of(FilterBlockEntity.FilterTypeEnum.getName(handler.getFilterType()));
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x, y, 0f, 0f, backgroundWidth, backgroundHeight, 256, 256);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        if (receiveItemsButton.isMouseOver(mouseX, mouseY)) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawTooltip(textRenderer, getButtonText(), mouseX, mouseY);
        }

        if (!receiveItemsButton.isMouseOver(mouseX, mouseY)) {
            receiveItemsButton.setFocused(false);
        }

        int filterType = handler.getFilterType();
        ItemStack renderButtonBlock = getDisplayedItemStack();
        context.drawItem(renderButtonBlock, this.x + 7, this.y + 15);

        switch (FilterBlockEntity.FilterTypeEnum.fromValue(filterType)) {
            case REJECTS:
            case IN_INVENTORY:
                int x = this.x + 25;
                int y = this.y + 14;
                int width = 8 * 18;
                int height = 3 * 18;
                int color = 0x80000000;
                context.fill(x, y, x + width, y + height, color);
                break;
            default:
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
            return NO_FILTER;
        }
    }
}