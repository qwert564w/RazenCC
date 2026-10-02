--▄▀█ █▀▄ █▀█ █ █▀ ▀█▀ █▀▀ █░░
--█▀█ █▄▀ █▀▄ █ ▄█ ░█░ ██▄ █▄▄

TW = 1 --top wetness --wet effect
BW = 1 --bottom wetness
events.TICK:register(function () 
    if player:isInRain() then --makes clothes wet if in rain
        TW = TW - 0.005
        BW = BW - 0.005
        if TW <= 0.6 then TW = 0.6 end
        if BW <= 0.6 then BW = 0.6 end
    end
    if player:isInWater() then BW = 0.6 end  --if player is standing in water, make bottom clothes wet
    if player:isUnderwater() then TW = 0.6 end --if player is submerged in water, make top clothes wet
	if not player:isUnderwater() and TW ~= 1 and not player:isInRain() then TW = TW + 0.005 end --if not submerged in water, dry top clothes
	if not player:isInWater() and BW ~= 1 and not player:isInRain() then BW = BW + 0.005 end --if not standing in water, dry bottom clothes
    if BW >= 1 then BW = 1 end
    if TW >= 1 then TW = 1 end
    if world.getTime() % 16 == 0 and (BW ~= 1 or TW ~= 1) then particles:newParticle("falling_dripstone_water",player:getPos().x + math.random() - 0.7, player:getPos().y +  math.random(), player:getPos().z + math.random() - 0.7, math.random(-1,1),  math.random(-1,1),  math.random(-1,1)) end
    --put clothes or fur on top half of body here
	--example: models.player.Base.Torso.Shirt:setColor(TW,TW,TW)
models.ChefHat.Head:setColor(TW,TW,TW)

    --put clothes or fur on bottom half of body here
	--example: models.player.Base.Torso.Pants:setColor(BW,BW,BW)

end)