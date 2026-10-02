-- Auto generated script file --

--  Axe
function events.item_render(item)
    if item.id:find("sword") then
        return models.thecooleraxe.Item
    end
end
