--Config

--Should the swords always be active?
local alwayson = true
--If so, what should the default state be when not holding an item?
local defaultState = "minecraft:iron_sword"
--Should the swords switch when holding a different sword?
local switchStates = true
--Should the held sword be shown?
local showHeldSword = false
--Should the swords always attack when swinging your arm, or just when swinging a sword?
local alwaysAttack = false
--Should the swords emit magic particles
local doParticles = true

local radius = 32
local swordCount = 6
local arc = 180
local length = 64
local particleRate = 80
local attackMode = true

local sword = models.model.root
local swords = { sword }

--The rest of the Script

local AdaptativeValue = {}
AdaptativeValue.__index = AdaptativeValue

function AdaptativeValue:tick()
    self.current = self.current + (self.target - self.current) * self.updateFactor
end

function AdaptativeValue:new(...)
    local obj = {}
    setmetatable(obj, self)
    obj:init(...)
    return obj
end

function AdaptativeValue:init()
    self.current = 0
    self.target = 0
    self.updateFactor = 0.1
end

local swordSpin = AdaptativeValue:new()
local attackModeX = AdaptativeValue:new()
local attackModeY = AdaptativeValue:new()
local attack = AdaptativeValue:new()
local randSword = nil

local itemTasks = {}

local function getSword(i)
  if swords[i] == nil then
    swords[i] = sword:copy("sword" .. i):moveTo(models.model)
  end
  return swords[i]
end

local function setSwordCount(count)
  count = math.clamp(count, 0, 64)
  if count ~= swordCount then
      for i, s in pairs(swords) do
          s:setVisible(i <= count)
      end
  end

  swordCount = count
end

function pings.swordCount(count)
  setSwordCount(count)
end

function pings.radius(rad)
  radius = rad
end

function pings.arc(degrees)
  arc = degrees
end

function pings.length(distance)
  length = distance
end

function pings.particles(state)
  doParticles = state
end

function pings.rate(rate)
  particleRate = rate
end

function pings.attackMode(state)
  attackMode = state
end

function pings.alwaysOn(state)
  alwayson = state
end

function pings.showHeldSword(state)
  showHeldSword = state
end

--tick event, called 20 times per second
function events.tick()
  if doParticles == true then
    for i = 1, swordCount do
      if math.random(particleRate) == 1 then
        particles:newParticle("minecraft:witch", (getSword(i):partToWorldMatrix()*vec(0,0,0,1)).xyz)
      end
    end
  end
end

--render event, called every time your avatar is rendered
function events.render(delta)
  local heldSword = "minecraft:air"
  if player:getHeldItem(false).id:find("_sword") and switchStates == true then
    if not renderer:isFirstPerson() and showHeldSword == false then
      vanilla_model.HELD_ITEMS:setVisible(false)
    else
      vanilla_model.HELD_ITEMS:setVisible(true)
    end
    heldSword = player:getHeldItem(false).id
  elseif alwayson == true then
    vanilla_model.HELD_ITEMS:setVisible(true)
    heldSword = defaultState
  end

  swordSpin:tick()
  attackModeX:tick()
  attackModeY:tick()
  attack:tick()

  swordSpin.updateFactor = 0.05

  for i = 1, swordCount do
    local ang = (i - 0.5) / swordCount * arc - (arc - 180) / 2
    local s = getSword(i)
    s:setPos((math.cos(math.rad(ang)) * (radius - attack.current * (radius / length))), (math.sin(math.rad(ang)) * (radius - attack.current * (radius / length)) + math.sin((world.getTime()+delta)*0.1)), -attack.current)
     :setRot(0, 0, ang)

    itemTasks[i] = s:newItem("task" .. i)
    itemTasks[i]:setItem(heldSword)
                :setRot(0, 0, -135)

    if player:getHeldItem(false).id:find("_sword") then
      if player:getSwingArm() == "MAIN_HAND" then
        attack.target = length
        attackModeX.target = 90
        attackModeY.target = 135
      else
        attack.target = 0
        if attackMode == false then
        attackModeX.target = 0
        attackModeY.target = 0
        end
      end

      if attackMode == true then
        attackModeX.target = 90
        attackModeY.target = 135
      end

    else
      attackModeX.target = 0
      attackModeY.target = 0
    end

    if math.random(2000) == 1 and randSword == nil then
      swordSpin.target = 360
      randSword = getSword(i)
    end
    
    s:setOffsetRot(attackModeX.current, attackModeY.current, 0)
  end

  if randSword ~= nil then
    randSword:setOffsetRot(swordSpin.current + attackModeX.current, attackModeY.current, 0)
  end

  if swordSpin.current >= 359 then
    randSword = nil
    swordSpin.target = 0
    swordSpin.current = 0
  end
