local lib = require("scripts.library")
local anims = require("lib.JimmyAnims")
local SwingingPhysics = require("lib.swinging_physics")
anims(animations["models.model"])

SwingingPhysics.swingOnHead(lib.char.body.head.hed.front_hair, 90, {-2,5,-50,50,-5,5})
SwingingPhysics.swingOnHead(lib.char.body.head.hed.hair.left_bun, 90, {-5,7,-50,50,-5,5})
SwingingPhysics.swingOnHead(lib.char.body.head.hed.hair.right_bun, 90, {-5,7,-50,50,-5,5})

local squapi = require("lib.SquAPI")

squapi.smoothHead:new(
    {
        lib.char.body,
        lib.char.body.head--element(you can have multiple elements in a table)
    },
		nil,    --(1) strength(you can make this a table too)
    nil,    --(0.1) tilt
    nil,    --(1) speed
    true     --(true) keepOriginalHeadPos
)




squapi.eye:new(
    lib.char.body.head.hed.eyes.iris,  --the eye element 
    0.5,  --(0.2) left distance
    0.5,  --(0.3) right distance
    0.7,  --(0.5) up distance
    0.7   --(0.5) down distance
)

squapi.eye:new(
    lib.char.body.head.hed.eyes.iris2,  --the eye element 
    0.5,  --(0.2) left distance
    0.5,  --(0.3) right distance
    0.7,  --(0.5) up distance
    0.7   --(0.5) down distance
)

squapi.ear:new(
    lib.char.body.head.antennaes.a1, --leftEar
    lib.char.body.head.antennaes.a2, --(nil) rightEar
    nil, --(1) rangeMultiplier
    false, --(false) horizontalEars
    nil, --(2) bendStrength
    nil, --(true) doEarFlick
    3000, --(400) earFlickChance
    nil, --(0.1) earStiffness
    nil  --(0.8) earBounce
)

squapi.ear:new(
    lib.char.body.head.antennaes.a1, --leftEar
    lib.char.body.head.antennaes.a2, --(nil) rightEar
    nil, --(1) rangeMultiplier
    false, --(false) horizontalEars
    nil, --(2) bendStrength
    nil, --(true) doEarFlick
    3000, --(400) earFlickChance
    nil, --(0.1) earStiffness
    nil  --(0.8) earBounce
)

squapi.leg:new(
    lib.char.Rightleg,  --element
    0.7,    --(1) strength
    true,    --(false) isRight
    nil     --(true) keepPosition
)

squapi.leg:new(
    lib.char.Leftleg,  --element
    0.7,    --(1) strength
    false,    --(false) isRight
    nil     --(true) keepPosition
)



local main_page = action_wheel:newPage()
action_wheel:setPage(main_page)

local sit = false
local sleep = false
local blink = squapi.randimation:new(
    animations["models.model"].blink, --animation
    nil,                    --(200) chanceRange
    true                    --(false) isBlink
)


function pings.actionsit()
    sit = not sit
    if sit then
        animations["models.model"].sitanim:play()
    else
        animations["models.model"].sitanim:stop()

    end
end

function pings.actionsleep()
    sleep = not sleep
    if sleep then
        animations["models.model"].sleepy:play()
        blink.enabled = false
    else
        animations["models.model"].sleepy:stop()
        blink.enabled = true
    end
end

main_page:newAction()
    :title("sit")
    :item("minecraft:gray_wool")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.actionsit)

    main_page:newAction()
    :title("sleep")
    :item("minecraft:black_wool")
    :hoverColor(1, 0, 1)
    :onLeftClick(pings.actionsleep)
