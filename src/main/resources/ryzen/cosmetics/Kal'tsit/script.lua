
vanilla_model.PLAYER:setVisible(false)
vanilla_model.ARMOR:setVisible(false)
vanilla_model.HELMET_ITEM:setVisible(false)
vanilla_model.CAPE:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)

--=====================================================================================================================--
-- API SETTINGS

require("GSAnimBlend")

local squapi = require("SquAPI")

local anims = require("JimmyAnims")
anims(animations.model)

local SwingingPhysics = require("swinging_physics")
local swingOnHead = SwingingPhysics.swingOnHead
local swingOnBody = SwingingPhysics.swingOnBody

--==========-- SQUAPI
-- Eyes
    -- eye range \\ squapi.eye:new(element, leftDistance, rightDistance, upDistance, downDistance, switchValues)
        squapi.eye:new(models.model.root.Torso.Head.eyes.lefteye, 1.3, 0.7, 1, 1)
        squapi.eye:new(models.model.root.Torso.Head.eyes.lefteye.lefthighlight, -0.9, 1.7, 0, .3)
        squapi.eye:new(models.model.root.Torso.Head.eyes.righteye, 0.7, 1.3, 1, 1)
        squapi.eye:new(models.model.root.Torso.Head.eyes.righteye.righthighlight, 1.7, -0.9, 0, .3)

    -- blink \\ squapi.randimation:new(animation, chanceRange, isBlink)
    	squapi.randimation:new(animations.model.earflick)
        squapi.randimation:new(animations.model.earflickR)
        squapi.randimation:new(animations.model.earflickL)
        squapi.randimation:new(animations.model.blink)
        	animations.model.blink:setSpeed(0.8)

-- Ears
	-- squapi.ear:new(leftEar, rightEar, rangeMultiplier, horizontalEars, bendStrength, doEarFlick, earFlickChance, earStiffness, earBounce)
		squapi.ear:new(models.model.root.Torso.Head.ears.leftear, models.model.root.Torso.Head.ears.rightear, 0.5, horizontalEars, bendStrength, false, earFlickChance, earStiffness, earBounce)


-- Body
	-- smooth torso rotation \\ squapi.smoothHead:new({element1, element2}, strength, tilt, speed, keepOriginalHeadPos)
			-- kal
		--squapi.smoothHead:new({models.model.root.Head}, strength, tilt, speed, keepOriginalHeadPos)
		squapi.smoothHead:new(
		    {
		        models.model.root.Torso.Head,
		        models.model.root.Torso --element(you can have multiple elements in a table)
		    },
		    {
		        0.7, 0.3 --(1) strength(you can have multiple strengths in a table)
		    },
		    nil,    --(0.1) tilt
		    nil,    --(1) speed
		    nil     --(true) keepOriginalHeadPos
)

	-- legs rotation \\ squapi.leg:new(element, strength, isRight(false), keepPosition)
		squapi.leg:new(models.model.root.LeftLeg, 0.5, isRight, keepPosition)
		squapi.leg:new(models.model.root.RightLeg, 0.5, true, keepPosition)

	-- arms rotation
		squapi.arm:new(models.model.root.Torso.LeftArm, 0.7, isRight, keepPosition)
		squapi.arm:new(models.model.root.Torso.RightArm, 0.7, true, keepPosition)

	-- tail
		tailSegmentList = {
			models.model.root.Torso.Body.tail,
			models.model.root.Torso.Body.tail.tail2,
			models.model.root.Torso.Body.tail.tail2.tail3,
			models.model.root.Torso.Body.tail.tail2.tail3.tail4,
		}
		squapi.tail:new(tailSegmentList, idleXMovement, idleYMovement, idleXSpeed, idleYSpeed, bendStrength, 2, initialMovementOffset, offsetBetweenSegments, stiffness, bounce, flyingOffset, downLimit, 90)
	
