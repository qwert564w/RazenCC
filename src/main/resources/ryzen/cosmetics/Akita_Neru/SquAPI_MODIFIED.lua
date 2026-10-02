
--[[--------------------------------------------------------------------------------------
███████╗ ██████╗ ██╗   ██╗██╗███████╗██╗  ██╗██╗   ██╗     █████╗ ██████╗ ██╗
██╔════╝██╔═══██╗██║   ██║██║██╔════╝██║  ██║╚██╗ ██╔╝    ██╔══██╗██╔══██╗██║
███████╗██║   ██║██║   ██║██║███████╗███████║ ╚████╔╝     ███████║██████╔╝██║
╚════██║██║▄▄ ██║██║   ██║██║╚════██║██╔══██║  ╚██╔╝      ██╔══██║██╔═══╝ ██║
███████║╚██████╔╝╚██████╔╝██║███████║██║  ██║   ██║       ██║  ██║██║     ██║
╚══════╝ ╚══▀▀═╝  ╚═════╝ ╚═╝╚══════╝╚═╝  ╚═╝   ╚═╝       ╚═╝  ╚═╝╚═╝     ╚═╝                                                                         
--]]--------------------------------------------------------------------------------------ANSI Shadow

-- Author: Squishy
-- Discord tag: @mrsirsquishy

-- Version: 1.0.0 
-- Legal: ARR

-- Special Thanks to 
-- @jimmyhelp for errors and just generally helping me get things working.

-- IMPORTANT FOR NEW USERS!!! READ THIS!!!

-- Thank you for using SquAPI! Unless you're experienced and wish to actually modify the functionality
-- of this script, I wouldn't reccomend snooping around. 
-- Don't know exactly what you're doing? This site contains a guide on how to use!(also linked on github):
-- https://mrsirsquishy.notion.site/Squishy-API-Guide-3e72692e93a248b5bd88353c96d8e6c5

-- This SquAPI file does have some mini-documentation on paramaters if you need like a quick reference, but
-- do not modify, and do not copy-paste code from this file unless you are an avid scripter who knows what they are doing.


-- Don't be afraid to ask me for help, just make sure to provide as much info as possible so I or someone can help you faster.



-- SCRIPT MODIFIED TO ONLY CONTAIN FUNCTIONS USED BY THIS AVATAR


--setup stuff
local squassets 
if pcall(require, "SquAssets_MODIFIED") then
    squassets = require("SquAssets_MODIFIED")
else
    error("§4Missing SquAssets file! Make sure to download that from the GitHub too!§c")
end
local squapi = {}


-- SQUAPI CONTROL VARIABLES AND CONFIG ----------------------------------------------------------
-------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------
-- these variables can be changed to control certain features of squapi.


--when true it will automatically tick and update all the functions, when false it won't do that. 
--if false, you can run each objects respective tick/update functions on your own - better control. 
squapi.autoFunctionUpdates = true


-- FUNCTIONS --------------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------

--BEWB PHYSICS
-- guide:(note if it has a * that means you can leave it blank to use reccomended settings)
-- element: 	    the bewb element that you want to affect(models.[modelname].path)
-- bendability(2):  how much the bewb should move when you move
-- stiff(0.05):	    how stiff the bewb should be
-- bounce(0.9):	    how bouncy the bewb should be
-- doIdle(true):    whether or not the bewb should have an idle sway(like breathing)
-- idleStrength(4): how much the bewb should sway when idle
-- idleSpeed(1):    how fast the bewb should sway when idle
-- downLimit(-10):  the lowest the bewb can rotate
-- upLimit(25):     the highest the bewb can rotate

squapi.bewbs = {}
squapi.bewb = {}
squapi.bewb.__index = squapi.bewb
function squapi.bewb:new(element, bendability, stiff, bounce, doIdle, idleStrength, idleSpeed, downLimit, upLimit)
    local self = setmetatable({}, squapi.bewb)

    -- INIT -------------------------------------------------------------------------
	assert(element,"§4Your model path for bewb is incorrect.§c")
    self.element = element
	if doIdle == nil then doIdle = true end
    self.doIdle = doIdle
	self.bendability = bendability or 2
	self.bewby = squassets.BERP:new(stiff or 0.05, bounce or 0.9, downLimit or -10, upLimit or 25 )
    self.idleStrength = idleStrength or 4
    self.idleSpeed = idleSpeed or 1
	self.target = 0

    -- CONTROL -------------------------------------------------------------------------

    self.enabled = true
    function self:toggle()
		self.enabled = not self.enabled
	end
    function self:disable()
        self.enabled = false
    end
    function self:enable()
        self.enabled = true
    end
    

    -- UPDATE -------------------------------------------------------------------------

    self.oldpose = "STANDING"
    function self:tick()
        if self.enabled then
            local vel = squassets.forwardVel()
            local yvel = squassets.verticalVel()
            local worldtime = world.getTime()

            if self.doIdle then 
                self.target = math.sin(worldtime/8*self.idleSpeed)*self.idleStrength
            end

            --physics when crouching/uncrouching
            local pose = player:getPose()
            if pose == "CROUCHING" and self.oldpose == "STANDING" then
                self.bewby.vel = self.bewby.vel + self.bendability
            elseif pose == "STANDING" and self.oldpose == "CROUCHING" then
                self.bewby.vel = self.bewby.vel - self.bendability
            end
            self.oldpose = pose

            --physics when moving
            self.bewby.vel = self.bewby.vel - yvel * self.bendability
            self.bewby.vel = self.bewby.vel - vel * self.bendability
        else
            self.target = 0
        end
    end

	function self:render(dt, context)
		self.element:setOffsetRot(self.bewby:berp(self.target, dt),0,0)
	end

    table.insert(squapi.bewbs, self)
    return self
