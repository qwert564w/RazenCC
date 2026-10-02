-- Auto generated script file --

models.model.ItemDevilsknife.handleBottom_outline:setPrimaryRenderType("translucent_cull")
models.model.ItemDevilsknife.handle_outline:setPrimaryRenderType("translucent_cull")
models.model.ItemDevilsknife.bladeBall_outline:setPrimaryRenderType("translucent_cull")
models.model.ItemDevilsknife.backBall_outline:setPrimaryRenderType("translucent_cull")

--function events.item_render(item)
--    if item.id:find("sword") then
--        return models.model.ItemDevilsknife
--    end
--end

function events.item_render(item, context)
	if player:isLoaded() then
		if item.id:find("sword") then
			local Scale = 1
			if context:find("FIRST_PERSON") then
				Scale = 0.75
			end
			return models.model.ItemDevilsknife:setScale(Scale)
		end
	end
end
