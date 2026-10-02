skirtPhysics = {}

local min = math.min
local max = math.max

-- root.Front
-- root.FrontRight
-- root.FrontLeft
-- root.Back
-- root.BackLeft
-- root.BackRight

function skirtPhysics.new(root, restAngle, angleAdd, legMultiplier, crouchOffset)
	assert(root, "The skirt root Modelpath is incorrect.")

	local handler = {}
	
	handler.root = root
	handler.restAngle = restAngle or 25
	handler.angleAdd = angleAdd
	handler.legMultiplier = legMultiplier or 1
	handler.crouchOffset = crouchOffset or vec(0,1.4,-1)
	
	handler.velocityModifer = 40
	handler.maxTension = 30
	
	handler.tensionLeft = 0
	handler.tensionRight = 0
	
	handler.angles = {
		FrontLeft = 0,
		FrontRight = 0,
		BackLeft = 0,
		BackRight = 0
	}
	
	local parts = {
		[1] = handler.root.SkirtFrontLeft,
		[2] = handler.root.SkirtFrontRight,
		[3] = handler.root.SkirtBackLeft,
		[4] = handler.root.SkirtBackRight
	}
	
	local previous_time = nil

	function events.render(delta)	
		local current_time = world:getTime()+delta
		
		local delta_time
		if previous_time then
			delta_time = current_time-previous_time
		else
			delta_time = 0
		end
		
		previous_time = current_time
	
		local angleAdd = handler.angleAdd
		
		handler.tensionLeft = 0
		handler.tensionRight = 0
	
		-- gravity
		for i,v in pairs(parts) do
			local delta_time = delta_time
		
			--funky very shallow parabala
			local change = 0.005*v:getOffsetRot().x^2
			
			-- check if diffrence is great enough, since lerp can somtimes bug out for really small changes
			if math.abs(v:getOffsetRot().x-change) > 0.1 then
				v:setOffsetRot(math.lerp(v:getOffsetRot().x, change, delta_time), 0, 0)
			else
				v:setOffsetRot(0, 0, 0)
			end
		end
	
		-- velocity
		local vertForce = player:getVelocity(delta).y*handler.velocityModifer*delta_time
		
		for i,v in pairs(parts) do
			local delta_time = delta_time
			if i > 2 then
				delta_time = -delta_time
			end
			
			v:setOffsetRot(v:getOffsetRot().x-vertForce*delta_time, 0, 0)
		end
	
		local leftLegRot = vanilla_model.LEFT_LEG:getOriginRot().x*handler.legMultiplier*0.7
		local rightLegRot = vanilla_model.RIGHT_LEG:getOriginRot().x*handler.legMultiplier*0.7
				
		
				
		-- leg avoidance
		if leftLegRot > handler.root.SkirtFrontLeft:getTrueRot().x-handler.angleAdd then
			handler.tensionLeft = 1
			handler.root.SkirtFrontLeft:setOffsetRot(leftLegRot-handler.root.SkirtFrontLeft:getRot().x+handler.angleAdd,0,0)
		elseif leftLegRot < handler.root.SkirtBackLeft:getTrueRot().x+handler.angleAdd then
			handler.tensionLeft = -1
			handler.root.SkirtBackLeft:setOffsetRot(leftLegRot-handler.root.SkirtBackLeft:getRot().x-handler.angleAdd,0,0)
		end
		
		if rightLegRot > handler.root.SkirtFrontRight:getTrueRot().x-handler.angleAdd then
			handler.tensionRight = 1
			handler.root.SkirtFrontRight:setOffsetRot(rightLegRot-handler.root.SkirtFrontRight:getRot().x+handler.angleAdd,0,0)
		elseif rightLegRot < handler.root.SkirtBackRight:getTrueRot().x+handler.angleAdd then
			handler.tensionRight = -1
			handler.root.SkirtBackRight:setOffsetRot(rightLegRot-handler.root.SkirtBackRight:getRot().x-handler.angleAdd,0,0)
		end
		
		-- tension		
		if handler.root.SkirtFrontLeft:getOffsetRot().x-handler.root.SkirtBackLeft:getOffsetRot().x > handler.maxTension then
			if handler.tensionLeft == 1 then
				handler.root.SkirtBackLeft:setOffsetRot(handler.root.SkirtFrontLeft:getOffsetRot().x-handler.maxTension,0,0)
			elseif handler.tensionLeft == -1 then
				handler.root.SkirtFrontLeft:setOffsetRot(handler.root.SkirtBackLeft:getOffsetRot().x+handler.maxTension,0,0)
			else
				handler.root.SkirtFrontLeft:setOffsetRot(handler.maxTension/2,0,0)
				handler.root.SkirtBackLeft:setOffsetRot(-handler.maxTension/2,0,0)
			end
		end
		
		if handler.root.SkirtFrontRight:getOffsetRot().x-handler.root.SkirtBackRight:getOffsetRot().x > handler.maxTension then
			if handler.tensionRight == 1 then
				handler.root.SkirtBackRight:setOffsetRot(handler.root.SkirtFrontRight:getOffsetRot().x-handler.maxTension,0,0)
			elseif handler.tensionRight == -1 then
				handler.root.SkirtFrontRight:setOffsetRot(handler.root.SkirtBackRight:getOffsetRot().x+handler.maxTension,0,0)
			else
				handler.root.SkirtFrontRight:setOffsetRot(handler.maxTension/2,0,0)
				handler.root.SkirtBackRight:setOffsetRot(-handler.maxTension/2,0,0)
			end
		end
		
		-- center
		local front_avg = (handler.root.SkirtFrontLeft:getOffsetRot().x+handler.root.SkirtFrontRight:getOffsetRot().x)/2
		local back_avg = (handler.root.SkirtBackLeft:getOffsetRot().x+handler.root.SkirtBackRight:getOffsetRot().x)/2
		
		handler.root.SkirtFront:setOffsetRot(front_avg*1.1,(handler.root.SkirtFrontLeft:getOffsetRot().x-handler.root.SkirtFrontRight:getOffsetRot().x)*-0.2,0)
		handler.root.SkirtBack:setOffsetRot(back_avg*1.1,(handler.root.SkirtBackLeft:getOffsetRot().x-handler.root.SkirtBackRight:getOffsetRot().x)*-0.2,0)
		
		-- crouch
		if player:isLoaded() then
			handler.root:setPos(player:isCrouching() and handler.crouchOffset or vec(0,0,0)):setRot(-vanilla_model.BODY:getOriginRot().x,0,0)
		end

		if player:isCrouching() then
			models.Miku16th_v3.root.UpperBody.Body.Skirt.veilL:setOffsetRot(-35,0,0)
			models.Miku16th_v3.root.UpperBody.Body.Skirt.veilR:setOffsetRot(-35,0,0)
		else
			models.Miku16th_v3.root.UpperBody.Body.Skirt.veilL:setOffsetRot(0,0,0)
			models.Miku16th_v3.root.UpperBody.Body.Skirt.veilR:setOffsetRot(0,0,0)
		end

		-- veil

		--front
		if handler.root.SkirtFrontRight:getOffsetRot().x < handler.root.SkirtFront:getOffsetRot().x then
			handler.root.SkirtFrontRight.test2:setOffsetRot(handler.root.SkirtFront:getOffsetRot().x)
		end
		if handler.root.SkirtFrontLeft:getOffsetRot().x < handler.root.SkirtFront:getOffsetRot().x then
			handler.root.SkirtFrontLeft.test:setOffsetRot(handler.root.SkirtFront:getOffsetRot().x)
		end

		--back

		if player:getVelocity():length()<0.5 then
			if handler.root.SkirtBackRight:getOffsetRot().x < -16 then
				models.Miku16th_v3.root.UpperBody.Body.Skirt.veilR:setOffsetRot(handler.root.SkirtBackRight:getOffsetRot().x/4.5)
			end

			if handler.root.SkirtBackLeft:getOffsetRot().x < -16 then
				models.Miku16th_v3.root.UpperBody.Body.Skirt.veilL:setOffsetRot(handler.root.SkirtBackLeft:getOffsetRot().x/4.5)
			end

		end
		
		if models.Miku16th_v3.root.UpperBody.Body.Skirt.veilR.sectionR1:getOffsetRot().x > 0 then
			if handler.root.SkirtBackRight:getOffsetRot().x > handler.root.SkirtBack:getOffsetRot().x then
				models.Miku16th_v3.root.UpperBody.Body.Skirt.veilR:setOffsetRot(handler.root.SkirtBack:getOffsetRot().x*1.2)
			else
				models.Miku16th_v3.root.UpperBody.Body.Skirt.veilR:setOffsetRot(handler.root.SkirtBackRight:getOffsetRot().x*1.2)
			end
			if handler.root.SkirtBackLeft:getOffsetRot().x > handler.root.SkirtBack:getOffsetRot().x then
				models.Miku16th_v3.root.UpperBody.Body.Skirt.veilL:setOffsetRot(handler.root.SkirtBack:getOffsetRot().x*1.2)
			else
				models.Miku16th_v3.root.UpperBody.Body.Skirt.veilL:setOffsetRot(handler.root.SkirtBackLeft:getOffsetRot().x*1.2)
			end
		end

		if player:isCrouching() then
			models.Miku16th_v3.root.UpperBody.Body.Skirt.veilR:setOffsetRot(-45,0,0)
			models.Miku16th_v3.root.UpperBody.Body.Skirt.veilL:setOffsetRot(-45,0,0)
		end

		--sides
		models.Miku16th_v3.root.UpperBody.Body.Skirt.right:setOffsetRot(handler.root.SkirtFrontRight:getOffsetRot().x/3)
		models.Miku16th_v3.root.UpperBody.Body.Skirt.left:setOffsetRot(handler.root.SkirtFrontLeft:getOffsetRot().x/3)


	end
end

return skirtPhysics