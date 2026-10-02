---FILE WHERE I THROW FUNCTIONS AND MODELPARTS, THANK YOU---
local data = require("scripts.code_assets")

-----------------------------------------------
---INIT PLAYER, CREATE CONFIG & APPLY CHANGES FROM IT(its my first time working with configs im sorry)---
-----------------------------------------------
config:setName("Elise")


function events.entity_init()

    --accessories
    if config:load("saved_jev") == nil then
        config:save("saved_jev", false)
    end
    data:toggleVis(data.jewelery, config:load("saved_jev"))

    --outfit
    if config:load("saved_outfit") == nil then
        config:save("saved_outfit", true)
    end
    data:toggleVis(data.outfit1, config:load("saved_outfit"))
    for i = 1, #data.outfit2, 1
    do
        data.outfit2[i]:setVisible(not config:load("saved_outfit"))
    end


    if config:load("saved_hair1") == nil then
        config:save("saved_hair1", true)
    end
    data:toggleVis(data.backHair1, config:load("saved_hair1"))
    for i = 1, #data.backHair2, 1
    do
        data.backHair2[i]:setVisible(not config:load("saved_hair1"))
    end


    if config:load("saved_hair2") == nil then
        config:save("saved_hair2", true)
    end
    data:toggleVis(data.frontHair1, config:load("saved_hair2"))
    for i = 1, #data.frontHair2, 1
    do
        data.frontHair2[i]:setVisible(not config:load("saved_hair2"))
    end
    if config:load("saved_wings") == nil then
        config:save("saved_wings", true)
    end
    models.model.whole.torsorot.torso.bodyrot.body.wings:setVisible(config:load("saved_wings"))

    if config:load("saved_eyes") == nil then
        config:save("saved_eyes", true)
    end
    data:toggleVis(data.eyesmain, config:load("saved_eyes"))
    for i = 1, #data.eyesimple, 1
    do
        data.eyesimple[i]:setVisible(not config:load("saved_eyes"))
    end
    if config:load("saved_eyesE") == nil then
        config:save("saved_eyesE", false)
    end
    if config:load("saved_eyesE") then
        models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.eyes:setSecondaryRenderType("EMISSIVE")
    else
        models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.eyes:setSecondaryRenderType("NONE")
    end
end

---HIDE THE PLAYER---
vanilla_model.ARMOR:setVisible(false)
vanilla_model.PLAYER:setVisible(false)
vanilla_model.CAPE:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)


---MAKE ITEMS SMALL AND CUTE---
models.model.whole.torsorot.torso.l_arm.hand.LeftItemPivot:setScale(0.7)
models.model.whole.torsorot.torso.r_arm.hand.RightItemPivot:setScale(0.7)
nameplate.Entity:setText("Elise")


---CHANGE SWORDS ITEM MODEL---
function events.item_render(item)
    if item.id == "minecraft:diamond_sword" or item.id == "minecraft:iron_sword" or item.id == "minecraft:netherite_sword"
        or item.id == "minecraft:stone_sword" or item.id == "minecraft:golden_sword" or item.id == "minecraft:wooden_sword" then
        return models.sword.Item
    end
end

-----------------------------------------------
--- SQUISHY APPLICATIONS. HEAD. EARS. ETC.---
-----------------------------------------------
local squapi = require("lib.SquAPI")
squapi.eye:new(
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.eyes.iris, --the eye element
    0.5,                                                                        --(0.2) left distance
    0.1,                                                                        --(0.3) right distance
    0.7,                                                                        --(0.5) up distance
    0.7                                                                         --(0.5) down distance
)

squapi.eye:new(
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.eyes.iris2, --the eye element
    0.1,                                                                         --(0.2) left distance
    0.5,                                                                         --(0.3) right distance
    0.7,                                                                         --(0.5) up distance
    0.7                                                                          --(0.5) down distance
)

