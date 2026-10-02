--[[

Down bellow you change "none" to what ever option you'd like
in the list provided. If you are savvy enough feel free to edit


none
small
medium
slimarms
large
giant

]]


local thing = {
  ["size"] = "none"
--CHANGE THE NONE HERE
}

-- NAMEPLATE AND POV
-- ONLY CHANGE THE CENTER VALUE 
nameplate.ENTITY:setPos(0, 0, 0)
renderer:setOffsetCameraPivot(0, 0, 0)
renderer:setEyeOffset(0,0,0)

--I F YOU WANT A CUSTOM SIZE EDIT THIS CODE
-- DON'T TOUCH THE FIRST LINE BELLOW THIS, EDIT SCALE TO WHAT EVER YOU LIKE.
      if thing.size == "none" then
models.MODELNAME:scale(1.0, 1.0, 1.0)


      elseif thing.size == "slimarms" then
models.MODELNAME:scale(1.0, 1.0, 1.0)
models.MODELNAME:setPos(0,0.5,0)
models.MODELNAME.Main.upper.Neck.Head:setScale(0.98,0.96,0.98)
models.MODELNAME.Main.upper.Neck.Head:setPos(0,-0.20,0)
models.MODELNAME.Main.upper.Body:setScale(0.9,1,1)
models.MODELNAME.Main.upper.Body:setPos(0,0,0)
models.MODELNAME.Main.upper.RightAnchor.RightArm:setScale(0.80,1,0.80)
models.MODELNAME.Main.upper.RightAnchor.RightArm:setPos(-0.40,-0.20,0)
models.MODELNAME.Main.upper.LeftAnchor.LeftArm:setScale(0.80,1,0.80)
models.MODELNAME.Main.upper.LeftAnchor.LeftArm:setPos(0.40,-0.20,0)
models.MODELNAME.Main.RightLeg:setScale(1,1,1)
models.MODELNAME.Main.RightLeg:setPos(0,0,0)
models.MODELNAME.Main.LeftLeg:setScale(1,1,1)
models.MODELNAME.Main.LeftLeg:setPos(0,0,0)


      elseif thing.size == "small" then
models.MODELNAME:scale(0.87, 0.87, 0.87)
nameplate.ENTITY:setPos(0, -0.3, 0)
renderer:setOffsetCameraPivot(0, -0.3, 0)
renderer:setEyeOffset(0,-0.3,0)

models.MODELNAME:setPos(0,0,0)

models.MODELNAME.Main.upper.Neck.Head:setPos(0,0.5,0)
models.MODELNAME.Main.upper.Body:setScale(1,0.96,1)
models.MODELNAME.Main.upper.Body:setPos(0,0.53,0)
models.MODELNAME.Main.upper.RightAnchor.RightArm:setScale(0.90,1,1)
models.MODELNAME.Main.upper.RightAnchor.RightArm:setPos(-0.20,0.40,0)
models.MODELNAME.Main.upper.LeftAnchor.LeftArm:setScale(0.90,1,1)
models.MODELNAME.Main.upper.LeftAnchor.LeftArm:setPos(0.20,0.40,0)
models.MODELNAME.Main.RightLeg:setScale(1,1.1,1)
models.MODELNAME.Main.RightLeg:setPos(0,1.6,0)
models.MODELNAME.Main.LeftLeg:setScale(1,1.1,1)
models.MODELNAME.Main.LeftLeg:setPos(0,1.6,0)
		
      elseif thing.size == "medium" then
nameplate.ENTITY:setPos(0,-0.3,0)
renderer:setOffsetCameraPivot(0,-0.3,0)
renderer:setEyeOffset(0,-0.3,0)
models.MODELNAME:scale(0.98, 0.97, 0.96)
models.MODELNAME:setPos(0,0.5,0)
models.MODELNAME.Main.upper.Neck.Head:setScale(0.97,0.99,0.99)
models.MODELNAME.Main.upper.Neck.Head:setPos(0,0.2,0)
models.MODELNAME.Main.upper.Body:setScale(0.94,1,1)
models.MODELNAME.Main.upper.RightAnchor.RightArm:setScale(0.9,1,1)
models.MODELNAME.Main.upper.RightAnchor.RightArm:setPos(-0.30,-0.30,0)
models.MODELNAME.Main.upper.LeftAnchor.LeftArm:setScale(0.9,1,1)
models.MODELNAME.Main.upper.LeftAnchor.LeftArm:setPos(0.30,-0.30,0)
models.MODELNAME.Main.RightLeg:setScale(1,1.1,1)
models.MODELNAME.Main.RightLeg:setPos(0.10,0,0)
models.MODELNAME.Main.LeftLeg:setScale(1,1.1,1)
models.MODELNAME.Main.RightLeg:setPos(-0.10,0,0)

      elseif thing.size == "large" then
models.MODELNAME:scale(1.2, 1.2, 1.2)
nameplate.ENTITY:setPos(0, 0.3, 0)
renderer:setOffsetCameraPivot(0, 0.3, 0)
renderer:setEyeOffset(0,0.3,0)

models.MODELNAME:setPos(0,0,0)
models.MODELNAME.Main.upper.Neck.Head:setScale(1,1,0.9)
models.MODELNAME.Main.upper.Neck.Head:setPos(0,-0.5,0)
models.MODELNAME.Main.upper.Body:setScale(1,0.9,1)
models.MODELNAME.Main.upper.Body:setPos(0,-0.5,0)
models.MODELNAME.Main.upper.RightAnchor.RightArm:setScale(1,1,1)
models.MODELNAME.Main.upper.RightAnchor.RightArm:setPos(0,-0.5,0)
models.MODELNAME.Main.upper.LeftAnchor.LeftArm:setScale(1,1,1)
models.MODELNAME.Main.upper.LeftAnchor.LeftArm:setPos(0,-0.5,0)
models.MODELNAME.Main.RightLeg:setScale(1,1.2,1)
models.MODELNAME.Main.LeftLeg:setScale(1,1.2,1)
models.MODELNAME.Main.RightLeg:setPos(0,1.5,0)
models.MODELNAME.Main.LeftLeg:setPos(0,1.5,0)

      elseif thing.size == "giant" then
models.MODELNAME:scale(1.3, 1.3, 1.3)
nameplate.ENTITY:setPos(0, 0.6, 0)
renderer:setOffsetCameraPivot(0, 0.6, 0)
renderer:setEyeOffset(0,0.6,0)

models.MODELNAME:setPos(0,0,0)
models.MODELNAME.Main.upper.Neck.Head:setScale(1,1,0.9)
models.MODELNAME.Main.upper.Neck.Head:setPos(0,-0.5,0)
models.MODELNAME.Main.upper.Body:setScale(1,0.9,1)
models.MODELNAME.Main.upper.Body:setPos(0,-0.5,0)
models.MODELNAME.Main.upper.RightAnchor.RightArm:setScale(1,1,1)
models.MODELNAME.Main.upper.RightAnchor.RightArm:setPos(0,-0.5,0)
models.MODELNAME.Main.upper.LeftAnchor.LeftArm:setScale(1,1,1)
models.MODELNAME.Main.upper.LeftAnchor.LeftArm:setPos(0,-0.5,0)
models.MODELNAME.Main.RightLeg:setScale(1,1.2,1)
models.MODELNAME.Main.LeftLeg:setScale(1,1.2,1)
models.MODELNAME.Main.RightLeg:setPos(0,1.5,0)
models.MODELNAME.Main.LeftLeg:setPos(0,1.5,0)
      end