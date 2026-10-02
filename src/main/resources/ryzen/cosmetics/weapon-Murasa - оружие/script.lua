vanilla_model.PLAYER:setVisible(false)
vanilla_model.CAPE:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)

pose = false
coatvis = true

-- Jimmy Anims: https://github.com/JimmyHelp/JimmyAnims
local anims = require("JimmyAnims")
anims(animations.murasa)

function events.entity_init()
end

function events.tick() 
     checkHair()
    
    if pose then
        animations.murasa.fly:setPlaying(not pose)
    end

    if player:getItem(5).id ~= "minecraft:elytra" then
        models.murasa.Body.Elytra:setVisible(false)
        if coatvis then
            models.murasa.Body.Coat:setVisible(true)
        end
    else
        models.murasa.Body.Elytra:setVisible(true)
        if coatvis then
            models.murasa.Body.Coat:setVisible(false)
        end
    end
end

function events.render(delta, context)
    if context:find("FIRST") then
        local holdingSword = player:getHeldItem(true).id:find("sword") ~= nil
        if player:isLeftHanded() and not holdingSword then
            models.murasa.ItemAnchor:setPos(-10,-10,-10)
        elseif player:isLeftHanded() or not holdingSword then
            models.murasa.ItemAnchor:setPos(5,-10,-10)
        else
            models.murasa.ItemAnchor:setPos(10,-10,-10)
        end
    else
        models.murasa.ItemAnchor:setPos(0,0,0)
    end
end

function events.skull_render(delta, blockstate, item, entity, context)
end

function events.item_render(item)
    if item.id:find("sword") then
        return models.murasa.ItemAnchor --Change this if you want
    end
end

------------------------ Action Wheel ----------------------------
local mainPage = action_wheel:newPage()
local secondPage = action_wheel:newPage()
action_wheel:setPage(mainPage)

--Swap to second page--
local toSecond = mainPage:newAction()
    :title("Change To Second Page")
    :item("glow_item_frame")
    :onLeftClick(function()
    action_wheel:setPage(secondPage)
    end)

--Gives the player the player head
--REQUIRES ENABLING CHAT MESSAGES IN FIGURA SETTINGS
function pings.getfumo(state)
    name = player:getName()
    --print(name)
    sounds:playSound("fumo",player:getPos())
    
    local version = tonumber(client:getVersion():sub(1, -3))
    --print(version)
    if version >= 1.21 then
        host:sendChatCommand("give @s minecraft:player_head[profile={name:".. name .."}]")
    elseif version <= 1.20 then
        host:sendChatCommand("give @s minecraft:player_head{SkullOwner:".. name .."}")
    end
end

local playsoundaction = mainPage:newAction()
    :title("Get Fumo")
    :item("minecraft:player_head")
    :onLeftClick(pings.getfumo)


--Toggles the coat
function pings.coatToggle(state)
    sounds:playSound("minecraft:item.armor.equip_leather", player:getPos())
    models.murasa.Body.Coat:setVisible(not state)
    coatvis = not coatvis
end

local coataction = mainPage:newAction()
    :title("Remove Coat")
    :toggleTitle("Show Coat")
    :item("barrier")
    :toggleItem("leather_chestplate")
    :setOnToggle(pings.coatToggle) 

--Toggles the hat
function pings.hatToggle(state)
    sounds:playSound("minecraft:item.armor.equip_leather", player:getPos())
    models.murasa.Head.Hat:setVisible(not state)
end

local hataction = mainPage:newAction()
    :title("Remove Hat")
    :toggleTitle("Show Hat")
    :item("barrier")
    :toggleItem("leather_helmet")
    :setOnToggle(pings.hatToggle) 

--------------------------------------------------------------------
--Second Page--
--------------------------------------------------------------------
--Swap to main page--
local toMain = secondPage:newAction()
    :title("Change To Main Page")
    :item("item_frame")
    :onLeftClick(function()
    action_wheel:setPage(mainPage)
end)

--Toggles custom item
function pings.itemtoggle(state)
    itemrender = not item_render
    models.murasa.ItemAnchor:setVisible(not state)
end

local itemaction = secondPage:newAction()
    :title("Hide custom item")
    :toggleTitle("Show custom item")
    :item("wooden_sword")
    :toggleItem("diamond_sword")
    :setOnToggle(pings.itemtoggle) 

------------------------ Functions ----------------------------

--Checks head rotation and rotates hood accordingly to not clip through body
function checkHair()
    local headRot = vanilla_model.HEAD:getOriginRot()

    if coatvis then
        models.murasa.Body.Coat.Hood:setRot((headRot.x + 5),0,0)
    end
end
