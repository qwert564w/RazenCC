--hide vanilla model
vanilla_model.PLAYER:setVisible(false)

--hide vanilla armor model
vanilla_model.ARMOR:setVisible(false)

--hide vanilla elytra model
vanilla_model.ELYTRA:setVisible(false)

--photon shader fix
models.Miku16th_v3:setPrimaryRenderType("Cutout")

--idle
animations.Miku16th_v3.WingsIdle:setPriority(1)
animations.Miku16th_v3.WingsIdle:play():speed(0.5)

--hair

local Left = models.Miku16th_v3.root.UpperBody.Head.hair.twintail.left_main_1
local Left_F = models.Miku16th_v3.root.UpperBody.Head.hair.twintail.left_main_1.left_front_1
local Left_B = models.Miku16th_v3.root.UpperBody.Head.hair.twintail.left_main_1.left_front_1.left_back_1

local Right = models.Miku16th_v3.root.UpperBody.Head.hair.twintail.right_main_1
local Right_F = models.Miku16th_v3.root.UpperBody.Head.hair.twintail.right_main_1.right_front_1
local Right_B = models.Miku16th_v3.root.UpperBody.Head.hair.twintail.right_main_1.right_front_1.right_back_1

local SkirtR1 = models.Miku16th_v3.root.UpperBody.Body.Skirt.veilR.sectionR1
local SkirtR2 = models.Miku16th_v3.root.UpperBody.Body.Skirt.veilR.sectionR1.sectionR2

local SkirtL1 = models.Miku16th_v3.root.UpperBody.Body.Skirt.veilL.sectionL1
local SkirtL2 = models.Miku16th_v3.root.UpperBody.Body.Skirt.veilL.sectionL1.sectionL2

-- Example usage --

-- Syntax: swingOnHead(modelpart, direction, limits, root, depth)
-- modelpart: the modelpart to swing
-- direction: basically imagine it dangling from a stick thats pointing in this direction
-- 0 means forward, 45 is 45 degree to the left, 90 is 90 degree to the left, and so on all the way around
-- limits: limit rotation for each axis, table layout {xLow, xHigh, yLow, yHigh, zLow, zHigh} (optional)
-- root: if chaining, put the root group of the chain
-- depth: for each chain element increase this number. used for increasing friction to make it look better. recommended to play around with it a bit to find values you like, also dont make it too high otherwise it will almost look stiff. mostly good values are between 1 and 5


local SwingingPhysics = require("swinging_physics")
local swingOnHead = SwingingPhysics.swingOnHead
local swingOnBody = SwingingPhysics.swingOnBody

swingOnHead(Left,90)
swingOnHead(Left.left_main_2,90,nil,Left,2)
swingOnHead(Left.left_main_2.left_main_3,90,nil,Left,2)
swingOnHead(Left.left_main_2.left_main_3.left_main_4,90,nil,Left,1)

swingOnHead(Left_F,90,nil,Left,2)
swingOnHead(Left_F.left_front_2,90,nil,Left,1)
swingOnHead(Left_F.left_front_2.left_front_3,90,nil,Left,0)
swingOnHead(Left_F.left_front_2.left_front_3.left_front_4,90,nil,Left,0)

swingOnHead(Left_B,90,nil,Left,2)
swingOnHead(Left_B.left_back_2,90,nil,Left,1)
swingOnHead(Left_B.left_back_2.left_back_3,90,nil,Left,0)
swingOnHead(Left_B.left_back_2.left_back_3.left_back_4,90,nil,Left,0)


swingOnHead(Right,270)
swingOnHead(Right.right_main_2,270,nil,Right,2)
swingOnHead(Right.right_main_2.right_main_3,270,nil,Right,2)
swingOnHead(Right.right_main_2.right_main_3.right_main_4,270,nil,Right,1)

swingOnHead(Right_F,270,nil,Right,2)
swingOnHead(Right_F.right_front_2,270,nil,Right,1)
swingOnHead(Right_F.right_front_2.right_front_3,270,nil,Right,0)
swingOnHead(Right_F.right_front_2.right_front_3.right_front_4,270,nil,Right,0)

swingOnHead(Right_B,270,nil,Right,2)
swingOnHead(Right_B.right_back_2,270,nil,Right,1)
swingOnHead(Right_B.right_back_2.right_back_3,270,nil,Right,0)
swingOnHead(Right_B.right_back_2.right_back_3.right_back_4,270,nil,Right,0)



local squapi = require("SquAPI")

--replace each nil with the value/parmater you want to use, or leave as nil to use default values :)
--parenthesis are default values for reference
squapi.eye:new(
    models.Miku16th_v3.root.UpperBody.Head.eyes.Reye,  --the eye element 
    0.3,  --(0.25) left distance
    0.2,  --(1.25) right distance
    nil,  --(0.5) up distance
    nil   --(0.5) down distance
)

squapi.eye:new(
    models.Miku16th_v3.root.UpperBody.Head.eyes.Leye,  --the eye element 
    0.2,  --(0.25) left distance
    0.3,  --(1.25) right distance
    nil,  --(0.5) up distance
    nil   --(0.5) down distance
)

squapi.eye:new(
    models.Miku16th_v3.root.UpperBody.Head.eyes.Reyetop,  --the eye element 
    0.3,  --(0.25) left distance
    0.2,  --(1.25) right distance
    0,  --(0.5) up distance
    nil   --(0.5) down distance
)

