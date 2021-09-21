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

    // snake vars
    public static float snakeUpdateDelay = 10f;
    public static float snakeHeadingRnd = 18f;
    public static float snakeLockOnChc = 0.008f;
    public static float snakeLockOffChc = 0.008f;
    public static int minPermSnakeSize = 12;
    public static float snakeSegLength = 10f;
    public static float snakeDecayRate = 0.0005f;
    public static float snakeChaseRate = 0.25f;
    public static float snakeHealRange = 80f;
    public static float snakeUnHealRate = 1f;
    public static float snakeBlHealRate = 1f;
    public static float baseDistributorDmg = 40f;
    public static float maxDistributorDmg = 79f;
    public static float distributorGrowChc = 0.02f;
    public static float baseRouterDmg = 19f;
    public static float baseConsumeChc = 0.05f;
    public static float baseSplitChc = 0.01f;

    // general vars
    public static float snakeCountDiv = 2000f;
    private static int maxSpawnedLength = 100;

    @Override
    public void init(){
        Log.info("Router Snake has awoken!");
        Events.on(WorldLoadEvent.class, e -> {
            snakes.clear();
            worldHeight = world.height() * tilesize;
            worldWidth = world.width() * tilesize;
            float snakeScl = Mathf.len(worldWidth, worldHeight);
            for(float i = 0f; i < snakeScl; i += snakeCountDiv){
                snakes.add(new routerSnake(0f, 0f, false, minPermSnakeSize));
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
                if(Integer.parseInt(args[3]) > maxSpawnedLength || Integer.parseInt(args[3]) < 1){
                    player.sendMessage("The specified length is invalid.");
                }else if(!player.admin){
                    player.sendMessage("You need to be admin to spawn snakes.");
                }else{
                    snakes.add(new routerSnake(Float.parseFloat(args[0]) * tilesize, Float.parseFloat(args[1]) * tilesize, Boolean.parseBoolean(args[2]), Integer.parseInt(args[3])));
                };
            }catch(Exception badArguments){
                player.sendMessage("Invalid arguments.");
            };
        });
    }
}
