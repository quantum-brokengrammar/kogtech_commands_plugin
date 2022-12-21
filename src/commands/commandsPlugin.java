package commands;

import arc.*;
import arc.util.*;
import arc.struct.Seq;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.Team;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.gen.Player;
import mindustry.mod.*;
import mindustry.maps.Map;
import mindustry.net.Administration.*;
import mindustry.world.blocks.storage.*;

import static mindustry.Vars.content;
import static mindustry.Vars.maps;
import static mindustry.Vars.netServer;
import static mindustry.Vars.state;
import static mindustry.Vars.world;

public class commandsPlugin extends Plugin{
    //mcv command
    private Seq<String> votes = new Seq<>();
	//nicknames and real names
    private Seq<String> nicknames = new Seq<>();
    //called when game initializes
    @Override
    public void init(){
        
    }
    
    public commandsPlugin() {
        Events.on(PlayerLeave.class, e -> {
            Player player = e.player;
            int cur = this.votes.size;
            int req = (int) Math.ceil(0.6 * Groups.player.size());
            if(votes.contains(player.uuid())) {
                votes.remove(player.uuid());
                Call.sendMessage("[red]MapClearVote: [accent]" + player.name + "[white] left, [green]" + cur + "[] votes, [green]" + req + "[] required");
            }
            if (nicknames.contains(nicknames.get(player.name)) != null) {
                nicknames.remove(player.name);
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
        /* Template
        handler.<Player>register("command", "<arg>", "A simple command that player's", (args, player) -> {
            // comands here
        });
        */
        //Commands
        handler.<Player>register("lag-test", "<text...>", "A simple ping command that echoes a player's text.", (args, player) -> {
            player.sendMessage("You said: [accent] " + args[0]);
        });
        handler.<Player>register("pm", "<player> <text...>", "Sends a private message to another player. Substitute spaces with dashes.", (args, player) -> {
            //find player by name
            Player other = Groups.player.find(p -> Strings.stripColors(p.name).equalsIgnoreCase(args[0].replace("_", " ")));
            //give error message with scarlet-colored text if play
            if(other == null){
                player.sendMessage("[scarlet]No player by that name found!");
                if (Groups.player.size() <= 10) {
                    player.sendMessage("[stat]Other players:");
                    Groups.player.each(e -> player.sendMessage("[white]"+Strings.stripColors(e.name).toLowerCase().replace("_", " ")));
                }
            }

            //send the other player a message, using [lightgray] for gray text color and [lightgray] to reset color
            player.sendMessage("[lightgray](pm) (me) -> " + other.name + "[lightgray]:[white] " + args[1]);
            other.sendMessage("[lightgray](pm) (" + player.name + "[lightgray]) -> (me):[white] " + args[1]);
        });
        handler.<Player>register("me", "<text...>", "Broadcasts a roleplay message with asterisks to all players.", (args, player) -> {
            Call.sendMessage("[lightgray]*".concat(nicknames.get(player.name)).concat("[lightgray] ").concat(args[0]).concat("[lightgray]*"));
        });
        handler.<Player>register("my", "<text...>", "Broadcasts a roleplay message with asterisks to all players.", (args, player) -> {
            Call.sendMessage("[lightgray]*".concat(nicknames.get(player.name)).concat("[lightgray]'s ").concat(args[0]).concat("[lightgray]*"));
        });
        handler.<Player>register("clrchat", "Clears the chat. Needs admin to execute this command.", (args, player) -> {
            if (player.admin) {
                for (int i=0; i<22; i++) {
                    Call.sendMessage("\n");
                }; 
                player.sendMessage("Cleared chat.");
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
        handler.<Player>register("nick", "<name...>", "Changes your nickname. You need to rejoin to clear it.", (args, player) -> {
            String nickname = args[0].replace("_", " ");
            if (player.name.length() > 80||nickname.length() > 30) {
                player.sendMessage("[#ff]Nickname Too Long!");
            } else {
            player.sendMessage("Changed nickname to: [accent]" + args[0]);
            player.name = nickname+"[lightgray] ("+player.name+"[lightgray])";
            }
            nicknames.add(player.name, "nickname");
        });
        
        handler.<Player>register("mcv", "Vote to clear map", (args, player) -> {
            this.votes.add(player.uuid());
            int cur = this.votes.size;
            int req = (int) Math.ceil(0.6f * Groups.player.size());
            Call.sendMessage("[red]MapClearVote: [accent]" + player.name + "[white] wants to clear the map, [green]" + cur +
                "[] votes, [green]" + req + "[] required");

            if (cur < req) {
                return;
            }

            this.votes.clear();
            Call.sendMessage("[red]MapClearVote: [green]vote passed, clearing map.");
            Call.infoMessage("[scarlet]\u26a0 The map will be cleared in [orange]10[] seconds! \u26a0\n[]All units, players, and buildings (except cores) will be destroyed.\nYou may lag or crash during the map clear.");
        	try { Thread.sleep(10000); } 
	        catch (InterruptedException e) {}
            // clear map
            mindustry.gen.Building block;
            Groups.unit.each(u -> u.kill());
            for (int x=0; x<world.width(); x++) {
        		for (int y=0; y<world.height(); y++) {
        			block = world.build(x, y);
        			if (block != null && (block.block != Blocks.coreShard && block.block != Blocks.coreNucleus && block.block != Blocks.coreFoundation && block.block != Blocks.coreBastion && block.block != Blocks.coreAcropolis && block.block != Blocks.coreCitadel && block.block != Blocks.worldProcessor && block.block != Blocks.worldCell && block.block != Blocks.worldMessage)) {
        				block.kill();
        			}
        		}
        	}
            Call.infoMessage("[green]Map cleaned! Removed all blocks and units!");
	    });
	    
        handler.<Player>register("players", "Outputs a list of all the players online in the server.", (args, player) -> {
            player.sendMessage("[stat]There are currently [green]"+Groups.player.size()+"[stat] players online.");
	    player.sendMessage("[stat]List of players:");
	    Groups.player.each(e -> player.sendMessage("[stat]"+e.name));
        });
	
	    handler.<Player>register("units", "Outputs how many units on the server.", (args, player) -> {
            player.sendMessage("[stat]There are currently "+Groups.unit.size()+" units.");
        });
	    handler.<Player>register("infop", "<player...>", "Gets player info.", (args, player) -> {
            if (player.admin) {
                if (Groups.player.find(e->Strings.stripColors(e.name).equalsIgnoreCase(args[0])) != null) {
                    // Player pfind = Groups.player.each(p -> Strings.stripColors(p.name).equalsIgnoreCase(args[0]));
                    Player pfind = Groups.player.find(e->Strings.stripColors(e.name).equalsIgnoreCase(args[0]));
                    player.sendMessage("[stat]Player information:");
                    player.sendMessage("[stat]Last name: [white]"+pfind.name.toString()); 
                    player.sendMessage("[stat]Current UUID: [white]"+pfind.uuid().toString()); 
                    player.sendMessage("[stat]IP: [white]"+pfind.ip().toString()); 
                    player.sendMessage("[stat]IPs: [white]"+pfind.getInfo().ips.toString()); 
                    player.sendMessage("[stat]Names: ["+pfind.getInfo().names.toString()); 
                    player.sendMessage("[stat]Times joined: [white]"+String.valueOf(pfind.getInfo().timesJoined)); 
                    player.sendMessage("[stat]Times kicked: [white]"+String.valueOf(pfind.getInfo().timesKicked)); 
                    player.sendMessage("[stat]Banned?: [white]"+String.valueOf(pfind.getInfo().banned)); 
                    player.sendMessage("[stat]Admin?: [white]"+String.valueOf(pfind.admin)); 
                } else {
                    player.sendMessage("[scarlet]No player by that name found!");
                }
            } else {
                player.sendMessage("[scarlet]You must be admin to use this command.");
            }
        });
        handler.<Player>register("maps", "[page]", "Sends a list of all maps in the server.", (args, player) -> {
            if(args.length == 1 && !Strings.canParseInt(args[0])){
        		player.sendMessage("[scarlet]page must be a number.");
                return;
            }
            int page = Strings.parseInt(args[0]);
            Seq<Map> maplist = mindustry.Vars.maps.all();
            int pages = (int) Math.ceil(maplist.size / 8);
            Map map;
            if (page > pages || page < 1) {
            	player.sendMessage("[scarlet]page must be a number between[stat] 1[] and [stat]" + pages + "[].");
            	return;
            }
            player.sendMessage("[gold]Map list");
            for (int i=(page-1)*8; i<8*page;i++) {
                map = maplist.get(i);
                player.sendMessage("[stat]-" + map.name() + " (" + map.width + "x" + map.height +") By: [white]"+map.author());
            }
        });
        handler.<Player>register("real", "<player...>", "A simple command that verifies a player.", (args, player) -> {
            Player realplayer = Groups.player.find(e->Strings.stripColors(e.name).equalsIgnoreCase(args[0]));
            player.sendMessage("Is the player real?");
            player.sendMessage("Player names: "+realplayer.getInfo().names.toString());
            player.sendMessage("Player joined "+String.valueOf(realplayer.getInfo().timesJoined)+" times.");
        });
    }
}
