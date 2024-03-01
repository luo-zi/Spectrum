package de.dafuqs.spectrum.entity.render;

import de.dafuqs.spectrum.api.energy.color.*;
import de.dafuqs.spectrum.entity.entity.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.util.math.*;
import net.minecraft.screen.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import org.joml.Math;
import org.joml.*;

public class MagicProjectileEntityRenderer extends EntityRenderer<MagicProjectileEntity> {

	private static final Identifier TEXTURE = new Identifier("textures/entity/experience_orb.png");
	private static final RenderLayer LAYER = RenderLayer.getItemEntityTranslucentCull(TEXTURE);

	public MagicProjectileEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	public void render(MagicProjectileEntity magicProjectileEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
		matrixStack.push();
		Vector3f starColor = InkColor.of(magicProjectileEntity.getDyeColor()).getColor();
		
		double time = (magicProjectileEntity.getWorld().getTime() % 24000) + tickDelta + Random.create(magicProjectileEntity.getId()).nextInt(200);
		float scale = 0.75F + 0.1F * (float) Math.sin(time / 10);
		matrixStack.scale(scale, scale, scale);
		
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
		
		float h = 0.75F;
		float k = 1F;
		float l = 0F;
		float m = 0.25F;
		int s = (int) (starColor.x() * 255.0F);
		int t = (int) (starColor.y() * 255.0F);
		int u = (int) (starColor.z() * 255.0F);
		MatrixStack.Entry entry = matrixStack.peek();
		Matrix4f matrix4f = entry.getPositionMatrix();
		Matrix3f matrix3f = entry.getNormalMatrix();
		
		matrixStack.translate(0.0D, 0.10000000149011612D, 0.0D);
		matrixStack.multiply(this.dispatcher.getRotation());
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
		
		vertex(vertexConsumer, matrix4f, matrix3f, -0.5F, -0.25F, s, t, u, h, m, light);
		vertex(vertexConsumer, matrix4f, matrix3f, 0.5F, -0.25F, s, t, u, k, m, light);
		vertex(vertexConsumer, matrix4f, matrix3f, 0.5F, 0.75F, s, t, u, k, l, light);
		vertex(vertexConsumer, matrix4f, matrix3f, -0.5F, 0.75F, s, t, u, h, l, light);
		matrixStack.pop();
	}
	
	private static void vertex(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Matrix3f normalMatrix, float x, float y, int red, int green, int blue, float u, float v, int light) {
		vertexConsumer.vertex(positionMatrix, x, y, 0.0F).color(red, green, blue, 128).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
	}

	@Override
	public Identifier getTexture(MagicProjectileEntity entity) {
		return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
	}
	
}
