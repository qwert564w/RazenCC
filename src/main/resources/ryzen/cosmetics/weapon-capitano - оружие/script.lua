-- Auto generated script file --

vanilla_model.PLAYER:setVisible(false)

-- API SETTINGS

local SwingingPhysics = require("libs.swinging_physics")
local swingOnHead = SwingingPhysics.swingOnHead
local swingOnBody = SwingingPhysics.swingOnBody

swingOnHead(models.model.root.Head.mask.ht, 0, {-10,10,-0,0,-10,10})
swingOnHead(models.model.root.Head.mask.ht2, 0, {-10,10,-0,0,-10,10})
swingOnHead(models.model.root.Head.mask.ht3, 0, {-10,10,-0,0,-10,10})

swingOnBody(models.model.root.Body.jeckett, 0, {-5,5,-5,5,-5,5})
swingOnBody(models.model.root.Body.jeckett.FR, 0, {-2,15,-5,5,-5,10})
swingOnBody(models.model.root.Body.jeckett.FL, 0, {-2,15,-5,5,-10,5})
swingOnBody(models.model.root.Body.jeckett.BR, 0, {-25,0,-5,5,-5,10})
swingOnBody(models.model.root.Body.jeckett.BL, 0, {-25,0,-5,5,-10,5})
swingOnBody(models.model.root.Body.jeckett.BC, 0, {-25,0,-5,5,-7,7})
swingOnBody(models.model.root.Body.jeckett.BC2, 0, {-30,0,-5,5,-5,5})
swingOnBody(models.model.root.Body.jeckett.FR.t, 0, {-2,5,-0,0,-20,20})
swingOnBody(models.model.root.Body.jeckett.FR.t.t2, 0, {-5,10,-0,0,-20,20})

-----------------------------------------------------------------

-- -- Action Wheel

local mainPage = action_wheel:newPage()
action_wheel:setPage(mainPage)

-----------------------------------------------------------------

-- Weapon -------------------------------------------------------------------

function events.ITEM_RENDER(item)
    if item.id:find("sword")  then
       return models.model.ItemA
    end
end