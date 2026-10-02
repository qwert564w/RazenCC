vanilla_model.PLAYER:setVisible(false)
vanilla_model.CAPE:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)

pose = false
tail = true

-- Jimmy Anims: https://github.com/JimmyHelp/JimmyAnims
local anims = require("JimmyAnims")
anims(animations.momiji)

function events.entity_init()
end

function events.tick() 
    if pose then
        animations.momiji.fly:setPlaying(not pose)
    end  

    fixModelParts()

    if player:getItem(5).id ~= "minecraft:elytra" then
        models.momiji.Body.Elytra:setVisible(false)
        if tail then
            models.momiji.Body.Tail:setVisible(true)
        end
    else
        models.momiji.Body.Elytra:setVisible(true)
        models.momiji.Body.Tail:setVisible(false)
    end
end

function events.render(delta, context)
    if context:find("FIRST") then
        local holdingSword = player:getHeldItem(true).id:find("sword") ~= nil
        if player:isLeftHanded() and not holdingSword then
            models.momiji.ItemSword:setPos(-10,-10,-10)
        elseif player:isLeftHanded() or not holdingSword then
            models.momiji.ItemSword:setPos(5,-10,-10)
        else
            models.momiji.ItemSword:setPos(10,-10,-10)
        end
    else
        models.momiji.ItemSword:setPos(0,0,0)
    end
end

function events.skull_render(delta, blockstate, item, entity, context)
end

function events.item_render(item)
    if item.id:find("sword") then  --Change this if you want
        return models.momiji.ItemSword
    end
    
    if item.id:find("shield") then  --Change this if you want
        return models.momiji.ItemShield
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

--Toggles the ears
function pings.earToggle(state)
    sounds:playSound("minecraft:item.armor.equip_leather", player:getPos())
    models.momiji.Head.LeftEar:setVisible(not state)
    models.momiji.Head.RightEar:setVisible(not state)
    models.momiji.Body.Tail:setVisible(not state)
    tail = not tail
end

local earaction = mainPage:newAction()
    :title("Hide Animal Parts")
    :toggleTitle("Show Animal Parts")
    :item("villager_spawn_egg")
    :toggleItem("wolf_spawn_egg")
    :setOnToggle(pings.earToggle) 

--Toggles the hat
function pings.hatToggle(state)
    sounds:playSound("minecraft:item.armor.equip_leather", player:getPos())
    models.momiji.Head.Hat:setVisible(not state)
end

local hataction = mainPage:newAction()
    :title("Remove Hat")
    :toggleTitle("Show Hat")
    :item("barrier")
    :toggleItem("leather_helmet")
    :setOnToggle(pings.hatToggle) 

--Toggles fly animation
function pings.flytoggle(state)
    pose = not pose
    animations.momiji.pose:setPlaying(state)
end

local flyposeaction = mainPage:newAction()
    :title("Enable Pose")
    :toggleTitle("Disable Pose")
    :item("armor_stand")
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
    models.momiji.ItemSword:setVisible(not state)
    models.momiji.ItemShield:setVisible(not state)
end

local itemaction = secondPage:newAction()
    :title("Hide custom items")
    :toggleTitle("Show custom items")
    :item("wooden_sword")
    :toggleItem("diamond_sword")
    :setOnToggle(pings.itemtoggle) 

--------------------------------------------------------------------
--Functions--
--------------------------------------------------------------------

--Fixes several model part problems
function fixModelParts()
    models.momiji.Head.LeftEar.ear:setPrimaryRenderType("TRANSLUCENT_CULL")
    models.momiji.Head.RightEar.ear:setPrimaryRenderType("TRANSLUCENT_CULL")

    models.momiji.Skull.ear1:setPrimaryRenderType("TRANSLUCENT_CULL")
    models.momiji.Skull.ear2:setPrimaryRenderType("TRANSLUCENT_CULL")

    if player:isLeftHanded() and player:getHeldItem(false).id == "minecraft:shield" and not animations.momiji.fly:isPlaying() then --left hand not fly, no offhand
        models.momiji.ItemShield:setRot(0,-270,0)
    elseif player:isLeftHanded() and player:getHeldItem(false).id == "minecraft:shield" and animations.momiji.fly:isPlaying() then --left hand fly, no offhand
        models.momiji.ItemShield:setRot(-90,180,-90)
    elseif player:isLeftHanded() and player:getHeldItem(true).id == "minecraft:shield" and not animations.momiji.fly:isPlaying() then --left hand not fly, offhand
        models.momiji.ItemShield:setRot(0,-90,0)
    elseif player:isLeftHanded() and player:getHeldItem(true).id == "minecraft:shield" and animations.momiji.fly:isPlaying() then  --left hand fly, offhand
        models.momiji.ItemShield:setRot(0,-90,0)
    elseif player:getHeldItem(true).id == "minecraft:shield" and not animations.momiji.fly:isPlaying() then  --right hand not fly, offhand
        models.momiji.ItemShield:setRot(0,-270,0)
    elseif player:getHeldItem(true).id == "minecraft:shield" and animations.momiji.fly:isPlaying() then  --right not fly, offhand
        models.momiji.ItemShield:setRot(270,180,270)
    else
        models.momiji.ItemShield:setRot(0,-90,0)
    end
end
