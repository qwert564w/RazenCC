vanilla_model.PLAYER:setVisible(false)
vanilla_model.ARMOR:setVisible(false)
vanilla_model.HELMET_ITEM:setVisible(false)
vanilla_model.CAPE:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)

models.model.root.torso.body.topbody.head.upperhead.googly.Rcutout:setPrimaryRenderType("CUTOUT_CULL")
models.model.root.torso.body.topbody.head.upperhead.googly.Lcutout:setPrimaryRenderType("CUTOUT_CULL")
models.model.root.torso.body.topbody.head.upperhead.googly.rightgoogle.rightgoogle_light:setSecondaryRenderType("EMISSIVE")
models.model.root.torso.body.topbody.head.upperhead.googly.leftgoogle.leftgoogle_light:setSecondaryRenderType("EMISSIVE")

require("GSAnimBlend")
local anims = require("EZAnims")
local repo = anims:addBBModel(animations.model)
local squapi = require("SquAPI")

animations.model.crouching:setBlendTime(1)
animations.model.yap:setBlendTime(1)

-- LEANWALK
      local prevVel = vec(0,0,0)
      local curVel = vec(0,0,0)
    function events.tick()
        prevVel = curVel
        curVel = player:getVelocity()
    end

      local walkStr = 50
    function events.render(delta)
          
      local vel = math.lerp(prevVel,curVel,delta):transform(matrices.rotation3(0, player:getRot().y)) * walkStr
      local moveRot = vec(-vel.z*0.7, 0, vel.x*0.7)

        models.model.root:offsetRot(moveRot/2)
        models.model.root.torso:offsetRot(moveRot/4)
    end

-- SMOOTH BODY & HEAD // Sparkie's smooth body: credits to @ar_aphelion on discord
local _rot
local rot = { 0,0,0 }

local head_part = models.model.root.torso.body.topbody.head
local torso_part = models.model.root.torso
local toptorso_part = models.model.root.torso.body.topbody
local model = models.model

function events.tick()
  _rot = (vanilla_model.HEAD:getOriginRot() + 180) % 360 - 180
end
function GetRotations()
  rot[1] = math.lerp(rot[1], _rot.x,0.1)

  rot[2] = math.lerp(rot[2], _rot.y,0.1)
end
function SmoothBodyRot(modelpart, xmod, ymod, zxmod, zymod)
  zxmod = zxmod or 0
  zymod = zymod or 0

  modelpart:setRot(rot[1] * xmod, rot[2] * ymod, rot[1] * zxmod + rot[2] * zymod)
  modelpart:setPos(-rot[1] * xmod / 200, -rot[2] * ymod / 2000, rot[1] * zxmod / 200 + rot[2] * zymod / 200)
end

function events.render()
  GetRotations()

  -- smoove body
    SmoothBodyRot(head_part, 0.5,0.5,0.1)
    SmoothBodyRot(torso_part, 0.3, 0.3, 0)
    SmoothBodyRot(toptorso_part, 0.3, 0, 0)
    SmoothBodyRot(model, 0.05, 0.1, 0)
end

	-- legs rotation \\ squapi.leg:new(element, strength, isRight(false), keepPosition)
		squapi.leg:new(models.model.root.legs.leftleg, 0.5, isRight, false)
		squapi.leg:new(models.model.root.legs.rightleg, 0.5, true, false)

	-- arms rotation
		squapi.arm:new(models.model.root.torso.body.topbody.arms.leftarm, 0.7, isRight, false)
		squapi.arm:new(models.model.root.torso.body.topbody.arms.rightarm, 0.7, true, false)

-- YAPPING
local keybindState = false
function pings.yapPing(state)
    keybindState = state
    if state then
	    animations.model.yap:play()
	else
		animations.model.yap:stop()
	end
end
local yapkey = keybinds:newKeybind("Yap Key","key.keyboard.g")
yapkey.press = function() pings.yapPing(true) end
yapkey.release = function() pings.yapPing(false) end

--========== EYE MVT 

