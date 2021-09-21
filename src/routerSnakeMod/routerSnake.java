package routerSnake;

import arc.Events;
import mindustry.game.EventType.*;
import mindustry.world.*;
import mindustry.mod.Plugin;
import mindustry.gen.*;
import mindustry.gen.Iconc;
import mindustry.content.*;
import mindustry.world.Tile;
import mindustry.entities.comp.*;
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
        if(i > 10f){
            Call.label(routerSnakeMod.rout, length / 6f, x, y);
            segments.remove(0);
            segments.add(new float[]{x, y});
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
            }else if(y < 0){
                heading = 90f;
            };
            heading += Mathf.random(-18f, 18f);
            x += Mathf.cosDeg(heading) * 10f;
            y += Mathf.sinDeg(heading) * 10f;
            if(Mathf.chance(0.008f) && Groups.player.size() > 0){
                target = Groups.player.index(Mathf.random(Groups.player.size() - 1));
            } else if(Mathf.chance(0.008f)){
                target = null;
            };
            if((length > 12 || canDecay) && Mathf.chance(0.0005f * Math.max((float)length, 10))){
                if(length == 1){
                    routerSnakeMod.snakes.remove(this);
                }else{
                    length--;
                    segments.remove(0);
                };
            };
            if(target != null && target.unit().type != null){
                heading = Mathf.slerp(heading, Mathf.angle(target.x - x, target.y - y), 0.25f);
                if(Mathf.len(target.x - x, target.y - y) < 80f){
                    target.unit().heal((target.unit().maxHealth / 1000f + 10f) * (float)length / 12f);
                };
            };
            segments.each(s -> {
                float segX = s[0];
                float segY = s[1];
                Tile newTile = world.tile(Mathf.floor(segX / 8), Mathf.floor(segY / 8));
                if(newTile != null && newTile.build != null){
                    Building newBuild = newTile.build;
                    if(newBuild.block != Blocks.distributor){
                        newBuild.heal(1f / 100f * newBuild.maxHealth());
                    };
                };
            });
            Tile newTile = world.tile(Mathf.floor(x / 8), Mathf.floor(y / 8));
            if(newTile != null && newTile.build != null){
                Building newBuild = newTile.build;
                if(newBuild.block == Blocks.distributor){
                    newBuild.damage(20f * (float)length / 12f);
                    x -= Mathf.cosDeg(heading) * 10f;
                    y -= Mathf.sinDeg(heading) * 10f;
                    heading = Mathf.angle(x - newBuild.x, y - newBuild.y);
                }else if(newBuild.block == Blocks.router && Mathf.chance((length < 10 && length != 0) ? 0.5f / length : 0.05f)){
                    if(Mathf.chance(0.01f * length)){
                        routerSnakeMod.snakes.add(new routerSnake(x, y, true, length / 2));
                        length = canDecay ? length / 2 : Math.max(length / 2, 12);
                        while(segments.size > length){
                            segments.remove(0);
                        };
                        newBuild.damage(19);
                    }else{
                        length++;
                        segments.add(new float[]{x, y});
                        newBuild.damage(19);
                    };
                };
            };
            i -= 10f;
        };
    }
}