squapi.eye:new(
    models.Miku16th_v3.root.UpperBody.Head.eyes.Leyetop,  --the eye element 
    0.2,  --(0.25) left distance
    0.3,  --(1.25) right distance
    0,  --(0.5) up distance
    nil   --(0.5) down distance
)

squapi.randimation:new(
    animations.Miku16th_v3.Blink,    --animation
    50,    --(200) chanceRange
    true     --(false) isBlink
)

--replace each nil with the value/parmater you want to use, or leave as nil to use default values :)
--parenthesis are default values for reference
squapi.smoothHead:new(
    {
        models.Miku16th_v3.root.UpperBody,
        models.Miku16th_v3.root.UpperBody.Head
    },
	{
        0.075,
        0.7
   },    --(1) strength(you can make this a table too)
        nil,  --(0.1) tilt
        0.6,    --(1) speed
        nil   --(true) keepOriginalHeadPos
)

--elytra (if rotation seems to be off it is because of both animations stacking)

function events.tick()

    if player:getItem(5).id == "minecraft:elytra" then

        animations.Miku16th_v3.EquipElytra:speed(1):play()
        animations.Miku16th_v3.WingsIdle:speed(0.5)

        if player:isGliding() then
            animations.Miku16th_v3.WhileGliding:speed(1):play()
            animations.Miku16th_v3.WingsIdle:speed(1)

        else 
            animations.Miku16th_v3.WhileGliding:speed(-1)
        end
            
    else
        animations.Miku16th_v3.EquipElytra:speed(-1)
    end

end

--skirt
local skirtPhysics = require("six_skirt")

skirtPhysics.new(models.Miku16th_v3.root.UpperBody.Body.Skirt, 25, 5, 0.75, vec(0,0.75,0))

swingOnBody(SkirtR1,180)
swingOnBody(SkirtR2,180,nil,SkirtR1,2)

swingOnBody(SkirtL1,180)
swingOnBody(SkirtL2,180,nil,SkirtL1,2)

--gesture

local Heart = models.Miku16th_v3.Camera_heart
Heart:setVisible(false)
Heart:setPrimaryRenderType("Translucent_cull")

animations.Miku16th_v3.gesture:setPriority(1) --higher priority overwrites everything below, same priority stacks

local mainPage = action_wheel:newPage()
action_wheel:setPage(mainPage)


function pings.anim()
    vanilla_model.HELD_ITEMS:setVisible(false)
    Heart:setVisible(true)
    Heart.effect:setVisible(true)
    Heart.sheet:setVisible(true)
    Heart.main:setVisible(true)
    Heart.rings:setVisible(true)
    animations.Miku16th_v3.gesture:play()

--hardcoded Lmao

    function events.render()

        if animations.Miku16th_v3.gesture:getTime() > 0 and animations.Miku16th_v3.gesture:getTime() < 0.5 then
            Heart:setOpacity(animations.Miku16th_v3.gesture:getTime()/0.5)
            Heart.rings:setOpacity(animations.Miku16th_v3.gesture:getTime()/0.5)
            Heart.main:setOpacity(animations.Miku16th_v3.gesture:getTime()/0.5)
            Heart.sheet:setOpacity(animations.Miku16th_v3.gesture:getTime()/0.5)
        end

        if animations.Miku16th_v3.gesture:getTime() > 1.58 and animations.Miku16th_v3.gesture:getTime() < 1.88 then
            Heart.rings:setOpacity(1-(animations.Miku16th_v3.gesture:getTime()-1.58)/0.3)
            Heart.sheet:setOpacity(1-(animations.Miku16th_v3.gesture:getTime()-1.58)/0.3)
        end

        if animations.Miku16th_v3.gesture:getTime() > 1.58 and animations.Miku16th_v3.gesture:getTime() < 2.04 then
            Heart.main:setOpacity(1-(animations.Miku16th_v3.gesture:getTime()-1.58)/0.46)
        end

    end
end


--to decrease file size do:
--make it smaller
--try pnggaunglet
--make it indexed
--add wings dampen so it doesnt stick to the body movement

local action = mainPage:newAction()
    :title("gesture")
    :item("diamond")
    :hoverColor(0,0,0)
    :onLeftClick(pings.anim)

--add toggle for custom items
--need to add glowsticks for end rod, ita bag when having an enderchest, totem plushy

--emissive (glow when dark, no glow when bright)

if client.compareVersions(client:getVersion(),"1.21.4") < 0 then
    function events.tick()
        models:setSecondaryColor(math.map(
            world.getLightLevel(player:getPos()),
            0, 15,
            1, 0)
        )
    end
else
    local emissives = {}
    for _, texture in ipairs(models:getTextures()) do
        if texture:getName():match("_e$") then
            emissives[#emissives+1] = {texture,texture:getDimensions():unpack()}
        end
    end
    local light = -1
    local _light = -1
    function events.tick()
        _light = light
        light = world.getLightLevel(player:getPos())
        if light == _light then return end
        local matr = matrices.mat4()*math.map(
            light,
            0, 15,
            1, 0.3)
        for _, data in ipairs(emissives) do
            data[1]:restore()
            data[1]:applyMatrix(0,0,data[2],data[3],matr)
            data[1]:update()
        end
    end
end

