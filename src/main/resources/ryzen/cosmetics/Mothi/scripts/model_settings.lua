local lib = require("scripts.library")

vanilla_model.PLAYER:setVisible(false)
vanilla_model.ARMOR:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)

lib.char.body.LeftArm.LeftItemPivot:setScale(0.8,0.8,0.8)
lib.char.body.RightArm.RightItemPivot:setScale(0.8,0.8,0.8)

animations["models.model"].i1:setSpeed(0.5)
animations["models.model"].i2:setSpeed(0.5)
animations["models.model"].i3:setSpeed(0.3)
animations["models.model"].sleepy:setSpeed(0.15)

animations["models.model"].i1:play()
animations["models.model"].i2:play()
animations["models.model"].i3:play()

