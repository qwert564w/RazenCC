---------------------------------------------------------------------------------------------------------------
---ANIMATION SETUP--- FIX CROUCH & LIMBS
local anims = require("lib.EZAnims")
local bunny = anims:addBBModel(animations.model)
bunny:setBlendTimes(0)

animations.model.smile:setBlendTime(5)
animations.model.sit:setBlendTime(5)
animations.model.hide_items:setBlendTime(5)
animations.model.jumpingup:setBlendTime(5)
animations.model.jumpingdown:setBlendTime(5)
---------------------------------------------------------------------------------------------------------------
---PLAYER MODEL ADJUSTMENTS---
vanilla_model.PLAYER:setVisible(false)
vanilla_model.ARMOR:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)
models.model.whole.body.torsorot.LeftArm.LeftItemPivot:setScale(0.8, 0.8, 0.8)
models.model.whole.body.torsorot.RightArm.RightItemPivot:setScale(0.8, 0.8, 0.8)
models.model.whole.body.torsorot.Body.ELYTRA_PIVOT:setScale(0.8, 0.8, 0.8)

models.model.whole:setScale(0.95, 0.95, 0.95)

---------------------------------------------------------------------------------------------------------------
---RENDER FIRST PERSON ARM---
function events.render(delta, context)
    local firstPerson = context == "FIRST_PERSON"
    models.model.RightArm:setVisible(firstPerson)
    models.model.whole.body.torsorot.RightArm:setVisible(not firstPerson)
end

nameplate.Entity:setText( avatar:getEntityName().." :bunny:")
---------------------------------------------------------------------------------------------------------------
---IDLE BODY MOVEMENTS---
animations.model.idle_2:setSpeed(0.15)
animations.model.idle_2:play()
animations.model.idle_1:setSpeed(0.3)
animations.model.idle_1:play()
animations.model.idle_3:setSpeed(0.3)
animations.model.idle_3:play()
animations.model.idle_4:setSpeed(0.2)
animations.model.idle_4:play()

---------------------------------------------------------------------------------------------------------------
---HAIR PHYSICS---
local SwingingPhysics = require("lib.swinging_physics")

SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.front_hair, 90, { -2, 5, -0, 0, -5, 5 },
    nil, 0)


SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.front_hair.lf_hair, 90, { -30, 40, -0, 0, -35, 5 },
    nil, 1)
SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.front_hair.rf_hair, 90, { -30, 40, -0, 0, -5, 35 },
    nil, 1)
SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.l_h, 90, { -10, 0, -0, 0, -15, 5 },
    nil, 2)
SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.r_h, 90, { -10, 0, -0, 0, -5, 15 },
    nil, 2)
SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.back_hair, 90, { -10, 2, -0, 0, -10, 10 },
    nil, 0)

SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.r_h.r_tail, 90, { -20, 20, -0, 0, -10, 10 },
    nil, 3)
SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.l_h.l_tail, 90, { -20, 20, -0, 0, -10, 10 },
    nil, 3)

---------------------------------------------------------------------------------------------------------------
---SQUAPI APPLICATIONS---
local squapi = require("lib.SquAPI")

---HEAD---
squapi.smoothHead:new(
    {
        models.model.whole.body,
        models.model.whole.body.torsorot.head,
        models.model.whole.body.torsorot.head.rabbit.head--element(you can have multiple elements in a table)
    },
    nil,                                      --(1) strength(you can make this a table too)
    nil,                                      --(0.1) tilt
    0.8,                                      --(1) speed
    false                                      --(true) keepOriginalHeadPos
)

---EYES---
squapi.eye:new(
    models.model.whole.body.torsorot.head.eyes.iris2, --the eye element
    0.5,                                              --(0.2) left distance
    0.5,                                              --(0.3) right distance
    0.7,                                              --(0.5) up distance
    0.7                                               --(0.5) down distance
)

squapi.eye:new(
    models.model.whole.body.torsorot.head.eyes.iris, --the eye element
    0.5,                                             --(0.2) left distance
    0.5,                                             --(0.3) right distance
    0.7,                                             --(0.5) up distance
    0.7                                              --(0.5) down distance
)



