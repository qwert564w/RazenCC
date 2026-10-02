-- Auto generated script file --

--hide vanilla model
vanilla_model.PLAYER:setVisible(false)

--hide vanilla armor model
vanilla_model.ARMOR:setVisible(false)


require("GSAnimBlend")
local anims = require("EZAnims")
local sleepy = anims:addBBModel(animations.model)



function events.tick()
    local randomtime = math.random(-300,300)

    if world.getTime() % randomtime == 0 then
        animations.model.blink:play()
    end
end


--SQUAPI--

local squapi = require("SquAPI")

squapi.eye:new(
    models.model.root.Head.eyes.eye_L,  --the eye element 
    nil,  --(0.25) left distance
    0.75,  --(1.25) right distance
    nil,  --(0.5) up distance
    nil   --(0.5) down distance
)

squapi.eye:new(
    models.model.root.Head.eyes.eye_R,  --the eye element 
    nil,  --(0.25) left distance
    0.75,  --(1.25) right distance
    nil,  --(0.5) up distance
    nil   --(0.5) down distance
)

squapi.bounceWalk:new(
    models.model,    --model
    0.5     --(1) bounceMultiplier
)


--ACTION WHEEL

local mainPage = action_wheel:newPage()
action_wheel:setPage(mainPage)


function pings.judging(state)
  animations.model.judging:setPlaying(state)
end

local newAction = mainPage:newAction()
    :title("Lay Down")
    :toggleTitle("Stop")
    :hoverColor(0, 1, 1)
    :item("minecraft:diamond_sword")
    :toggleItem("minecraft:coal")
    :onToggle(function(state)
    pings.judging(state)
  end)


function pings.chilling(state)
  animations.model.chilling:setPlaying(state)
end

local newAction = mainPage:newAction()
    :title("Lay Down")
    :toggleTitle("Stop")
    :hoverColor(0, 1, 1)
    :item("minecraft:salmon")
    :toggleItem("minecraft:coal")
    :onToggle(function(state)
    pings.chilling(state)
  end)
