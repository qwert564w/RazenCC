-- Blahaj Sword Avatar Script for Figura


models.items.ItemBlahaj:setVisible(false)
function events.item_render(item)
    local isSword = item.id:find("sword") ~= nil
    models.items.ItemBlahaj:setVisible(isSword)
    if isSword then
        return models.items.ItemBlahaj
    end
end
