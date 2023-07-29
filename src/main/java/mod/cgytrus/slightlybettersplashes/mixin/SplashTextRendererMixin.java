package mod.cgytrus.slightlybettersplashes.mixin;

import mod.cgytrus.slightlybettersplashes.SlightlyBetterSplashes;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.SplashTextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Util;
import net.minecraft.util.math.Axis;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashTextRenderer.class)
public abstract class SplashTextRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void renderMultilineSplashText(GuiGraphics graphics, int screenWidth, TextRenderer textRenderer, int alpha, CallbackInfo ci) {
        if(SlightlyBetterSplashes.splashLines.isEmpty())
            return;
        ci.cancel();

        // code copied from SplashTextRenderer#render but modified to draw the multiline splash text instead of the vanilla one

        MatrixStack matrices = graphics.getMatrices();

        matrices.push();
        matrices.translate(screenWidth / 2f + 123f, 69f, 0f);
        matrices.multiply(Axis.Z_POSITIVE.rotationDegrees(-20f));
        float h = 1.8f - MathHelper.abs(
            MathHelper.sin((float)(Util.getMeasuringTimeMs() % 1000L) / 1000f * (float)(Math.PI * 2d)) * 0.1f);
        h *= SlightlyBetterSplashes.splashTextScale;
        matrices.scale(h, h, h);
        int y = -8;
        for(OrderedText line : SlightlyBetterSplashes.splashLines) {
            graphics.drawCenteredShadowedText(textRenderer, line, 0, y, 0xffff00 | alpha);
            y += 9;
        }
        matrices.pop();
    }
}
