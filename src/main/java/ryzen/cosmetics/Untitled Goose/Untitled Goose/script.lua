-- Auto generated script file --

--hide vanilla armor model
vanilla_model.ARMOR:setVisible(false)

--hide vanilla cape model
vanilla_model.CAPE:setVisible(false)

--hide vanilla elytra model
vanilla_model.ELYTRA:setVisible(false)

vanilla_model.PLAYER:setVisible(false)

require("GSAnimBlend")
local anims = require("EZAnims")
local squapi=require("SquAPI")


function events.tick()
	if player:getPose() == "CROUCHING" then
	models.goose.root:setPos(0,2,0)
	else models.goose.root:setPos(0,0,0)
	end
end

function events.tick()
	if player:getPose() == "SWIMMING" then
	models.goose.root:setRot(90,0,0)
	else models.goose.root:setRot(0,0,0)
	end
end

myTail = {
	models.goose.root.gorso.waggler1,
  models.goose.root.gorso.waggler1.waggler2
}

squapi.tail:new(myTail,
    4,    --(15) idleXMovement
    5,    --(5) idleYMovement
    1.3,    --(1.2) idleXSpeed
    1,    --(2) idleYSpeed
    2.1,    --(2) bendStrength
    0,    --(0) velocityPush
    0,    --(0) initialMovementOffset
    1,    --(1) offsetBetweenSegments
    .005,    --(.005) stiffness
    0.9,    --(.9) bounce
    90,    --(90) flyingOffset
    5,    --(-90) downLimit
    10     --(45) upLimit
)

squapi.smoothHead:new(
    {
        models.goose.root.gorso.gneck1,
        models.goose.root.gorso.gneck1.gneck2,
        models.goose.root.gorso.gneck1.gneck2.head
      
    }
)
--blend things
animations.goose.KBwings:setBlendTime(5)
animations.goose.foldwings:setBlendTime(1)
animations.goose.random2:setBlendTime(1)
animations.goose.Esit:setBlendTime(5)
animations.goose.Esleep:setBlendTime(5)

function events.render()
  if animations.goose.KBwings:isPlaying(false) then
    animations.goose.foldwings:setPlaying(false)
  else
    animations.goose.foldwings:setPlaying(animations.goose.idling:isPlaying() or animations.goose.walking:isPlaying() or animations.goose.walkingback:isPlaying() or animations.goose.crouching:isPlaying() or animations.goose.sprinting:isPlaying()or animations.goose.swimming:isPlaying()or animations.goose.crouchwalk:isPlaying() or animations.goose.crouchwalkback:isPlaying()or animations.goose.jumpingup:isPlaying() or animations.goose.jumpingdown:isPlaying())
  end
    
end

--honnk
function pings.honk()
    sounds:playSound("Honk", player:getPos())
    animations.goose.KBhonk:play()
    
end


local honkKey = keybinds:newKeybind("Honk", "key.keyboard.g")
honkKey.press = pings.honk

--wings

local  wingskeyState = false
function pings.wingsPing(wingsState)
    wingskeyState = wingsState
end
local flapKey = keybinds:newKeybind("Wings", "key.keyboard.r", false)
flapKey.press = function()
    pings.wingsPing(true)
end
flapKey.release = function()
    pings.wingsPing(false)
end

function events.tick()
    animations.goose.KBwings:setPlaying(wingskeyState)
end

--tailwaggles


squapi.randimation:new(
    animations.goose.random2,    --animation
    230,    --(200) chanceRange
    false     --(false) isBlink
)
--stops emotes when movingg
function events.tick()
    if player:getVelocity():length() > 0 then
        animations.goose.Esit:stop()
        animations.goose.Esleep:stop()
    end
end

--action wheel stufff
local mainPage = action_wheel:newPage()


action_wheel:setPage(mainPage)
--emote sit
function pings.emoteSit()
    animations.goose.Esit:play()
end

local action = mainPage:newAction()
   :title("Sit")
   :item("minecraft:white_wool")
   :hoverColor(1,0,1)
  :onLeftClick(pings.emoteSit)

  --emote sleep

  function pings.emoteSleep()
    animations.goose.Esleep:play()
end

local action = mainPage:newAction()
   :title("Sleep")
   :item("minecraft:red_bed")
   :hoverColor(1,0,1)
  :onLeftClick(pings.emoteSleep)

--toggleable goodies
--TO DO:
--uhhh figure out the second page thing
--Bell, Bow (blue on other goose), Crown, Pipe, Knife