end


--RANDOM ANIMATION OBJECT
--this object will take in an animation and plays it randomly every tick by a specified amount. 
--animation:    the animation to play
--*chanceRange: an optional paramater that sets the range. 0 means every tick, larger values mean lower chances of playing every tick.
--*isBlink:     if this is for blinking set this to true so that it doesn't blink while sleeping. 

squapi.randimation = {}
squapi.randimation.__index = squapi.randimation
function squapi.randimation:new(animation, chanceRange, isBlink)
	local self = setmetatable({}, squapi.randimation)
	
    -- INIT -------------------------------------------------------------------------
    self.isBlink = isBlink
    self.animation = animation
	self.chanceRange = chanceRange or 200


    -- CONTROL -------------------------------------------------------------------------
	
    self.enabled = true
    function self:toggle()
		self.enabled = not self.enabled
	end
    function self:disable()
        self.enabled = false
    end
    function self:enable()
        self.enabled = true
    end

    -- UPDATES -------------------------------------------------------------------------

	function events.tick()
		if self.enabled and (not self.isBlink or player:getPose() ~= "SLEEPING") and math.random(0, self.chanceRange) == 0 and self.animation:isStopped() then
            self.animation:play()
		end
	end

	return self
end


-- MOVING EYES
--guide:(note if it has a * that means you can leave it blank to use reccomended settings)
-- element:	 		the eye element that is going to be moved, each eye is seperate.
-- *leftdistance: 	the distance from the eye to it's leftmost posistion
-- *rightdistance: 	the distance from the eye to it's rightmost posistion
-- *updistance: 	the distance from the eye to it's upmost posistion
-- *downdistance: 	the distance from the eye to it's downmost posistion
squapi.eyes = {}
squapi.eye = {}
squapi.eye.__index = squapi.eye
function squapi.eye:new(element, leftDistance, rightDistance, upDistance, downDistance, switchValues)
    local self = setmetatable({}, squapi.eye)

    -- INIT -------------------------------------------------------------------------
	assert(element,
	"§4Your eye model path is incorrect.§c")
	self.switchValues = switchValues or false
	self.left = leftDistance or .25
	self.right = rightDistance or 1.25
	self.up = upDistance or 0.5
	self.down = downDistance or 0.5
	
    self.x = 0 
    self.y = 0
    self.eyeScale = 1

    -- CONTROL -------------------------------------------------------------------------

    --For funzies if you want to change the scale of the eyes you can use this.(lerps to scale)
    function self:setEyeScale(scale)
        self.eyeScale = scale 
    end

    self.enabled = true
    function self:toggle()
		self.enabled = not self.enabled
	end
    function self:disable()
        self.enabled = false
    end
    function self:enable()
        self.enabled = true
    end

    --resets position
    function self:zero()
        self.x, self.y = 0, 0
    end

    -- UPDATES -------------------------------------------------------------------------

    function self:tick()
        if self.enabled then 
            local headrot = squassets.getHeadRot()
            headrot[2] = math.max(math.min(50, headrot[2]), -50)

            --parabolic curve so that you can control the middle position of the eyes. 
            self.x = -squassets.parabolagraph(-50, -self.left, 0,0, 50, self.right, headrot[2])
            self.y = squassets.parabolagraph(-90, -self.down, 0,0, 90, self.up, headrot[1])
            
            --prevents any eye shenanigans
            self.x = math.max(math.min(self.left, self.x), -self.right)
            self.y = math.max(math.min(self.up, self.y), -self.down)
        end

    end

	function self:render(dt, context)
        local c = element:getPos()
		if self.switchValues then
			element:setPos(0,math.lerp(c[2], self.y, dt),math.lerp(c[3], -self.x, dt))
		else
			element:setPos(math.lerp(c[1], self.x, dt),math.lerp(c[2], self.y, dt),0)
		end
        local scale = math.lerp(element:getOffsetScale()[1], self.eyeScale, dt)
		element:setOffsetScale(scale, scale, scale)
	end

    table.insert(squapi.eyes, self)
    return self
end	


-- UPDATES ALL SQUAPI FEATURES --------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------

if squapi.autoFunctionUpdates then

    function events.tick()
        for i, v in pairs(squapi.eyes) do
            v:tick()
        end
        for i, v in pairs(squapi.bewbs) do
            v:tick()
        end
    end

    function events.render(dt, context)
        for i, v in pairs(squapi.eyes) do
            v:render(dt, context)
        end
        for i, v in pairs(squapi.bewbs) do
            v:render(dt, context)
        end
    end

end
	

return squapi