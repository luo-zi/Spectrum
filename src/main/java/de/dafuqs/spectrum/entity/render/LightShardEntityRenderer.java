package de.dafuqs.spectrum.entity.render;

import de.dafuqs.spectrum.entity.entity.*;
import net.fabricmc.api.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.util.math.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

@Environment(EnvType.CLIENT)
public class LightShardEntityRenderer extends EntityRenderer<LightShardEntity> {

    public LightShardEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
    
    @Override
    public void render(LightShardEntity shard, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
	
		var age = shard.age;
        var alpha = MathHelper.clamp(1 - MathHelper.lerp(tickDelta, shard.getVanishingProgress(age - 1), shard.getVanishingProgress(age)), 0F, 1F);
        var scaleFactor = MathHelper.sin((age + tickDelta) / 16F) / 10F + shard.getScaleOffset();
	
		matrices.multiply(this.dispatcher.getRotation());
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));
		matrices.scale(scaleFactor, scaleFactor, 1);
        matrices.translate(-0.5F, -0.5F, 0);
        
        var consumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentCull(getTexture(shard)));
        var matrix = matrices.peek();
        var positions = matrix.getPositionMatrix();
        var normals = matrix.getNormalMatrix();
        
        consumer.vertex(positions, 0, 0, 0).color(1f, 1f, 1f, alpha).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(normals, 0, 1, 0).next();
        consumer.vertex(positions, 1, 0, 0).color(1f, 1f, 1f, alpha).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(normals, 0, 1, 0).next();
        consumer.vertex(positions, 1, 1, 0).color(1f, 1f, 1f, alpha).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(normals, 0, 1, 0).next();
        consumer.vertex(positions, 0, 1, 0).color(1f, 1f, 1f, alpha).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(normals, 0, 1, 0).next();
        
        matrices.pop();
        
        super.render(shard, yaw, tickDelta, matrices, vertexConsumers, light);
    }
    
    @Override
    public Identifier getTexture(LightShardEntity entity) {
        return entity.getTexture();
    }
}
