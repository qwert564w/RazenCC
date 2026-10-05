package org.ryzen.utils.combat.neuro;

import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import org.ryzen.utils.combat.rotations.AuraRotation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class NeuroRotation implements AuraRotation {
    
    private static final long SEED = System.nanoTime();
    private static final Random seededRand = new Random(SEED);
    
    private enum State { TRACKING, STABILIZING, ATTACKING, CORRECTING, RECOVERING }
    private State currentState = State.TRACKING;
    
    private enum Zone { HEAD_CENTER, HEAD_SIDE, UPPER_BODY, CENTER_BODY, LOWER_BODY, SHOULDER, ARM, LEG }
    private Zone currentZone = Zone.CENTER_BODY;
    private final Deque<Zone> zoneHistory = new ArrayDeque<>();
    private long zoneHoldTime = 0;
    private long lastZoneChange = 0;
    
    private float currentYaw = 0.0F;
    private float currentPitch = 0.0F;
    private float desiredYaw = 0.0F;
    private float desiredPitch = 0.0F;
    
    // Bezier curve state
    private float startYaw, startPitch;
    private float ctrl1Yaw, ctrl1Pitch;
    private float ctrl2Yaw, ctrl2Pitch;
    private float endYaw, endPitch;
    private float curveProgress = 1.0F;
    private float curveSpeed = 0.1F;
    
    private float yawVelocity = 0.0F;
    private float pitchVelocity = 0.0F;
    
    private float aimError = 0.0F;
    private float stability = 0.0F;
    
    private class_243 lastTargetPos = null;
    private float predictionBlend = 0.0F;
    
    private float focusDistance = 0.5F;
    private float targetFocusDistance = 0.5F;
    
    private boolean canAttack = false;
    private long lastAttackTime = 0;
    
    // Noise state for organic jitter
    private float noiseTime = 0.0F;
    
    private static float seededRandom() {
        return seededRand.nextFloat();
    }
    
    // --- PRIVATE CHEAT MATH ENGINE ---
    
    // Fast 1D Value Noise for organic mouse tremor
    private float valueNoise(float x) {
        int xi = (int) Math.floor(x);
        float xf = x - xi;
        float u = xf * xf * (3.0F - 2.0F * xf); // Smoothstep
        
        float a = hashFloat(xi);
        float b = hashFloat(xi + 1);
        
        return a + u * (b - a);
    }
    
    private float hashFloat(int x) {
        x = ((x >> 16) ^ x) * 0x45d9f3b;
        x = ((x >> 16) ^ x) * 0x45d9f3b;
        x = (x >> 16) ^ x;
        return (float)(x & 0xFFFF) / 65535.0F;
    }
    
    // Smoothstep / Sigmoid easing for human-like acceleration
    private float easeInOutCubic(float t) {
        return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3) / 2.0F;
    }
    
    // Normalize angle difference to [-180, 180]
    private float wrapAngle(float angle) {
        angle %= 360.0F;
        if (angle >= 180.0F) angle -= 360.0F;
        if (angle < -180.0F) angle += 360.0F;
        return angle;
    }
    
    // Generate Bezier control points to simulate wrist arc
    private void generateBezierPath(float targetYaw, float targetPitch) {
        startYaw = currentYaw;
        startPitch = currentPitch;
        endYaw = targetYaw;
        endPitch = targetPitch;
        
        float diffYaw = wrapAngle(targetYaw - currentYaw);
        float diffPitch = targetPitch - currentPitch;
        
        // Human wrist doesn't move in a straight line, it arcs.
        // We offset control points perpendicularly to the direct path.
        float arcStrength = (Math.abs(diffYaw) + Math.abs(diffPitch)) * (0.05F + seededRandom() * 0.1F);
        
        // Add random overshoot to the end point
        float overshootYaw = (seededRandom() - 0.5F) * 1.5F;
        float overshootPitch = (seededRandom() - 0.5F) * 0.8F;
        endYaw += overshootYaw;
        endPitch += overshootPitch;
        
        // Control point 1 (acceleration phase)
        ctrl1Yaw = startYaw + diffYaw * 0.3F + (seededRandom() - 0.5F) * arcStrength;
        ctrl1Pitch = startPitch + diffPitch * 0.2F + (seededRandom() - 0.5F) * arcStrength * 0.5F;
        
        // Control point 2 (deceleration phase)
        ctrl2Yaw = endYaw - diffYaw * 0.2F + (seededRandom() - 0.5F) * arcStrength;
        ctrl2Pitch = endPitch - diffPitch * 0.3F + (seededRandom() - 0.5F) * arcStrength * 0.5F;
        
        // Curve speed depends on distance (faster for large turns, slower for micro-corrections)
        float dist = (float)Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
        curveSpeed = class_3532.method_15363(0.05F + dist * 0.008F, 0.05F, 0.35F);
        
        curveProgress = 0.0F;
    }
    
    // Evaluate cubic Bezier curve
    private float[] evaluateBezier(float t) {
        float u = 1.0F - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;
        
        float y = uuu * startYaw + 3.0F * uu * t * ctrl1Yaw + 3.0F * u * tt * ctrl2Yaw + ttt * endYaw;
        float p = uuu * startPitch + 3.0F * uu * t * ctrl1Pitch + 3.0F * u * tt * ctrl2Pitch + ttt * endPitch;
        
        return new float[]{y, p};
    }
    
    // --- END MATH ENGINE ---
    
    @Override
    public float[] getRotations(class_1309 target) {
        if (target == null || !target.method_4892()) {
            return new float[]{0.0F, 0.0F};
        }
        
        long currentTime = System.currentTimeMillis();
        update(target, currentTime);
        
        return new float[]{currentYaw, currentPitch};
    }
    
    private void update(class_1309 target, long currentTime) {
        noiseTime += 0.05F; // Advance noise time
        
        float speed = analyzeTargetSpeed(target);
        stability = calculateStability(speed);
        
        selectZone(speed, currentTime);
        
        class_243 targetPoint = calculateTargetPoint(target);
        calculateDesiredRotation(targetPoint);
        
        // If we reached the end of the curve, or target moved significantly, generate new path
        if (curveProgress >= 1.0F || Math.abs(wrapAngle(desiredYaw - endYaw)) > 2.0F || Math.abs(desiredPitch - endPitch) > 1.5F) {
            generateBezierPath(desiredYaw, desiredPitch);
        }
        
        applyMotorControl();
        
        applyCorrection(target);
        
        checkAttackTiming(target, currentTime);
    }
    
    private float analyzeTargetSpeed(class_1309 target) {
        class_243 currentPos = target.method_73189();
        if (lastTargetPos == null) {
            lastTargetPos = currentPos;
            return 0.0F;
        }
        
        double dx = currentPos.field_1352 - lastTargetPos.field_1352;
        double dy = currentPos.field_1351 - lastTargetPos.field_1351;
        double dz = currentPos.field_1350 - lastTargetPos.field_1350;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        lastTargetPos = currentPos;
        return (float) dist;
    }
    
    private float calculateStability(float speed) {
        float stabilityD = class_3532.method_15363((float)(speed * 4.0), 0.0F, 1.0F);
        return 1.0F - stabilityD;
    }
    
    private void selectZone(float speed, long currentTime) {
        if (currentTime - lastZoneChange < zoneHoldTime) {
            return;
        }
        
        Zone newZone = currentZone;
        float rand = seededRandom();
        
        if (speed < 0.05F) {
            if (rand < 0.4F) newZone = Zone.HEAD_CENTER;
            else if (rand < 0.7F) newZone = Zone.UPPER_BODY;
            else newZone = Zone.CENTER_BODY;
        } else if (speed < 0.15F) {
            if (rand < 0.3F) newZone = Zone.HEAD_SIDE;
            else if (rand < 0.6F) newZone = Zone.UPPER_BODY;
            else newZone = Zone.CENTER_BODY;
        } else {
            if (rand < 0.35F) newZone = Zone.CENTER_BODY;
            else if (rand < 0.65F) newZone = Zone.LOWER_BODY;
            else if (rand < 0.85F) newZone = Zone.ARM;
            else newZone = Zone.LEG;
        }
        
        if (zoneHistory.size() >= 2) {
            Zone last = zoneHistory.peekLast();
            Zone[] historyArray = zoneHistory.toArray(new Zone[0]);
            Zone secondLast = historyArray.length >= 2 ? historyArray[historyArray.length - 2] : null;
            if (secondLast != null && newZone == secondLast && last != newZone) {
                newZone = last;
            }
        }
        
        if (newZone != currentZone) {
            zoneHistory.addLast(newZone);
            if (zoneHistory.size() > 8) {
                zoneHistory.removeFirst();
            }
            currentZone = newZone;
            lastZoneChange = currentTime;
            // Log-normal-ish reaction time for human variance
            zoneHoldTime = 40 + (long)(Math.abs(seededRand.nextGaussian() * 20) + 60);
        }
    }
    
    private class_243 calculateTargetPoint(class_1309 target) {
        class_243 feet = target.method_73189();
        class_243 targetEye = target.method_33571();
        
        double heightFactor = 0.55;
        double localOffsetX = 0;
        
        switch (currentZone) {
            case HEAD_CENTER: heightFactor = 0.92; break;
            case HEAD_SIDE: heightFactor = 0.90; localOffsetX = (seededRandom() - 0.5) * 0.3; break;
            case UPPER_BODY: heightFactor = 0.75; localOffsetX = (seededRandom() - 0.5) * 0.2; break;
            case CENTER_BODY: heightFactor = 0.55; localOffsetX = (seededRandom() - 0.5) * 0.2; break;
            case LOWER_BODY: heightFactor = 0.35; localOffsetX = (seededRandom() - 0.5) * 0.15; break;
            case SHOULDER: heightFactor = 0.80; localOffsetX = (seededRandom() > 0.5 ? 0.4 : -0.4); break;
            case ARM: heightFactor = 0.60; localOffsetX = (seededRandom() > 0.5 ? 0.5 : -0.5); break;
            case LEG: heightFactor = 0.20; localOffsetX = (seededRandom() - 0.5) * 0.3; break;
        }
        
        double baseY = feet.field_1351 + (targetEye.field_1351 - feet.field_1351) * heightFactor;
        double targetX = feet.field_1352 + localOffsetX;
        double targetZ = feet.field_1350;
        
        return new class_243(targetX, baseY, targetZ);
    }
    
    private void calculateDesiredRotation(class_243 targetPoint) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) return;
        
        class_243 eyePos = mc.field_1724.method_33571();
        
        double dx = targetPoint.field_1352 - eyePos.field_1352;
        double dy = targetPoint.field_1351 - eyePos.field_1351;
        double dz = targetPoint.field_1350 - eyePos.field_1350;
        
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));
        
        desiredYaw = yaw;
        desiredPitch = pitch;
    }
    
    private void applyMotorControl() {
        if (curveProgress < 1.0F) {
            curveProgress += curveSpeed;
            if (curveProgress > 1.0F) curveProgress = 1.0F;
            
            // Apply sigmoid easing so it accelerates and decelerates like a human
            float t = easeInOutCubic(curveProgress);
            float[] bezierPoint = evaluateBezier(t);
            
            currentYaw = bezierPoint[0];
            currentPitch = bezierPoint[1];
        }
        
        aimError = (float) Math.sqrt(wrapAngle(desiredYaw - currentYaw) * wrapAngle(desiredYaw - currentYaw) + 
                                      (desiredPitch - currentPitch) * (desiredPitch - currentPitch));
        
        // Organic Mouse Tremor (Value Noise instead of Random)
        float noiseYaw = (valueNoise(noiseTime * 1.5F) - 0.5F) * 0.12F;
        float noisePitch = (valueNoise(noiseTime * 1.8F + 100.0F) - 0.5F) * 0.08F;
        
        // Increase tremor when stabilizing (holding aim on target)
        if (aimError < 3.0F) {
            noiseYaw *= 1.5F;
            noisePitch *= 1.5F;
        }
        
        currentYaw += noiseYaw;
        currentPitch += noisePitch;
        
        // GCD Mouse Delta Simulation
        float gcd = gcdStep();
        if (gcd > 0.0F) {
            currentYaw = applyMouseDelta(currentYaw, gcd);
            currentPitch = applyMouseDelta(currentPitch, gcd);
        }
        
        // Hard clamp pitch
        currentPitch = class_3532.method_15363(currentPitch, -90.0F, 90.0F);
    }
    
    private static float gcdStep() {
        try {
            class_310 mc = class_310.method_1551();
            if (mc == null || mc.field_1690 == null) return 0.0F;
            
            Object opt = mc.field_1690.method_42495().method_41753();
            if (opt == null) return 0.0F;
            
            double sensitivity;
            if (opt instanceof Number) {
                sensitivity = ((Number)opt).doubleValue();
            } else {
                return 0.0F;
            }
            
            if (sensitivity <= 0.0) return 0.0F;
            double factor = sensitivity * 0.6 + 0.2;
            return (float)(factor * factor * factor * 1.2);
        } catch (Exception e) {
            return 0.0F;
        }
    }
    
    // Simulate vanilla mouse grid snapping
    private float applyMouseDelta(float value, float gcd) {
        if (gcd <= 0.0F) return value;
        // Vanilla client rounds to the nearest grid step
        return Math.round(value / gcd) * gcd;
    }
    
    private void applyCorrection(class_1309 target) {
        if (aimError < 2.0F) {
            currentState = State.STABILIZING;
        } else if (aimError < 10.0F) {
            currentState = State.TRACKING;
        } else {
            currentState = State.CORRECTING;
        }
        
        if (lastTargetPos != null) {
            class_243 currentPos = target.method_73189();
            double dx = currentPos.field_1352 - lastTargetPos.field_1352;
            double dz = currentPos.field_1350 - lastTargetPos.field_1350;
            double speed = Math.sqrt(dx * dx + dz * dz);
            
            float predFactor = class_3532.method_15363((float)(speed * 2.5), 0.0F, 0.8F);
            predictionBlend = class_3532.method_15363(predictionBlend + 0.1F * predFactor, 0.0F, 0.5F);
        }
        
        if (stability > 0.7F) {
            targetFocusDistance = 0.3F + seededRandom() * 0.2F;
        } else {
            targetFocusDistance = 0.5F + seededRandom() * 0.3F;
        }
        focusDistance = focusDistance * 0.9F + targetFocusDistance * 0.1F;
    }
    
    private void checkAttackTiming(class_1309 target, long currentTime) {
        canAttack = false;
        
        if (target == null || !target.method_4892()) return;
        
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) return;
        
        class_243 eyePos = mc.field_1724.method_33571();
        class_243 targetPos = target.method_73189();
        
        double dx = targetPos.field_1352 - eyePos.field_1352;
        double dy = targetPos.field_1351 - eyePos.field_1351;
        double dz = targetPos.field_1350 - eyePos.field_1350;
        
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq > 9.0) return;
        
        if (!serverRaycastCheck(target, currentYaw, currentPitch)) return;
        
        // Only attack when perfectly stabilized on the curve
        if (aimError > 3.5F) return;
        
        canAttack = true;
        lastAttackTime = currentTime;
    }
    
    private boolean serverRaycastCheck(class_1309 target, float yaw, float pitch) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) return false;
        
        class_243 eyePos = mc.field_1724.method_33571();
        double reach = 3.0;
        
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        double dirX = -Math.sin(yawRad) * Math.cos(pitchRad);
        double dirY = -Math.sin(pitchRad);
        double dirZ = Math.cos(yawRad) * Math.cos(pitchRad);
        
        double width = target.method_17681() / 2.0 + 0.15;
        class_243 targetPos = target.method_73189();
        class_243 targetEye = target.method_33571();
        double feetY = targetPos.field_1351;
        double headY = targetEye.field_1351 + 0.2;
        
        double minX = targetPos.field_1352 - width;
        double maxX = targetPos.field_1352 + width;
        double minY = feetY;
        double maxY = headY;
        double minZ = targetPos.field_1350 - width;
        double maxZ = targetPos.field_1350 + width;
        
        double tMin = 0.0;
        double tMax = reach;
        
        double[] rayOrigin = {eyePos.field_1352, eyePos.field_1351, eyePos.field_1350};
        double[] rayDir = {dirX, dirY, dirZ};
        double[] boxMin = {minX, minY, minZ};
        double[] boxMax = {maxX, maxY, maxZ};
        
        for (int i = 0; i < 3; i++) {
            if (Math.abs(rayDir[i]) < 1e-8) {
                if (rayOrigin[i] < boxMin[i] || rayOrigin[i] > boxMax[i]) {
                    return false;
                }
            } else {
                double t1 = (boxMin[i] - rayOrigin[i]) / rayDir[i];
                double t2 = (boxMax[i] - rayOrigin[i]) / rayDir[i];
                if (t1 > t2) {
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }
                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);
                if (tMin > tMax) {
                    return false;
                }
            }
        }
        
        return tMin <= reach && tMax >= 0.0;
    }
    
    public boolean canAttack() {
        return canAttack;
    }
}
