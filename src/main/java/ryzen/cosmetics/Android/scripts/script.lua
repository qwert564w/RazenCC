-- Auto generated script file --

--hide vanilla model
vanilla_model.PLAYER:setVisible(false)
vanilla_model.ARMOR:setVisible(false)
vanilla_model.CAPE:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)

local squapi = require("scripts.SquAPI")

--sets viewheight to eyelevel upon start
renderer:offsetCameraPivot(0,0.5,0)
renderer:setEyeOffset(0,0.5,0)

local mainPage = action_wheel:newPage()
action_wheel:setPage(mainPage)

--toggles between real view height and model view height
local toggleaction = mainPage:newAction()
:title("Regular View-height")
:toggleTitle("Android View-Height")
:item("skeleton_skull")
:toggleItem("lime_concrete")
:setOnToggle(function(state)
  if state then
      renderer:offsetCameraPivot(0,0,0)
      renderer:setEyeOffset(0,0,0)
  else
      renderer:offsetCameraPivot(0,0.5,0)
      renderer:setEyeOffset(0,0.5,0)
  end
end)


local myTail = {
    models.android.Body.Tail,
    models.android.Body.Tail.tailseg1
}
squapi.tail:new(myTail,
    1,    --(15) idleXMovement
    8,    --(5) idleYMovement
    nil,    --(1.2) idleXSpeed
    1,    --(2) idleYSpeed
    nil,    --(2) bendStrength
    nil,    --(0) velocityPush
    nil,    --(0) initialMovementOffset
    nil,    --(1) offsetBetweenSegments
    nil,    --(.005) stiffness
    nil,    --(.9) bounce
    nil,    --(90) flyingOffset
    nil,    --(-90) downLimit
    nil     --(45) upLimit
)

squapi.ear:new(
    models.android.Head.LeftEar,
    models.android.Head.RightEar,
    nil, --(1) rangeMultiplier
    nil, --(false) horizontalEars
    4, --(2) bendStrength
    false, --(true) doEarFlick
    nil, --(400) earFlickChance
    nil, --(0.1) earStiffness
    nil  --(0.8) earBounce
)