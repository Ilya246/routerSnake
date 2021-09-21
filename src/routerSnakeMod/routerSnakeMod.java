package routerSnakeMod;

import arc.Events;
import mindustry.game.EventType.*;
import mindustry.world.*;
import mindustry.mod.Plugin;
import mindustry.gen.*;
import mindustry.gen.Iconc;
import mindustry.content.*;
import mindustry.world.Tile;
import arc.Core.*;
import arc.math.*;
import arc.util.*;
import arc.util.Log;
import arc.struct.Seq;
import mindustry.mod.*;
import routerSnake.*;

import static mindustry.Vars.*;

public class routerSnakeMod extends Plugin{
    public static int worldHeight = 0;
    public static int worldWidth = 0;
    public static String rout = Character.toString(Iconc.blockRouter);
    public static Seq<routerSnake> snakes = new Seq<>();

    @Override
    public void init(){
        Log.info("Router Snake has awoken!");
        Events.on(WorldLoadEvent.class, e -> {
            snakes.clear();
            worldHeight = world.height() * 8;
            worldWidth = world.width() * 8;
            float snakeScl = Mathf.len(worldWidth, worldHeight);
            for(int i = 0; i < snakeScl; i += 2000){
                snakes.add(new routerSnake(0f, 0f, false, 12));
            };
            snakes.each(s -> {
                s.x = Mathf.random(worldWidth);
                s.y = Mathf.random(worldHeight);
            });
        });
        Events.run(Trigger.update, () -> {
            snakes.each(s -> s.update());
        });
    }
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("spawnsnake", "<x> <y> <canDie> <length>", "Spawn a router snake.", (args, player) -> {
            try{
                if(Integer.parseInt(args[3]) > 100 || Integer.parseInt(args[3]) < 1){
                    player.sendMessage("The specified length is invalid.");
                }else if(!player.admin){
                    player.sendMessage("You need to be admin to spawn snakes.");
                }else{
                    snakes.add(new routerSnake(Float.parseFloat(args[0]) * 8f, Float.parseFloat(args[1]) * 8f, Boolean.parseBoolean(args[2]), Integer.parseInt(args[3])));
                };
            }catch(Exception badArguments){
                player.sendMessage("Invalid arguments.");
            };
        });
    }
}
