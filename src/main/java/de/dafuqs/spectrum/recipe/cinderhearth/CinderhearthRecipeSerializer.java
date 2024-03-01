package de.dafuqs.spectrum.recipe.cinderhearth;

import com.google.gson.*;
import de.dafuqs.spectrum.api.recipe.*;
import de.dafuqs.spectrum.recipe.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.recipe.*;
import net.minecraft.util.*;

import java.util.*;

public class CinderhearthRecipeSerializer implements GatedRecipeSerializer<CinderhearthRecipe> {
	
	public final RecipeFactory recipeFactory;
	
	public CinderhearthRecipeSerializer(RecipeFactory recipeFactory) {
		this.recipeFactory = recipeFactory;
	}
	
	public interface RecipeFactory {
		CinderhearthRecipe create(Identifier id, String group, boolean secret, Identifier requiredAdvancementIdentifier, Ingredient inputIngredient, int time, float experience, List<Pair<ItemStack, Float>> outputsWithChance);
	}
	
	@Override
	public CinderhearthRecipe read(Identifier identifier, JsonObject jsonObject) {
		String group = readGroup(jsonObject);
		boolean secret = readSecret(jsonObject);
		Identifier requiredAdvancementIdentifier = readRequiredAdvancementIdentifier(jsonObject);
		
		Ingredient inputIngredient = Ingredient.fromJson(JsonHelper.hasArray(jsonObject, "ingredient") ? JsonHelper.getArray(jsonObject, "ingredient") : JsonHelper.getObject(jsonObject, "ingredient"));
		int time = JsonHelper.getInt(jsonObject, "time");
		float experience = JsonHelper.getFloat(jsonObject, "experience");
		
		List<Pair<ItemStack, Float>> outputsWithChance = new ArrayList<>();
		for (JsonElement outputEntry : JsonHelper.getArray(jsonObject, "results")) {
			JsonObject outputObject = outputEntry.getAsJsonObject();
			ItemStack outputStack = RecipeUtils.itemStackWithNbtFromJson(outputObject);
			float outputChance = 1.0F;
			if (JsonHelper.hasNumber(outputObject, "chance")) {
				outputChance = JsonHelper.getFloat(outputObject, "chance");
			}
			outputsWithChance.add(new Pair<>(outputStack, outputChance));
		}
		
		return this.recipeFactory.create(identifier, group, secret, requiredAdvancementIdentifier, inputIngredient, time, experience, outputsWithChance);
	}
	
	@Override
	public void write(PacketByteBuf packetByteBuf, CinderhearthRecipe recipe) {
		packetByteBuf.writeString(recipe.group);
		packetByteBuf.writeBoolean(recipe.secret);
		writeNullableIdentifier(packetByteBuf, recipe.requiredAdvancementIdentifier);
		
		recipe.inputIngredient.write(packetByteBuf);
		packetByteBuf.writeInt(recipe.time);
		packetByteBuf.writeFloat(recipe.experience);
		
		packetByteBuf.writeInt(recipe.outputsWithChance.size());
		for (Pair<ItemStack, Float> output : recipe.outputsWithChance) {
			packetByteBuf.writeItemStack(output.getLeft());
			packetByteBuf.writeFloat(output.getRight());
		}
	}
	
	@Override
	public CinderhearthRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
		String group = packetByteBuf.readString();
		boolean secret = packetByteBuf.readBoolean();
		Identifier requiredAdvancementIdentifier = readNullableIdentifier(packetByteBuf);
		
		Ingredient inputIngredient = Ingredient.fromPacket(packetByteBuf);
		int time = packetByteBuf.readInt();
		float experience = packetByteBuf.readFloat();
		
		int outputCount = packetByteBuf.readInt();
		List<Pair<ItemStack, Float>> outputsWithChance = new ArrayList<>(outputCount);
		for (int i = 0; i < outputCount; i++) {
			outputsWithChance.add(new Pair<>(packetByteBuf.readItemStack(), packetByteBuf.readFloat()));
		}
		
		return this.recipeFactory.create(identifier, group, secret, requiredAdvancementIdentifier, inputIngredient, time, experience, outputsWithChance);
	}
	
}
