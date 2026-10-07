package org.ryzen.utils.combat.rotations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_746;
import net.minecraft.class_3532;
import org.ryzen.context.RotationContext;
import org.ryzen.utils.combat.AuraRaycast;

@Environment(EnvType.CLIENT)
public class RWNeuroRotation implements AuraRotation {
    private class_1309 currentTarget;

    @Override
    public void setCurrentTarget(class_1309 target) {
        this.currentTarget = target;
    }

    @Override
    public void tick(class_746 player, class_1309 target, class_243 targetPos, boolean canAttack) {
        if (target == null || targetPos == null) return;
        
        // Use RotationContext to handle the smooth rotation math correctly with eye position
        RotationContext.rotateToPosition(player, targetPos);
    }

    @Override
    public void onAttack() {
    }

    @Override
    public void reset() {
        this.currentTarget = null;
    }
}