function events.render()
    headRotX = (vanilla_model.HEAD:getOriginRot().x+180)%360-180
    headRotY = (vanilla_model.HEAD:getOriginRot().y+180)%360-180

  if headRotY > 0 then
      models.model.root.torso.body.topbody.head.upperhead.googly.leftgoogle.leftgoogle_light:setPos(math.clamp(headRotY / 150 * -1, 1, 0),math.clamp(headRotX / 100 * 1, -1, 0),0)
      models.model.root.torso.body.topbody.head.upperhead.googly.rightgoogle.rightgoogle_light:setPos(0,math.clamp(headRotX / 100 * 1, -2, 0),0)
  else if headRotY < 0 then
      models.model.root.torso.body.topbody.head.upperhead.googly.leftgoogle.leftgoogle_light:setPos(0,math.clamp(headRotX / 100 * 1, -2, 0),0)
      models.model.root.torso.body.topbody.head.upperhead.googly.rightgoogle.rightgoogle_light:setPos(math.clamp(headRotY / 150 * -1, 0, 1),math.clamp(headRotX / 100 * 1, -1, 0),0)
  end
  end

end

--========== EYE JIGGLE WIGGLE PHYSICS // credits to @invalid_os on discord

config:name("GooglyEyes")

-- velocity dampening constant for when a collision occurs
local BOUNCINESS = 0.7

-- acceleration from gravity
local GRAVITY = 0.08

-- determines how the googly eyes move
-- 0 causes them to only move with gravity and player movement
-- 1 causes them to follow the head rotation
-- 2 combines them, using 0's physics while applying a force towards the positon the iris would be at with 1
local movementMode = config:load("Mode") or 0

-- the position values of each googly eye's iris are relative to their respective eye's center
local leftGooglePos = vec(-0.25,0)
local leftGooglePrevPos = vec(-0.25,0)
local leftGoogleVel = vec(0,0)

local rightGooglePos = vec(0.25,0)
local rightGooglePrevPos = vec(0.25,0)
local rightGoogleVel = vec(0,0)

local headRot = vec(0,0)
local prevHeadRot = vec(0,0)
local headRotVel = vec(0,0)

--- Classic head rotation-based movement.
--- @return Vector2
--- @return Vector2
local function getGooglyEyePosFromHeadRot()
    local headRotX = (vanilla_model.HEAD:getOriginRot().x+180)%360-180
    local headRotY = (vanilla_model.HEAD:getOriginRot().y+180)%360-180

    local headYawSign = math.sign(headRotY)
    local leftMult  = headYawSign == 1 and 0.01    or 0.00667 -- 1/100 if head yaw is greater than 0, ~1/150 if less than or equal to 0
    local rightMult = headYawSign == 1 and 0.00667 or 0.01    -- ~1/150 if head yaw is greater than 0, 1/100 if less than or equal to 0

    return
        -- left eye pos
        vec(
            math.clamp(-headRotY * leftMult, -1, 0.5),
            math.clamp(headRotX * 0.05, -0.5, 0.5)
        ),

        -- right eye pos
        vec(
            math.clamp(-headRotY * rightMult, -0.5, 1),
            math.clamp(headRotX * 0.05, -0.5, 0.5)
        )
end

--- Returns a force vector from head rotation.
--- @return Vector2
local function headRotVelForce()
    return vec(-headRotVel.y / 90, headRotVel.x / 90)
end

--- Detects if a collision occurred, and returns where and when it happened, plus the normal vector of the surface it hit.
--- @param pos Vector2
--- @param vel Vector2
--- @return Vector2 posHit
--- @return number time
--- @return Vector2 normal
local function doCollisionStep(pos, vel)
    -- for detecting if we've hit the edge, we can simply assume the iris is a point, and the googly eye itself is only 1 pixel wide
    -- this means the boundaries for the eye are at +-0.5 on both axes

    local time = 1

    local collisionTimes = {1, 1}
    local posHit = {vec(0,0), vec(0,0)}
    local axisVel = 0
    local truePosHit = vec(0,0)

    -- find collision times for each side
    for index = 1,2 do
        local nextPos = pos[index] + vel[index]

        -- absolute value lets us not have to test each side, only ones we can collide with this tick
        if math.abs(nextPos) > 0.5 then
            --pos + vel / x = 0.5
            --vel / x = 0.5 - pos
            --x = (0.5 - pos) / vel

            -- attempt to avoid divison by 0
            collisionTimes[index] = vel[index] == 0 and 1 or ((math.sign(vel[index]) * 0.5 - pos[index]) / vel[index])
            posHit[index] = pos + vel * collisionTimes[index]

            truePosHit = posHit[index]
            axisVel = vel[index]
        end
    end

    -- if the collision time is greater than 1, it didn't happen this frame.
    if collisionTimes[1] >= 1 and collisionTimes[2] >= 1 then return pos+vel, 1, vec(0,0) end

    -- get collision normal/time
    local normal = vec(1,1)
    time = collisionTimes[1]

    if collisionTimes[1] < collisionTimes[2] then
        normal = vec(1,0)
    elseif collisionTimes[2] < collisionTimes[1] then
        normal = vec(0,1)
        time = collisionTimes[2]
    end

    normal = -math.sign(axisVel) * normal

    return truePosHit, time, normal
