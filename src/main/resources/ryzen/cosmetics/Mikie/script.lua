---------------------------------------------------------------------------------------------------------------
---PLAYER MODEL ADJUSTMENTS---
vanilla_model.PLAYER:setVisible(false)
vanilla_model.ARMOR:setVisible(false)
models.model.whole.body.LeftArm.LeftItemPivot:setScale(0.8, 0.8, 0.8)
models.model.whole.body.RightArm.RightItemPivot:setScale(0.8, 0.8, 0.8)
models.model.whole.body.Body.ELYTRA_PIVOT:setScale(0.8, 0.8, 0.8)

---------------------------------------------------------------------------------------------------------------
---IDLE BODY MOVEMENTS---
animations.model.i_ears:setSpeed(0.15)
animations.model.i_ears2:setSpeed(0.1)
animations.model.i_ears:play()
animations.model.i_ears2:play()
animations.model.i3:setSpeed(0.3)
animations.model.i3:play()

---------------------------------------------------------------------------------------------------------------
---HAIR PHYSICS---
local SwingingPhysics = require("lib.swinging_physics")

SwingingPhysics.swingOnHead(models.model.whole.body.head.hair.front_hair, 90, { -2, 5, -0, 0, -5, 5 },
    nil, 0)


SwingingPhysics.swingOnHead(models.model.whole.body.head.hair.front_hair.lf_hair, 90, { -30, 40, -0, 0, -35, 5 },
    nil, 1)
SwingingPhysics.swingOnHead(models.model.whole.body.head.hair.front_hair.rf_hair, 90, { -30, 40, -0, 0, -5, 35 },
    nil, 1)
SwingingPhysics.swingOnHead(models.model.whole.body.head.hair.l_h, 90, { -10, 0, -0, 0, -15, 5 },
    nil, 2)
SwingingPhysics.swingOnHead(models.model.whole.body.head.hair.r_h, 90, { -10, 0, -0, 0, -5, 15 },
    nil, 2)
SwingingPhysics.swingOnHead(models.model.whole.body.head.hair.back_hair, 90, { -10, 2, -0, 0, -10, 10 },
    nil, 0)
SwingingPhysics.swingOnHead(models.model.whole.body.head.hair.back_hair, 90, { -10, 2, -0, 0, -10, 10 },
    nil, 0)

SwingingPhysics.swingOnHead(models.model.whole.body.head.hair.back_hair.ponytail.p1, 90, { -30, 10, -0, 0, -30, 30 },
    nil, 1)
SwingingPhysics.swingOnHead(models.model.whole.body.head.hair.back_hair.ponytail.p1.p2, 90, { -30, 10, -0, 0, -30, 30 },
    nil, 2)

---------------------------------------------------------------------------------------------------------------
---SQUAPI APPLICATIONS---
local squapi = require("lib.SquAPI")

---HEAD---
squapi.smoothHead:new(
    {
        models.model.whole.body,
        models.model.whole.body.head --element(you can have multiple elements in a table)
    },
    nil,                             --(1) strength(you can make this a table too)
    nil,                             --(0.1) tilt
    nil,                             --(1) speed
    true                             --(true) keepOriginalHeadPos
)

---EYES---
squapi.eye:new(
    models.model.whole.body.head.eyes.iris2, --the eye element
    0.5,                                     --(0.2) left distance
    0.5,                                     --(0.3) right distance
    0.7,                                     --(0.5) up distance
    0.7                                      --(0.5) down distance
)

squapi.eye:new(
    models.model.whole.body.head.eyes.iris, --the eye element
    0.5,                                    --(0.2) left distance
    0.5,                                    --(0.3) right distance
    0.7,                                    --(0.5) up distance
    0.7                                     --(0.5) down distance
)

---ARMS AND LEGS--- MAKES ROTATION SMALLER SO LOOKS LESS WONKY, SIMPLY DELETE IF NOT NEEDED
squapi.arm:new(
    models.model.whole.body.LeftArm, --element
    0.6,                             --(1) strength
    nil,                             --(false) isRight
    false                            --(true) keepPosition
)



squapi.arm:new(
    models.model.whole.body.RightArm, --element
    0.6,                              --(1) strength
    true,                             --(false) isRight
    false                             --(true) keepPosition
)

squapi.leg:new(
    models.model.whole.LeftLeg, --element
    0.8,                        --(1) strength
    true,                       --(false) isRight
    nil                         --(true) keepPosition
)
squapi.leg:new(
    models.model.whole.RightLeg, --element
    0.8,                         --(1) strength
    false,                       --(false) isRight
    nil                          --(true) keepPosition
)

squapi.ear:new(
    models.model.whole.body.head.ears.e1, --leftEar
    models.model.whole.body.head.ears.e2, --(nil) rightEar
    nil,                                  --(1) rangeMultiplier
    nil,                                  --(false) horizontalEars
    nil,                                  --(2) bendStrength
    nil,                                  --(true) doEarFlick
    nil,                                  --(400) earFlickChance
    nil,                                  --(0.1) earStiffness
    nil                                   --(0.8) earBounce
)

myTail = { 
    models.model.whole.body.Body.tail,
    models.model.whole.body.Body.tail.t1,
    models.model.whole.body.Body.tail.t1.t2,
    models.model.whole.body.Body.tail.t1.t2.t3,
    models.model.whole.body.Body.tail.t1.t2.t3.t4 
}

squapi.tail:new(myTail,
    nil, --(15) idleXMovement
    nil, --(5) idleYMovement
    nil, --(1.2) idleXSpeed
    nil, --(2) idleYSpeed
    nil, --(2) bendStrength
    nil, --(0) velocityPush
    nil, --(0) initialMovementOffset
    nil, --(1) offsetBetweenSegments
    nil, --(.005) stiffness
    nil, --(.9) bounce
    nil, --(90) flyingOffset
    nil, --(-90) downLimit
    nil  --(45) upLimit
)
---TAIL---


---EARFLICKS AND BLINKS---

local blink = squapi.randimation:new(
    animations.model.blink, --animation
    nil,                    --(200) chanceRange
    true                    --(false) isBlink
)

