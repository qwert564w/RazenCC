local squapi = require("SquAPI")

--replace each nil with the value/parmater you want to use, or leave as nil to use default values :)
--parenthesis are default values for reference
squapi.eye:new(
    models.MODELNAME.Main.upper.Neck.Head.skin.Face1.EyesR,  --the eye element 
    0.2,  --(0.25) left distance
    0.8,  --(1.25) right distance
    0.2,  --(0.5) up distance
    0   --(0.5) down distance
)

--replace each nil with the value/parmater you want to use, or leave as nil to use default values :)
--parenthesis are default values for reference
squapi.eye:new(
    models.MODELNAME.Main.upper.Neck.Head.skin.Face1.EyesL,  --the eye element 
    0.8,  --(0.25) left distance
    0.2,  --(1.25) right distance
    0.2,  --(0.5) up distance
    0   --(0.5) down distance
)

--replace each nil with the value/parmater you want to use, or leave as nil to use default values :)
--parenthesis are default values for reference
squapi.crouch(
    animations.MODELNAME.glare, --crouch
    nil, --(nil) uncrouch
    nil, --(nil) crawl
    nil  --(nil) uncrawl
)


---------------------------------------------