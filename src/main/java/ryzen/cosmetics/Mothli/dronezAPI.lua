dronezAPI = {}

local droneObjects = {}
local droneObjectsCount = 0

-- drone obj
function dronezAPI.new(path)
	--instantiate
	local self = {}
	
	droneObjects[droneObjectsCount+1] = self
	droneObjectsCount = droneObjectsCount+1
	
	--check
	assert(path, "DronezAPI - new\nModelpath is incorrect.")
	
	--init vars
	self.modelPath = path
	self.index = droneObjectsCount
	
	--stats
	self.topSpeed = 6 -- maximum speed in blocks/tick drone can reach
	self.acceleration = 0.03 -- velocity change per tick
	self.airFriction = 0.9 -- multiplies velocity every tick to reduce speed
	self.brakeMultiplier = 1 -- velocity multiplier when stopping
	self.slowThreshold = 3 -- maximum distance to start slowing down 
	self.stopThreshold = vec(1.5,1.5,1.5) -- maximum distance per axis to target to consider close enough and stop
	
	self.punchImpulse = 0.5 -- force added when punched
	
	self.warpThreshold = 20 -- threshold to be far enough to warp closer to target. set to math.huge to effectively disable
	self.warpMultiplier = 0.9 -- mutlipler of max speed to set velocity to when exiting warp
	self.warpDistance = 5 -- distance from target to warp to
	
	--status
	self.pos = vec(0,0,0)
	self.posOld = vec(0,0,0)
	self.rot = vec(0,0,0)
	self.rotOld = vec(0,0,0)
	self.velocity = vec(0,0,0)
	
	self.targetEntity = nil
	
	if player:isLoaded() then
		self.pos = player:getPos() + vec(0,player:getBoundingBox().y+0.5,0)
	end
	
	--var adjusting functions
	function self:setTopSpeed(arg)
		self.topSpeed = arg
		return self
	end
	
	function self:setAcceleration(arg)
		self.acceleration = arg
		return self
	end
	
	function self:setAirFriction(arg)
		self.airFriction = arg
		return self
	end
	
	function self:setBrakeMultiplier(arg)
		self.brakeMultiplier = arg
		return self
	end
	
	function self:setStopThreshold(x, y, z)
		if type(x) == "Vector3" then
			self.stopThreshold = x
		elseif type(x) == "number" then
			if type(y) == "number" then
				self.stopThreshold = vec(x,y,z)
			else
				self.stopThreshold = vec(x,x,x)
			end
		else
			error("DronezAPI - setStopThreshold\nInvalid argument(s), expected Vector3 or 1 or 3 numbers")
		end
		
		return self
	end
	
	
	function self:setPunchImpulse(arg)
		self.punchImpulse = arg
		return self
	end
	
	
	function self:setWarpThreshold(arg)
		self.warpThreshold = arg
		return self
	end
	
	function self:setWarpMultiplier(arg)
		self.warpMultiplier = arg
		return self
	end
	
	function self:setWarpDistance(arg)
		self.warpDistance = arg
		return self
	end
	
	
	function self:setTargetEntity(arg)
		self.targetEntity = arg
		return self
	end
	
	-- targeting function specific setters	
	function self:setLocalOffset(x, y, z)
		if type(x) == "Vector3" then
			self.localOffset = x
		elseif type(x) == "number" then
			if type(y) == "number" then
				self.localOffset = vec(x,y,z)
			else
				self.localOffset = vec(x,x,x)
			end
		else
			error("DronezAPI - setLocalOffset\nInvalid argument(s), expected Vector3 or 1 or 3 numbers")
		end
		
		return self
	end
	
	function self:setOffsetRot(x, y, z)
		if type(x) == "Vector3" then
			self.offsetRot = x
		elseif type(x) == "number" then
			if type(y) == "number" then
				self.offsetRot = vec(x,y,z)
			else
				self.offsetRot = vec(x,x,x)
			end
		else
			error("DronezAPI - setOffsetRot\nInvalid argument(s), expected Vector3 or 1 or 3 numbers")
		end
		
		return self
	end
	
	function self:setAquireWaitTime(arg)
		self.aquireWaitTime = arg
		return self
	end
	
	--status adjusting
	function self:setPos(x, y, z)
		if type(x) == "Vector3" then
			self.pos = x
		elseif type(x) == "number" then
			if type(y) == "number" then
				self.pos = vec(x,y,z)
			else
				self.pos = vec(x,x,x)
			end
		else
			error("DronezAPI - setPos\nInvalid argument(s), expected Vector3 or 1 or 3 numbers")
		end
		
		return self
	end
	
	function self:setVelocity(x, y, z)
		if type(x) == "Vector3" then
			self.velocity = x
		elseif type(x) == "number" then
			if type(y) == "number" then
				self.pos = vec(x,y,z)
			else
				self.pos = vec(x,x,x)
			end
		else
			error("DronezAPI - setVelocity\nInvalid argument(s), expected Vector3 or 1 or 3 numbers")
		end
		
		return self
	end
	
	--event calls
	function events.entity_init()
		self.pos = player:getPos() + vec(0,player:getBoundingBox().y+0.5,0)
	end
	
	function events.tick()
		self.posOld = self.pos
		self.rotOld = self.rot
		
		local targetPos = self.getTargetPos(self)
		local targetRot =self.getTargetRot(self)
		targetPos = targetPos or vec(0,0,0)
		targetRot = targetRot or vec(0,0,0)
		local acceleration = vec(0,0,0)
		local braking = false
		local punched = false
		
		--drone punching
		--if host:isHost() then
		for k,interactor in pairs(world.getPlayers()) do
			if interactor:getSwingTime() == 1 then
				local pos = interactor:getPos()
				local eyePos = vec(pos.x, pos.y + interactor:getEyeHeight(), pos.z)
				
				if raycast:aabb(eyePos, eyePos+interactor:getLookDir()*3.5, {{self.pos + vec(-5/16,-5/16,-5/16), self.pos + vec(5/16,5/16,5/16)}}) then
					local returned = self.dronePunched(self, interactor)
					if returned ~= false then
						pings.dronezAPI_syncPunch(self.index, interactor:getLookDir()*(type(returned) == "number" and returned or self.punchImpulse))
						function pings.dronezAPI_syncInteract(index, a, b, c, d)
    local drone = droneObjects[index]
    local interactorUUID = client:intUUIDToString(a, b, c, d)
    local interactor = world.getEntity(interactorUUID)
    if drone and interactor then
        drone.droneInteracted(drone, interactor)  -- Викликаємо подію на всіх клієнтах
    end
