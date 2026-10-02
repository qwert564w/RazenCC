-- V2

local items = {}

local function getItem(item,itemid)
    return item.id:find(itemid) or item:getName():find(itemid)
end

local function isPlaying(equip,unequip)
    return (equip and equip:isPlaying()) or (unequip and unequip:isPlaying())
end

local complexList = {}

function events.render(_,ctx)
    vanilla_model.RIGHT_ITEM:setVisible(true)
    vanilla_model.RIGHT_ITEM:setVisible(true)
    for _, value in pairs(complexList) do
        if value.right then
            vanilla_model.RIGHT_ITEM:setVisible(ctx == "FIRST_PERSON" or false)
        end
        if value.left then
            vanilla_model.LEFT_ITEM:setVisible(ctx == "FIRST_PERSON" or false)
        end
    end
end

---@param itemtype string
---@param extrapart ModelPart | table
---@param firstpart ModelPart
---@param thirdpartright ModelPart | table
---@param thirdpartleft ModelPart | table
---@param rightequip Animation
---@param rightunequip Animation
---@param leftequip Animation
---@param leftunequip Animation
function items:complexReplace(itemtype,extrapart,firstpart,thirdpartright,thirdpartleft,rightequip,rightunequip,leftequip,leftunequip)
    if type(itemtype) ~= "string" then
        error("Provided item to replace is not a string",2)
    end
    if type(firstpart) ~= "ModelPart" then
        error("Provided first person part isn't a modelpart",2)
    end
    if type(thirdpartright) ~= "ModelPart" and type(thirdpartright) ~= "table" then
        error("Provided third person right hand part isn't a modelpart or a table",2)
    end
    if type(thirdpartleft) ~= "ModelPart" and type(thirdpartleft) ~= "table" then
        error("Provided third person left hand part isn't a modelpartor a table",2)
    end

    complexList[itemtype] = {}

    if rightequip then rightequip:setLoop("ONCE") end
    if rightunequip then rightunequip:setLoop("ONCE") end
    if leftequip then leftequip:setLoop("ONCE") end
    if leftunequip then leftunequip:setLoop("ONCE") end

    local expart = type(extrapart) == "table" and extrapart or {extrapart}
    local rpart = type(thirdpartright) == "table" and thirdpartright or {thirdpartright}
    local lpart = type(thirdpartleft) == "table" and thirdpartleft or {thirdpartleft}

    firstpart:setParentType("Item")
    events.item_render:remove(itemtype.."Complex")
    events.item_render:register(
        function(item,mode)
            if getItem(item,itemtype) and mode:find("FIRST") then
                return firstpart
            end
        end,
    itemtype.."Complex")

    local oldrighthold
    local oldlefthold
    events.entity_init:remove(itemtype.."Complex")
    events.entity_init:register(
        function()
            local lefty = player:isLeftHanded()
            oldrighthold = getItem(player:getHeldItem(lefty),itemtype)
            oldlefthold = getItem(player:getHeldItem(not lefty),itemtype)
        end,
    itemtype.."Complex")
    
    events.render:remove(itemtype.."Complex")
    events.render:register(
        function()
            local lefty = player:isLeftHanded()
            local righthold = getItem(player:getHeldItem(lefty),itemtype)
            local lefthold = getItem(player:getHeldItem(not lefty),itemtype)
            complexList[itemtype].right = righthold or false
            complexList[itemtype].left = lefthold or false

            for _,ex in pairs(expart) do
                ex:setVisible((not (righthold or lefthold)) or isPlaying(rightequip,rightunequip) or isPlaying(leftequip,leftunequip))
            end

            for _, right in pairs(rpart) do
                right:setVisible(righthold or isPlaying(rightequip,rightunequip))
            end
            for _,left in pairs(lpart) do
                left:setVisible(lefthold or isPlaying(leftequip,leftunequip))
            end

            local newrighthold = righthold
            if oldrighthold ~= newrighthold then
                for _, right in pairs(rpart) do
                    right:setVisible(oldrighthold)
                end
                for _,ex in pairs(expart) do
                    ex:setVisible(newrighthold) 
                end
                if rightequip then rightequip:setPlaying(newrighthold) end
                if rightunequip then rightunequip:setPlaying(oldrighthold) end
            end
            oldrighthold = newrighthold

            local newlefthold = lefthold
            if oldlefthold ~= newlefthold then
                for _,left in pairs(lpart) do
                    left:setVisible(oldlefthold)
                end
                for _,ex in pairs(expart) do
                    ex:setVisible(newlefthold) 
                end
                if leftequip then leftequip:setPlaying(newlefthold) end
                if leftunequip then leftunequip:setPlaying(oldlefthold) end
            end
            oldlefthold = newlefthold
        end,
    itemtype.."Complex")
    return self
end

---@param itemtype string
---@param newparts ModelPart
---@param extrapart ModelPart | table
function items:simpleReplace(itemtype,newparts,extrapart)
    if type(itemtype) ~= "string" then
        error("Provided item to replace is not a string",2)
    end
    if type(newparts) ~= "ModelPart" then
        error("Provided replacement item is not a modelpart",2)
    end
    newparts:setParentType("Item")
    events.item_render:remove(itemtype.."Simple")
    events.item_render:register(
        function(item)
            if getItem(item,itemtype) then
                return newparts
            end
        end,
    itemtype.."Simple")
    if not extrapart then return self end
    local part = type(extrapart) == "table" and extrapart or {extrapart}
    events.tick:remove(itemtype.."Simple")
    events.tick:register(
        function()
            for _, value in pairs(part) do
                value:setVisible(not (getItem(player:getHeldItem(),itemtype) or getItem(player:getHeldItem(true),itemtype)))
            end
        end,
    itemtype.."Simple")
    return self
end

return items