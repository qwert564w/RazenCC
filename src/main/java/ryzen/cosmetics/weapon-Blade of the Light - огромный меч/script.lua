-- Auto generated script file --

-- Holy Greatsword
function events.item_render(item)
    if item.id:find("sword") then
        return models.thegreatersword.Item
    end
end
