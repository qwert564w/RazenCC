local data = {}

---VARIABLES, YEA.---
data.mode = nil
data.pose = 'main'
local anim = animations.model

---ANIMATIONS TO BLEND.---
data.toBlend = {
    anim.walkmain,
    anim.walkbackmain,
    anim.sprintmain,
    anim.idlemain,
    anim.jumpdownmain,
    anim.jumpupmain,
    anim.crouch,
    anim.crouchwalk,
    anim.climb,
    anim.climbstill,
    anim.climbcrouch,
    anim.crawl,
    anim.crawlstill,
    anim.water,
    anim.waterdown,
    anim.waterup,
    anim.swim,
    anim.walksword,
    anim.walkbacksword,
    anim.sprintsword,
    anim.idlesword,
    anim.jumpupsword,
    anim.jumpdownsword,
    anim.idleproud,
    anim.walkproud,
    anim.walkbackproud,
    anim.idlegoblet,
    anim.walkgoblet,
    anim.walkbackgoblet,
    anim.sprintgoblet,
    anim.lanternhold,
    anim.walklamp,
    anim.walkbacklamp,
    anim.jumpuplamp,
    anim.jumpdownlamp,
    anim.sprintlamp,
    anim.sitting,
    anim.fly,
    anim.att1,
    anim.att2,
    anim.att3,
    anim.att4,
    anim.bowR,
    anim.bowL,
    anim.flick
}

data.toBlendSmol = {
    anim.useR,
    anim.useL,
    anim.mineR,

}

--OUTFIT 2--
data.outfit2 = {
    models.model.whole.torsorot.torso.bodyrot.body.bodyrot2.chest.o2,
    models.model.whole.torsorot.torso.bodyrot.body.bodyrot2.o2,
    models.model.whole.torsorot.torso.bodyrot.body.skirt2,
    models.model.whole.torsorot.torso.l_arm.o2,
    models.model.whole.torsorot.torso.l_arm.hand.o2,
    models.model.whole.torsorot.torso.r_arm.o2,
    models.model.whole.torsorot.torso.r_arm.hand.o2,
    models.model.whole.l_leg.o2,
    models.model.whole.l_leg.knee.o2,
    models.model.whole.r_leg.o2,
    models.model.whole.r_leg.knee.o2

}

data.outfit1 = {
    models.model.whole.torsorot.torso.bodyrot.body.skirt,
    models.model.whole.torsorot.torso.bodyrot.body.bodyrot2.chest.o1,
    models.model.whole.torsorot.torso.r_arm.hand.o1,
    models.model.whole.torsorot.torso.r_arm.o1,
    models.model.whole.torsorot.torso.l_arm.o1,
    models.model.whole.torsorot.torso.l_arm.hand.o1,
    models.model.whole.l_leg.o1,
    models.model.whole.l_leg.knee.o1,
    models.model.whole.r_leg.o1,
    models.model.whole.r_leg.knee.o1,
    models.model.whole.torsorot.torso.bodyrot.body.bodyrot2.o1

}

data.backHair1 = {
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.pom1,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.pom2,
}

data.backHair2 = {
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.bun
}

data.frontHair1 = {
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.front_hair.strand1.main,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.front_hair.strand2.main,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.right_hair.main,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.front_hair.main
}

data.frontHair2 = {
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.front_hair.strand2.bloody,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.right_hair.bloody,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.front_hair.bloody,
}


---MODELPARTS FOR TOGGLES---
data.jewelery = {
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.front_hair.tiara,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.ears.ear2.earring,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.ears.ear1.earring,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.ears.ear2.earring2,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.ears.ear1.earring2,
    models.model.whole.torsorot.torso.bodyrot.body.skirt.front.chain,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.hair.back_tiara,
}
data.eyesmain = {
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.eyes.iris2.detail,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.eyes.iris.detail
}
data.eyesimple = {
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.eyes.iris.simple,
    models.model.whole.torsorot.torso.bodyrot.body.neck.headrot.head.eyes.iris2.simple
}

data.wings = {
    models.model.whole.torsorot.torso.bodyrot.body.wings
}

-----------------------------------------------
---POSE MANAGEMENT FUNCTIONS, PRETTY SELF EXPLANATORY---
-----------------------------------------------
function data:SwordCheck()
    if player:getHeldItem(false).id ~= "minecraft:air" and player:getHeldItem(false).id == "minecraft:diamond_sword" or player:getHeldItem(false).id == "minecraft:iron_sword" or player:getHeldItem(false).id == "minecraft:netherite_sword"
        or player:getHeldItem(false).id == "minecraft:stone_sword" or player:getHeldItem(false).id == "minecraft:golden_sword" or player:getHeldItem(false).id == "minecraft:wooden_sword" then
        data.mode = 'sword'
    else
        data.mode = data.pose
    end
end

