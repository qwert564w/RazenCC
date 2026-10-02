vanilla_model.PLAYER:setVisible(false)
vanilla_model.ARMOR:setVisible(false)
vanilla_model.CAPE:setVisible(false)



function events.tick()
	animations.model.walk:speed(player:getVelocity():length()*15)
	animations.model.walkback:speed(player:getVelocity():length()*15)
	animations.model.sprint:speed(player:getVelocity():length()*8)
	animations.model.crouchwalk:speed(player:getVelocity():length()*18)
	animations.model.waterwalk:speed(player:getVelocity():length()*4)
	animations.model.waterup:speed(player:getVelocity():length()*4)
	animations.model.waterdown:speed(player:getVelocity():length()*4)
	animations.model.climb:speed(player:getVelocity():length()*15)
	animations.model.climbdown:speed(player:getVelocity():length()*15)
	animations.model.fly:speed(player:getVelocity():length()*1)
end

animations.model.holdL:setPriority(1)

	function events.tick()
	if player:getPose() == "CROUCHING" then
	models.model.Body:setPos(0,2,0)
	else models.model.Body:setPos(0,0,0)
	end
end

	function events.tick()
 	if player:getPose() == "SLEEPING" then 
   	models.model.Body:setRot(-90, 0, 180) models.neovenator.root:setPos(0,10,-8)
 	else models.model.Body:setRot(0, 0, 0)
 	end
end


--thanks to GemOfEvan!---

local antiAnimationRotation = 0
local antiAnimationRotationStep = 0
local wasFlying = false

function events.render(delta, context)
    models.model:setRot(math.min(math.max(antiAnimationRotation + delta * antiAnimationRotationStep, 0), 90), 0, 0)
end

function events.tick()
    local antiAnimationRotationDirection = -1
    local antiAnimationRotationTicks = 11
    if player:getPose() == 'SWIMMING' then
        antiAnimationRotationDirection = 1
        models.model:setPivot(0, 17, -5)

        wasFlying = false
    elseif player:getPose() == 'FALL_FLYING' then
        antiAnimationRotationDirection = 1
        models.model:setPivot(0, 0, -2)

        antiAnimationRotationTicks = 9.2

        wasFlying = true
    else
        models.model:setPivot(0, 0, 0)
    end

    if wasFlying and player:getPose() ~= 'FALL_FLYING' then
        antiAnimationRotation = 0
        antiAnimationRotationStep = 0
    else
        antiAnimationRotation = math.min(math.max(antiAnimationRotation + antiAnimationRotationStep, 0), 90)
        antiAnimationRotationStep = antiAnimationRotationDirection * 90 / antiAnimationRotationTicks
    end
end