-- Auto generated script file --

--hide vanilla armor model
vanilla_model.ARMOR:setVisible(false)

--hide vanilla cape model
vanilla_model.CAPE:setVisible(false)

--hide vanilla elytra model
vanilla_model.ELYTRA:setVisible(false)

vanilla_model.PLAYER:setVisible(false)

models.model:setPrimaryRenderType("TRANSLUCENT_CULL") 

models:setSecondaryRenderType("EMISSIVE")

function events.tick()
    local walking = player:getVelocity().xz:length() > .01

    animations.model.animation:setPlaying(walking and not crouching and not sprinting)
end

local mainPage = action_wheel:newPage()
action_wheel:setPage(mainPage)

function pings.helmettoggle7(toggle)
    if toggle then
        pings.changetexturelit()
    else
        pings.changetexturecola()
    end
end

function pings.changetexturelit()
    models.model.cola:setPrimaryTexture("Custom", textures["cola"])
    sounds:playSound("openBanky", player:getPos(), 1, 1)
end

function pings.changetexturecola()
    models.model.cola:setPrimaryTexture("Custom", textures["litenergy"])
    sounds:playSound("openBanky", player:getPos(), 1, 1)
end

function pings.changetexturemd()
    models.model.cola:setPrimaryTexture("Custom", textures["mountaindew"])
    sounds:playSound("openBanky", player:getPos(), 1, 1)
end

--local toggleaction = mainPage:newAction()
    --        :title(toJson({
   --     {text = "The Cola", color = "#3ECDED"},
  --    }))
  --  :toggleTitle(toJson({
  --      {text = "The litenergy", color = "#EDE83E"},
  --    }))
  --  :item("minecraft:stone")
  --  :setOnToggle(pings.helmettoggle7)

local md = mainPage:newAction()
      :title("MountainDew")
      :item("minecraft:grass_block")
      :onLeftClick(pings.changetexturemd)

local litenergy = mainPage:newAction()
      :title("litenergy")
      :item("minecraft:stone")
      :onLeftClick(pings.changetexturecola)

local cola = mainPage:newAction()
      :title("CokaCola")
      :item("minecraft:grass_block")
      :onLeftClick(pings.changetexturelit)