end

local function flipVec(vec, normal)
    if normal.x ~= 0 then vec.x = -vec.x end
    if normal.y ~= 0 then vec.y = -vec.y end

    return vec
end

--- Handles collision.
--- @param pos Vector2
--- @param vel Vector2
--- @return Vector2 newPos
--- @return Vector2 newVel
local function doCollision(pos, vel)
    local panicked = false
    local doGrav = true
    local timeLatest = 0
    local iter = 0

    local prevPos = pos
    while true do
        -- stop infinite loops
        if iter > 50 then
            panicked = true
            break
        end

        -- velocity for this step
        local stepVel = iter == 0 and vel or (pos - prevPos)

        -- do step
        local posHit, time, normal = doCollisionStep(prevPos, stepVel)
        --print(posHit, time, normal)

        local normalSquare = normal:lengthSquared() -- normal.normal

        -- break if the collision doesn't happen this tick
        if time >= 1 then
            break
        else
            if normalSquare ~= 0 then
                -- save this for later
                prevPos = pos

                --[[ set new position / velocity
                vel = flipVec(vel, normal) * BOUNCINESS + headRotVelForce()
                pos = posHit + normal*0.005]]

                local velDot = (1-time) * stepVel:dot(normal)
                local wallSlideVec = ((1-time) * stepVel) - (velDot/normalSquare)*normal

                -- new pos
                pos = posHit + wallSlideVec

                -- new vel part 1
                vel = vel * normal.yx:applyFunc(math.abs) * 0.99 +
                    flipVec(vel,normal) * normal:applyFunc(math.abs) * BOUNCINESS
            end

            -- new vel part 2
            vel = vel + headRotVelForce()
        end

        iter = iter + 1
    end

    --print(pos, vel, iter)

    -- if an infinite loop occurred, extinguish the fire
    if panicked then pos, vel = vec(0,0), vec(0,0) end

    return pos, vel
end

local prevVel = vec(0,0,0)
local prevHeadRotVel = vec(0,0)
function events.TICK()
    headRot = vec((vanilla_model.HEAD:getOriginRot().x+180)%360-180, (vanilla_model.HEAD:getOriginRot().y+180)%360-180)
    headRotVel = headRot - prevHeadRot

    local accel = player:getVelocity() - prevVel
    local lrAccel = (accel * matrices.rotation3(0, player:getRot().y, 0)).x
    local udAccel = accel.y

    local headRotAccel = (headRotVel - prevHeadRotVel).yx
    prevHeadRotVel = headRotVel

    local accelRelative = vec(
        lrAccel, -udAccel
    ) / 2 + headRotAccel * (2*math.pi / 360)

    leftGooglePrevPos = leftGooglePos
    rightGooglePrevPos = rightGooglePos

    -- disable gravity for mode 2
    local grav = movementMode == 2 and 0 or GRAVITY

    -- drag and gravity
    leftGoogleVel = (leftGoogleVel - vec(0,grav)) * 0.95
    rightGoogleVel = (rightGoogleVel - vec(0,grav)) * 0.9 -- ever so slightly different value so they fall out of sync and look sillier

    if movementMode ~= 1 then
        local leftForce = accelRelative
        local rightForce = accelRelative

        if movementMode == 2 then
            local leftTarget, rightTarget = getGooglyEyePosFromHeadRot()
            rightTarget = rightTarget * vec(1,-1) -- again, sillier

            leftGoogleVel = leftGoogleVel + (leftTarget - leftGooglePos) / 6
            rightGoogleVel = rightGoogleVel + (rightTarget - rightGooglePos) / 6
        end

        leftGoogleVel = leftGoogleVel + leftForce
        rightGoogleVel = rightGoogleVel + rightForce

        leftGooglePos, leftGoogleVel = doCollision(leftGooglePos, leftGoogleVel)
        rightGooglePos, rightGoogleVel = doCollision(rightGooglePos, rightGoogleVel)

        leftGooglePos = leftGooglePos + leftGoogleVel
        rightGooglePos = rightGooglePos + rightGoogleVel

        -- just in case
        leftGooglePos = vec(math.clamp(leftGooglePos.x, -0.5, 0.5), math.clamp(leftGooglePos.y, -0.5, 0.5))
        rightGooglePos = vec(math.clamp(rightGooglePos.x, -0.5, 0.5), math.clamp(rightGooglePos.y, -0.5, 0.5))
    else
        -- get new positions
        leftGooglePos, rightGooglePos = getGooglyEyePosFromHeadRot()

        -- set new velocities in case the mode gets switched
        leftGoogleVel = leftGooglePos - leftGooglePrevPos
        rightGoogleVel = rightGooglePos - rightGooglePrevPos
    end

    prevHeadRot = headRot