end

-- Action wheel
local swordPage = action_wheel:newPage()
local swordAnimPage = action_wheel:newPage()

local countAct = swordPage:newAction()
                             :setItem("minecraft:iron_sword")
                             :setTitle("Swords: 6")
countAct:setOnLeftClick(
function()
        if swordCount == 6 then return end
        setSwordCount(6)
        countAct:setTitle("Swords: 6")
    end
)
countAct:setOnScroll(
    function(dir)
        if (dir < 0 and swordCount == 0) or (dir > 0 and swordCount == 64) then return end
        setSwordCount(swordCount + dir)
        countAct:setTitle("Swords: " .. swordCount)
        if not pinged then
          pings.swordCount(swordCount)
          pinged = true
      end
    end
)

local radAct = swordPage:newAction()
                           :setItem("minecraft:snowball")
                           :setTitle("Radius: 32")
radAct:setOnLeftClick(
function()
        if radius == 32 then return end
        radius = 32
        pings.radius(32)
        radAct:setTitle("Radius: 32")
    end
)
radAct:setOnScroll(
    function(dir)
        if dir < 0 and radius == 1 then return end
        radius = radius + dir
        radAct:setTitle("Radius: " .. radius)
        if not pinged then
            pings.radius(radius)
            pinged = true
        end
    end
)

local arcAct = swordPage:newAction()
                           :setItem("minecraft:clay_ball")
                           :setTitle("Arc: 180 degrees")
arcAct:setOnLeftClick(
  function()
    if arc == 180 then return end
    arc = 180
    pings.arc(180)
    arcAct:setTitle("Arc: 180 degrees")
  end
)
arcAct:setOnScroll(
  function(dir)
    if (dir < 0 and arc == 0) or (dir > 0 and arc == 360) then return end
    arc = arc + dir * 5
    arcAct:setTitle("Arc: " .. arc .. " degrees")
    if not pinged then
        pings.arc(arc)
        pinged = true
    end
  end
)

swordPage:newAction():title("Toggle Particles"):item("minecraft:enchanted_book"):toggleItem("minecraft:book"):onToggle(function(state)
  pings.particles(not state)
end)

local particleAct = swordPage:newAction()
                           :setItem("minecraft:brewing_stand")
                           :setTitle("Particle Rate: 80")
particleAct:setOnLeftClick(
function()
        if particleRate == 80 then return end
        particleRate = 80
        pings.rate(80)
        particleAct:setTitle("Particle Rate: 80")
    end
)
particleAct:setOnScroll(
    function(dir)
        if dir < 0 and particleRate == 1 then return end
        particleRate = particleRate + dir
        particleAct:setTitle("Particle Rate: " .. particleRate)
        if not pinged then
            pings.rate(particleRate)
            pinged = true
        end
    end
)

swordPage:newAction():title("Animation Settings")
  :setItem("minecraft:jukebox")
	:onLeftClick(function()
		action_wheel:setPage(swordAnimPage)
	end)

swordAnimPage:newAction():title("Back")
  :setItem("minecraft:barrier")
	:onLeftClick(function()
		action_wheel:setPage(swordPage)
	end)
local lengthAct = swordAnimPage:newAction()
                           :setItem("minecraft:stick")
                           :setTitle("Attack Distance: 64")
lengthAct:setOnLeftClick(
  function()
    if length == 64 then return end
    length = 64
    pings.length(64)
    lengthAct:setTitle("Attack Distance: 64")
  end
)
lengthAct:setOnScroll(
  function(dir)
    if (dir < 0 and length == 1) or (dir > 0 and length == 640) then return end
    length = length + dir
    lengthAct:setTitle("Attack Distance: " .. length)
    if not pinged then
        pings.length(length)
        pinged = true
    end
  end
)

swordAnimPage:newAction():title("Toggle Attacking Mode"):item("minecraft:diamond_sword"):toggleItem("minecraft:wooden_sword"):onToggle(function(state)
  pings.attackMode(not state)
end)

swordAnimPage:newAction():title("Toggle Always Visible"):item("minecraft:potion"):toggleItem("minecraft:glass_bottle"):onToggle(function(state)
  pings.alwaysOn(not state)
end)

swordAnimPage:newAction():title("Toggle Sword in Hand"):item("minecraft:wooden_sword"):toggleItem("minecraft:golden_sword"):onToggle(function(state)
  pings.showHeldSword(state)
end)

action_wheel:setPage(swordPage)