end
					end
				end
			end
		end
		--end
		
		--move
		if not punched then	
			local targetDist = math.sqrt((self.pos.x-targetPos.x)^2+(self.pos.y-targetPos.y)^2+(self.pos.z-targetPos.z)^2)
			local targetDistAxis = vec(math.abs(self.pos.x-targetPos.x),math.abs(self.pos.y-targetPos.y),math.abs(self.pos.z-targetPos.z))
			
			--warp
			if targetDist > self.warpThreshold then
				--warp
				if self.droneWarp(self) ~= false then
					self.pos = targetPos-(self.pos-targetPos):normalize()*-self.warpDistance
					self.velocity = (self.pos-targetPos):normalize()*-self.topSpeed*self.warpMultiplier
					
					self.dronePostWarp(self)
				end
			end
			if targetDistAxis.x > self.stopThreshold.x or targetDistAxis.y > self.stopThreshold.y or targetDistAxis.z > self.stopThreshold.z then
				-- get moving
				if self.velocity:length() < self.topSpeed then
					--accelerate
					acceleration = (self.pos-targetPos):normalize()*-self.acceleration*math.min(targetDist/self.slowThreshold, 1)
					
					self.velocity = self.velocity+acceleration
				end
			else
				--brake
				acceleration = self.velocity*-self.acceleration*self.brakeMultiplier

				self.velocity = self.velocity+acceleration
				braking = true
			end
		end
		
		--air friction
		self.velocity = self.velocity*self.airFriction
		
		--apply speed
		self.pos = self.pos+self.velocity
		
		--rotate
