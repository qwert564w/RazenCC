-- Auto generated script file --

--hide vanilla armor model
vanilla_model.ARMOR:setVisible(false)

local crownRoot = models.CrackedCrown.Head
crownRoot:setParentType("Head")
events.TICK:register(function()
    -- Keep the root anchored to head to prevent occasional X/Z drift.
    crownRoot:setPos(0, crownRoot:getPos().y, 0)
end)

animations.CrackedCrown.Idle:play()
