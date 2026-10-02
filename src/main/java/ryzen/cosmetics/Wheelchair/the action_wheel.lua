--animations that are always playing
local anims = require("JimmyAnims")
anims(animations.MODELNAME)

--enable the single things that we still want visible.
vanilla_model.ALL:setVisible(false)
vanilla_model.ARMOR:setVisible(false)
vanilla_model.HELMET_ITEM:setVisible(true)
vanilla_model.PARROTS:setVisible(true)
vanilla_model.HELD_ITEMS:setVisible(true)


-- ANIMATION BLEND
animations.MODELNAME.book:blendTime(2)
animations.MODELNAME.book2:blendTime(2)
animations.MODELNAME.hold:blendTime(2)




-- Defining Pages
action_wheel:setPage(mainPage)
local mainPage= action_wheel:newPage()
local sittingPage = action_wheel:newPage()
local posePage = action_wheel:newPage()
local outfitPage = action_wheel:newPage()
local cosmeticPage = action_wheel:newPage()
-- MainPage Buttons
mainPage:newAction():title("Sitting Animations"):item("minecraft:spruce_sapling"):onLeftClick(function(state, self)
    action_wheel:setPage(sittingPage)
end)

mainPage:newAction():title("Pose Page"):item("minecraft:oak_sapling"):onLeftClick(function(state, self)
    action_wheel:setPage(posePage)

end)
mainPage:newAction():title("Accessories Page"):item("minecraft:dark_oak_sapling"):onLeftClick(function(state, self)
    action_wheel:setPage(cosmeticPage)

end)
mainPage:newAction():title("Outfit Page"):item("minecraft:jungle_sapling"):onLeftClick(function(state, self)
    action_wheel:setPage(outfitPage)
end)


--DON'T TOUCH THIS ONE
outfitPage:newAction():title("Go back"):item("minecraft:structure_void"):onLeftClick(function(state, self)
    action_wheel:setPage(mainPage)
end)

-- EQUIP FACE
outfitPage:newAction():title("Moving Face"):item("minecraft:white_tulip"):toggled(false):onToggle(function(state, self)
  pings.face(state)
end)

function pings.face(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Neck.Head.skin.Face1:visible(true)
	models.MODELNAME.Main.upper.Neck.Head.skin.Face2:visible(false)
  else
	models.MODELNAME.Main.upper.Neck.Head.skin.Face1:visible(false)
	models.MODELNAME.Main.upper.Neck.Head.skin.Face2:visible(true)
  end
end

-- EQUIP TEARS
outfitPage:newAction():title("Ears"):item("minecraft:ghast_tear"):toggled(false):onToggle(function(state, self)
  pings.ears(state)
end)

function pings.ears(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Neck.Head.EARS:visible(true)
  else
	models.MODELNAME.Main.upper.Neck.Head.EARS:visible(false)
  end
end

-- EQUIP TEARS
outfitPage:newAction():title("Horns"):item("minecraft:goat_horn"):toggled(false):onToggle(function(state, self)
  pings.HORNS(state)
end)

function pings.HORNS(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Neck.Head.HORNS:visible(true)
  else
	models.MODELNAME.Main.upper.Neck.Head.HORNS:visible(false)
  end
end


-- EQUIP BUN
outfitPage:newAction():title("Bun"):item("minecraft:honeycomb"):toggled(false):onToggle(function(state, self)
  pings.BUN(state)
end)

function pings.BUN(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Neck.Head.BUN:visible(true)
  else
	models.MODELNAME.Main.upper.Neck.Head.BUN:visible(false)
  end
end

-- EQUIP WINGS
outfitPage:newAction():title("Tail"):item("minecraft:spider_eye"):toggled(false):onToggle(function(state, self)
  pings.Tail(state)
end)

function pings.Tail(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Body.Stomach.TAIL:visible(true)
  else
	models.MODELNAME.Main.upper.Body.Stomach.TAIL:visible(false)
  end
end

-- EQUIP WINGS
outfitPage:newAction():title("Wings"):item("minecraft:elytra"):toggled(false):onToggle(function(state, self)
  pings.Wings(state)
end)

function pings.Wings(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Body.Chest.WINGS:visible(true)
  else
	models.MODELNAME.Main.upper.Body.Chest.WINGS:visible(false)
  end
end

action_wheel:setPage(mainPage)


-- COSMETIC PAGE
 
--DON'T TOUCH THIS ONE
cosmeticPage:newAction():title("Go back"):item("minecraft:structure_void"):onLeftClick(function(state, self)
    action_wheel:setPage(mainPage)
end)


-- POTION BOTTLE
cosmeticPage:newAction():title("Potion Bottle"):item("minecraft:glass_bottle"):toggled(false):onToggle(function(state, self)
  pings.bottle(state)
end)

function pings.bottle(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Body.Stomach.Viles:visible(true)
  else
	models.MODELNAME.Main.upper.Body.Stomach.Viles:visible(false)
  end
end

models.MODELNAME.Main.upper.Body.Stomach.Viles:setScale(0.5,0.7,0.5)
models.MODELNAME.Main.upper.Body.Stomach.Viles:setPos(-1.1,-2,-1.9)





-- EQUIP HAT
cosmeticPage:newAction():title("Black Witch Hat"):item("minecraft:red_candle"):toggled(false):onToggle(function(state, self)
  pings.hat(state)
end)

function pings.hat(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Neck.Head.WitchHats.Hat:visible(true)
	models.MODELNAME.Main.upper.Neck.Head.HORNS:visible(false)
	models.MODELNAME.Main.upper.Neck.Head.BUN:visible(false)
  else
	models.MODELNAME.Main.upper.Neck.Head.WitchHats.Hat:visible(false)
	models.MODELNAME.Main.upper.Neck.Head.HORNS:visible(true)
	models.MODELNAME.Main.upper.Neck.Head.BUN:visible(true)
	
  end
end





--POSE PAGE 
--DON'T TOUCH THIS ONE
posePage:newAction():title("Go back"):item("minecraft:structure_void"):onLeftClick(function(state, self)
    action_wheel:setPage(mainPage)
end)


-- HAIR TUG
posePage:newAction():title("Debby Ryan"):item("minecraft:brown_mushroom"):toggled(false):onToggle(function(state, self)
    pings.hairtug(state)
end)

function pings.hairtug(bool)
  if bool == true then 
    animations.MODELNAME.hairtug:setPlaying(true)
	chainhold1 = true
  else
	animations.MODELNAME.hairtug:setPlaying(false)
  end
end


-- WAVE
posePage:newAction():title("Wave"):item("minecraft:sea_pickle"):onLeftClick(function(state, self)
    pings.wave(state)
end)

function pings.wave()
    animations.MODELNAME.wave:play()
end

-- SITTING PAGE 
sittingPage:newAction():title("Go back"):item("minecraft:structure_void"):onLeftClick(function(state, self)
    action_wheel:setPage(mainPage)
end)


--NODOFF
sittingPage:newAction():title("Nod Off"):item("minecraft:white_bed"):toggled(false):onToggle(function(state, self)
    pings.nodoff(state)
end)

function pings.nodoff(bool)
  if bool == true then 
    animations.MODELNAME.nodoff:setPlaying(true)
	animations.MODELNAME.idle:setPlaying(false)
  else
	animations.MODELNAME.nodoff:setPlaying(false)
	animations.MODELNAME.idle:setPlaying(true)

  end
end



function events.post_render()

    if user:getVehicle() and user:getVehicle():getType() == "minecraft:armor_stand" then
        animations.MODELNAME.defaultsit:play()
    else
        animations.MODELNAME.defaultsit:stop()
    end
end





