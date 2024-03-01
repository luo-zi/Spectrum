package de.dafuqs.spectrum.inventories.widgets;

import de.dafuqs.revelationary.api.advancements.*;
import de.dafuqs.spectrum.api.energy.color.*;
import de.dafuqs.spectrum.blocks.energy.*;
import de.dafuqs.spectrum.registries.*;
import net.fabricmc.api.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.narration.*;
import net.minecraft.client.gui.widget.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

import static de.dafuqs.spectrum.helpers.RenderHelper.*;

@Environment(EnvType.CLIENT)
public class ColorSelectionWidget extends ClickableWidget {
	
	protected final ColorPickerBlockEntity colorPicker;
	
	@Nullable
	private Consumer<InkColor> changedListener;
	protected final Screen screen;
	
	final List<Pair<InkColor, Boolean>> usableColors = new ArrayList<>(); // stores if a certain color should be displayed
	
	final int selectedDotX;
	final int selectedDotY;
	
	public ColorSelectionWidget(int x, int y, int selectedDotX, int selectedDotY, Screen screen, ColorPickerBlockEntity colorPicker) {
		super(x, y, 56, 14, Text.literal(""));
		this.colorPicker = colorPicker;
		this.selectedDotX = selectedDotX;
		this.selectedDotY = selectedDotY;
		this.screen = screen;
		
		for (InkColor inkColor : InkColor.all()) {
			usableColors.add(new Pair<>(inkColor, AdvancementHelper.hasAdvancementClient(inkColor.getRequiredAdvancement())));
		}
	}
	
	public void setChangedListener(@Nullable Consumer<InkColor> changedListener) {
		this.changedListener = changedListener;
	}
	
	private void onChanged(InkColor newColor) {
		if (this.changedListener != null) {
			this.changedListener.accept(newColor);
		}
	}

	@Override
	protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {

	}

	@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
		MinecraftClient client = MinecraftClient.getInstance();
		boolean colorUnselectionClicked = mouseX >= (double) selectedDotX && mouseX < (double) (selectedDotX + 4) && mouseY >= (double) selectedDotY && mouseY < (double) (selectedDotY + 4);
		if (colorUnselectionClicked) {
			client.player.playSound(SpectrumSoundEvents.BUTTON_CLICK, SoundCategory.NEUTRAL, 1.0F, 1.0F);
			onChanged(null);
		}
		
		boolean colorSelectionClicked = mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
		if (colorSelectionClicked && button == 0) {
			int xOffset = MathHelper.floor(mouseX) - this.getX();
			int yOffset = MathHelper.floor(mouseY) - this.getY();
			
			int horizontalColorOffset = xOffset / 7;
			int verticalColorOffset = yOffset / 7;
			int newColorIndex = horizontalColorOffset + verticalColorOffset * 8;
			InkColor newColor = InkColor.all().get(newColorIndex);
			if (this.colorPicker.getSelectedColor() != newColor) {
				if (AdvancementHelper.hasAdvancementClient(newColor.getRequiredAdvancement())) {
					client.player.playSound(SpectrumSoundEvents.BUTTON_CLICK, SoundCategory.NEUTRAL, 1.0F, 1.0F);
					onChanged(newColor);
				} else {
					client.player.playSound(SpectrumSoundEvents.USE_FAIL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
					onChanged(null);
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		builder.put(NarrationPart.TITLE, Text.translatable("spectrum.narration.color_selection", this.colorPicker.getSelectedColor()));
	}

	public void draw(DrawContext drawContext) {
		// draw selection icons
		int i = -1;
		int currentX = this.getX() + 1;
		int currentY = this.getY() + 1;
		for (Pair<InkColor, Boolean> color : usableColors) {
			if (color.getRight()) {
				fillQuad(drawContext.getMatrices(), currentX, currentY, 5, 5, color.getLeft().getColor());
			}
			i = i + 1;
			currentX = currentX + 7;
			if (i == 7) {
				currentY = currentY + 7;
				currentX = this.getX() + 1;
			}
		}
		
		// draw currently selected icon
		InkColor selectedColor = this.colorPicker.getSelectedColor();
		if (selectedColor != null) {
			fillQuad(drawContext.getMatrices(), selectedDotX, selectedDotY, 4, 4, selectedColor.getColor());
		}
	}
	
	public void drawMouseoverTooltip(DrawContext drawContext, int mouseX, int mouseY) {
		MinecraftClient client = MinecraftClient.getInstance();
		boolean overUnselection = mouseX >= (double) selectedDotX && mouseX < (double) (selectedDotX + 4) && mouseY >= (double) selectedDotY && mouseY < (double) (selectedDotY + 4);
		if (overUnselection) {
			drawContext.drawTooltip(client.textRenderer, List.of(Text.translatable("spectrum.tooltip.ink_powered.unselect_color")), Optional.empty(), getX(), getY());
		} else {
			
			int xOffset = MathHelper.floor(mouseX) - this.getX();
			int yOffset = MathHelper.floor(mouseY) - this.getY();
			
			int horizontalColorOffset = xOffset / 7;
			int verticalColorOffset = yOffset / 7;
			int newColorIndex = horizontalColorOffset + verticalColorOffset * 8;
			InkColor newColor = InkColor.all().get(newColorIndex);
			
			if (AdvancementHelper.hasAdvancementClient(newColor.getRequiredAdvancement())) {
				drawContext.drawTooltip(client.textRenderer, List.of(newColor.getName()), Optional.empty(), getX(), getY());
			} else {
				drawContext.drawTooltip(client.textRenderer, List.of(Text.translatable("spectrum.tooltip.ink_powered.unselect_color")), Optional.empty(), getX(), getY());
			}
		}
	}
}
