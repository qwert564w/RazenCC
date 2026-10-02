-- Auto generated script file --

-- Dragon Halberd
function events.item_render(item)
    if item.id:find("sword") then
        return models.halberdstuff.Item
    end
end