squapi.ear:new(
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.ears.ear2, --leftEar
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.ears.ear1, --(nil) rightEar
    nil,                                                                        --(1) rangeMultiplier
    false,                                                                      --(false) horizontalEars
    nil,                                                                        --(2) bendStrength
    nil,                                                                        --(true) doEarFlick
    3000,                                                                       --(400) earFlickChance
    nil,                                                                        --(0.1) earStiffness
    nil                                                                         --(0.8) earBounce
)

squapi.smoothHead:new(
    {
        models.model.whole.torsorot.torso,
        models.model.whole.torsorot.torso.bodyrot.body.neck,
        models.model.whole.torsorot.torso.bodyrot.body.neck.headrot
        --element(you can have multiple elements in a table)
    },
    nil, --(1) strength(you can make this a table too)
    nil, --(0.1) tilt
    nil, --(1) speed
    true --(true) keepOriginalHeadPos
)

squapi.bewb:new(
    models.model.whole.torsorot.torso.bodyrot.body.bodyrot2.chest, --element
    nil,                                                           --(2) bendability
    nil,                                                           --(0.05) stiff
    nil,                                                           --(0.9) bounce
    nil,                                                           --(true) doIdle
    2,                                                             --(4) idleStrength
    0.5,                                                           --(1) idleSpeed
    nil,                                                           --(-10) downLimit
    nil                                                            --(25) upLimit
)

local blink = squapi.randimation:new(
    animations.model.blink, --animation
    nil,                    --(200) chanceRange
    true                    --(false) isBlink
)

local flick = squapi.randimation:new(
    animations.model.flick, --animation
    nil,                    --(200) chanceRange
    true                    --(false) isBlink
)

-----------------------------------------------
---HAIR SKIRT ALL THAT PHYSICS---
-----------------------------------------------
local SwingingPhysics = require("lib.swinging_physics")
SwingingPhysics.swingOnBody(models.model.whole.torsorot.torso.bodyrot.body.skirt, 90, { -2, 2, -0, 0, -2, 2 }, nil, 0)

SwingingPhysics.swingOnHead(models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.front_hair, 90,
    { -2, 5, -0, 0, -5, 5 },
    nil, 0)
SwingingPhysics.swingOnHead(models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.front_hair.strand1, 90,
    { -20, 20, -0, 0, -10, 5 },
    nil, 0)
SwingingPhysics.swingOnHead(models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.front_hair.strand2, 90,
    { -20, 20, -0, 0, -5, 10 },
    nil, 0)
SwingingPhysics.swingOnHead(models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.right_hair, 90,
    { -10, 10, -0, 0, -5, 20 },
    nil, 2)
SwingingPhysics.swingOnHead(models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.left_hair, 90,
    { -10, 10, -0, 0, -20, 5 },
    nil, 2)

SwingingPhysics.swingOnHead(models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.pom2.twintail, 90,
    { -50, 20, -0, 0, -15, 40 },
    nil, 1)
SwingingPhysics.swingOnHead(models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.pom1.twintail, 90,
    { -50, 20, -0, 0, -40, 15 },
    nil, 1)

SwingingPhysics.swingOnHead(models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.ears.ear1.earring, 90,
    { -70, 70, -0, 0, -70, 70 }, nil, 1)
SwingingPhysics.swingOnHead(models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.ears.ear2.earring, 90,
    { -70, 70, -0, 0, -70, 70 }, nil, 1)

-----------------------------------------------
---ANIMATION RELATED STUFF---
-----------------------------------------------
---THANK YOU JIMMY, BLESS YOU---
local anims = require("lib.JimmyAnims")
local anim = animations.model


