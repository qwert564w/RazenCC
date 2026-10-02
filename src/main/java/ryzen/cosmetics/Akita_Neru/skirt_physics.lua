skirtPhysics = {}

local min = math.min
local max = math.max

function skirtPhysics.new(root, restAngle, angleAdd, legMultiplier, crouchOffset)
	assert(root, "The skirt root Modelpath is incorrect.")

	local handler = {}
	
	handler.root = root
	handler.restAngle = restAngle or 25
	handler.angleAdd = angleAdd or 0
	handler.legMultiplier = legMultiplier or 1
	handler.crouchOffset = crouchOffset or vec(0,1.4,-1)

	function events.render()
		local restAngle = handler.restAngle
		local angleAdd = handler.angleAdd
		local legMultiplier = handler.legMultiplier
	
		local leftLegRot = vanilla_model.LEFT_LEG:getOriginRot().x*legMultiplier+angleAdd
		local rightLegRot = vanilla_model.RIGHT_LEG:getOriginRot().x*legMultiplier+angleAdd
	
		--front
		local frontLeft = max(restAngle, leftLegRot)
		local frontRight = max(restAngle, rightLegRot)
		
		handler.root.Front.FrontCenter:setRot(max(restAngle, leftLegRot*0.66, rightLegRot*0.66),frontRight-frontLeft,0)
		handler.root.Front.FrontLeft:setRot(frontLeft,0,0)
		handler.root.Front.FrontRight:setRot(frontRight,0,0)
		
		--back
		local backLeft = min(-restAngle, leftLegRot)
		local backRight = min(-restAngle, rightLegRot)
		
		handler.root.Back.BackCenter:setRot(min(-restAngle, leftLegRot*0.66, rightLegRot*0.66),backRight-backLeft,0)
		handler.root.Back.BackLeft:setRot(backLeft,0,0)
		handler.root.Back.BackRight:setRot(backRight,0,0)
		
		--corners
		handler.root.FrontLeftCorner:setRot(restAngle,max(45,frontLeft),0)
		.FrontLeftCornerPivot:setRot(0,0,max(0,frontLeft/5))
		
		handler.root.FrontRightCorner:setRot(restAngle,min(-45,-frontRight),0)
		.FrontRightCornerPivot:setRot(0,0,min(0,-frontRight/5))
		
		handler.root.BackLeftCorner:setRot(-restAngle,min(-45,backLeft),0)
		.BackLeftCornerPivot:setRot(0,0,max(0,-backLeft/5))
		
		handler.root.BackRightCorner:setRot(-restAngle,max(45,-backRight),0)
		.BackRightCornerPivot:setRot(0,0,min(0,backRight/5))
		
		--crouch adjust
		if player:isLoaded() then
			handler.root:setPos(player:getPose() == "CROUCHING" and handler.crouchOffset or vec(0,0,0)):setRot(-vanilla_model.BODY:getOriginRot().x,0,0)
		end
	end
	
	return handler
end

return skirtPhysics