-- Monst3r
	-- random animations \\ squapi.randimation:new(animation, chanceRange, isBlink)
		squapi.randimation:new(animations.model.happimons)
		squapi.randimation:new(animations.model.monsspin)
		squapi.randimation:new(animations.model.monsupdownR, 400)
		squapi.randimation:new(animations.model.monsupdownL, 400)
		squapi.randimation:new(animations.model.monsblink)

	-- hover \\ squapi.hoverPoint:new(element, springStrength, mass, resistance, rotationSpeed, doCollisions)
		local point = squapi.hoverPoint:new(
		    models.model.monst3r,    --element
			    0.1,   				 --(0.2) springStrength
			    10,    			     --(5) mass
			    2,   				 --(1) resistance
			    0.05, 		  	     --(0.05) rotationSpeed
		    	false    		     --(false) doCollisions
		)

		local speed = 0
			function events.tick()
			    animations.model.idleMonsBob:setSpeed(1 + math.min(point.vel:length()*3, 3))
			end
	-- head torso smoove
		--squapi.smoothHead:new({models.model.monst3r.minimons.minihead, models.model.monst3r.minimons.minibody}, {0.2, 0.2}, tilt, 0.1, keepOriginalHeadPos)
		-- unfortunately this messes up head/body positions during actions like crouching so its off by default


--=====================================================================================================================--
-- PHYSICS
-- swingOnBody(part, dir, limits, root, depth)
    -- dir = Returns movement angle relative to look direction (2D top down view, ignores Y)
        -- 0   : forward
        -- 45  : left forward
        -- 90  : left
        -- 135 : left backwards
        -- 180 : backwards
        -- -135: right backwards
        -- -90 : right
        -- -45 : right forward
    -- limits (min, max): {x, x, y, y, z, z}

-- fronthair
	swingOnHead(models.model.root.Torso.Head.hair.midhair, 0, {-1,20,0,0,-30,30})
	swingOnHead(models.model.root.Torso.Head.hair.rightbang, -45, {-1,40,0,0,-20,20})
	swingOnHead(models.model.root.Torso.Head.hair.leftbang, 45, {-1,40,0,0,-20,20})

-- main hair
	swingOnHead(models.model.root.Torso.Head.hair.backhair, 0, {-10,10,0,0,-10,10})
	swingOnHead(models.model.root.Torso.Head.hair.righthair, -135, {-20,20,0,0,-20,5})
	swingOnHead(models.model.root.Torso.Head.hair.lefthair, 135, {-20,20,0,0,-5,20})

-- scarf
	swingOnBody(models.model.root.Torso.Body.scarf.scarf1, 0, {-1,10,0,0,-5,5})
	swingOnBody(models.model.root.Torso.Body.scarf.scarf1.scarf2, 0, {-5,30,0,0,-10,10})

-- coat
	swingOnBody(models.model.root.Torso.Body.Coat.CoatLeft.leftcoatall, 180, {-30,10,-10,10,-10,10})
	swingOnBody(models.model.root.Torso.Body.Coat.CoatRight.rightcoatall, 180, {-30,10,-10,10,-10,10})

	swingOnBody(models.model.root.Torso.Body.Coat.CoatLeft.leftcoatall.Strap, 180, {-20,5,-10,10,-10,10})
	swingOnBody(models.model.root.Torso.Body.Coat.CoatRight.rightcoatall.Strap2, 180, {-20,5,-10,10,-10,10})

	swingOnBody(models.model.root.Torso.Body.Coat.CoatLeft.leftcloakback, 180, {-20,5,-10,10,-10,10})
	swingOnBody(models.model.root.Torso.Body.Coat.CoatRight.rightcloakback, 180, {-20,5,-10,10,-10,10})

	swingOnBody(models.model.root.Torso.Body.Coat.CoatMiddle, 180, {-20,5,-10,10,-10,10}) 

--====================================================================================--
-- ACTION WHEEL SETUP

local mainPage = action_wheel:newPage()
action_wheel:setPage(mainPage)

