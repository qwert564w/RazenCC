vanilla_model.PLAYER:setVisible(false)
vanilla_model.CAPE:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)

pose = false

-- Jimmy Anims: https://github.com/JimmyHelp/JimmyAnims
local anims = require("JimmyAnims")
anims(animations.yori)

function events.entity_init()
end

function events.tick() 
    if pose then
        animations.yori.fly:setPlaying(not pose)
    end

    if player:getItem(5).id ~= "minecraft:elytra" then
        models.yori.Body.Elytra:setVisible(false)
    else
        models.yori.Body.Elytra:setVisible(true)
    end
end

function events.render(delta, context)
    if context:find("FIRST") then
        local holdingSword = player:getHeldItem(true).id:find("sword") ~= nil
        if player:isLeftHanded() and not holdingSword then
            models.yori.ItemSword:setPos(-10,-10,-10)
        elseif player:isLeftHanded() or not holdingSword then
            models.yori.ItemSword:setPos(5,-10,-10)
        else
            models.yori.ItemSword:setPos(10,-10,-10)
        end
    else
        models.yori.ItemSword:setPos(0,0,0)
    end
end

function events.skull_render(delta, blockstate, item, entity, context)
end

function events.item_render(item)
    if item.id:find("sword") then
        return models.yori.ItemSword --Change this if you want
    end
end


------------------------ Action Wheel ----------------------------
local mainPage = action_wheel:newPage()
local secondPage = action_wheel:newPage()

action_wheel:setPage(mainPage)
--------------------------------------------------------------------
--First Page--
--------------------------------------------------------------------


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


--Toggles fly animation
function pings.flytoggle(state)
    pose = not pose
    animations.yori.pose:setPlaying(state)
end

local flyposeaction = mainPage:newAction()
    :title("Enable Flying Pose")
    :toggleTitle("Disable Flying Pose")
    :item("feather")
    :toggleItem("barrier")
    :setOnToggle(pings.flytoggle) 

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
    models.yori.ItemSword:setVisible(not state)
end

local itemaction = secondPage:newAction()
    :title("Hide custom item")
    :toggleTitle("Show custom item")
    :item("wooden_sword")
    :toggleItem("diamond_sword")
    :setOnToggle(pings.itemtoggle) 
