package org.ryzen.utils.combat.neuro;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;
import org.ryzen.utils.combat.rotations.AuraRotation;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class NeuroRotation implements AuraRotation {

    private NeuroRotationData data;
    private float currentYaw;
    private float currentPitch;
    private boolean initialized;

    private float avgYawSpeed = 4.5F;
    private float avgPitchSpeed = 2.0F;
    private float maxYawSpeed = 8.0F;
    private float maxPitchSpeed = 4.0F;

    public void setData(NeuroRotationData data) {
        this.data = data;
        this.initialized = false;
        if (data != null && data.sampleCount() > 0) {
            analyzeData(data);
        }
    }

    public NeuroRotationData data() {
        return this.data;
    }

    public boolean hasData() {
        return this.data != null && this.data.sampleCount() > 0;
    }

    private void analyzeData(NeuroRotationData data) {
        List<NeuroSample> samples = data.samples();
        if (samples.size() < 2) return;

        float totalYawSpeed = 0;
        float totalPitchSpeed = 0;
        float localMaxYaw = 0;
        float localMaxPitch = 0;
        int count = 0;

        for (int i = 1; i < samples.size(); i++) {
            NeuroSample prev = samples.get(i - 1);
            NeuroSample curr = samples.get(i);

            float yawDelta = Math.abs(class_3532.method_15393(curr.yaw() - prev.yaw()));
            float pitchDelta = Math.abs(curr.pitch() - prev.pitch());

            totalYawSpeed += yawDelta;
            totalPitchSpeed += pitchDelta;
            localMaxYaw = Math.max(localMaxYaw, yawDelta);
            localMaxPitch = Math.max(localMaxPitch, pitchDelta);
            count++;
        }

        if (count > 0) {
            avgYawSpeed = Math.max((totalYawSpeed / count) * 0.80F, 2.0F);
            avgPitchSpeed = Math.max((totalPitchSpeed / count) * 0.80F, 1.0F);
            maxYawSpeed = Math.max(localMaxYaw * 0.95F, 5.0F);
            maxPitchSpeed = Math.max(localMaxPitch * 0.95F, 2.5F);
        }
    }

    @Override
    public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
        if (target == null || player == null) return;

        if (!initialized) {
            currentYaw = RotationContext.isActive() ? RotationContext.getFreeYaw() : player.method_36454();
            currentPitch = RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_36455();
            initialized = true;
        }

        class_243 eye = player.method_33571();
        class_243 delta = targetEyePos.method_1020(eye);
        double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
        float wantedYaw = (float) Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
        float wantedPitch = (float) (-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));

        float speedYaw;
        float speedPitch;
        if (hasData()) {
            speedYaw = attackLikely ? avgYawSpeed : avgYawSpeed * 0.6F;
            speedPitch = attackLikely ? avgPitchSpeed : avgPitchSpeed * 0.6F;
            speedYaw = Math.min(speedYaw, maxYawSpeed);
            speedPitch = Math.min(speedPitch, maxPitchSpeed);
        } else {
            speedYaw = attackLikely ? 5.0F : 3.0F;
            speedPitch = attackLikely ? 2.5F : 1.5F;
        }

        float jitter = (float) (Math.random() * 0.3 - 0.15);
        speedYaw *= (1.0F + jitter);
        speedPitch *= (1.0F + jitter * 0.5F);

        float yawDiff = class_3532.method_15393(wantedYaw - currentYaw);
        float pitchDiff = wantedPitch - currentPitch;

        float yawStep = Math.max(-speedYaw, Math.min(speedYaw, yawDiff));
        float pitchStep = Math.max(-speedPitch, Math.min(speedPitch, pitchDiff));

        float errorMag = (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
        if (errorMag < speedYaw * 0.5F) {
            yawStep *= 0.5F;
            pitchStep *= 0.5F;
        }

        float microYaw = (float) (Math.random() * 0.08 - 0.04);
        float microPitch = (float) (Math.random() * 0.04 - 0.02);

        currentYaw += yawStep + microYaw;
        currentPitch = Math.max(-90.0F, Math.min(90.0F, currentPitch + pitchStep + microPitch));

        float step = gcdStep();
        if (step > 0.0F) {
            float playerYaw = RotationContext.isActive() ? RotationContext.getFreeYaw() : player.method_36454();
            float playerPitch = RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_36455();
            float deltaYaw = class_3532.method_15393(currentYaw - playerYaw);
            float deltaPitch = currentPitch - playerPitch;
            deltaYaw = Math.round(deltaYaw / step) * step;
            deltaPitch = Math.round(deltaPitch / step) * step;
            RotationContext.setRotation(playerYaw + deltaYaw, Math.max(-90.0F, Math.min(90.0F, playerPitch + deltaPitch)));
        } else {
            RotationContext.setRotation(currentYaw, currentPitch);
        }
    }

    @Override
    public void onAttack() {
    }

    @Override
    public void reset() {
        currentYaw = 0;
        currentPitch = 0;
        initialized = false;
    }

    private static float gcdStep() {
        Object opt = class_310.method_1551().field_1690.method_42495().method_41753();
        if (opt == null) return 0.0F;
        if (!(opt instanceof Number)) return 0.0F;
        double sensitivity = ((Number) opt).doubleValue();
        if (sensitivity <= 0.0) return 0.0F;
        double factor = sensitivity * 0.6 + 0.2;
        return (float) (factor * factor * factor * 1.2);
    }
}
