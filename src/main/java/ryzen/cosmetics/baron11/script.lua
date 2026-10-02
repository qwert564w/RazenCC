--hide vanilla model
vanilla_model.PLAYER:setVisible(false)

--hide vanilla armor model
vanilla_model.HELMET:setVisible(true)
vanilla_model.CHESTPLATE:setVisible(false)
vanilla_model.LEGGINGS:setVisible(false)
vanilla_model.BOOTS:setVisible(false)


--hide vanilla cape model
vanilla_model.CAPE:setVisible(false)

--hide vanilla elytra model
vanilla_model.ELYTRA:setVisible(true)






local anims = require('JimmyAnims')
anims.excluBlendTime = 0
anims.incluBlendTime = 0
anims.autoBlend = false
anims.dismiss = false
anims.addExcluOverrider()
anims.addIncluOverrider()
anims.addAllOverrider()
anims(animations.model)

-- Player anim vars
function events.tick()


end




--entity init event, used for when the avatar entity is loaded for the first time
function events.entity_init()
  --player functions goes here
end

--tick event, called 20 times per second
function events.tick()
  --code goes here
end

--render event, called every time your avatar is rendered


-- function events.render(delta, context)
  --code goes here
-- end

local mainPage = action_wheel:newPage()  
action_wheel:setPage(mainPage)  

function pings.bunnyYawn()  
    animations.model.a1:play()  
end  

function pings.bunnyYa()  	
local crouching = player:getPose() == "CROUCHING"
local walking = player:getVelocity().xz:length() > .01
local sprinting = player:isSprinting()

  animations.model.walk:play(walking and not crouching)
end  

local action = mainPage:newAction()  
    :title("*Yawn*")  
    :item("minecraft:wind_charge")  
    :hoverColor(1, 0, 1)  
    :onLeftClick(pings.bunnyYawn) 
	
local action = mainPage:newAction()  
    :title("DISABLE custom Walk anim")  
	:item("minecraft:leather_boots")  
	:toggleTitle("ENABLE custom Walk anim")  
    :toggleItem("minecraft:barrier")  
    :setOnToggle(pings.bunnyYa) 
	

	
	
	
	
-- VANILLA animation Replace
	


-- Visual height change

function events.render()  
  if player:isVisuallySwimming() then
    models.model:setPos(0,12,0)
    renderer:offsetCameraPivot(0,0,0)
    renderer:setEyeOffset(0,0,0)
else
  if player:isCrouching() then
      models.model:setPos(0,0,0)
      models.model:offsetRot(0,0,0)
      models.model.root:setPos(0,2,0)
      models.model.root:setOffsetPivot(0,-4,0)
      renderer:offsetCameraPivot(0,-0.75,0)
      renderer:setEyeOffset(0,-1.05,0)
  else
      models.model:setPos(0,0,0)
      models.model:offsetRot(0,0,0)
      models.model.root:setPos(0,0,0)
      models.model.root:setOffsetPivot(0,0,0)
      renderer:offsetCameraPivot(0,-0.55,0)
      renderer:setEyeOffset(0,-1.25,0)
  end
end
end