end

function events.RENDER(delta)
    models.model.root.torso.body.topbody.head.upperhead.googly.leftgoogle:pos(
        math.lerp(leftGooglePrevPos, leftGooglePos, delta).xy_
    )

    models.model.root.torso.body.topbody.head.upperhead.googly.rightgoogle:pos(
        math.lerp(rightGooglePrevPos, rightGooglePos, delta).xy_
    )
end

--[[function events.render()
    local headRotX = (vanilla_model.HEAD:getOriginRot().x+180)%360-180
    local headRotY = (vanilla_model.HEAD:getOriginRot().y+180)%360-180

    local headYawSign = math.sign(headRotY)
    local leftMult  = headYawSign == 1 and 0.01    or 0.00667 -- 1/100 if head yaw is greater than 0, ~1/150 if less than or equal to 0
    local rightMult = headYawSign == 1 and 0.00667 or 0.01    -- ~1/150 if head yaw is greater than 0, 1/100 if less than or equal to 0

    models.model.root.torso.body.topbody.head.upperhead.googly.leftgoogle:setPos(
        math.clamp(-headRotY * leftMult, -1, 0),
        math.clamp(headRotX * 0.05, -1, 0.5),
        0
    )

    models.model.root.torso.body.topbody.head.upperhead.googly.rightgoogle:setPos(
        math.clamp(-headRotY * rightMult, -0.5, 0),
        math.clamp(headRotX * 0.05, -1, 0.5),
        0
    )

    models.model.root.torso.body.topbody.head.upperhead.googly.leftgoogle.leftgoogle_light:setPos(
        math.clamp(-headRotY / 100, -.5, 0),
        math.clamp(headRotX / 100, -1, 0),
        0
    )

    models.model.root.torso.body.topbody.head.upperhead.googly.rightgoogle.rightgoogle_light:setPos(
        math.clamp(-headRotY / 100, 0, .5),
        math.clamp(headRotX / 100, -1, 0),
        0
    )

end]]

--========== ACTION WHEEL SETUP

local mainPage = action_wheel:newPage()
action_wheel:setPage(mainPage)

--========== MAIN WHEEL

function pings.changeMode()
    movementMode = (movementMode + 1) % 3
    config:save("Mode", movementMode)
end

local googlyMovementTbl = {
    {
        title = "Eye Physics: physics only",
        color = vectors.hexToRGB("FF2222")
    },
    {
        title = "Eye Physics: normal eyes",
        color = vectors.hexToRGB("22FF22")
    },
    {
        title = "Eye Physics: combo",
        color = vectors.hexToRGB("2222FF")
    }
}

local googlyMovementAction = mainPage:newAction()
    :title("Change Eye Mode")
    :item("minecraft:ender_eye")
    :onToggle(pings.changeMode)

function events.TICK()
    googlyMovementAction
        :title(googlyMovementTbl[movementMode+1].title)
        :color(googlyMovementTbl[movementMode+1].color)
end

