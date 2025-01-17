package fuzs.mindfuldarkness.client.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.mindfuldarkness.MindfulDarkness;
import fuzs.mindfuldarkness.client.gui.screens.PixelConfigScreen;
import fuzs.mindfuldarkness.client.util.ScreenIdentifierHelper;
import fuzs.mindfuldarkness.config.ClientConfig;
import fuzs.mindfuldarkness.mixin.client.accessor.AbstractContainerMenuAccessor;
import fuzs.puzzleslib.api.client.screen.v2.ScreenHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.DefaultedValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class DaytimeSwitcherHandler {
    public static final ResourceLocation TEXTURE_LOCATION = MindfulDarkness.id("textures/gui/daytime_switcher.png");

    private static AbstractWidget[] buttons;

    public static void onEndTick(Minecraft minecraft) {
        setHorizontalButtonPosition(minecraft.screen);
    }

    public static void onAfterMouseClick(Screen screen, double mouseX, double mouseY, int button) {
        setHorizontalButtonPosition(screen);
    }

    private static void setHorizontalButtonPosition(Screen screen) {
        if (buttons == null) return;
        if (screen instanceof AbstractContainerScreen<?> containerScreen && screen instanceof RecipeUpdateListener) {
            int leftPos = ScreenHelper.INSTANCE.getLeftPos(containerScreen);
            int imageWidth = ScreenHelper.INSTANCE.getImageWidth(containerScreen);
            buttons[0].setX(leftPos + imageWidth - 3 - 21);
            buttons[1].setX(leftPos + imageWidth - 3 - 40);
            buttons[2].setX(leftPos + imageWidth - 3 - 68);
            buttons[3].setX(leftPos + imageWidth - 3 - 95);
        }
    }

    public static EventResult onScreenOpening(@Nullable Screen oldScreen, DefaultedValue<Screen> newScreen) {
        Screen screen = newScreen.get();
        if (screen == null) buttons = null;
        Minecraft minecraft = Minecraft.getInstance();
        if (screen != null && MindfulDarkness.CONFIG.get(ClientConfig.class).debugAllScreens) {
            String identifier = ScreenIdentifierHelper.getScreenIdentifier(screen);
            if (identifier != null) {
                Component message = Component.translatable("screen.debug.identifier", ComponentUtils.wrapInSquareBrackets(Component.literal(identifier)));
                // we don't need both as chat messages are logged automatically
                if (minecraft.level != null) {
                    minecraft.gui.getChat().addMessage(message);
                } else {
                    MindfulDarkness.LOGGER.info(message.getString());
                }
            }
        }
        if (screen instanceof AbstractContainerScreen<?> containerScreen && MindfulDarkness.CONFIG.get(ClientConfig.class).debugContainerTypes) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            MenuType<?> type = ((AbstractContainerMenuAccessor) containerScreen.getMenu()).mindfuldarkness$getMenuType();
            if (type != null) {
                Component component = Component.literal(BuiltInRegistries.MENU.getKey(type).toString());
                Component message = Component.translatable("screen.debug.menuType", ComponentUtils.wrapInSquareBrackets(component));
                minecraft.gui.getChat().addMessage(message);
            }
        }
        return EventResult.PASS;
    }

    public static void onDrawBackground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (supportsDaytimeSwitcher(screen)) {
            int leftPos = ScreenHelper.INSTANCE.getLeftPos(screen);
            int topPos = ScreenHelper.INSTANCE.getTopPos(screen);
            int imageWidth = ScreenHelper.INSTANCE.getImageWidth(screen);
            drawThemeBg(guiGraphics, leftPos, topPos, imageWidth);
        }
    }

    public static void drawThemeBg(GuiGraphics guiGraphics, int leftPos, int topPos, int imageWidth) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE_LOCATION, leftPos + imageWidth - 3 - 101, topPos - 24, 0, 226, 101, 24, 256, 256);
    }

    private static boolean supportsDaytimeSwitcher(AbstractContainerScreen<?> containerScreen) {
        if (MindfulDarkness.CONFIG.get(ClientConfig.class).hideInGameSwitcher) return false;
        if (containerScreen.height >= ScreenHelper.INSTANCE.getImageHeight(containerScreen) + 2 * 24) {
            if (containerScreen instanceof CreativeModeInventoryScreen) return false;
            MenuType<?> type = ((AbstractContainerMenuAccessor) containerScreen.getMenu()).mindfuldarkness$getMenuType();
            return type == null || !MindfulDarkness.CONFIG.get(ClientConfig.class).menuBlacklist.contains(type);
        }
        return false;
    }

    public static void onAfterInit(Minecraft minecraft, Screen screen, int screenWidth, int screenHeight, List<AbstractWidget> widgets, Consumer<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen && supportsDaytimeSwitcher(containerScreen)) {
            int leftPos = ScreenHelper.INSTANCE.getLeftPos(containerScreen);
            int topPos = ScreenHelper.INSTANCE.getTopPos(containerScreen);
            int imageWidth = ScreenHelper.INSTANCE.getImageWidth(containerScreen);
            buttons = makeButtons(minecraft, screen, leftPos, topPos, imageWidth);
            for (AbstractWidget button : buttons) {
                addWidget.accept(button);
            }
        }
    }

    public static AbstractWidget[] makeButtons(Minecraft minecraft, Screen screen, int leftPos, int topPos, int imageWidth) {
        AbstractWidget[] abstractWidgets = new AbstractWidget[4];
        abstractWidgets[0] = new ImageButton(leftPos + imageWidth - 3 - 21, topPos - 18, 15, 15, 224, 0, TEXTURE_LOCATION, button -> {
            screen.onClose();
        });
        abstractWidgets[1] = new ImageButton(leftPos + imageWidth - 3 - 40, topPos - 18, 15, 15, 239, 0, TEXTURE_LOCATION, button -> {
            if (screen instanceof PixelConfigScreen pixelConfigScreen) {
                pixelConfigScreen.closeToLastScreen();
            } else {
                minecraft.setScreen(new PixelConfigScreen(screen));
            }
        });
        abstractWidgets[2] = new ImageButton(leftPos + imageWidth - 3 - 68, topPos - 20, 24, 19, 200, 0, TEXTURE_LOCATION, button -> {
            toggleThemeButtons(abstractWidgets[3], abstractWidgets[2], true);
        });
        abstractWidgets[3] = new ImageButton(leftPos + imageWidth - 3 - 95, topPos - 20, 24, 19, 176, 0, TEXTURE_LOCATION, button -> {
            toggleThemeButtons(abstractWidgets[3], abstractWidgets[2], true);
        });
        toggleThemeButtons(abstractWidgets[3], abstractWidgets[2], false);
        return abstractWidgets;
    }

    private static void toggleThemeButtons(AbstractWidget lightThemeWidget, AbstractWidget darkThemeWidget, boolean toggleSetting) {
        if (toggleSetting) activateDaytimeSwitch();
        boolean darkTheme = MindfulDarkness.CONFIG.get(ClientConfig.class).darkTheme.get();
        lightThemeWidget.active = darkTheme;
        darkThemeWidget.active = !darkTheme;
    }

    public static void activateDaytimeSwitch() {
        boolean darkTheme = MindfulDarkness.CONFIG.get(ClientConfig.class).darkTheme.get();
        darkTheme = !darkTheme;
        MindfulDarkness.CONFIG.get(ClientConfig.class).darkTheme.set(darkTheme);
        ColorChangedAssetsManager.INSTANCE.recordedReset();
    }
}
