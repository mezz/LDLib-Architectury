package com.lowdragmc.lowdraglib.client.shader;

import com.google.common.collect.ImmutableMap;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.shader.management.Shader;
import com.lowdragmc.lowdraglib.gui.texture.ShaderTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_POSITION;

@Environment(EnvType.CLIENT)
public class Shaders {

	public static Shader IMAGE_F;
	public static Shader IMAGE_V;
	public static Shader GUI_IMAGE_V;
	public static Shader SCREEN_V;
	public static Shader ROUND_F;
	public static Shader PANEL_BG_F;
	public static Shader ROUND_BOX_F;
	public static Shader PROGRESS_ROUND_BOX_F;
	public static Shader FRAME_ROUND_BOX_F;
	public static Shader ROUND_LINE_F;

	public static void init() {
		IMAGE_F = load(Shader.ShaderType.FRAGMENT, new ResourceLocation(LDLib.MOD_ID, "image"));
		IMAGE_V = load(Shader.ShaderType.VERTEX, new ResourceLocation(LDLib.MOD_ID, "image"));
		GUI_IMAGE_V = load(Shader.ShaderType.VERTEX, new ResourceLocation(LDLib.MOD_ID, "gui_image"));
		SCREEN_V = load(Shader.ShaderType.VERTEX, new ResourceLocation(LDLib.MOD_ID, "screen"));
		ROUND_F = load(Shader.ShaderType.FRAGMENT, new ResourceLocation(LDLib.MOD_ID, "round"));
		PANEL_BG_F = load(Shader.ShaderType.FRAGMENT, new ResourceLocation(LDLib.MOD_ID, "panel_bg"));
		ROUND_BOX_F = load(Shader.ShaderType.FRAGMENT, new ResourceLocation(LDLib.MOD_ID, "round_box"));
		PROGRESS_ROUND_BOX_F = load(Shader.ShaderType.FRAGMENT, new ResourceLocation(LDLib.MOD_ID, "progress_round_box"));
		FRAME_ROUND_BOX_F = load(Shader.ShaderType.FRAGMENT, new ResourceLocation(LDLib.MOD_ID, "frame_round_box"));
		ROUND_LINE_F = load(Shader.ShaderType.FRAGMENT, new ResourceLocation(LDLib.MOD_ID, "round_line"));
	}

	public static Map<ResourceLocation, Shader> CACHE = new HashMap<>();

	public static void reload() {
		for (Shader shader : CACHE.values()) {
			if (shader != null) {
				shader.deleteShader();
			}
		}
		CACHE.clear();
		init();
		DrawerHelper.init();
		ShaderTexture.clearCache();
	}

	public static Shader load(Shader.ShaderType shaderType, ResourceLocation resourceLocation) {
		return CACHE.computeIfAbsent(new ResourceLocation(resourceLocation.getNamespace(), "shaders/" + resourceLocation.getPath() + shaderType.shaderExtension), key -> {
			try {
				Shader shader = Shader.loadShader(shaderType, key);
				LDLib.LOGGER.debug("load shader {} resource {} success", shaderType, resourceLocation);
				return shader;
			} catch (IOException e) {
				LDLib.LOGGER.error("load shader {} resource {} failed", shaderType, resourceLocation);
				LDLib.LOGGER.error("caused by ", e);
				return IMAGE_F;
			}
		});
	}

	// *** vanilla **//

	@Getter
	private static ShaderInstance particleShader;
	@Getter
	private static ShaderInstance blitShader;
	@Getter
	private static ShaderInstance hsbShader;
	@Getter
	private static ShaderInstance compassLineShader;

	/**
	 * the vertex format for HSB color, three four of float
	 */
	private static final VertexFormatElement HSB_Alpha = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.COLOR, 4);

	public static final VertexFormat HSB_VERTEX_FORMAT = new VertexFormat(
			ImmutableMap.<String, VertexFormatElement>builder()
					.put("Position", ELEMENT_POSITION)
					.put("HSB_ALPHA", HSB_Alpha)
					.build());

    public static List<Pair<ShaderInstance, Consumer<ShaderInstance>>> registerShaders(ResourceProvider resourceProvider) {
		try {
			return List.of(
					Pair.of(new ShaderInstance(resourceProvider,
									new ResourceLocation(LDLib.MOD_ID, "particle").toString(), DefaultVertexFormat.PARTICLE),
							shaderInstance -> particleShader = shaderInstance),
					Pair.of(new ShaderInstance(resourceProvider,
									new ResourceLocation(LDLib.MOD_ID, "fast_blit").toString(), DefaultVertexFormat.POSITION),
							shaderInstance -> blitShader = shaderInstance),
					Pair.of(new ShaderInstance(resourceProvider,
									new ResourceLocation(LDLib.MOD_ID, "hsb_block").toString(), HSB_VERTEX_FORMAT),
							shaderInstance -> hsbShader = shaderInstance),
					Pair.of(new ShaderInstance(resourceProvider,
									new ResourceLocation(LDLib.MOD_ID, "compass_line").toString(), DefaultVertexFormat.POSITION_TEX_COLOR),
							shaderInstance -> compassLineShader = shaderInstance)
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
}