--========== SKIN WHEEL
local skinColour = config:load("Skin") or 0

function events.tick()
    if skinColour == 0 then
        models.model.root.legs:setPrimaryTexture("CUSTOM", textures["model.blue"])
        models.model.root.torso.body.botbody:setPrimaryTexture("CUSTOM", textures["model.blue"])
        models.model.root.torso.body.topbody.chest:setPrimaryTexture("CUSTOM", textures["model.blue"])
        models.model.root.torso.body.topbody.head.throat:setPrimaryTexture("CUSTOM", textures["model.blue"])
        models.model.root.torso.body.topbody.head.upperhead.mound:setPrimaryTexture("CUSTOM", textures["model.blue"])
        models.model.root.torso.body.topbody.arms:setPrimaryTexture("CUSTOM", textures["model.blue"])
    end
    if skinColour == 1 then
        models.model.root.legs:setPrimaryTexture("CUSTOM", textures["model.lightblue"])
        models.model.root.torso.body.botbody:setPrimaryTexture("CUSTOM", textures["model.lightblue"])
        models.model.root.torso.body.topbody.chest:setPrimaryTexture("CUSTOM", textures["model.lightblue"])
        models.model.root.torso.body.topbody.head.throat:setPrimaryTexture("CUSTOM", textures["model.lightblue"])
        models.model.root.torso.body.topbody.head.upperhead.mound:setPrimaryTexture("CUSTOM", textures["model.lightblue"])
        models.model.root.torso.body.topbody.arms:setPrimaryTexture("CUSTOM", textures["model.lightblue"])
        end
    if skinColour == 2 then
        models.model.root.legs:setPrimaryTexture("CUSTOM", textures["model.green"])
        models.model.root.torso.body.botbody:setPrimaryTexture("CUSTOM", textures["model.green"])
        models.model.root.torso.body.topbody.chest:setPrimaryTexture("CUSTOM", textures["model.green"])
        models.model.root.torso.body.topbody.head.throat:setPrimaryTexture("CUSTOM", textures["model.green"])
        models.model.root.torso.body.topbody.head.upperhead.mound:setPrimaryTexture("CUSTOM", textures["model.green"])
        models.model.root.torso.body.topbody.arms:setPrimaryTexture("CUSTOM", textures["model.green"])
    end
    if skinColour == 3 then
        models.model.root.legs:setPrimaryTexture("CUSTOM", textures["model.yellow"])
        models.model.root.torso.body.botbody:setPrimaryTexture("CUSTOM", textures["model.yellow"])
        models.model.root.torso.body.topbody.chest:setPrimaryTexture("CUSTOM", textures["model.yellow"])
        models.model.root.torso.body.topbody.head.throat:setPrimaryTexture("CUSTOM", textures["model.yellow"])
        models.model.root.torso.body.topbody.head.upperhead.mound:setPrimaryTexture("CUSTOM", textures["model.yellow"])
        models.model.root.torso.body.topbody.arms:setPrimaryTexture("CUSTOM", textures["model.yellow"])
    end
    if skinColour == 4 then
        models.model.root.legs:setPrimaryTexture("CUSTOM", textures["model.grey"])
        models.model.root.torso.body.botbody:setPrimaryTexture("CUSTOM", textures["model.grey"])
        models.model.root.torso.body.topbody.chest:setPrimaryTexture("CUSTOM", textures["model.grey"])
        models.model.root.torso.body.topbody.head.throat:setPrimaryTexture("CUSTOM", textures["model.grey"])
        models.model.root.torso.body.topbody.head.upperhead.mound:setPrimaryTexture("CUSTOM", textures["model.grey"])
        models.model.root.torso.body.topbody.arms:setPrimaryTexture("CUSTOM", textures["model.grey"])
    end
    if skinColour == 5 then
        models.model.root.legs:setPrimaryTexture("CUSTOM", textures["model.purple"])
        models.model.root.torso.body.botbody:setPrimaryTexture("CUSTOM", textures["model.purple"])
        models.model.root.torso.body.topbody.chest:setPrimaryTexture("CUSTOM", textures["model.purple"])
        models.model.root.torso.body.topbody.head.throat:setPrimaryTexture("CUSTOM", textures["model.purple"])
        models.model.root.torso.body.topbody.head.upperhead.mound:setPrimaryTexture("CUSTOM", textures["model.purple"])
        models.model.root.torso.body.topbody.arms:setPrimaryTexture("CUSTOM", textures["model.purple"])
    end
    if skinColour == 6 then
        models.model.root.legs:setPrimaryTexture("CUSTOM", textures["model.repo"])
        models.model.root.torso.body.botbody:setPrimaryTexture("CUSTOM", textures["model.repo"])
        models.model.root.torso.body.topbody.chest:setPrimaryTexture("CUSTOM", textures["model.repo"])
        models.model.root.torso.body.topbody.head.throat:setPrimaryTexture("CUSTOM", textures["model.repo"])
        models.model.root.torso.body.topbody.head.upperhead.mound:setPrimaryTexture("CUSTOM", textures["model.repo"])
        models.model.root.torso.body.topbody.arms:setPrimaryTexture("CUSTOM", textures["model.repo"])
    end
    if skinColour == 7 then
        models.model.root.legs:setPrimaryTexture("CUSTOM", textures["model.magenta"])
        models.model.root.torso.body.botbody:setPrimaryTexture("CUSTOM", textures["model.magenta"])
        models.model.root.torso.body.topbody.chest:setPrimaryTexture("CUSTOM", textures["model.magenta"])
        models.model.root.torso.body.topbody.head.throat:setPrimaryTexture("CUSTOM", textures["model.magenta"])
        models.model.root.torso.body.topbody.head.upperhead.mound:setPrimaryTexture("CUSTOM", textures["model.magenta"])
        models.model.root.torso.body.topbody.arms:setPrimaryTexture("CUSTOM", textures["model.magenta"])
    end
    if skinColour == 8 then
        models.model.root.legs:setPrimaryTexture("CUSTOM", textures["model.pink"])
        models.model.root.torso.body.botbody:setPrimaryTexture("CUSTOM", textures["model.pink"])
        models.model.root.torso.body.topbody.chest:setPrimaryTexture("CUSTOM", textures["model.pink"])
        models.model.root.torso.body.topbody.head.throat:setPrimaryTexture("CUSTOM", textures["model.pink"])
        models.model.root.torso.body.topbody.head.upperhead.mound:setPrimaryTexture("CUSTOM", textures["model.pink"])
        models.model.root.torso.body.topbody.arms:setPrimaryTexture("CUSTOM", textures["model.pink"])
    end

