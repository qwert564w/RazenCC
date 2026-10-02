---------------------------------------------------------------------------------------------------------------
-- APIS  --

require("GSAnimBlend")
local anim = animations.model


------------------------------------------------------------------------------------------
-- PLAYER MODEL ADJUSTMENTS --

vanilla_model.ELYTRA:setVisible(false)

-- ANIMATION FIXES --
anim.idle:setBlendTime(4)
anim.walk:setBlendTime(4)
anim.crouch:setBlendTime(4)
anim.elytra:setBlendTime(4)
anim.elytradown:setBlendTime(4)