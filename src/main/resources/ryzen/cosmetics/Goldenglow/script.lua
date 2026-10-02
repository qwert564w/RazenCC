vanilla_model. PLAYER:setVisible(false)
vanilla_model. ARMOR:setVisible(false)

local squapi = require("SquAPI")
local anims = require("JimmyAnims")
anims(animations.Goldenglow)
require("GSAnimBlend")

animations.Goldenglow.crouch:setBlendTime(2)
animations.Goldenglow.jumpup:setBlendTime(2)
animations.Goldenglow.sprintjumpup:setBlendTime(2)
animations.Goldenglow.walk:setBlendTime(4)
animations.Goldenglow.walkback:setBlendTime(4)
animations.Goldenglow.sprint:setBlendTime(4)

animations.Goldenglow.Drone:play()
animations.Goldenglow.StaticDrone:play()
animations.Goldenglow.StaticStaff:play()

--Head Location
HeadModel = (models.Goldenglow.CharModel.RootBody.Torso.FHead)

-- = Squishy's API Functions =

squapi.blink(animations.Goldenglow.blink, 2)

tail={
  models.Goldenglow.CharModel.RootBody.Torso.Body.TailBone1.TailBone2,
  models.Goldenglow.CharModel.RootBody.Torso.Body.TailBone1.TailBone2.TailBone3,
  models.Goldenglow.CharModel.RootBody.Torso.Body.TailBone1.TailBone2.TailBone3.TailBone4
}
squapi.tails(tail)

squapi.floatPoint(models.Goldenglow.Drone, nil, nil, nil, -20, nil, nil, 30)

-- = Snqwblind's Eye Movement =
function events.render()
  headRotX = (vanilla_model.HEAD:getOriginRot().x+180)%360-180
  headRotY = vanilla_model.HEAD:getOriginRot().y
  leftLegRot = vanilla_model.LEFT_LEG:getOriginRot().x
  rightLegRot = vanilla_model.RIGHT_LEG:getOriginRot().x
  leftArmRotX = vanilla_model.LEFT_ARM:getOriginRot().x
  rightArmRotX = vanilla_model.RIGHT_ARM:getOriginRot().x
  leftArmRotY = vanilla_model.LEFT_ARM:getOriginRot().y
  rightArmRotY = vanilla_model.RIGHT_ARM:getOriginRot().y
  leftArmRotZ = vanilla_model.LEFT_ARM:getOriginRot().z
  rightArmRotZ = vanilla_model.RIGHT_ARM:getOriginRot().z
end

function events.render()
  --Eye Movement
  if headRotY > 0 then
  HeadModel.eyes.eyes2.leftEye:setPos(math.clamp(headRotY / 50 * -1, -1, 0),0,0)
  HeadModel.eyes.eyes2.rightEye:setPos(math.clamp(headRotY / 100 * -1, -0.5, 0),0,0)
  else if headRotY < 0 then
    HeadModel.eyes.eyes2.leftEye:setPos(math.clamp(headRotY / 100 * -1, 0, 0.5),0,0)
    HeadModel.eyes.eyes2.rightEye:setPos(math.clamp(headRotY / 50 * -1, 0, 1),0,0)
  end
  end
  HeadModel.eyes.eyes2:setPos(0, math.clamp(headRotX / 100, -0.5, 0.5),0)
  HeadModel.eyes.eyelids:setPos(0, math.clamp(headRotX / 100, -0.25, 0.25),0)

  --Head Rotation
  HeadModel:setRot(math.clamp(headRotX / 1.5, -40, 40), math.clamp(headRotY / 1.5, -40, 40),0)

  --Leg Rotation
  models.Goldenglow.CharModel.RightLeg:setRot(math.clamp(rightLegRot / -2, -90, 90), 0 ,0)
  models.Goldenglow.CharModel.LeftLeg:setRot(math.clamp(leftLegRot / -2, -90, 90), 0 ,0)

  --Arm Rotation
  models.Goldenglow.CharModel.RootBody.Torso.RightArm:setRot(math.clamp(rightArmRotX / -2, -90, 90), nil ,20)
  models.Goldenglow.CharModel.RootBody.Torso.LeftArm:setRot(math.clamp(leftArmRotX / -2, -90, 90), nil ,-20)

  --Hair Strands
  HeadModel.Braid.BraidBone:setRot(math.clamp(-headRotX / 2,-30,30),0,0)
  HeadModel.Bangs:setRot(math.clamp(-headRotX / 2,0,15),0,0)
end

-- = Sword =
function events.item_render(item)
  if item.id:find("sword") then
      return models.Goldenglow.ItemStaff
  end
end