squapi.ear:new(
    models.model.whole.body.torsorot.head.ears.e1, --leftEar
    models.model.whole.body.torsorot.head.ears.e2, --(nil) rightEar
    nil,                                           --(1) rangeMultiplier
    false,                                         --(false) horizontalEars
    nil,                                           --(2) bendStrength
    nil,                                           --(true) doEarFlick
    nil,                                           --(400) earFlickChance
    nil,                                           --(0.1) earStiffness
    nil                                            --(0.8) earBounce
)

squapi.bewb:new(
    models.model.whole.body.torsorot.head.rabbit, --element
    1, --(2) bendability
    nil, --(0.05) stiff
    nil, --(0.9) bounce
    false, --(true) doIdle
    nil, --(4) idleStrength
    nil, --(1) idleSpeed
    nil, --(-10) downLimit
    nil  --(25) upLimit
)
local blink = squapi.randimation:new(
    animations.model.blink, --animation
    nil,                    --(200) chanceRange
    true                    --(false) isBlink
)

---------------------------------------------------------------------------------------------------------------

---ELYTRA RENDER---

local wings = models.model.whole.body.torsorot.Body.wings
local rabbit = models.model.whole.body.torsorot.head.rabbit


---
---ACTION WHEEL---
local actionwheel = action_wheel:newPage()
action_wheel:setPage(actionwheel)

local smile = false
local ely = false
local sit = false
function pings.smileSwitch()
    smile = not smile
    blink.enabled = not smile
    animations.model.smile:setPlaying(smile)
end

function pings.sit()
    sit = not sit
    animations.model.sit:setPlaying(sit)
    animations.model.hide_items:setPlaying(sit)
end

function events.tick()
    local crouching = player:getPose() == "CROUCHING"
    local sprinting = player:isSprinting()
    local walking = player:getVelocity().xz:length() > 0.01 or animations.model.walking:isPlaying()
    local jumping = animations.model.jumpingup:isPlaying()

    if sit 
    then if not (walking or crouching or idlesOff or jumping or sprinting) then
        animations.model.sit:setPlaying(sit)
        animations.model.hide_items:setPlaying(sit)
    else
        sit = false
        animations.model.hide_items:stop()
        animations.model.sit:stop()
    end
    end
    if not ely then
        if player:getItem(6).id == "minecraft:air" then
            rabbit:setVisible(false)
        else
            rabbit:setVisible(true)
        end

    if player:getItem(5).id == "minecraft:elytra" then
        wings:setVisible(true)
    else
        wings:setVisible(false)
    end
end
end


function pings.elytoggle()
    ely = not ely

    vanilla_model.ELYTRA:setVisible(ely)
    rabbit:setVisible(not ely)
    wings:setVisible(not ely)
end

actionwheel:newAction()
    :title("Smile")
    :item("minecraft:golden_carrot")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.smileSwitch)

    actionwheel:newAction()
    :title("Sit")
    :item("minecraft:oak_slab")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.sit)
    
    actionwheel:newAction()
    :title("Custom equipment")
    :item("minecraft:feather")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.elytoggle)



---------------------------------------------------------------------------------------------------------------
---CUSTOM LEGS - ARMS - SKIRT MOVEMENT
function events.render()
    local rot_l = vanilla_model.LEFT_LEG:getOriginRot()
    local rot_r = vanilla_model.RIGHT_LEG:getOriginRot()

    --skirt
    models.model.whole.body.torsorot.Body.skirt.c_front:setRot((math.abs(rot_l.x) + math.abs(rot_r.x)) * 0.15, nil, nil)
    models.model.whole.body.torsorot.Body.skirt.c_back:setRot(-(math.abs(rot_l.x) + math.abs(rot_r.x)) * 0.15, nil, nil)
    models.model.whole.body.torsorot.Body.skirt.l_leg:setRot(rot_l.x * 0.2, nil, nil)
    models.model.whole.body.torsorot.Body.skirt.r_leg:setRot(rot_r.x * 0.2, nil, nil)

    --arms
    models.model.whole.body.torsorot.RightArm:setRot(-rot_l * 0.3)
    models.model.whole.body.torsorot.LeftArm:setRot(-rot_r * 0.3)
    --legs
    models.model.whole.RightLeg:setRot(-rot_r * 0.2)
    models.model.whole.LeftLeg:setRot(-rot_l * 0.2)

    models.model.whole.body.torsorot.head.ears:setRot(-(math.abs(rot_l.x) + math.abs(rot_r.x)) * 0.05, nil, nil)

end
