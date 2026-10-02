



    



--[[









function pings.outfit_siwthc()
    sounds:playSound("item.armor.equip_leather", player:getPos(), 5)
    for i = 1, 40, 1
    do
        confetti.newParticle(
    "flake",
    player:getPos()+vec(-0.5+(1-math.random()),0+ (1+math.random()*0.3),-0.5+(1-math.random())),
    vec(0,-math.random()*0.05,0),
    {
        billboard=true,
        emissive=true,
        lifetime=80
    })
end
    toggleVis(witch_outfit)
    toggleVis(bodysuit)
end

local mode1 = actionwheel:newAction()
    :title("outfit")
    :item("minecraft:light_blue_wool")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.outfit_siwthc)
    ]]
