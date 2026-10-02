---------------------------------------------------------------------------------------------------------------
local conf = {}
if host:isHost() then
    config:setName("Hyalisse")

    config:save("pov", config:load("pov") or false)
    local confFile = config:load()

    config:save("jacket", config:load("jacket") or false)
    local confFile = config:load()


    for k, v in pairs(confFile) do
        conf[k] = v
    end
end

function writeConf(cname, val)
    config:save(cname, val)
end
--nameplate.ALL:setText('[{"text":":leaf: Aria","color":"#7e856a"}]')
nameplate.ENTITY:setPos(0,-0.3,0)
---------------------------------------------------------------------------------------------------------------
---HAIR PHYSICS---
local SwingingPhysics = require("lib.swinging_physics")

SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.front_hair, 0, { -2, 5, -0, 0, -5, 5 },
    nil, 0)

    SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.front_hair.lf_hair, 0, { -0, 5, -0, 0, -10, 5 },
    models.model.whole.body.torsorot.head.hair.front_hair, 1)

        SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.front_hair.rf_hair, 0, { -0, 5, -0, 0, -5, 10 },
   models.model.whole.body.torsorot.head.hair.front_hair, 1)

        SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.front_hair.lf_hair.lff, 0, { -0, 5, -0, 0, -10, 5 },
    models.model.whole.body.torsorot.head.hair.front_hair.lf_hair, 2)

        SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.front_hair.rf_hair.rff, 0, { -0, 5, -0, 0, -5, 10 },
    models.model.whole.body.torsorot.head.hair.front_hair.rf_hair, 2)
SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.l_h, 90, { -10, 10, -0, 0, -20, 5 },
    nil, 0)

    SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.r_h, -90, { -10, 10, -0, 0, -5, 20 },
    nil, 0)
    SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.back_hair, -90, { -20, 0, -0, 0, -5, 5 },
    nil, 0)
    
       SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.back_hair.bh1, -90, { -0, 20, -0, 0, -10, 10 },
    models.model.whole.body.torsorot.head.hair.back_hair, 1)

           SwingingPhysics.swingOnHead(models.model.whole.body.torsorot.head.hair.back_hair.bh2, -90, { -40, 0, -0, 0, -15, 15 },
    models.model.whole.body.torsorot.head.hair.back_hair, 1)


---PLAYER MODEL ADJUSTMENTS---
vanilla_model.PLAYER:setVisible(false)
vanilla_model.ARMOR:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)

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
---SQUAPI APPLICATIONS---
local squapi = require("lib.SquAPI")

---HEAD---
squapi.smoothHead:new(
    {
        models.model.whole.body,
        models.model.whole.body.torsorot.head --element(you can have multiple elements in a table)
    },
    nil,                                      --(1) strength(you can make this a table too)
    nil,                                      --(0.1) tilt
    0.8,                                      --(1) speed
    true                                    --(true) keepOriginalHeadPos
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
    true,                                         --(false) horizontalEars
    nil,                                           --(2) bendStrength
    nil,                                           --(true) doEarFlick
    nil,                                           --(400) earFlickChance
    nil,                                           --(0.1) earStiffness
    nil                                            --(0.8) earBounce
)

local blink = squapi.randimation:new(
    animations.model.blink, --animation
    nil,                    --(200) chanceRange
    true                    --(false) isBlink
)



---ACTION WHEEL---
local actionwheel = action_wheel:newPage()
action_wheel:setPage(actionwheel)


function pushChanges()


    if host:isHost() then
        if not conf["pov"] then
            renderer:setEyeOffset(0, 0, 0)
            renderer:setOffsetCameraPivot(0, 0, 0)
        else
                renderer:setEyeOffset(0, -0.3, 0)
                renderer:setOffsetCameraPivot(0, -0.3, 0)
            end
        end
    end

function pings.actiontpovclicked()
    conf["pov"] = not conf["pov"]
    if host:isHost() then
        writeConf("pov", conf["pov"])
    end
    pushChanges()
end



local povtog = actionwheel:newAction()
    :title("Normal sight active")
    :toggleTitle("Height sight active")
    :item("minecraft:cake")
    :toggleItem("minecraft:milk_bucket")
    :hoverColor(1, 0, 1)
    :onToggle(pings.actiontpovclicked)



povtog:setToggled(config:load("pov"))



function pings.pushConfig(targetConf)
    if not host:isHost() then
        if targetConf ~= nil then
            local cfg = targetConf:sub(1, string.len(targetConf) - 1)
            local val = (0 ~= tonumber(targetConf:sub(#targetConf, #targetConf)))
            conf[cfg] = val
        end
    end
end

local pingTimer = 0
function events.tick(delta, context)
    if pingTimer < 20 then
        pingTimer = pingTimer + 1
    else
        pingTimer = 0
        pushChanges()
        if host:isHost() then
            for k, v in pairs(conf) do
                local payload
                if v then
                    payload = k .. "1"
                else
                    payload = k .. "0"
                end
                pings.pushConfig(payload)
            end
        end
    end
end

---------------------------------------------------------------------------------------------------------------
---CUSTOM LEGS - ARMS 
function events.render()
    local rot_l = vanilla_model.LEFT_LEG:getOriginRot()
    local rot_r = vanilla_model.RIGHT_LEG:getOriginRot()

    --skirt
    models.model.whole:setPos(nil, (math.sin(math.abs(rot_r.x / 70)) * 1.5), nil)     -- bounce
    --arms
    models.model.whole.body.torsorot.RightArm:setRot(-rot_l * 0.3)
    models.model.whole.body.torsorot.LeftArm:setRot(-rot_r * 0.3)
    models.model.whole.body.torsorot.Body.wings.w2:setRot(0, -rot_r.x * 0.2, 0)
        models.model.whole.body.torsorot.Body.wings.w1:setRot(0, rot_r.x * 0.2, 0)
    --legs
    models.model.whole.RightLeg:setRot(-rot_r * 0.2)
    models.model.whole.LeftLeg:setRot(-rot_l * 0.2)
end