local Expressions = action_wheel:newPage('Expressions')
    mainPage:newAction(1):title('Expressions'):setTexture(textures["model.expressions"], 0, 0, 25, 25, 1):onLeftClick(function() action_wheel:setPage(Expressions) end)
    Expressions:newAction(1):title('Back'):setTexture(textures["model.back"], 0, 0, 25, 25, 1):onLeftClick(function() action_wheel:setPage(mainPage) end) 

--=====================================-- MAIN WHEEL

-- == jacket [ty to snqwblind for the original 3D coat from their Goldenglow model] ==
		function pings.jacket(bool)
		    models.model.root.Torso.Body.Coat:setVisible(not bool)
		    models.model.root.Torso.LeftArm.LeftCloakarm:setVisible(not bool)
		    models.model.root.Torso.RightArm.RightCloakarm:setVisible(not bool)
		end
		function pings.clouds()
			particles:newParticle("minecraft:cloud", models.model.root.Torso.Body.smoke1:partToWorldMatrix(2,2,2):apply()):setScale(4):setLifetime(12)
			particles:newParticle("minecraft:cloud", models.model.root.Torso.Body.smoke2:partToWorldMatrix(2,2,2):apply()):setScale(4):setLifetime(12)
			particles:newParticle("minecraft:cloud", models.model.root.Torso.Body.smoke3:partToWorldMatrix(2,2,2):apply()):setScale(4):setLifetime(12)
			particles:newParticle("minecraft:cloud", models.model.root.Torso.Body.smoke4:partToWorldMatrix(2,2,2):apply()):setScale(4):setLifetime(12)
			sounds["minecraft:item.armor.equip_leather"]:pos(player:getPos()):subtitle('[{text:"Cloak Shuffles"}]'):pitch(1):volume(2):play()
		end
	mainPage:newAction()
	:title("Jacket Off")
	:toggleTitle("Jacket On")
	:hoverColor(0, 1, 1)
	:setTexture(textures["model.jacket"], 0, 0, 25, 25, 0.9)
	:toggleItem("minecraft:barrier")
	:onLeftClick(pings.clouds)
	:onToggle(pings.jacket)

-- == change skin ==
		function pings.skin(bool)
		    models.model.root.Torso.RightArm.transparentR:setVisible(not bool)
		    models.model.root.Torso.LeftArm.transparentL:setVisible(not bool)
		    models.model.root.Torso.Body.skirt:setVisible(not bool)
		    models.model.root.Torso.Body.bewbO:setVisible(not bool)
		    models.model.root.Torso.Body.chestO:setVisible(not bool)

		    models.model.root.Torso.RightArm.sleeveR:setVisible(bool)
		    models.model.root.Torso.LeftArm.sleeveL:setVisible(bool)
		    models.model.root.Torso.Body.scarf:setVisible(bool)
		    models.model.root.Torso.Body.shorts:setVisible(bool)
		    models.model.root.Torso.Body.bewbS:setVisible(bool)
		    models.model.root.Torso.Body.chestS:setVisible(bool)
		end
	mainPage:newAction()
	:title("L2D Outfit")
	:toggleTitle("Original Outfit")
	:hoverColor(0, 1, 1)
	:setTexture(textures["model.outfit"], 0, 0, 25, 25, 0.9)
	:onLeftClick(pings.clouds)
	:onToggle(pings.skin)

-- == monst3r ==
		function pings.mons(bool)
		    models.model.monst3r.minimons:setVisible(not bool)
		end
		function pings.summon()
			animations.model.summon:play()
			animations.model.monsspin:play()
			animations.model.blink:play()
				-- minimon puffs
			particles:newParticle("minecraft:cloud", models.model.monst3r.minimons.puffs.puff5:partToWorldMatrix(2,2,2):apply()):setScale(3):setLifetime(12)
			particles:newParticle("minecraft:cloud", models.model.monst3r.minimons.puffs.puff6:partToWorldMatrix(2,2,2):apply()):setScale(3):setLifetime(12)
			particles:newParticle("minecraft:cloud", models.model.monst3r.minimons.puffs.puff7:partToWorldMatrix(2,2,2):apply()):setScale(3):setLifetime(12)
			particles:newParticle("minecraft:cloud", models.model.monst3r.minimons.puffs.puff8:partToWorldMatrix(2,2,2):apply()):setScale(3):setLifetime(12)
		end
	mainPage:newAction()
	:title("Retreat Monst3r")
	:toggleTitle("Summon Monst3r")
	:hoverColor(0, 1, 1)
	:onLeftClick(pings.summon)
	:onToggle(pings.mons)
	:setTexture(textures["model.monsummonicon"], 0, 0, 25, 25, 0.9)

