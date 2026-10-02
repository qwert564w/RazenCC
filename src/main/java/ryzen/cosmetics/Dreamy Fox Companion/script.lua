
page = action_wheel:newPage()
local fox = models.model.root.Head.fox.fox
local snow_fox = models.model.root.Head.fox.snow_fox
action_wheel:setPage(page)

function default()
    fox.setVisible(fox, true)
    snow_fox.setVisible(snow_fox, false)
    action_wheel:setPage(page)
end
function snow()
    fox.setVisible(fox, false)
    snow_fox.setVisible(snow_fox, true)
    action_wheel:setPage(page)
end

page:newAction()
    :title("Fox")
    :item("minecraft:orange_dye")
    :onLeftClick(default)
page:newAction()
    :title("Snow Fox")
    :item("minecraft:white_dye")
    :onLeftClick(snow)