if self.velocity:length() > 0.02 then
    local yaw = math.deg(math.atan2(self.velocity.x, self.velocity.z))
    yaw = yaw + 180
    self.rot = math.lerpAngle(self.rot, vec(0, yaw, 0), 0.25)
            else

            end
		
		--sync
		if world:getTime()%20 == 0	 then
			if self.targetEntity.uuid then
				pings.dronezAPI_syncState(self.index, self.pos, self.velocity, client:uuidToIntArray(self.targetEntity:getUUID()))
			else
				pings.dronezAPI_syncState(self.index, self.pos, self.velocity)
			end
		end
	end
	
	function events.render(delta)
		-- lerp
		self.modelPath:setPos(math.lerp(self.posOld*16, self.pos*16, delta))	
		self.modelPath:setRot(math.lerpAngle(self.rotOld, self.rot, delta))
	end
	
	--drone events
	
	-- called before attempting to warp. return false to cancel
	function self.droneWarp(droneObj)
		sounds:playSound("entity.fox.teleport", droneObj.pos, 0.5, 1.1)
	end
	
	-- called after warping, sfx go here
	function self.dronePostWarp(droneObj)
		sounds:playSound("entity.fox.teleport", droneObj.pos, 0.5, 1.1)
	end
	
	-- called when punched. return false to cancel
	function self.dronePunched(droneObj, interactor)
		sounds:playSound("minecraft:entity.silverfish.hurt", droneObj.pos, 0.5, 1.2)
	end
	
	self.getTargetPos = dronezAPI.targetFunctions.followEntity
	self.getTargetRot = function() return vec(0,0,0) end
	
	function self:setTargetPosFunction(arg)
		self.getTargetPos = arg
		return self
	end
	
	function self:setTargetRotFunction(arg)
		self.getTargetRot = arg
		return self
	end
	
	return self
end

function dronezAPI.dumpObjects()
	logTable(droneObjects)
end

-- ping stuff

function pings.dronezAPI_syncState(index, pos, velocity, a,b,c,d)
	-- sync most information
	if not host:isHost() then
		droneObjects[index].pos = pos+velocity
		droneObjects[index].velocity = velocity
		if a then
			droneObjects[index].targetEntity = world.getEntity(client:intUUIDToString(a,b,c,d))
		end
	end
end

function pings.dronezAPI_syncEntity(index, a,b,c,d)
	-- just sync entity
	droneObjects[index].targetEntity = world.getEntity(client:intUUIDToString(a,b,c,d))
	droneObjects[index].aquireCounter = 0
end

function pings.dronezAPI_syncPunch(index, impulse)
	droneObjects[index].velocity = droneObjects[index].velocity + impulse
end

dronezAPI.targetFunctions = {}

-- follows and looks at the targetEntity.
function dronezAPI.targetFunctions.followEntity(droneObj)
	droneObj.localOffset = droneObj.localOffset or vec(0,1,0) -- offset in look direction 
	droneObj.offsetRot = droneObj.offsetRot or vec(0,1,0) -- offset rot

	-- fallback target
	if not droneObj.targetEntity then
		droneObj.targetEntity = player
	end

	-- check if the target is loaded to avoid uninit error
	if droneObj.targetEntity:isLoaded() then
		-- return target pos as 1 block above their hitbox, and the rotation
		return droneObj.targetEntity:getPos() + vec(0,droneObj.targetEntity:getBoundingBox().y,0)+vectors.rotateAroundAxis(math.deg(math.atan2(droneObj.targetEntity:getLookDir().x, droneObj.targetEntity:getLookDir().z)), droneObj.localOffset, vec(0,1,0))
	end
end

function noOwner(entity)
	return entity ~= player and entity:isLiving()
end

