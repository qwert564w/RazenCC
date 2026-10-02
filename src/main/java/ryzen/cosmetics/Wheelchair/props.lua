------------------------------ Custom Code ------------------------------
function events.item_render(item)
    if item.id == "minecraft:iron_sword"
            and item:getName() == "Iron Sword" then
        return models.weapons.sword.Item
    elseif item.id == "minecraft:iron_sword" then
        return 
    end
end


function events.item_render(item)
    if item.id == "minecraft:trident"
            and item:getName() == "Trident" then
        return models.weapons.trident.Item
    elseif item.id == "minecraft:trident" then
        return 
    end
end

function events.item_render(item)
    if item.id == "minecraft:bow"
            and item:getName() == "Bow" then
        return models.weapons.bow.Item
    elseif item.id == "minecraft:bow" then
        return 
    end
end



--SWORD
function isSword(item)
    return item.id == "minecraft:iron_sword"
end


local showSwordonback = false
function events.tick()
    local main = player:getHeldItem()
    local off = player:getHeldItem(true)
    local holdingSword = isSword(main) or isSword(off)
if showSwordonback then 
models.MODELNAME.Main.upper.Body.Stomach.Sword2:setVisible(not holdingSword)
end
end

function pings.ironsword(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Body.Stomach.Sword2:visible(true)
  else
	models.MODELNAME.Main.upper.Body.Stomach.Sword2:visible(false)
  end
showSwordonback = bool
end


--BOW
function isBow(item)
    return item.id == "minecraft:bow"
end


local showBowonback = false
function events.tick()
    local main = player:getHeldItem()
    local off = player:getHeldItem(true)
    local holdingBow = isBow(main) or isBow(off)
if showBowonback then 
models.MODELNAME.Main.upper.Body.Stomach.Bow2:setVisible(not holdingBow)
end
end

function pings.bow(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Body.Stomach.Bow2:visible(true)
  else
	models.MODELNAME.Main.upper.Body.Stomach.Bow2:visible(false)
  end
showBowonback = bool
end


--TRIDENT
function isTrident(item)
    return item.id == "minecraft:trident"
end


local showTridentonback = false
function events.tick()
    local main = player:getHeldItem()
    local off = player:getHeldItem(true)
    local holdingTrident = isTrident(main) or isTrident(off)
if showTridentonback then 
models.MODELNAME.Main.upper.Body.Stomach.Glaive2:setVisible(not holdingTrident)
end
end

function pings.trident(bool)
  if bool == true then 
	models.MODELNAME.Main.upper.Body.Stomach.Glaive2:visible(true)
  else
	models.MODELNAME.Main.upper.Body.Stomach.Glaive2:visible(false)
  end
showTridentonback = bool
end


-------------------------------------------------------------

function events.TICK()
  local item = player:getItem(1).id
  local book = item == "minecraft:written_book"
  local book2 = item == "minecraft:writable_book"
  local book3 = item == "minecraft:book"
  local sword = item == "minecraft:iron_sword"
   local bow = item == "minecraft:bow"
  local trident = item == "minecraft:trident"



  animations.MODELNAME.book:setPlaying(book2)
  animations.MODELNAME.book2:setPlaying(book)
  models.MODELNAME.Main.upper.Body.Chest.Book2:visible(book2)
  models.MODELNAME.Main.upper.Body.Chest.Book3:visible(book)


  vanilla_model.RIGHT_ITEM:setVisible(not (book or book2))
end

function events.TICK()
  local item = player:getItem(2).id
  local sword = item == "minecraft:iron_sword"



  animations.MODELNAME.hold:setPlaying(sword)
end




