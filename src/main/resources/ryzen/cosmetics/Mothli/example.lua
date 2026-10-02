local dronezAPI = require("dronezAPI")

local droneHandle = dronezAPI.new(models.mothli.World)
	:setTopSpeed(2) -- change stats
	:setTargetPosFunction(dronezAPI.targetFunctions.followNearbyEntities) -- change targeting function
	:setLocalOffset(0,-0.1,1) -- change targeting function stat

-- warp event, prevents warping when holding lilac
function droneHandle.droneWarp(droneObj)
    if not player:isLoaded() then
        return
    end

    local held = player:getHeldItem()
    local isHoldingLilac = held and held.id == "minecraft:lilac"

    if isHoldingLilac then

        host:setActionbar("§fMothli doesn't like this plant so he won't teleport.")
        

        return false
    else

        sounds:playSound("entity.fox.teleport", droneObj.pos, 0.5, 1.1)
    end


end
-- punched event, prevents punching if sneaking and increases impulse when holding a sword
function droneHandle.dronePunched(droneObj, interacter)
    
	if interacter:isLoaded() then
        if interacter:isSneaking() then
            -- cancel punch
            return false 
        elseif interacter:getHeldItem().id:find("sword") then
            sounds:playSound("minecraft:entity.silverfish.hurt", droneObj.pos, 0.75, 1.1)
            droneObj.justGotPunched = true
            return droneObj.punchImpulse * 2
        else
            sounds:playSound("minecraft:entity.silverfish.hurt", droneObj.pos, 0.5, 1.2)
            droneObj.justGotPunched = true
        end
    end
    
    -- returning nothing allows event to go as normal
end

function droneHandle.droneInteracted(droneObj, interactor)
    -- Перевірка, чи можна взаємодіяти (наприклад, не sneaking)
    if interactor:isSneaking() then
        return false  -- Скасувати, якщо sneaking
    end

    -- Активуйте анімацію pat (припустимо, вона існує в моделі)
    animations.mothli.pat:play()  -- Або animations.mothli.pat:setPlaying(true) якщо looped

    -- Додаткові ефекти: звук, частинки тощо
    sounds:playSound("minecraft:entity.cat.purr", droneObj.pos, 0.7, 1.0)
    particles:newParticle("happy_villager", droneObj.pos + vec(0, 0.5, 0))  -- Сердечка або щось

    -- Якщо анімація не looped, додайте таймер для зупинки (в events.tick)
    droneObj.patTimer = 20  -- 1 секунда (20 тіків)
end


if droneHandle.patTimer and droneHandle.patTimer > 0 then
    droneHandle.patTimer = droneHandle.patTimer - 1
    if droneHandle.patTimer <= 0 then
        animations.mothli.pat:stop()
    end
end
function events.tick()
	if droneHandle.justGotPunched then
        host:setActionbar("Mothli don't like this...")
        droneHandle.justGotPunched = false 
    end

end
 ---Animation

function events.tick() 
 	local speed = droneHandle.velocity:length()

    if speed < 0.1 then
        animations.mothli.fly:setPlaying(true)
        animations.mothli.fly_fast:setPlaying(false)  
    else
     
        animations.mothli.fly:setPlaying(false)
        animations.mothli.fly_fast:setPlaying(true)
    end
