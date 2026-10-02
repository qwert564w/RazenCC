function events.render()
    animations.model.ambientanimation:play()
end


local function yesFunc(state)
    animations.model.noanimation:stop()
    animations.model.spinanimation:stop()
    animations.model.yesanimation:play()
end

local function noFunc(state)
    animations.model.yesanimation:stop()
    animations.model.spinanimation:stop()
    animations.model.noanimation:play()
end

local function spinFunc(state)
    animations.model.yesanimation:stop()
    animations.model.noanimation:stop()
    animations.model.spinanimation:play()
end

local mainPage = action_wheel:newPage()
action_wheel:setPage(mainPage)

local yes = mainPage:newAction():title("Yes"):onLeftClick(yesFunc):item("green_wool")

local no = mainPage:newAction():title("No"):onLeftClick(noFunc):item("red_wool")

local spin = mainPage:newAction():title("Spin"):onLeftClick(spinFunc):item("potion")