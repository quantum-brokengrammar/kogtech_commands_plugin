package example;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.world.blocks.storage.*;

public class ExamplePlugin extends Plugin{

    //called when game initializes
    @Override
    public void init(){}

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("reactors", "List all thorium reactors in the map.", args -> {
            for(int x = 0; x < Vars.world.width(); x++){
                for(int y = 0; y < Vars.world.height(); y++){
                    //loop through and log all found reactors
                    //make sure to only log reactor centers
                    if(Vars.world.tile(x, y).block() == Blocks.thoriumReactor && Vars.world.tile(x, y).isCenter()){
                        Log.info("Reactor at @, @", x, y);
                    }
                }
            }
        });
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
                commands.add("whisper", "<username|ID> <message...>","Send a private message to a player", false, false, (arg, data) -> {
        	Players result = Players.findByNameOrID(arg);
        	
        	if (result.found) {
        		String message = String.join(" ", result.rest);
        		
        		if (!Strings.stripColors(message).isBlank()) {
        			result.data.msgData.setTarget(data);
            		Call.sendMessage(data.player.con, message, "[sky]me [gold]--> " + NetClient.colorizeName(result.player.id, result.data.realName), data.player);
            		Call.sendMessage(result.player.con, message, NetClient.colorizeName(data.player.id, data.realName) + " [gold]--> [sky]me", data.player);
        		
        		} else Players.err(data.player, "[#ff]Error:[white] You can't send an empty message.");
        	} else Players.errNotOnline(data.player);
         });
    }
}
