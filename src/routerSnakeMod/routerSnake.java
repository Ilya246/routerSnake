package routerSnake;

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
import routerSnakeMod.*;

import static mindustry.Vars.*;

public class routerSnake{
    public float x = 0f;
    public float y = 0f;
    public float heading = 0f;
    public float i = 0f;
    public Player target;
    public Seq<float[]> segments = new Seq<>();
    public int length = 12;
    public boolean canDecay = false;
    public static float[] tmp = new float[2];

    public routerSnake(float x, float y, boolean canDecay, int length){
        this.x = x;
        this.y = y;
        this.canDecay = canDecay;
        this.length = length;
        for(int it = 0; it < length; it++){
            segments.add(new float[]{0f, 0f});
        };
    }

    public void update(){
        i += Time.delta;
        if(i > routerSnakeMod.snakeUpdateDelay){
            if(length < 1){
                routerSnakeMod.snakes.remove(this);
                return;
            };
            Call.label(routerSnakeMod.rout, length / 6f, x, y);
            tmp = segments.remove(0);
            tmp[0] = x;
            tmp[1] = y;
            segments.add(tmp);
            while(segments.size < length){
                segments.add(new float[]{x, y});
            };
            while(segments.size > length){
                segments.remove(0);
            };
            if(x > routerSnakeMod.worldWidth){
                heading = 180f;
            }else if(x < 0f){
                heading = 0f;
            };
            if(y > routerSnakeMod.worldHeight){
                heading = 270f;
            }else if(y < 0f){
                heading = 90f;
            };
            heading += Mathf.random(-routerSnakeMod.snakeHeadingRnd, routerSnakeMod.snakeHeadingRnd);
            x += Mathf.cosDeg(heading) * routerSnakeMod.snakeSegLength;
            y += Mathf.sinDeg(heading) * routerSnakeMod.snakeSegLength;
            if(Mathf.chance(routerSnakeMod.snakeLockOnChc) && Groups.player.size() > 0){
                target = Groups.player.index(Mathf.random(Groups.player.size() - 1));
            }else if(Mathf.chance(routerSnakeMod.snakeLockOffChc)){
                target = null;
            };
            if((length > routerSnakeMod.minPermSnakeSize || canDecay) && Mathf.chance(routerSnakeMod.snakeDecayRate * Math.max((float)length, routerSnakeMod.minPermSnakeSize))){
                length--;
                segments.remove(0);
            };
            if(target != null && !target.unit().isNull()){
                heading = Mathf.slerp(heading, Mathf.angle(target.x - x, target.y - y), routerSnakeMod.snakeChaseRate);
                if(Mathf.len(target.x - x, target.y - y) < routerSnakeMod.snakeHealRange){
                    target.unit().heal((target.unit().maxHealth / 10000f + 1f) * (float)length * routerSnakeMod.snakeUnHealRate);
                };
            };
            segments.each(s -> {
                float segX = s[0];
                float segY = s[1];
                Tile newTile = world.tile(Mathf.floor(segX / tilesize), Mathf.floor(segY / tilesize));
                if(newTile != null && newTile.build != null){
                    Building newBuild = newTile.build;
                    if(newBuild.block != Blocks.distributor){
                        newBuild.heal(routerSnakeMod.snakeBlHealRate / 100f * newBuild.maxHealth());
                    };
                };
            });
            Tile newTile = world.tile(Mathf.floor(x / tilesize), Mathf.floor(y / tilesize));
            if(newTile != null && newTile.build != null){
                Building newBuild = newTile.build;
                if(newBuild.block == Blocks.distributor){
                    newBuild.damage(Math.min(routerSnakeMod.baseDistributorDmg * (float)length / routerSnakeMod.minPermSnakeSize, routerSnakeMod.maxDistributorDmg));
                    x -= Mathf.cosDeg(heading) * routerSnakeMod.snakeSegLength;
                    y -= Mathf.sinDeg(heading) * routerSnakeMod.snakeSegLength;
                    heading = Mathf.angle(x - newBuild.x, y - newBuild.y);
                    if(Mathf.chance(routerSnakeMod.distributorGrowChc * length)){
                        length++;
                        segments.add(new float[]{x, y});
                    };
                }else if(newBuild.block == Blocks.router && Mathf.chance(routerSnakeMod.baseConsumeChc * Math.max(routerSnakeMod.minPermSnakeSize / length, 1f))){
                    if(Mathf.chance(routerSnakeMod.baseSplitChc * length)){
                        routerSnakeMod.snakes.add(new routerSnake(x, y, true, length / 2));
                        length = canDecay ? length / 2 : Math.max(length / 2, routerSnakeMod.minPermSnakeSize);
                        while(segments.size > length){
                            segments.remove(0);
                        };
                    }else{
                        length++;
                        segments.add(new float[]{x, y});
                    };
                    newBuild.damage(routerSnakeMod.baseRouterDmg);
                };
            };
            i -= 10f;
        };
    }
}
