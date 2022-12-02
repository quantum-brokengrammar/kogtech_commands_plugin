package commands;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.world.blocks.storage.*;

public class commandsPlugin extends Plugin{
    
    private static double ratio = 0.6;
    private HashSet<String> votes = new HashSet<>();
    
    //called when game initializes
    @Override
    public void init(){}
    
    public commandsPlugin() {
        Events.on(PlayerLeave.class, e -> {
            Player player = e.player;
            int cur = this.votes.size();
            int req = (int) Math.ceil(ratio * Groups.player.size());
            if(votes.contains(player.uuid())) {
                votes.remove(player.uuid());
                Call.sendMessage("MAP: [white]" + player.name + "[white] left, [green]" + cur + "[] votes, [green]" + req + "[] required");
            }
        });
        // clear votes on game over
        Events.on(GameOverEvent.class, e -> {
            this.votes.clear();
        });
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

        //register a simple reply command
        handler.<Player>register("lag-test", "<text...>", "A simple ping command that echoes a player's text.", (args, player) -> {
            player.sendMessage("You said: [accent] " + args[0]);
        });

        //register a whisper command which can be used to send other players messages
        handler.<Player>register("pm", "<player> <text...>", "Sends a private message to another player.", (args, player) -> {
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
        //rtv command from mayli/RockTheVotePlugin
        handler.<Player>register("votemap", "[map...]", "Vote to change map", (args, player) -> {
            this.votes.add(player.uuid());
            int cur = this.votes.size();
            int req = (int) Math.ceil(0.6 * Groups.player.size());
            Call.sendMessage("[red]MAP: [white]" + player.name + "[white] wants to change the map, [green]" + cur +
                "[] votes, [green]" + req + "[] required");

            if (cur < req) {
                return;
            }

            this.votes.clear();
            Call.sendMessage("MAP: [green]vote passed, changing map.");
            Events.fire(new GameOverEvent(Team.crux));
        });
    }
}
