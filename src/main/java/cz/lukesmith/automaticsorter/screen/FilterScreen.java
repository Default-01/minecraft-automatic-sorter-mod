package cz.lukesmith.automaticsorter.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.network.FilterTextPayload;
import cz.lukesmith.automaticsorter.network.FilterTypePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class FilterScreen extends HandledScreen<FilterScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of(AutomaticSorter.MOD_ID, "textures/gui/filter.png");
    private static final Identifier TEXTURE_TEXT = Identifier.of(AutomaticSorter.MOD_ID, "textures/gui/filter_text.png");
    private ButtonWidget receiveItemsButton;
    private TextFieldWidget textFilterInput;
    private static final ItemStack CHEST_BLOCK = new ItemStack(Blocks.CHEST);
    private static final ItemStack FILTER_BLOCK = new ItemStack(ModBlocks.FILTER_BLOCK);
    private static final ItemStack REJECTS = new ItemStack(Blocks.BARRIER);
    private static final ItemStack TEXT_FILTER_ICON = new ItemStack(Items.BOOK);


    public FilterScreen(FilterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Check if text field is visible and focused, if so let it handle input first
        if (textFilterInput != null && textFilterInput.isVisible() && textFilterInput.isFocused()) {
            if (textFilterInput.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        
        // Prevent inventory key from closing GUI when text field is focused (like anvil screen)
        if (this.client != null && this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            if (textFilterInput != null && textFilterInput.isVisible() && textFilterInput.isFocused()) {
                return true; // Block inventory key when typing
            }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (textFilterInput != null && textFilterInput.isVisible() && textFilterInput.isFocused()) {
            return textFilterInput.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Let text field handle clicks first
        if (textFilterInput != null && textFilterInput.isVisible()) {
            if (textFilterInput.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(textFilterInput);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
            // Reinitialize to update text field visibility and focus
            this.clearChildren();
            this.init();
        }).dimensions(this.x + 6, this.y + 14, 18, 18).build();

        this.addDrawableChild(receiveItemsButton);

        // Create text field for TEXT_FILTER mode (styled like anvil)
        textFilterInput = new TextFieldWidget(this.textRenderer, this.x + 31, this.y + 20, 130, 12, Text.of("Filter"));
        textFilterInput.setMaxLength(50); // Prevent overflow
        textFilterInput.setDrawsBackground(false); // Disable background - using PNG texture instead
        textFilterInput.setEditableColor(16777215); // White text
        textFilterInput.setUneditableColor(11184810); // Gray text when not focused
        
        // Load the saved text from the block entity
        String savedText = handler.blockEntity.getTextFilter();
        if (savedText != null && !savedText.isEmpty()) {
            textFilterInput.setText(savedText);
        }
        
        // Text is automatically saved on each change via network
        textFilterInput.setChangedListener(text -> {
            BlockPos blockPos = handler.getBlockPos();
            FilterTextPayload payload = new FilterTextPayload(blockPos, text);
            ClientPlayNetworking.send(payload);
            handler.blockEntity.setTextFilter(text); // Update local copy immediately
        });
        
        // Check if we're in TEXT_FILTER mode and set visibility/focus accordingly
        int filterType = handler.getFilterType();
        if (filterType == FilterBlockEntity.FilterTypeEnum.TEXT_FILTER.getValue()) {
            textFilterInput.setVisible(true);
            textFilterInput.setFocused(true);
            this.setFocused(textFilterInput);
            this.setInitialFocus(textFilterInput);
        } else {
            textFilterInput.setVisible(false);
            textFilterInput.setFocused(false);
        }
        
        this.addDrawableChild(textFilterInput);
    }

    private Text getButtonText() {
        return Text.of(FilterBlockEntity.FilterTypeEnum.getName(handler.getFilterType()));
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // Use different texture based on filter mode
        int filterType = handler.getFilterType();
        if (filterType == FilterBlockEntity.FilterTypeEnum.TEXT_FILTER.getValue()) {
            RenderSystem.setShaderTexture(0, TEXTURE_TEXT);
            context.drawTexture(TEXTURE_TEXT, x, y, 0, 0, backgroundWidth, backgroundHeight);
        } else {
            RenderSystem.setShaderTexture(0, TEXTURE);
            context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        }
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

        // Update text field visibility and render it for TEXT_FILTER mode
        if (filterType == FilterBlockEntity.FilterTypeEnum.TEXT_FILTER.getValue()) {
            textFilterInput.setVisible(true);
            textFilterInput.render(context, mouseX, mouseY, delta);
        } else {
            textFilterInput.setVisible(false);
        }

        // Add darkening overlay for REJECTS and IN_INVENTORY modes
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
        } else if (filterType == FilterBlockEntity.FilterTypeEnum.TEXT_FILTER.getValue()) {
            return TEXT_FILTER_ICON;
        } else {
            return REJECTS;
        }
    }
}