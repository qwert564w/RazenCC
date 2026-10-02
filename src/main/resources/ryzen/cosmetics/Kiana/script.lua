
vanilla_model.HELMET_ITEM:setVisible(true)
vanilla_model.CAPE:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)
vanilla_model.PLAYER:setVisible(false)
vanilla_model.ARMOR:setVisible(false)


--function events.render()
--  logTable(animations:getPlaying())
--end



--- Set Up ---



--Requirements
local anims = require('JimmyAnims')
anims(animations.Kiana)
local batterUp = require("BatterUp")
local anim = animations.Kiana
local squapi=require("SquAPI")
local SwingingPhysics = require("swinging_physics")
--



-- Custom Trident --
function events.item_render(item)
  if item.id:find("trident") then
      return models.Trident.ItemTrident
  end
end
--Outline
local outline = require "outline"
outline(models.Trident.ItemTrident.MainCrystal,{color=vec(1,1,1)})
--When Flying/Swimming stop hold anim
function events.tick()
  if animations.Kiana.fly:getPlayState() == "PLAYING"
  then
    animations.Kiana.holdR:stop()
    animations.Kiana.holdL:stop()
  end
  if animations.Kiana.swim:getPlayState() == "PLAYING"
  then
    animations.Kiana.holdR:stop()
    animations.Kiana.holdL:stop()
  end
end
--



--Animations
--(JimmyAnims)
animations.Kiana.idle:play()
animations.Kiana.blink:play()
animations.Trident.Trident:play()
animations.Kiana.idle:setBlendTime(10)
animations.Kiana.fly:setBlendTime(2)
animations.Kiana.flywalk:setBlendTime(2)
animations.Kiana.flywalkback:setBlendTime(2)
animations.Kiana.ID__axe_mineR:setPriority(1)
animations.Kiana.ID_pickaxe_mineR:setPriority(1)
--



--Action Chains (Under Development)
--(BatterUp)
--This next block is so the holding animations are stopped while the attacking animations are playing
function events.tick()
  if animations.Kiana.swordAttack1:getPlayState() == "PLAYING"
  then
    animations.Kiana.holdR:stop()
    animations.Kiana.holdL:stop()
  end
  --
  if animations.Kiana.swordAttack2:getPlayState() == "PLAYING"
  then
    animations.Kiana.holdR:stop()
    animations.Kiana.holdL:stop()
  end
  --
  if animations.Kiana.swordAttack3:getPlayState() == "PLAYING"
  then
    animations.Kiana.holdR:stop()
    animations.Kiana.holdL:stop()
  end
  --
  if animations.Kiana.tridentAttack1:getPlayState() == "PLAYING"
  then
    animations.Kiana.holdR:stop()
    animations.Kiana.holdL:stop()
  end
  --
  if animations.Kiana.tridentAttack2:getPlayState() == "PLAYING"
  then
    animations.Kiana.holdR:stop()
    animations.Kiana.holdL:stop()
  end
end
--Fist fight
local chained = {
  anim.attack1,
  anim.attack2,
}
batterUp:addChainedSwings(chained,"right","air","attack",true,20)

--Sword fight
local anim = animations.Kiana
local chained = {
  anim.swordAttack1,
  anim.swordAttack2,
  anim.swordAttack3,
}
batterUp:addChainedSwings(chained,"right","sword","attack",true,20)

--Trident fight
local anim = animations.Kiana
local chained = {
  anim.swordAttack1,
  anim.tridentAttack1,
  anim.tridentAttack2,
}
batterUp:addChainedSwings(chained,"right","trident","attack",true,20)
--



--Horse riding
--(JimmyAnims)
function events.tick()
  animations.Kiana.Horse:setPlaying(player:getVehicle() and player:getVehicle():getType():find("horse"))
  animations.Kiana.Horse:setPriority(1)
  if animations.Kiana.Horse:getPlayState() == "PLAYING"
  then
    animations.Kiana.idle:play()
  end
end
--Lama riding
function events.tick()
  animations.Kiana.Horse:setPlaying(player:getVehicle() and player:getVehicle():getType():find("lama"))
  animations.Kiana.Horse:setPriority(1)
  if animations.Kiana.Horse:getPlayState() == "PLAYING"
  then
    animations.Kiana.idle:play()
  end
end
--



--Squishy API smooth head and moving eye script
--(SquAPI)
squapi.smoothHead(models.Kiana.root.head, true) --Smooth head
squapi.eye(models.Kiana.root.head.Eyes.left_eye.left_pupil, .25, 1.25, .1, .1) --Moving eyes
squapi.eye(models.Kiana.root.head.Eyes.right_eye.right_pupil, 1.25, .25, .1, .1) --Moving eyes



-- Hair Physics --
--(swinging_physics)
local swingOnHead = SwingingPhysics.swingOnHead

swingOnHead(models.Kiana.root.head.hair.extrabangs.bangsleft, 50, {-90, 30, 0, 0, -30, 0})
swingOnHead(models.Kiana.root.head.hair.extrabangs.bangsright, 50, {-90, 30, 0, 0, 0, 30})

swingOnHead(models.Kiana.root.head.hair.bangs.MiddleStrand, 0, {-1, 20, 0, 0, -15, 15})
  swingOnHead(models.Kiana.root.head.hair.bangs.RightStrand, -45, {-1, 15, 0, 0, -10, 10})
  swingOnHead(models.Kiana.root.head.hair.bangs.LeftStrand, 45, {-1, 15, 0, 0, -10, 10})
--



-- Glowing features in the dark (Trident is always glowing)
function events.tick()
  models.Kiana:setSecondaryColor(math.map(
      world.getLightLevel(player:getPos()), 
      0, 15, 
      1, 0)
  )
end

local function updateShiny() -- :3
  local time = world.getTimeOfDay()
  local isNight = time >= 12000 and time <=24000

end

events.TICK:register(updateShiny)
--





return animsTable