--=====================================-- EXPRESSIONS

-- == default ==
    function pings.default() 
        animations.model.squint:stop()
 		animations.model.earsdown:stop()
 		animations.model.earflick:play()
    end
Expressions:newAction()
    :title("Clear Expression")
    :hoverColor(1,0,0.3)
    :setTexture(textures["model.nil"], 0, 0, 25, 25, 0.8)
    :onLeftClick(pings.default)

-- == squint ==
    function 
      pings.squint() 
      animations.model.squint:play() 
    end
Expressions:newAction()
    :title("Squint")
    :hoverColor(1,0,0.3)
    :setTexture(textures["model.squint"], 0, 0, 25, 25, 1)
    :onLeftClick(pings.squint)

-- == shock ==4
    function 
      pings.surprise() 
      animations.model.surprise:play() 
    end
Expressions:newAction()
    :title("Surprise")
    :hoverColor(1,0,0.3)
    :setTexture(textures["model.surprise"], 0, 0, 25, 25, 1)
    :onLeftClick(pings.surprise)

-- == wink ==
    function 
      pings.wink() 
      animations.model.wink:play()
      animations.model.squint:stop()
    end
Expressions:newAction()
    :title("Wink")
    :hoverColor(1,0,0.3)
    :setTexture(textures["model.wink"], 0, 0, 25, 25, 1)
    :onLeftClick(pings.wink)

-- == ears down ==
    function 
      pings.eard() 
      animations.model.earsdown:play()
      animations.model.earflick:play()
    end
Expressions:newAction()
    :title("Ears Down")
    :hoverColor(1,0,0.3)
    :setTexture(textures["model.earsdown"], 0, 0, 25, 25, 1.1)
    :onLeftClick(pings.eard)

--====================================================================================--
-- EVENTS // PARTICLES
       --== particles:newParticle(particleID, position, velocity)
             --== sounds:playSound(soundID, position, volume, pitch, loop)

function events.tick()
	  time = world.getTime()

-- clothing action wheel changes
		local CoatOn = models.model.root.Torso.Body.Coat:getVisible(true)
		local OriginalSkin = models.model.root.Torso.Body.skirt:getVisible(true)
		local ldSkin = models.model.root.Torso.Body.shorts:getVisible(true)

	if CoatOn then
		models.model.root.Torso.RightArm.transparentR:setVisible(false)
		models.model.root.Torso.LeftArm.transparentL:setVisible(false)
		models.model.root.Torso.RightArm.sleeveR:setVisible(false)
		models.model.root.Torso.LeftArm.sleeveL:setVisible(false)

	end
	if OriginalSkin and not CoatOn then
		models.model.root.Torso.RightArm.transparentR:setVisible(true)
		models.model.root.Torso.LeftArm.transparentL:setVisible(true)
	end
	if ldSkin and not CoatOn then
		models.model.root.Torso.RightArm.sleeveR:setVisible(true)
		models.model.root.Torso.LeftArm.sleeveL:setVisible(true)
	end

-- wink / blink
	if animations.model.wink:isPlaying() then animations.model.blink:stop() end

end

--====================================================================================--
-- GSAnimBlend // ANIMATIONS & SETTINGS

