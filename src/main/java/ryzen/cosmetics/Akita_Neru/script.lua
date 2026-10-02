local skirtPhysics = require("skirt_physics")
--local swingPirate = require("swingpirate")
local squapi = require("SquAPI_MODIFIED")
local physBone = require('physBoneAPI')

local namejson
local nameoverridejson

--123yeah_boi321's SQUASHSCRIPT
local heads = {}
local skull_model = models.model.Skull2

--hide vanilla model
vanilla_model.PLAYER:setVisible(false)

--hide vanilla armor model
vanilla_model.ARMOR:setVisible(false)

--remove parents
models.model.root.LeftLeg:setParentType("none")
models.model.root.RightLeg:setParentType("none")

--skirt physics
skirtPhysics.new(models.model.root.Body.Skirt, 25, 5, 0.75, vec(0,1,-0.6))

--[[leftTail = swingPirate.SwingPhysics(models.model.root.Head.LeftTailMount.LeftTail, "default_head", {90, -90, 90, -90, 10, -90}, {
			resistance = 0.55,
			weight = 1.6,
			bounce = 0,
			offsetStick = 0.2
		})
		
rightTail = swingPirate.SwingPhysics(models.model.root.Head.RightTailMount.RightTail, "default_head", {90, -90, 90, -90, 90, -10}, {
			resistance = 0.55,
			weight = 1.6,
			bounce = 0,
			offsetStick = 0.2
		})
		
leftTail2 = swingPirate.SwingPhysics(models.model.root.Head.LeftTailMount.LeftTail.LeftTailOffset.LeftTail2, "default_head", {25, -25, 90, -90, 5, -25}, {
			resistance = 0.75,
			weight = 2,
			bounce = 0,
			offsetStick = 0.4
		}, leftTail)
		
rightTail2 = swingPirate.SwingPhysics(models.model.root.Head.RightTailMount.RightTail.RightTailOffset.RightTail2, "default_head", {25, -25, 90, -90, 5, -25}, {
			resistance = 0.75,
			weight = 2,
			bounce = 0,
			offsetStick = 0.4
		}, rightTail)
		
leftTail3 = swingPirate.SwingPhysics(models.model.root.Head.LeftTailMount.LeftTail.LeftTailOffset.LeftTail2.LeftTail3, "default_head", {25, -25, 90, -90, 5, -25}, {
			resistance = 0.65,
			weight = 1,
			bounce = 0,
			offsetStick = 0.6
		}, leftTail)
		
rightTail3 = swingPirate.SwingPhysics(models.model.root.Head.RightTailMount.RightTail.RightTailOffset.RightTail2.RightTail3, "default_head", {25, -25, 90, -90, 5, -25}, {
			resistance = 0.65,
			weight = 1,
			bounce = 0,
			offsetStick = 0.6
		}, rightTail)
		
tie = swingPirate.SwingPhysics(models.model.root.Body.Tie, "default_body", {90, -0.9, 90, -90, 10, -10}, {
			resistance = 0.6,
			weight = 1.2,
			bounce = 0,
			offsetStick = 0.9
		})]]

squapi.randimation:new(
    animations.model.Blink,    --animation
    200,    --(200) chanceRange
    true     --(false) isBlink
)

squapi.eye:new(
	models.model.root.Head.Eyes.LEye, --element
	nil, --(.25)leftdistance
	nil, --(1.25)rightdistance
	nil, --(.5)updistance
	nil --(.5)downdistance
)

squapi.eye:new(
	models.model.root.Head.Eyes.REye, --element
	1.25, --(.25)leftdistance
	0.25, --(1.25)rightdistance
	nil, --(.5)updistance
	nil --(.5)downdistance
)

--generate gradient names
--yoinked and modified code from riftlight
local text = "Akita Neru"
local pText = {}
for char in text:gmatch("[\x00-\x7F\xC2-\xF4][\x80-\xBF]*") do
	table.insert(pText, char)
