package commands;

import java.util.HashSet;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.Team;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.world.blocks.storage.*;

public class commandsPlugin extends Plugin{
    
    //called when game initializes
    @Override
    public void init(){}
    
    public commandsPlugin() {
        /*Events.on(PlayerLeave.class, e -> {
            Player player = e.player;
            
        });*/
    }
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
        /* Template
        handler.<Player>register("command", "<arg>", "A simple command that player's", (args, player) -> {
            // comands here
        });
        */
        //register a simple reply command
        handler.<Player>register("lag-test", "<text...>", "A simple ping command that echoes a player's text.", (args, player) -> {
            player.sendMessage("You said: [accent] " + args[0]);
        });

        //register a whisper command which can be used to send other players messages
        handler.<Player>register("pm", "<player> <text...>", "Sends a private message to another player. Substitute spaces with dashes.", (args, player) -> {
            //find player by name
            Player other = Groups.player.find(p -> Strings.stripColors(p.name).equalsIgnoreCase(args[0].replace("_", " ")));

            //give error message with scarlet-colored text if player isn't found
            if(other == null){
                player.sendMessage("[scarlet]No player by that name found!");
                return;
            }

            //send the other player a message, using [lightgray] for gray text color and [] to reset color
            player.sendMessage("[lightgray](pm) (me) -> " + other.name + "[lightgray]:[white] " + args[1]);
            other.sendMessage("[lightgray](pm) (" + player.name + "[lightgray]) -> (me):[white] " + args[1]);
        });
        handler.<Player>register("me", "<text...>", "Broadcasts a roleplay message with asterisks to all players.", (args, player) -> {
            Call.sendMessage("[lightgray]*"+player.name+"[lightgray] "+args[0]+"[lightgray]*");
        });
        handler.<Player>register("clear-chat", "Clears the chat. Needs admin to execute this command.", (args, player) -> {
            if (player.admin) {
                for (int i=0; i<22; i++) {
                    Call.sendMessage("\n");
                }; 
                player.sendMessage("Cleared chat successfully.");
                Call.announce("[tan]Chat cleared by "+player.name);
            } else {
                player.sendMessage("[scarlet]You must be admin to use this command.");
            }
        });
        handler.<Player>register("despawn", "Despawns all units. Needs admin to execute this command.", (args, player) -> {
            if (player.admin) {
                Groups.unit.each(u->u.kill());
                player.sendMessage("Despawned units successfully.");
            } else {
                player.sendMessage("[scarlet]You must be admin to use this command.");
            }
        });
        handler.<Player>register("nickname", "<name...>", "Changes your nickname. Substitute spaces with dashes.", (args, player) -> {
            String nickname = args[0].replace("_", " ");
            if (player.name.length() > 100||nickname.length() > 100) {
                player.sendMessage("[#ff]Nickname Too Long!");
            } else {
            player.sendMessage("Changed nickname to: [accent]" + args[0]);
            player.name = nickname+"[lightgray]("+player.name+"[lightgray])";
            }
        });
    }
}
