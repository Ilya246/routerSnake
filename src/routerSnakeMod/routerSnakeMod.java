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
}