end
local color1 = vectors.hexToRGB("#eefaf0")--vec(238/256, 250/256, 240/256)
local color2 = vectors.hexToRGB("#ffd900")--vec(9/256, 63/256, 95/256)
local json = {}
for i, c in ipairs(pText) do
	  table.insert(json, {
			text = c,
			color = "#" .. vectors.rgbToHex(math.lerp(color1, color2, (i - 1) / #pText)),
	  })
end

nameoverridejson = toJson(json)

text = avatar:getEntityName()
pText = {}
json = {}
for char in text:gmatch("[\x00-\x7F\xC2-\xF4][\x80-\xBF]*") do
	table.insert(pText, char)
end
for i, c in ipairs(pText) do
	  table.insert(json, {
			text = c,
			color = "#" .. vectors.rgbToHex(math.lerp(color1, color2, (i - 1) / #pText)),
	  })
end

namejson = toJson(json)

--action wheel

local mainPage = action_wheel:newPage()
action_wheel:setPage(mainPage)

function pings.name_override(state)
	--optimisation that saves litearly 1 bit of your disc space (you're welcome)
	config:save("name_override", state or nil)
	
	nameplate.ALL:setText(state and nameoverridejson or namejson)
end
	
local action_name = mainPage:newAction()
    :title("Override Name\n§7Name will show as: \"Akita Neru\"")
    :toggleTitle("Default Name\n§7Name will show as: \"" .. avatar:getEntityName() .. "\"")
    :item("minecraft:iron_block")
    :toggleItem("minecraft:grass_block")
	:setToggleColor(0,0,0)
    :setOnToggle(pings.name_override) --huh why is this also sending a nil argument

--init pings (loading and syncing from config)
pings.name_override(config:load("name_override"))
action_name:setToggled(config:load("name_override"))

local leftTail
local leftTail2
local leftTail3

local rightTail
local rightTail2
local rightTail3

local tie

function events.entity_init()
	physBone:setPreset("mikuHair",nil,2.5,-9.81*0.8,1.5,nil,nil,nil,nil,vec(1,1,1))

	leftTail = models.model.root.Head.LeftTailMount.LeftTail:newPhysBone("mikuHair")
	leftTail2 = models.model.root.Head.LeftTailMount.LeftTail.LeftTailOffset.LeftTail2:newPhysBone("mikuHair")
	leftTail3 = models.model.root.Head.LeftTailMount.LeftTail.LeftTailOffset.LeftTail2.LeftTail3:newPhysBone("mikuHair")

	tie = models.model.root.Body.Tie:newPhysBone("physBone")
end

local windflow = 0

function events.tick()
	--swingPirate.AnchorHead(leftTail)
	--swingPirate.AnchorHead(rightTail)

	animations.model.EyesClosed:setPlaying(player:getPose() == "SLEEPING")
	
	--crappy wind
	if player:isLoaded() then
		if world:getTime()%20 == 0 then
			--recalculate windflow
			windflow = 0
			
			if raycast:block(player:getPos(), player:getPos()+vec(0,10,0)):getID() == "minecraft:air" then --up
				windflow = windflow+1
			end
			if raycast:block(player:getPos()+vec(0,1,0), player:getPos()+vec(5,0,0)):getID() == "minecraft:air" then --east
				windflow = windflow+1
			end
			if raycast:block(player:getPos()+vec(0,1,0), player:getPos()+vec(5,0,5)):getID() == "minecraft:air" then --southest
				windflow = windflow+1
			end
			if raycast:block(player:getPos()+vec(0,1,0), player:getPos()+vec(0,0,5)):getID() == "minecraft:air" then --south
				windflow = windflow+1
			end
			if raycast:block(player:getPos()+vec(0,1,0), player:getPos()+vec(-5,0,5)):getID() == "minecraft:air" then --southwest
				windflow = windflow+1
			end
			if raycast:block(player:getPos()+vec(0,1,0), player:getPos()+vec(-5,0,0)):getID() == "minecraft:air" then --west
				windflow = windflow+1
			end
			if raycast:block(player:getPos()+vec(0,1,0), player:getPos()+vec(-5,0,-5)):getID() == "minecraft:air" then --northwest
				windflow = windflow+1
			end
			if raycast:block(player:getPos()+vec(0,1,0), player:getPos()+vec(0,0,-5)):getID() == "minecraft:air" then --north
				windflow = windflow+1
			end
			if raycast:block(player:getPos()+vec(0,1,0), player:getPos()+vec(5,0,-5)):getID() == "minecraft:air" then --northeast
				windflow = windflow+1
			end
		end
		
		if windflow ~= 0 and player:getPos().y > 0 then
			windflow = windflow or 0
		
			local tick = world:getTime()
		
			--noise-ish waveform
			local wind = vec(
				math.sin(tick/23)*0.23+math.sin(tick/54)*0.34+math.sin(tick/1238)*0.78,
				math.sin((tick+6)/17)*0.18+math.sin((tick-31)/72)*0.26+math.sin((tick-1278)/1108)*0.63
				)*0.5
				
			local wind_strength = ((player:getPos().y^0.9-40)*0.08+1)*(windflow/9)*(1+world.getRainGradient()*0.2)*4
			
			if world.isThundering() then
				wind_strength = wind_strength*1.2
			end
			
			local theta = math.rad(player:getRot().y);

			local cs = math.cos(-theta);
			local sn = math.sin(-theta);
			
			local out = vec(
				wind.x*cs-wind.y*sn,
				0,
				wind.x*sn+wind.y*cs)*wind_strength
		
			--print(out*wind_strength)
		
			leftTail:setForce(out)
			leftTail2:setForce(out)
			leftTail3:setForce(out)
			
			return out
		else
			local out = vec(0,0,0)
			
			leftTail:setForce(out)
			leftTail2:setForce(out)
			leftTail3:setForce(out)
			
		end
	end
end

function events.item_render(item)
	if item.id:find("sword") then
		return models.model.ItemLeek
	end
end

function events.render()
	models.model.root.LeftLeg:setRot(vanilla_model.LEFT_LEG:getOriginRot()*0.75):setPos(vanilla_model.LEFT_LEG:getOriginPos())
	models.model.root.RightLeg:setRot(vanilla_model.RIGHT_LEG:getOriginRot()*0.75):setPos(vanilla_model.RIGHT_LEG:getOriginPos())
end

local DURATION = 10

--123yeah_boi321's SQUASHSCRIPT
function events.world_tick()
	local count = 0
    for i,v in pairs(heads) do 
        count = count + 1
        v.stretch = v.stretch + 1
        if v.stretch >= DURATION then heads[i] = nil end
    end
	
end

--easing modified from GNTweenLib

function events.SKULL_RENDER(delta,block,item,entity,type)
    if type == "BLOCK" then
        for name,player in pairs(world.getPlayers()) do
            local target_block,hit_pos,side = player:getTargetedBlock()
            if player:getSwingTime() == 2 and target_block:getPos() == block:getPos() then
                sounds:playSound("entity.axolotl.idle_air",block:getPos(),2,2)
                heads[tostring(block:getPos())] = {stretch = 0}
            end
        end
        local head = heads[tostring(block:getPos())]
        if head then
            local stretch = outElastic(head.stretch+delta, 0.1, -0.1, DURATION, 1, 6)
            if block.id:find("wall") then
				stretch = stretch/2
                skull_model:setScale(1+stretch,1+stretch,1-stretch)
                skull_model:setPos(0,-stretch*4,stretch*4)
            else
                skull_model:setScale(1+stretch,1-stretch,1+stretch)
            end
        else
            skull_model:setScale(1)
			skull_model:setPos(0,0,0)
        end
    else
        skull_model:setScale(1)
		skull_model:setPos(0,0,0)	
    end
end

-- time, begin, change, duration, aplitude, period

function outElastic(t, b, c, d, a, p)
  if t == 0 then return b end

  t = t / d

  if t == 1 then return b + c end

  if not p then p = d * 0.3 end

  local s

  if not a or a < math.abs(c) then
    a = c
    s = p / 4
  else
    s = p / (2 * math.pi) * math.asin(c/a)
  end

  return a * math.pow(2, -10 * t) * math.sin((t * d - s) * (2 * math.pi) / p) + c + b
end