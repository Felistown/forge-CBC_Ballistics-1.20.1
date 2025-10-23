package net.felis.cbc_ballistics.entity.model;// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.felis.cbc_ballistics.CBC_Ballistics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RadioModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(CBC_Ballistics.MODID, "custommodel"), "main");
	public final ModelPart BODY;

	public RadioModel(ModelPart root) {
		this.BODY = root.getChild("Body");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, 2.0F, 8.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(26, 0).addBox(-2.0F, -21.0F, 3.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-4.0F, -1.0F, 2.0F, 8.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 13).addBox(4.0F, 1.0F, 3.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 30, 21);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		BODY.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}