--logTable(textures:getTextures())

end

function pings.changeSkin()
    skinColour = (skinColour + 1) % 9
    config:save("Skin", skinColour)
end

local skinTbl = {
    {
        title = "Skin: Blue",
        color = vectors.hexToRGB("1E6ADE"),
        item = ("blue_shulker_box")
    },
    {
        title = "Skin: Lightblue",
        color = vectors.hexToRGB("38D9E5"),
        item = ("light_blue_shulker_box")
    },
    {
        title = "Skin: Green",
        color = vectors.hexToRGB("30D14B"),
        item = ("lime_shulker_box")
    },
    {
        title = "Skin: Yellow",
        color = vectors.hexToRGB("F9B327"),
        item = ("yellow_shulker_box")
    },
    {
        title = "Skin: Grey",
        color = vectors.hexToRGB("4D4F73"),
        item = ("gray_shulker_box")
    },
    {
        title = "Skin: Purple",
        color = vectors.hexToRGB("8A2FFF"),
        item = ("purple_shulker_box")
    },
    {
        title = "Skin: Red",
        color = vectors.hexToRGB("FF2F2F"),
        item = ("red_shulker_box")
    },
    {
        title = "Skin: Magenta",
        color = vectors.hexToRGB("F92FFF"),
        item = ("magenta_shulker_box")
    },
    {
        title = "Skin: Pink",
        color = vectors.hexToRGB("FF6FED"),
        item = ("pink_shulker_box")
    }

}

local skinChangeAction = mainPage:newAction()
    :title("Change Skin")
    :item("minecraft:red_shulker_box")
    :onToggle(pings.changeSkin)

function events.TICK()
    skinChangeAction
        :title(skinTbl[skinColour+1].title)
        :color(skinTbl[skinColour+1].color)
        :item(skinTbl[skinColour+1].item)
end