end
-------------------------------------------------------------------------------------------------------------------------------------------------------
---Wander
function wanderingTargetPos(droneObj)
    local basePos = dronezAPI.targetFunctions.followEntity(droneObj) or vec(0,0,0)
    
    if not droneObj.targetEntity:isLoaded() then
        return basePos
    end
    
	if PlayerHolding() then
        droneObj.wanderTimer = 0
        droneObj.wanderPause = 0
        droneObj.wanderDelayTimer = 0
        return basePos
    end
	
    local targetSpeed = droneObj.targetEntity:getVelocity():length()
    droneObj.lastTargetSpeed = droneObj.lastTargetSpeed or 0
    
    if targetSpeed < 0.05 then

        droneObj.wanderDelayTimer = droneObj.wanderDelayTimer or 0
        
        if droneObj.lastTargetSpeed >= 0.05 and targetSpeed < 0.05 then
            droneObj.wanderDelayTimer = math.random(90, 180)
            droneObj.wanderTimer = 0
            droneObj.wanderPause = 0
        end
        
        if droneObj.wanderDelayTimer > 0 then
            droneObj.wanderDelayTimer = droneObj.wanderDelayTimer - 1
            return basePos
        end
        
        droneObj.wanderTimer = droneObj.wanderTimer or 0
        droneObj.wanderTarget = droneObj.wanderTarget or basePos
        droneObj.wanderPause = droneObj.wanderPause or 0
        
        if droneObj.wanderTimer <= 0 then
            local radius = math.random(3, 6)
            local angle = math.random() * 2 * math.pi
            local heightOffset = math.random(-1.5, 1)
            
            droneObj.wanderTarget = basePos + vec(
                math.cos(angle) * radius,
                heightOffset,
                math.sin(angle) * radius
            )
            
            droneObj.wanderTimer = math.random(40, 120)
            droneObj.wanderPause = math.random(40, 80)
        else
            droneObj.wanderTimer = droneObj.wanderTimer - 1
        end
        
        local distToWander = (droneObj.pos - droneObj.wanderTarget):length()
        if distToWander < 0.5 then
            if droneObj.wanderPause > 0 then
                droneObj.wanderPause = droneObj.wanderPause - 1
                return droneObj.wanderTarget
            else
                droneObj.wanderTimer = 0
            end
        end
        
        return droneObj.wanderTarget
    else
        droneObj.wanderDelayTimer = 0
        droneObj.wanderTimer = 0
        droneObj.wanderPause = 0
        return basePos
    end
end
        droneHandle:setTargetPosFunction(wanderingTargetPos)
function events.tick()
     
    local speed = droneHandle.velocity:length()
    local targetSpeed = droneHandle.targetEntity and droneHandle.targetEntity:getVelocity():length() or 0

    if targetSpeed < 0.05 then
        droneHandle:setTopSpeed(0.3)
        droneHandle:setAcceleration(0.01)
    else
        droneHandle:setTopSpeed(2)
        droneHandle:setAcceleration(0.03)
    end
end

-------------------------------------------------------------------------------------------------------------------------------------------------------
---Mothli loves the light. :3

function PlayerHolding()
    if not player:isLoaded() then return false end
    local held = player:getHeldItem()
    if not held or not held.id then return false end
    
    local id = held.id
    return id:find("torch") or id:find("lantern") or id:find("glow_berries") or id:find("shroomlight") or id:find("glowstone") or id:find("ochre_froglight") or id:find("verdant_froglight") or id:find("pearlescent_froglight") or id:find("sea_lantern") or id:find("end_rod")
end

function events.tick()

    if PlayerHolding() then
        droneHandle.targetEntity = player
        droneHandle.wanderTimer = 0
        droneHandle.wanderPause = 0
        droneHandle.wanderDelayTimer = 0
        droneHandle.wanderTarget = playerPos
    end
	
	if PlayerHolding() then

    local playerPos = player:getPos() + vec(0, player:getEyeHeight() - 1, 0)
    
    local currentDist = (droneHandle.pos - playerPos):length()
    if currentDist > 0.8 then 

        droneHandle.pos = math.lerp(droneHandle.pos, playerPos, 0.15)
        droneHandle.velocity = vec(0,0,0)
    end
    
    local dirToPlayer = (playerPos - droneHandle.pos):normalize()
    
    local yaw = math.deg(math.atan2(dirToPlayer.x, dirToPlayer.z)) + 180
    
    local targetRot = vec(0, yaw, 0)
    droneHandle.rot = math.lerpAngle(droneHandle.rot, targetRot, 0.25)
end
	
end

------------------------------------------------------------------------------------
---pat pat pat
function droneHandle.droneInteracted(droneObj, interactor)
    if interactor:isSneaking() then
        return false
    end

    local anim = animations.mothli and animations.mothli.pat
    if anim then
        anim:stop()
        anim:setTime(0)
        anim:play()
    end

    sounds:playSound("minecraft:entity.cat.purr", droneObj.pos, 0.8, 1.0)
    particles:newParticle("minecraft:heart", droneObj.pos + vec(0, 0.7, 0), 12)
end