---FINE TUNING ANIMATIONS, ADDING IDLE MOVEMENT---
anim.i1:play()
anim.i1:setSpeed(0.2)
anim.i2:play()
anim.i2:setSpeed(0.2)
anim.i3:play()
anim.i3:setSpeed(0.2)
anim.lanternglow:play()
anim.walkmain:setSpeed(2.0)
anim.walkbackmain:setSpeed(2.0)
anim.sprintmain:setSpeed(0.8)
anim.idlemain:setSpeed(0.1)
anim.walksword:setSpeed(2.0)
anim.walkbacksword:setSpeed(2.0)
anim.sprintsword:setSpeed(0.8)
anim.idlesword:setSpeed(0.1)
anim.crouchwalk:setSpeed(0.5)
anim.idleproud:setSpeed(0.1)
anim.walkproud:setSpeed(2.0)
anim.walkbackproud:setSpeed(2.0)
anim.idlegoblet:setSpeed(0.1)
anim.walkbackgoblet:setSpeed(2.0)
anim.walkgoblet:setSpeed(2.0)
anim.walklamp:setSpeed(2.0)
anim.sprintlamp:setSpeed(0.8)
anim.sprintgoblet:setSpeed(0.8)
anim.lanternhold:setSpeed(0.1)
anim.walkbacklamp:setSpeed(2.0)

anim.sitting:priority(1)
anims.autoBlend = false
anims(animations.model)


---APPLYING BLENDING, I'M NOT SORRY---
data:Applyblend(data.toBlend, 5)
data:Applyblend(data.toBlendSmol, 3)


---APPLYING BATTERUP---
local batterUp = require("lib.BatterUp")
anim.att1:setSpeed(1.5)
anim.att2:setSpeed(1.5)
local randos = {
    anim.att1,
    anim.att2,
}
local randos2 = {
    anim.att3,
    anim.att4,
}
local swords = { "stone_sword", "wooden sword", "golden_sword", "iron_sword", "diamond_sword", "netherite_sword" }
batterUp:addRandomSwings(randos, "right", swords, "attack", false)
batterUp:addRandomSwings(randos2, "right", nil, "attack", true)

-----------------------------------------------
---RENDER POSES WITH ITEMS PROPERLY WOHOO---
-----------------------------------------------
function events.render(delta, context)
    data:SwordCheck()
    data:LanternCheck()
    if data.mode == 'sword' then
        data:StopLampPoses()
        data:StopPoses()
        data:PlaySwordPose()
        anim.hiderighthand:stop()
    elseif data.mode == 'leftlantern' then
        data:StopSwordPoses()
        data:PlayLeftLanternPose()
        data:StopPoses()
    else
        data:StopSwordPoses()
        data:StopLampPoses()
        if data.mode == 'main' then
            data:PlayMainPose()
        elseif data.mode == 'proud' then
            data:PlayDownPose()
        elseif data.mode == 'goblet' then
            data:PlayGobletPose()
        end
    end
end

-----------------------------------------------
---ACTION WHEEL STUFF---
-----------------------------------------------

---PAGES 'N PAGES SWITCH---
local actionwheel = action_wheel:newPage()
local toggles = action_wheel:newPage()
action_wheel:setPage(actionwheel)

local toggleswitch = actionwheel:newAction()
    :title("deco toggles")
    :item("amethyst_shard")
    :onLeftClick(function()
        action_wheel:setPage(toggles)
    end)
local toggleswitchb = toggles:newAction()
    :title("back")
    :item("crafting_table")
    :onLeftClick(function()
        action_wheel:setPage(actionwheel)
    end)


---POSE FUNCTION SWITCH---
function pings.togglePoseSet()
    if data.pose == 'main' then
        data.pose = 'proud'
        data:StopPoses()
    elseif data.pose == 'proud' then
        models.model.whole.torsorot.torso.l_arm.hand.goblet:setVisible(true)
        data.pose = 'goblet'
        data:StopPoses()
    elseif data.pose == 'goblet' then
        models.model.whole.torsorot.torso.l_arm.hand.goblet:setVisible(false)
        data.pose = 'main'
        data:StopPoses()
    end
end

local myParticle = particles["white_smoke"]
myParticle:setColor(1, 1, 1)





local animswitch = actionwheel:newAction()
    :title("pose switch")
    :item("redstone")
    :onLeftClick(pings.togglePoseSet)

---VISIBILITY TOGGLES---
function pings.wings()
    data:toggleVis(data.wings)
    config:save("saved_wings", not config:load("saved_wings"))
    for i = 1, 20, 1
    do
        particles:newParticle("squid_ink 0 0 1 1", player:getPos()+vec(-0.5+(1-math.random()),-0.5+ (1+math.random()*0.8),-0.5+(1-math.random())), vec(0,-math.random()*0.05,0))
    
    
    end
