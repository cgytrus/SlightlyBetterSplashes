package mod.cgytrus.slightlybettersplashes.mixin;

import mod.cgytrus.slightlybettersplashes.SlightlyBetterSplashes;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.Axis;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Shadow
    @Nullable
    private String splashText;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void alwaysRefreshSplashText(CallbackInfo ci) {
        // setting splashText to null in the beginning of init would cause the null check always fail,
        // causing the text to always be reset to a new splash every time you enter the title screen
        splashText = null;
        SlightlyBetterSplashes.splashText = MultilineText.EMPTY;
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void createMultilineSplashText(CallbackInfo ci) {
        if(splashText == null)
            return;
        StringVisitable text = StringVisitable.plain(splashText);
        int textWidth = textRenderer.getWidth(text);
        // if our text is small enough just render it the same as vanilla
        if(textWidth <= 256) {
            SlightlyBetterSplashes.splashText = MultilineText.EMPTY;
            return;
        }

        // binary search for the perfect line length to line count ratio
        // (either 48:1 or 16:1 depending on the entire text length)
        int targetRatio = textWidth <= 1024 ? 48 : 16;
        int left = Math.min(targetRatio, width);
        int right = width;
        int wrapWidth;
        do {
            wrapWidth = (left + right) / 2;
            List<OrderedText> lines = textRenderer.wrapLines(text, wrapWidth);
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

        SlightlyBetterSplashes.splashText = MultilineText.create(textRenderer, text, wrapWidth);
        SlightlyBetterSplashes.splashTextScale = 100f / (float)(164 + 32);
        splashText = null;
    }

    @Inject(method = "render", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void renderMultilineSplashText(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci,
        float f, float g) {
        if(SlightlyBetterSplashes.splashText == MultilineText.EMPTY)
            return;

        // code copied from TitleScreen#render but modified to draw the multiline splash text instead of the vanilla one

        int i = MathHelper.ceil(g * 255f) << 24;
        if((i & 0xfc000000) == 0)
            return;

        matrices.push();
        matrices.translate((float)(width / 2 + 90), 70f, 0f);
        matrices.multiply(Axis.Z_POSITIVE.rotationDegrees(-20f));
        float h = 1.8f - MathHelper.abs(
            MathHelper.sin((float)(Util.getMeasuringTimeMs() % 1000L) / 1000f * (float)(Math.PI * 2d)) * 0.1f);
        h *= SlightlyBetterSplashes.splashTextScale;
        matrices.scale(h, h, h);
        SlightlyBetterSplashes.splashText.drawCenterWithShadow(matrices, 0, -8, 9, 0xffff00 | i);
        matrices.pop();
    }
}
