vanilla_model.CAPE:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)

models.model.root.Head.Halo:setPrimaryRenderType("TRANSLUCENT_CULL")
models.model.root.Body.Wings:setPrimaryRenderType("CUTOUT_CULL")
animations.model.idle:play()

--setting pages--
local mainPage = action_wheel:newPage()
local armorPage = action_wheel:newPage()
action_wheel:setPage(mainPage)

--functions for pages--
function pings.wingsToggle(state)
	hasWings = state
	models.model.root.Body.Wings:setVisible(not hasWings)
    vanilla_model.CAPE:setVisible(hasWings)
    vanilla_model.ELYTRA:setVisible(hasWings)
end

function pings.haloToggle(state)
    hasHalo = state
    models.model.root.Head.Halo:setVisible(not hasHalo)
end

function setArmorPage() 
    action_wheel:setPage(armorPage) 
end

function setMainPage()
	action_wheel:setPage(mainPage)
end

function pings.helmetToggle(state)
	hasHelmet = state
	vanilla_model.HELMET:setVisible(not hasHelmet)
end

function pings.chestplateToggle(state)
	hasChestplate = state
	vanilla_model.CHESTPLATE:setVisible(not hasChestplate)
end

function pings.leggingsToggle(state)
	hasLeggings = state
	vanilla_model.LEGGINGS:setVisible(not hasLeggings)
end

function pings.bootsToggle(state)
	hasBoots = state
	vanilla_model.BOOTS:setVisible(not hasBoots)
end

function pings.fullArmorToggle(state)
	hasHelmet = state
	hasChestplate = state
	hasLeggings = state
	hasBoots = state
	vanilla_model.HELMET:setVisible(not hasHelmet)
	vanilla_model.CHESTPLATE:setVisible(not hasChestplate)
	vanilla_model.LEGGINGS:setVisible(not hasLeggings)
	vanilla_model.BOOTS:setVisible(not hasBoots)
end

--main page--
local toggleWings = mainPage:newAction()
    :title("Disable Wings")
    :item("red_wool")
    :toggleTitle("Enable Wings")
    :toggleItem("green_wool")
    :setOnToggle(pings.wingsToggle)
    :setToggled(false)
local toggleHalo = mainPage:newAction()
    :title("Disable Halo")
    :item("red_wool")
    :toggleTitle("Enable Halo")
    :toggleItem("green_wool")
    :setOnToggle(pings.haloToggle)
    :setToggled(false)
local toArmorPage = mainPage:newAction()
    :title("Armor toggles")
    :item("iron_helmet")
    :setOnLeftClick(setArmorPage)
	
--armor page--
local toMainPage = armorPage:newAction()
	:title("Main page")
	:item("item_frame")
	:setOnLeftClick(setMainPage)
local helmetToggle = armorPage:newAction()
	:title("Disable all")
	:item("iron_block")
	:toggleTitle("Enable all")
    :toggleItem("gold_block")
	:setOnToggle(pings.fullArmorToggle)
	:setToggled(false)
local helmetToggle = armorPage:newAction()
	:title("Disable helmet")
	:item("iron_helmet")
	:toggleTitle("Enable helmet")
    :toggleItem("golden_helmet")
	:setOnToggle(pings.helmetToggle)
	:setToggled(false)
local chestplateToggle = armorPage:newAction()
	:title("Disable chestplate")
	:item("iron_chestplate")
	:toggleTitle("Enable chestplate")
    :toggleItem("golden_chestplate")
	:setOnToggle(pings.chestplateToggle)
	:setToggled(false)
local leggingsToggle = armorPage:newAction()
	:title("Disable leggings")
	:item("iron_leggings")
	:toggleTitle("Enable leggings")
    :toggleItem("golden_leggings")
	:setOnToggle(pings.leggingsToggle)
	:setToggled(false)
local bootsToggle = armorPage:newAction()
	:title("Disable boots")
	:item("iron_boots")
	:toggleTitle("Enable boots")
    :toggleItem("golden_boots")
	:setOnToggle(pings.bootsToggle)
	:setToggled(false)

