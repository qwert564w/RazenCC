-- Auto generated script file --

-- Apple hammer
function events.item_render(item)
    if item.id:find("sword") then
        return models.apple_hammar.Item
    end
end