animations.model.idleMonsBob:play()

		--=====================================-- PRIORITY

		--==========================-- BLENDTIME

	animations.model.summon:setBlendTime(1)
	animations.model.happimons:setBlendTime(0)
	animations.model.monsspin:setBlendTime(0)
	animations.model.monsupdownR:setBlendTime(0)
	animations.model.monsupdownL:setBlendTime(0)
	animations.model.monsblink:setBlendTime(2)

		-- expressions
	animations.model.squint:setBlendTime(2)
 	animations.model.earsdown:setBlendTime(1)

		--==========================-- SPEED

--====================================================================================--
-- texture types
-- models._:setSecondaryRenderType("CUTOUT, CUTOUT_CULL, TRANSLUCENT, TRANSLUCENT_CULL, EMISSIVE, EMISSIVE_SOLID, EYES, END_PORTAL, 
                                -- END_GATEWAY, TEXTURED_PORTAL, GLINT, GLINT2, TEXTURED_GLINT, LINES, LINES_STRIP, SOLID, BLURRY")

models.model.monst3r.minimons.minihead.minimoneyes.rightMiniEye:setSecondaryRenderType("EMISSIVE")

--==========================-- TILTING via VELOCITY

local modelRoot = models.model.root
local modelHead = models.model.root.Torso.Head
local legObjects = {
    models.model.RightLeg,
    models.model.LeftLeg
}

local MAX_TILT = 20 -- maximum tilt angle
local TILT_SPEED = 0.1 -- tilt responsiveness
local currentTilt = 0 -- current tilt angle
local currentHeadRotation = 0 -- current head rotation angle
local currentLegRotations = {} -- table to store current leg rotations

local MAX_SPEED = 0.3 -- speed at which maximum tilt is reached (normal walking speed)
local TILT_RANGE_FACTOR = 1.5 -- 0 = linear tilt (same tilt range for all speeds)
                              -- 1 = maximum difference (very little tilt at slow speeds, full tilt at max speed)
                              -- 0 -> 1 range for a mix

local HEAD_ROTATION_SENSITIVITY = 0.5 -- 0 (none) -> 1 to adjust head rotation intensity
local LEG_ROTATION_SENSITIVITY = 1.5 -- leg rotation sensitivity 

-- Initialize leg rotations
for i = 1, #legObjects do
    currentLegRotations[i] = 0
end

function events.RENDER(delta, context)
    if not player:isLoaded() or context == "FIRST_PERSON" then
        return
    end

    local vel = player:getVelocity()
    local speed = math.sqrt(vel.x^2 + vel.z^2) -- horizontal speed
    local speedRatio = math.min(speed / MAX_SPEED, 1)
    local tiltFactor = speedRatio ^ (1 + TILT_RANGE_FACTOR)
    local targetTilt = -MAX_TILT * tiltFactor
    targetTilt = math.max(targetTilt, -MAX_TILT) -- so tilt doesn't go past MAX_TILT
    targetTilt = math.min(targetTilt, 0) -- prevent backwards tilt

    currentTilt = currentTilt + (targetTilt - currentTilt) * TILT_SPEED -- transition body tilt

    -- head tilt (opposite to body tilt)
	    local targetHeadRotation = -currentTilt * HEAD_ROTATION_SENSITIVITY
	    currentHeadRotation = currentHeadRotation + (targetHeadRotation - currentHeadRotation) * TILT_SPEED -- transition head tilt

    -- leg tilt 
	    local targetLegRotation = -speedRatio * MAX_TILT * LEG_ROTATION_SENSITIVITY
	    for i = 1, #legObjects do
	        currentLegRotations[i] = currentLegRotations[i] + (targetLegRotation - currentLegRotations[i]) * TILT_SPEED -- transition leg tilt
	    end

    -- apply tilt
	    if modelRoot then
	        modelRoot:setRot(currentTilt, 0, 0) -- rotation to body
	    end

	    if modelHead then
	        modelHead:setRot(currentHeadRotation, 0, 0) -- rotation to head
	    end
	    for i, legObject in ipairs(legObjects) do
	        if legObject then
	            legObject:setRot(currentLegRotations[i], 0, 0) -- rotation to leg
	        end
	    end
end