function dronezAPI.targetFunctions.followNearbyEntities(droneObj)
	droneObj.localOffset = droneObj.localOffset or vec(0,1,0) -- offset in look direction 

	droneObj.aquireWaitTime = droneObj.aquireWaitTime or 300 -- time to wait between scanning for nearby targets or returning to player
	droneObj.aquireCounter = droneObj.aquireCounter or 0 -- counter until aquiring
	
	-- add keybind
	if not droneObj.aquireKeybind then
	droneObj.aquireKeybind = keybinds:newKeybind("Switch Drone Target", "key.keyboard.i")
		:setOnPress(function()
			local raystart = player:getPos() + vec(0,player:getEyeHeight(),0)
			local rayend = raystart+player:getLookDir()*16
			
			local block, hitpos = raycast:block(raystart, rayend)
			
			if debugOn then
				line(raystart, hitpos, "cyan")
			end
			
			local entity = raycast:entity(raystart, hitpos, noOwner)
			
			droneTarget = entity
			droneTimer = 0
			
			if entity then
				pings.dronezAPI_syncEntity(droneObj.index, client:uuidToIntArray(entity:getUUID()))
			else
				pings.dronezAPI_syncEntity(droneObj.index, client:uuidToIntArray(player:getUUID()))
			end
		end)
	end
	
	-- increment counter
	droneObj.aquireCounter = droneObj.aquireCounter + 1

	-- if counter finished
	if droneObj.aquireCounter > droneObj.aquireWaitTime then		
		if droneObj.targetEntity == player then	
			-- stupid amount of raycasts, host only
			if host:isHost() then 
				local casts = 0
				-- raycasts spread over 20 ticks, calculate cast number here
				local yawcasts = droneObj.aquireCounter-droneObj.aquireWaitTime

				casts = 0
				repeat
					-- find start/end of ray
					raystart = droneObj.pos
					-- angle based on no. of casts
					rayend = raystart+vectors.angleToDir(0+5*(casts-6/2), math.map(yawcasts, 0, 20, 0, 360))*16
					
					-- check for block collision
					local block, hitpos = raycast:block(raystart, rayend)
					
					-- find entity
					local entity = raycast:entity(raystart, hitpos, noOwner)
					
					-- found entity
					if entity then
						--droneObj.targetEntity = entity
						droneObj.aquireCounter = 0
						-- sync
						pings.dronezAPI_syncEntity(droneObj.index, client:uuidToIntArray(entity:getUUID()))
						break
					end
					
					casts = casts+1
				until casts > 5
			end
		else
			-- if already following an entity, go back to player
			droneObj.targetEntity = player
			droneObj.aquireCounter = 0
		end
	end

	-- fallback target
	if not droneObj.targetEntity then
		droneObj.targetEntity = player
	end

	-- check if the target is loaded to avoid uninit error
	if droneObj.targetEntity:isLoaded() then	
		-- return target pos as 1 block above their hitbox, and the rotation
		return droneObj.targetEntity:getPos() + vec(0,droneObj.targetEntity:getBoundingBox().y,0)+vectors.rotateAroundAxis(math.deg(math.atan2(droneObj.targetEntity:getLookDir().x, droneObj.targetEntity:getLookDir().z)), droneObj.localOffset, vec(0,1,0))
	end
end

-- looks at entity
function dronezAPI.targetFunctions.lookAt(droneObj)
	droneObj.offsetRot = droneObj.offsetRot or vec(0,0,0) -- offset rot
	if droneObj.targetEntity:isLoaded() then
		return vec(0,math.deg(math.atan2(droneObj.pos.x-droneObj.targetEntity:getPos().x, droneObj.pos.z-droneObj.targetEntity:getPos().z))+180,0)+droneObj.offsetRot
	end
end
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-- Right-click PAT PAT PAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAT


pings.dronezAPI_syncInteract = function(index)
    local drone = droneObjects[index]
    if not drone then return end
    
    drone.droneInteracted(drone, player)
end

function events.MOUSE_PRESS(button, action, modifiers)
    if button ~= 1 or action ~= 1 then return end

    local eyePos = player:getPos() + vec(0, player:getEyeHeight(), 0)
    local lookDir = player:getLookDir()
    local rayEnd = eyePos + lookDir * 5.5

    for _, drone in ipairs(droneObjects) do
        local halfSize = vec(0.2, 0.2, 0.2)
        local bbMin = drone.pos - halfSize
        local bbMax = drone.pos + halfSize

        if raycast:aabb(eyePos, rayEnd, {{bbMin, bbMax}}) then
            pings.dronezAPI_syncInteract(drone.index)
            break
        end
    end
end
return dronezAPI