local fourthPage = action_wheel:newPage()
action_wheel:setPage(fourthPage)


local action14 = fourthPage:newAction()
action14:title("Hide Elytras")
action14:item("minecraft:elytra")
action14:hoverColor(0,0,1)
function pings.action14Clicked(state)
  if state then
	vanilla_model.ELYTRA:setVisible(false)
	else
	vanilla_model.ELYTRA:setVisible(true)
	end
end
action14:onToggle(pings.action14Clicked)

local action15 = fourthPage:newAction()
action15:title("Hide armor")
action15:item("minecraft:diamond_chestplate")
action15:hoverColor(0,0,1)
function pings.action15Clicked(state)
  if state then
	vanilla_model.ARMOR:setVisible(false)
	else
	vanilla_model.ARMOR:setVisible(true)
	end
end
action15:onToggle(pings.action15Clicked)

local action16 = fourthPage:newAction()
action16:title("Hide Cape")
action16:item("minecraft:black_banner")
action16:hoverColor(0,0,1)
function pings.action16Clicked(state)
  if state then
	vanilla_model.Cape:setVisible(false)
	else
	vanilla_model.Cape:setVisible(true)
	end
end
action16:onToggle(pings.action16Clicked)

local action17 = fourthPage:newAction()
action17:title("Hide vanilla player")
action17:item("minecraft:player_head")
action17:hoverColor(0,0,1)
function pings.action17Clicked(state)
  if state then
	vanilla_model.Player:setVisible(false)
	else
	vanilla_model.Player:setVisible(true)
	end
end
action17:onToggle(pings.action17Clicked)