function data:LanternCheck()
    if player:getHeldItem(false).id ~= "minecraft:air" and player:getHeldItem(false).id == "minecraft:lantern" then
        data.mode = 'leftlantern'
    end
end

function data:PlaySwordPose()
    anim.walksword:setPlaying(anim.walk:isPlaying())
    anim.idlesword:setPlaying(anim.idle:isPlaying())
    anim.walkbacksword:setPlaying(anim.walkback:isPlaying())
    anim.sprintsword:setPlaying(anim.sprint:isPlaying())
    anim.jumpupsword:setPlaying(anim.jumpup:isPlaying())
    anim.jumpdownsword:setPlaying(anim.jumpdown:isPlaying())
    anim.hidelefthand:play()
    anim.att3:stop()
    anim.att4:stop()
    models.model.whole.torsorot.torso.r_arm.hand.lamp:setVisible(false)
    models.model.whole.torsorot.torso.l_arm.hand.goblet:setVisible(false)
end

function data:PlayLeftLanternPose()
    anim.lanternhold:setPlaying(anim.idle:isPlaying())
    anim.hiderighthand:play()
    anim.walklamp:setPlaying(anim.walk:isPlaying())
    anim.jumpuplamp:setPlaying(anim.jumpup:isPlaying())
    anim.jumpdownlamp:setPlaying(anim.jumpdown:isPlaying())
    anim.walkbacklamp:setPlaying(anim.walkback:isPlaying())
    anim.sprintlamp:setPlaying(anim.sprint:isPlaying())
    models.model.whole.torsorot.torso.r_arm.hand.lamp:setVisible(true)
    models.model.whole.torsorot.torso.l_arm.hand.goblet:setVisible(false)
end

function data:StopLampPoses()
    anim.hiderighthand:stop()
    anim.lanternhold:stop()
    anim.walklamp:stop()
    anim.walkbacklamp:stop()
    anim.sprintlamp:stop()
    anim.jumpuplamp:stop()
    anim.jumpdownlamp:stop()
    models.model.whole.torsorot.torso.r_arm.hand.lamp:setVisible(false)
end

function data:StopSwordPoses()
    anim.walksword:stop()
    anim.idlesword:stop()
    anim.walkbacksword:stop()
    anim.sprintsword:stop()
    anim.hidelefthand:stop()
    anim.jumpupsword:stop()
    anim.jumpdownsword:stop()

end

function data:StopLanternPose()
    anim.lanternhold:stop()
    anim.hidelefthand:stop()
end

function data:PlayMainPose()
    anim.walkmain:setPlaying(anim.walk:isPlaying())
    anim.idlemain:setPlaying(anim.idle:isPlaying())
    anim.walkbackmain:setPlaying(anim.walkback:isPlaying())
    anim.sprintmain:setPlaying(anim.sprint:isPlaying())
    anim.jumpupmain:setPlaying(anim.jumpup:isPlaying())
    anim.jumpdownmain:setPlaying(anim.jumpdown:isPlaying())
end

function data:PlayDownPose()
    anim.walkproud:setPlaying(anim.walk:isPlaying())
    anim.idleproud:setPlaying(anim.idle:isPlaying())
    anim.walkbackproud:setPlaying(anim.walkback:isPlaying())
    anim.sprintmain:setPlaying(anim.sprint:isPlaying())
    anim.jumpupmain:setPlaying(anim.jumpup:isPlaying())
    anim.jumpdownmain:setPlaying(anim.jumpdown:isPlaying())
    anim.hiderighthand:play()
end

function data:PlayGobletPose()
    anim.walkgoblet:setPlaying(anim.walk:isPlaying())
    anim.idlegoblet:setPlaying(anim.idle:isPlaying())
    anim.walkbackgoblet:setPlaying(anim.walkback:isPlaying())
    anim.sprintgoblet:setPlaying(anim.sprint:isPlaying())
    anim.jumpupmain:setPlaying(anim.jumpup:isPlaying())
    anim.jumpdownmain:setPlaying(anim.jumpdown:isPlaying())

    anim.hidelefthand:play()
end

function data:StopPoses()
    anim.walkmain:stop()
    anim.idlemain:stop()
    anim.walkbackmain:stop()
    anim.sprintmain:stop()
    anim.walkproud:stop()
    anim.idleproud:stop()
    anim.walkbackproud:stop()
    anim.jumpupmain:stop()
    anim.jumpdownmain:stop()
    anim.idlegoblet:stop()
    anim.walkbackgoblet:stop()
    anim.walkgoblet:stop()
    anim.hidelefthand:stop()
end

---APPLY BLENDING, YA---
function data:Applyblend(anims, ticks)
    for f = 1, #anims, 1
    do
        anims[f]:setBlendTime(ticks)
    end
end

---HIDE.SHOW. MODELPARTS---
function data:toggleVis(piece, bool)
    for i = 1, #piece, 1
    do
        piece[i]:setVisible(bool or not piece[i]:getVisible())
    end
end

return data