end
local isSitting = false
function pings.sit()
    --data:toggleVis(data.wings)
    anim.sitting:setPlaying(not isSitting)
    isSitting = not isSitting
    vanilla_model.HELD_ITEMS:setVisible(not isSitting)
    models.sword.sword:setVisible(isSitting)

end


function pings.jevels()
    data:toggleVis(data.jewelery)
    sounds:playSound("block.amethyst_block.step", player:getPos(), 1, 3)
    config:save("saved_jev", not config:load("saved_jev"))

    for i = 1, 10, 1
    do
        particles:newParticle("end_rod 0 0 1 1", player:getPos()+vec(-0.7+(1-math.random()),0.4+ (1+math.random()*0.7),-0.5+(1-math.random())), vec(0,-math.random()*0.05,0))
    
    
    end
end

function pings.outfitSwitch()
    data:toggleVis(data.outfit1)
    data:toggleVis(data.outfit2)
    
    config:save("saved_outfit", not config:load("saved_outfit"))
    for i = 1, 30, 1
    do
        particles:newParticle("squid_ink 0 0 1 1", player:getPos()+vec(-0.5+(1-math.random()),-0.5+ (1+math.random()*0.8),-0.5+(1-math.random())), vec(0,-math.random()*0.05,0))
    
    
    end

end

function pings.hair1Switch()
    data:toggleVis(data.backHair1)
    data:toggleVis(data.backHair2)
    sounds:playSound("item.armor.equip_leather", player:getPos(), 5)
    config:save("saved_hair1", not config:load("saved_hair1"))
    for i = 1, 10, 1
    do
        particles:newParticle("squid_ink 0 0 1 1", player:getPos()+vec(-0.7+(1-math.random()),0.4+ (1+math.random()*0.7),-0.5+(1-math.random())), vec(0,-math.random()*0.05,0))
    
    
    end

end

function pings.hair2Switch()
    data:toggleVis(data.frontHair1)
    data:toggleVis(data.frontHair2)
    sounds:playSound("item.armor.equip_leather", player:getPos(), 5)
    config:save("saved_hair2", not config:load("saved_hair2"))
    for i = 1, 10, 1
    do
        particles:newParticle("squid_ink 0 0 1 1", player:getPos()+vec(-0.7+(1-math.random()),0.4+ (1+math.random()*0.7),-0.5+(1-math.random())), vec(0,-math.random()*0.05,0))
    
    
    end

end

function pings.eyeSwitch()
    data:toggleVis(data.eyesimple)
    data:toggleVis(data.eyesmain)
    sounds:playSound("entity.item.pickup", player:getPos())
    config:save("saved_eyes", not config:load("saved_eyes"))
end

function pings.eyeESwitch()
    config:save("saved_eyesE", not config:load("saved_eyesE"))
    if config:load("saved_eyesE") then
        models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.eyes:setSecondaryRenderType("EMISSIVE")
    else
        models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.eyes:setSecondaryRenderType("NONE")
    end

end
local mode1 = toggles:newAction()
    :title("wings")
    :item("minecraft:feather")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.wings)

local mode2 = toggles:newAction()
    :title("jevelery")
    :item("minecraft:iron_ingot")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.jevels)

local mode3 = toggles:newAction()
    :title("outfit")
    :item("minecraft:green_wool")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.outfitSwitch)

local mode4 = toggles:newAction()
    :title("back hair")
    :item("minecraft:brown_wool")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.hair1Switch)

local mode5 = toggles:newAction()
    :title("front hair")
    :item("minecraft:brown_wool")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.hair2Switch)

local mode6 = toggles:newAction()
    :title("eye shape")
    :item("minecraft:redstone")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.eyeSwitch)

    local mode7 = toggles:newAction()
    :title("eye glow")
    :item("minecraft:redstone")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.eyeESwitch)

    local mode8 = actionwheel:newAction()
    :title("sit on ground")
    :item("minecraft:black_wool")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.sit)

