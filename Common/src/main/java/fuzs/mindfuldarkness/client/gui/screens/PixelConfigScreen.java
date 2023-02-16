package fuzs.mindfuldarkness.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.mindfuldarkness.MindfulDarkness;
import fuzs.mindfuldarkness.client.gui.components.NewTextureButton;
import fuzs.mindfuldarkness.client.gui.components.NewTextureSliderButton;
import fuzs.mindfuldarkness.client.handler.ColorChangedResourcesHandler;
import fuzs.mindfuldarkness.client.handler.DaytimeSwitchHandler;
import fuzs.mindfuldarkness.client.util.PixelDarkener;
import fuzs.mindfuldarkness.config.ClientConfig;
import fuzs.puzzleslib.config.core.AbstractConfigValue;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

public class PixelConfigScreen extends Screen {
    private static final Component ALGORITHM_COMPONENT = Component.translatable("screen.daytime_switcher.algorithm");
    private static final Component INTERFACE_DARKNESS_COMPONENT = Component.translatable("screen.daytime_switcher.interface_darkness");
    private static final Component FONT_DARKNESS_COMPONENT = Component.translatable("screen.daytime_switcher.front_brightness");
    private final Screen lastScreen;

    protected int imageWidth = 176;
    protected int imageHeight = 166;
    protected int leftPos;
    protected int topPos;

    public PixelConfigScreen(Screen lastScreen) {
        super(Component.empty());
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        DaytimeSwitchHandler.makeButtons(this.minecraft, this, this.leftPos, this.topPos, this.imageWidth, this::addRenderableWidget);
        ClientConfig clientConfig = MindfulDarkness.CONFIG.get(ClientConfig.class);
        this.addRenderableWidget(new NewTextureButton(this.leftPos + 13, this.topPos + 32, 150, 20, clientConfig.darkeningAlgorithm.get().getComponent(), button -> {
            AbstractConfigValue<PixelDarkener> configValue = clientConfig.darkeningAlgorithm;
            PixelDarkener pixelDarkener = PixelDarkener.values()[(configValue.get().ordinal() + 1) % PixelDarkener.values().length];
            configValue.set(pixelDarkener);
            button.setMessage(pixelDarkener.getComponent());
            ColorChangedResourcesHandler.INSTANCE.recordedReset();
        }));
        this.addRenderableWidget(new NewTextureSliderButton(this.leftPos + 13, this.topPos + 81, 150, 18, Component.empty(), clientConfig.textureDarkness.get()) {

            @Override
            protected void updateMessage() {

            }

            @Override
            protected void applyValue() {
                clientConfig.textureDarkness.set(this.value);
                ColorChangedResourcesHandler.INSTANCE.recordedReset();
            }
        });
        this.addRenderableWidget(new NewTextureSliderButton(this.leftPos + 13, this.topPos + 129, 150, 18, Component.empty(), clientConfig.fontBrightness.get()) {

            @Override
            protected void updateMessage() {

            }

            @Override
            protected void applyValue() {
                clientConfig.fontBrightness.set(this.value);
            }
        });
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        this.renderBg(poseStack, partialTick, mouseX, mouseY);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderLabels(poseStack, mouseX, mouseY);
    }

    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, DaytimeSwitchHandler.TEXTURE_LOCATION);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        DaytimeSwitchHandler.drawThemeBg(poseStack, this.leftPos, this.topPos, this.imageWidth);
    }

    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        NewTextureButton.drawCenteredString(poseStack, this.font, ALGORITHM_COMPONENT, this.width / 2, this.topPos + 19, 4210752, false);
        NewTextureButton.drawCenteredString(poseStack, this.font, INTERFACE_DARKNESS_COMPONENT, this.width / 2, this.topPos + 67, 4210752, false);
        NewTextureButton.drawCenteredString(poseStack, this.font, FONT_DARKNESS_COMPONENT, this.width / 2, this.topPos + 115, 4210752, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.lastScreen.onClose();
    }

    public void closeToLastScreen() {
        this.minecraft.setScreen(this.lastScreen);
    }
}
