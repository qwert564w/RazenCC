--[[--------------------------------------------------------------------------------------
  ____              _     _                _                 _       
 / ___|  __ _ _   _(_)___| |__  _   _     / \   ___ ___  ___| |_ ___ 
 \___ \ / _` | | | | / __| '_ \| | | |   / _ \ / __/ __|/ _ \ __/ __|
  ___) | (_| | |_| | \__ \ | | | |_| |  / ___ \\__ \__ \  __/ |_\__ \
 |____/ \__, |\__,_|_|___/_| |_|\__, | /_/   \_\___/___/\___|\__|___/
           |_|                  |___/                                                           
--]]--------------------------------------------------------------------------------------Standard

--[[
-- Author: Squishy
-- Discord tag: @mrsirsquishy

-- Version: 1.0.0 
-- Legal: ARR

Framework Functions and classes for SquAPI. 
This contains some math functions, some simplified calls to figura features, some debugging scripts for convenience, and classes used in SquAPI or for debugging.

You can also make use of these functions, however it's for more advanced scripters. remember to call: local squassets = require("SquAssets")


]]

-- SCRIPT MODIFIED TO ONLY CONTAIN FUNCTIONS USED BY THIS AVATAR

local squassets = {}

--Useful Calls
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------

-- returns how fast the player moves forward, negative means backward
function squassets.forwardVel()
	return player:getVelocity():dot((player:getLookDir().x_z):normalize())
end

-- returns y velocity(negative is down)
function squassets.verticalVel()
	return player:getVelocity()[2]
end

--returns a cleaner vanilla head rotation value to use
function squassets.getHeadRot()
	return (vanilla_model.HEAD:getOriginRot()+180)%360-180
end




--Math Functions
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------

--Parabolic graph
--locally generates a parabolic graph between three points, returns the y value at t on that graph
function squassets.parabolagraph(x1, y1, x2, y2, x3, y3, t)
    local denom = (x1 - x2) * (x1 - x3) * (x2 - x3)
    
	local a = (x3 * (y2 - y1) + x2 * (y1 - y3) + x1 * (y3 - y2)) / denom
    local b = (x3^2 * (y1 - y2) + x2^2 * (y3 - y1) + x1^2 * (y2 - y3)) / denom
    local c = (x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2 + x1 * x2 * (x1 - x2) * y3) / denom

    return a * t^2 + b * t + c
end





--Classes
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------


--stiffness factor, > 0
--bounce factor, reccomended when in range of 0-1. bigger is bouncier.
--if you want to limit the positioning, use lowerlimit and upperlimit, or leave nil
squassets.BERP = {}
squassets.BERP.__index = squassets.BERP
function squassets.BERP:new(stiff, bounce, lowerLimit, upperLimit, initialPos, initialVel)
	local self = setmetatable({}, squassets.BERP)

	self.stiff = stiff or 0.1
	self.bounce = bounce or 0.1
	self.pos = initialPos or 0
	self.vel = initialVel or 0
	self.acc = 0
	self.lower = lowerLimit or nil
	self.upper = upperLimit or nil

	--target is the target position
	--dt, or delta time, the time between now and the last update(delta from the events.update() function)
	--if you want it to have a different stiff or bounce when run input a different stiff bounce
	function self:berp(target, dt, stiff, bounce)
		local dt = dt or 1

		--certified bouncy math
		local dif = (target or 10) - self.pos
		self.acc = ((dif * math.min(stiff or self.stiff, 1)) * dt) --based off of spring force F = -kx
		self.vel = self.vel + self.acc

		--changes the position, but adds a bouncy bit that both overshoots and decays the movement
		self.pos = self.pos + (dif * (1-math.min(bounce or self.bounce, 1)) + self.vel) * dt
		
		--limits range

		if self.upper and self.pos > self.upper then
			self.pos = self.upper
			self.vel = 0
		elseif self.lower and self.pos < self.lower then
			self.pos = self.lower
			self.vel = 0
		end

		--returns position so that you can immediately apply the position as it is changed. 
		return self.pos
	end



	return self
end	


return squassets