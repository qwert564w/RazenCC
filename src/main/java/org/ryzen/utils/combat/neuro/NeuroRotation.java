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

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class NeuroRotation implements AuraRotation {

    // --- Existing Data Hooks (to satisfy NeuroManager compilation) ---
    private NeuroRotationData data;
    private boolean warnedEmpty;

    public void setData(NeuroRotationData data) {
        this.data = data;
        this.warnedEmpty = false;
    }

    public NeuroRotationData data() {
        return this.data;
    }

    public boolean hasData() {
        return this.data != null && this.data.sampleCount() > 0;
    }

    // --- Neuro Controller State ---
    private enum State { TRACKING, STABILIZING, ATTACKING, CORRECTING, RECOVERING }
    private State currentState = State.TRACKING;

    private long seed = System.nanoTime();

    // Motor Control
    private float yawVelocity = 0.0F;
    private float pitchVelocity = 0.0F;
    private float yawAcceleration = 0.0F;
    private float pitchAcceleration = 0.0F;
    
    private float desiredYaw = 0.0F;
    private float desiredPitch = 0.0F;
    private float currentYaw = 0.0F;
    private float currentPitch = 0.0F;

    // Perception
    private final List<class_243> targetHistory = new ArrayList<>();
    private class_243 targetVelocity = class_243.field_24340;
    private class_243 predictedPos = null;
    private long lastDirectionChangeTime = 0;

    // Intent (Multipoint)
    private enum HitZone {
        HEAD_CENTER, HEAD_SIDE, UPPER_BODY, CENTER_BODY, LOWER_BODY, SHOULDER, ARM, LEG
    }
    private HitZone currentZone = HitZone.HEAD_CENTER;
    private final List<HitZone> zoneHistory = new ArrayList<>();
    private long lastZoneSwitchTime = 0;
    private int holdTimeTarget = 60;
    
    private double localOffsetX = 0.0;
    private double localOffsetY = 0.72;
    private double localOffsetZ = 0.0;

    // Focus Distance
    private float desiredFocusDistance = 0.5F;
    private float currentFocusDistance = 0.5F;

    // Attack
    private long lastCorrectionTime = 0;

    private double seededRandom() {
        seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        return (double)(seed >>> 16) / (double)(1L << 32);
    }

    @Override
    public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
        long now = System.currentTimeMillis();
        if (target == null || player == null) return;

        float playerYaw = RotationContext.isActive() ? RotationContext.getFreeYaw() : player.method_36454();
        float playerPitch = RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_36455();
        
        if (currentYaw == 0.0F && currentPitch == 0.0F) {
            currentYaw = playerYaw;
            currentPitch = playerPitch;
        }

        // --- 1. Perception ---
        updatePerception(target, now);

        // --- 2. Intent (Multipoint & Focus) ---
        updateIntent(player, target, targetEyePos, now);

        // --- 3. Motor Control & State Machine ---
        updateMotor(player, target, playerYaw, playerPitch, attackLikely, now);
    }

    private void updatePerception(class_1309 target, long now) {
        class_243 currentPos = target.method_73189();
        if (targetHistory.isEmpty()) {
            targetHistory.add(currentPos);
        } else {
            class_243 diff = targetHistory.get(targetHistory.size() - 1).method_1020(currentPos);
            double distSq = diff.field_1352 * diff.field_1352 + diff.field_1351 * diff.field_1351 + diff.field_1350 * diff.field_1350;
            if (distSq > 0.0001) {
                targetHistory.add(currentPos);
                if (targetHistory.size() > 10) targetHistory.remove(0);
            }
        }

        class_243 velocity = class_243.field_24340;
        if (targetHistory.size() >= 2) {
            class_243 prev = targetHistory.get(targetHistory.size() - 2);
            velocity = currentPos.method_1020(prev);
        }
        
        // Reaction Delay: detect direction change
        double dot = targetVelocity.field_1352 * velocity.field_1352 + targetVelocity.field_1350 * velocity.field_1350;
        double vSpeed = Math.sqrt(velocity.field_1352 * velocity.field_1352 + velocity.field_1350 * velocity.field_1350);
        if (dot < 0 && vSpeed > 0.05) {
            lastDirectionChangeTime = now;
        }
        
        targetVelocity = targetVelocity.method_1020(velocity).method_1022(0.6).method_1019(velocity.method_1022(0.4));
        
        double dx = targetVelocity.field_1352;
        double dy = targetVelocity.field_1351;
        double dz = targetVelocity.field_1350;
        double speed = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        double maxPred = 0.8;
        double predFactor = class_3532.method_15363(speed * 2.5, 0.0, maxPred);
        predictedPos = currentPos.method_1019(targetVelocity.method_1022(predFactor));
    }

    private void updateIntent(class_746 player, class_1309 target, class_243 targetEyePos, long now) {
        // Hold time logic
        double dx = targetVelocity.field_1352;
        double dy = targetVelocity.field_1351;
        double dz = targetVelocity.field_1350;
        double speed = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        float stability = 1.0F - (float)class_3532.method_15363(speed * 4.0, 0.0, 1.0);
        holdTimeTarget = (int) (40 + stability * 110); // 40 to 150ms
        
        if (now - lastZoneSwitchTime > holdTimeTarget) {
            selectNextZone(now);
            lastZoneSwitchTime = now;
        }

        // Focus Distance
        class_243 refPos = predictedPos != null ? predictedPos : target.method_73189();
        class_243 toTarget = refPos.method_1020(player.method_33571());
        double tDx = toTarget.field_1352;
        double tDy = toTarget.field_1351;
        double tDz = toTarget.field_1350;
        float targetDist = (float)Math.sqrt(tDx*tDx + tDy*tDy + tDz*tDz);
        
        float optimalFocus = 0.5F;
        if (targetDist > 3.0F) optimalFocus = 0.8F;
        else if (targetDist < 1.5F) optimalFocus = 0.3F;
        
        desiredFocusDistance += (optimalFocus - desiredFocusDistance) * 0.05F;
        currentFocusDistance += (desiredFocusDistance - currentFocusDistance) * 0.1F;

        // Calculate point
        double width = target.method_17681();
        class_243 feet = target.method_73189();
        
        double heightFactor = 0.72;
        switch (currentZone) {
            case HEAD_CENTER: heightFactor = 0.92; localOffsetX = 0; localOffsetZ = 0; break;
            case HEAD_SIDE: heightFactor = 0.90; localOffsetX = (seededRandom() - 0.5) * 0.3; localOffsetZ = 0; break;
            case UPPER_BODY: heightFactor = 0.75; localOffsetX = (seededRandom() - 0.5) * 0.2; localOffsetZ = 0; break;
            case CENTER_BODY: heightFactor = 0.55; localOffsetX = (seededRandom() - 0.5) * 0.2; localOffsetZ = 0; break;
            case LOWER_BODY: heightFactor = 0.35; localOffsetX = (seededRandom() - 0.5) * 0.15; localOffsetZ = 0; break;
            case SHOULDER: heightFactor = 0.80; localOffsetX = (seededRandom() > 0.5 ? 0.4 : -0.4); localOffsetZ = 0; break;
            case ARM: heightFactor = 0.60; localOffsetX = (seededRandom() > 0.5 ? 0.5 : -0.5); localOffsetZ = 0; break;
            case LEG: heightFactor = 0.20; localOffsetX = (seededRandom() - 0.5) * 0.3; localOffsetZ = 0; break;
        }

        class_243 base = feet.method_1031(0.0, (targetEyePos.field_1351 - feet.field_1351) * heightFactor, 0.0);
        double yawRadians = Math.toRadians(target.method_36454());
        double rightX = Math.cos(yawRadians);
        double rightZ = Math.sin(yawRadians);
        
        class_243 aim = base.method_1031(
            rightX * localOffsetX * width - rightZ * localOffsetZ * width, 
            0.0, 
            rightZ * localOffsetX * width + rightX * localOffsetZ * width
        );

        // Apply focus distance (depth modifier)
        double depthMod = (currentFocusDistance - 0.5) * 0.4;
        aim = aim.method_1031(-rightZ * depthMod * width, 0.0, rightX * depthMod * width);

        // Predictive blend
        if (predictedPos != null && currentState != State.CORRECTING) {
            double blend = class_3532.method_15363(speed * 1.5, 0.0, 0.6);
            class_243 diff = predictedPos.method_1020(aim);
            aim = aim.method_1019(diff.method_1022(blend));
        }

        class_243 delta = aim.method_1020(player.method_33571());
        double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
        desiredYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
        desiredPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
    }

    private void selectNextZone(long now) {
        HitZone next = currentZone;
        int attempts = 0;
        while (attempts < 10) {
            double roll = seededRandom();
            if (roll < 0.45) {
                // Small transitions
                if (currentZone == HitZone.HEAD_CENTER || currentZone == HitZone.HEAD_SIDE) {
                    next = seededRandom() > 0.5 ? HitZone.SHOULDER : HitZone.UPPER_BODY;
                } else if (currentZone == HitZone.UPPER_BODY || currentZone == HitZone.SHOULDER) {
                    next = seededRandom() > 0.5 ? HitZone.CENTER_BODY : HitZone.HEAD_CENTER;
                } else {
                    next = HitZone.CENTER_BODY;
                }
            } else if (roll < 0.85) {
                // Medium transitions
                next = HitZone.values()[(int)(seededRandom() * HitZone.values().length)];
            } else {
                // Rare large transitions
                next = seededRandom() > 0.5 ? HitZone.LEG : HitZone.HEAD_CENTER;
            }

            if (!zoneHistory.contains(next) || zoneHistory.size() < 3) {
                break;
            }
            attempts++;
        }

        currentZone = next;
        zoneHistory.add(currentZone);
        if (zoneHistory.size() > 8) zoneHistory.remove(0);
    }

    private void updateMotor(class_746 player, class_1309 target, float playerYaw, float playerPitch, boolean attackLikely, long now) {
        float yawError = class_3532.method_15393(desiredYaw - currentYaw);
        float pitchError = desiredPitch - currentPitch;
        float errorMag = (float)Math.sqrt(yawError * yawError + pitchError * pitchError);

        // State Machine
        boolean inHitbox = isAimInHitbox(currentYaw, currentPitch, player, target);
        
        if (attackLikely && inHitbox) {
            currentState = State.ATTACKING;
        } else if (errorMag > 12.0F) {
            currentState = State.CORRECTING;
            lastCorrectionTime = now;
        } else if (now - lastCorrectionTime < 400) {
            currentState = State.RECOVERING;
        } else if (inHitbox && errorMag < 2.5F) {
            currentState = State.STABILIZING;
        } else {
            currentState = State.TRACKING;
        }

        // Reaction Delay factor
        float reactionFactor = 1.0F;
        if (now - lastDirectionChangeTime < 150) {
            reactionFactor = 0.65F; // Delayed reaction to sudden reverse
        }

        // Adaptive Speed (30-38 equivalent during attack)
        float maxYawStep = 6.0F;
        float maxPitchStep = 2.9F;
        
        if (currentState == State.ATTACKING) {
            if (errorMag < 2.0F) maxYawStep = 5.5F;
            else if (errorMag < 5.0F) maxYawStep = 6.2F;
            else maxYawStep = 6.8F;
            
            if (now - lastCorrectionTime < 300) {
                float t = (now - lastCorrectionTime) / 300.0F;
                maxYawStep = 6.8F - t * 1.3F; // Gradual recovery
            }
            maxPitchStep = maxYawStep * 0.45F;
        } else if (currentState == State.CORRECTING) {
            maxYawStep = 7.0F;
            maxPitchStep = 3.2F;
        } else {
            maxYawStep = 4.5F + errorMag * 0.1F;
            maxPitchStep = 2.0F + errorMag * 0.05F;
        }

        maxYawStep = class_3532.method_15363(maxYawStep, 4.0F, 7.5F);
        maxPitchStep = class_3532.method_15363(maxPitchStep, 1.5F, 3.5F);

        // Motor Model
        yawVelocity = step(yawVelocity, yawError, 0.45F, 0.82F, 0.55F, maxYawStep, 0.25F, true, reactionFactor);
        pitchVelocity = step(pitchVelocity, pitchError, 0.35F, 0.85F, 0.35F, maxPitchStep, 0.18F, false, reactionFactor);

        // Deadzone for pitch
        if (Math.abs(pitchError) < 0.5F) {
            pitchVelocity *= 0.8F;
        }

        float newYaw = currentYaw + yawVelocity;
        float newPitch = currentPitch + pitchVelocity;
        
        currentYaw = newYaw;
        currentPitch = clampPitch(newPitch);

        // GCD Correction
        float deltaYaw = class_3532.method_15393(currentYaw - playerYaw);
        float deltaPitch = currentPitch - playerPitch;
        
        float step = gcdStep();
        if (step > 0.0F) {
            deltaYaw = Math.round(deltaYaw / step) * step;
            deltaPitch = Math.round(deltaPitch / step) * step;
        }
        
        float finalYaw = playerYaw + deltaYaw;
        float finalPitch = clampPitch(playerPitch + deltaPitch);

        // Safety check: if GCD pushed us out during attack, relax GCD slightly or pull back
        if (attackLikely && !isAimInHitbox(finalYaw, finalPitch, player, target)) {
            finalYaw = playerYaw + deltaYaw; 
            finalPitch = clampPitch(playerPitch + deltaPitch);
        }

        RotationContext.setRotation(finalYaw, finalPitch);
    }

    private float step(float velocity, float error, float tracking, float inertia, float acceleration, float maxStep, float jerkSmoothing, boolean yawAxis, float reactionFactor) {
        float wanted = class_3532.method_15363(error * tracking * reactionFactor, -maxStep, maxStep);
        float blended = velocity * inertia + wanted * (1.0F - inertia);
        float requestedAcceleration = blended - velocity;
        float previousAcceleration = yawAxis ? yawAcceleration : pitchAcceleration;
        float smoothed = class_3532.method_16439(jerkSmoothing, previousAcceleration, requestedAcceleration);
        smoothed = class_3532.method_15363(smoothed, -acceleration, acceleration);
        
        if (yawAxis) yawAcceleration = smoothed;
        else pitchAcceleration = smoothed;

        float next = velocity + smoothed;
        float ceiling = maxStep * (1.0F + (float)(seededRandom() - 0.5) * 0.12F);
        next = class_3532.method_15363(next, -ceiling, ceiling);
        
        if (error > 0.0F && next > error) next = error;
        else if (error < 0.0F && next < error) next = error;

        return next;
    }

    private boolean isAimInHitbox(float yaw, float pitch, class_746 player, class_1309 target) {
        class_243 eye = player.method_33571();
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double dx = -Math.cos(pitchRad) * Math.sin(yawRad);
        double dy = -Math.sin(pitchRad);
        double dz = Math.cos(pitchRad) * Math.cos(yawRad);
        
        class_243 targetCenter = target.method_73189().method_1031(0, target.method_5982() / 2.0, 0);
        class_243 toCenter = targetCenter.method_1020(eye);
        
        double t = toCenter.field_1352 * dx + toCenter.field_1351 * dy + toCenter.field_1350 * dz;
        if (t < 0 || t > 8.0) return false;
        
        class_243 closest = eye.method_1031(dx * t, dy * t, dz * t);
        class_243 diff = closest.method_1020(targetCenter);
        
        double distSq = diff.field_1352 * diff.field_1352 + diff.field_1350 * diff.field_1350;
        double radius = target.method_17681() / 2.0 + 0.15; 
        
        double vertDist = Math.abs(closest.field_1351 - targetCenter.field_1351);
        double heightTolerance = target.method_5982() / 2.0 + 0.2;
        
        return distSq < (radius * radius) && vertDist < heightTolerance;
    }

    private static float gcdStep() {
        double sensitivity = (Double) class_310.method_1551().field_1690.method_42495().method_41753();
        if (sensitivity <= 0.0) return 0.0F;
        double factor = sensitivity * 0.6 + 0.2;
        return (float)(factor * factor * factor * 1.2);
    }

    private static float clampPitch(float pitch) {
        return class_3532.method_15363(pitch, -90.0F, 90.0F);
    }

    @Override
    public void onAttack() {
        yawVelocity *= 0.4F;
        pitchVelocity *= 0.4F;
        yawAcceleration = 0.0F;
        pitchAcceleration = 0.0F;
    }

    @Override
    public void reset() {
        yawVelocity = 0.0F;
        pitchVelocity = 0.0F;
        yawAcceleration = 0.0F;
        pitchAcceleration = 0.0F;
        currentYaw = 0.0F;
        currentPitch = 0.0F;
        targetHistory.clear();
        zoneHistory.clear();
        currentState = State.TRACKING;
        seed = System.nanoTime();
    }
}