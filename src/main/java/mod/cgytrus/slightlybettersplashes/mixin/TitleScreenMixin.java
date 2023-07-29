package mod.cgytrus.slightlybettersplashes.mixin;

import mod.cgytrus.slightlybettersplashes.SlightlyBetterSplashes;
import net.minecraft.client.gui.SplashTextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Shadow
    @Nullable
    private SplashTextRenderer splashTextRenderer;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void alwaysRefreshSplashText(CallbackInfo ci) {
        // setting splashTextRenderer to null in the beginning of init would cause the null check always fail,
        // causing the text to always be reset to a new splash every time you enter the title screen
        splashTextRenderer = null;
        SlightlyBetterSplashes.splashLines = SlightlyBetterSplashes.EmptySplashLines;
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void createMultilineSplashText(CallbackInfo ci) {
        if(splashTextRenderer == null)
            return;
        StringVisitable text = StringVisitable.plain(splashTextRenderer.splashText);
        int textWidth = textRenderer.getWidth(text);
        // if our text is small enough just render it the same as vanilla
        if(textWidth <= 256) {
            SlightlyBetterSplashes.splashLines = SlightlyBetterSplashes.EmptySplashLines;
            return;
        }

        // binary search for the perfect line length to line count ratio
        // (either 48:1 or 16:1 depending on the entire text length)
        int targetRatio = textWidth <= 1024 ? 48 : 16;
        int left = Math.min(targetRatio, width);
        int right = width;
        List<OrderedText> lines;
        do {
            int wrapWidth = (left + right) / 2;
            lines = textRenderer.wrapLines(text, wrapWidth);
            int ratio = Math.round((float)wrapWidth / lines.size());
            // don't mind the logs 😳
            //BetterSplashes.LOGGER.info("{}, {}-{}, {}/{}", ratio, left, right, wrapWidth, lines.size());
            if(ratio == targetRatio || left + 1 >= right) {
                //BetterSplashes.LOGGER.info("found");
                break;
            }
            if(ratio < targetRatio)
                left = wrapWidth;
            else
                right = wrapWidth;
        } while(true);

        SlightlyBetterSplashes.splashLines = lines;
        SlightlyBetterSplashes.splashTextScale = 100f / (float)(164 + 32);
    }
}
