package ecosystem;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class MeatEater extends MainObj {
    public static ArrayList<MeatEater> LIST = new ArrayList<MeatEater>();
    
    public MeatEater() {
        super();
        this.hungry = this.life / 8;
        super.r = 200;
        super.g = super.b = 64;
        super.col = new Color(super.r, super.g, super.b);
        super.untilCopulate = 10;
        LIST.add(0, this);
    }
    
    
    @Override
    public void onEnterFrame() {
        if(--super.life <= 0) {
            // 死
            LIST.remove(this);
            super.changeSeed();
            return;
        } else if(--super.untilEat <= 0 && !super.isHungry) {
            // 捕食可能
            super.isHungry = true;
        } else if(super.life < super.hungry && !super.isLimit) {
            // 空腹限界
            super.setLimit();
        }
        
        if(--super.repeat <= 0) {
            // 方向変換
            super.changeDirection(super.isLimit);
        }
        
        NearestObj nearestObj = new NearestObj();
        NearestObj nObj;
        if(super.untilCopulate <= 0 && !super.isLimit) {
            // 交尾可能
//            NearestObj nearestObj = super.getNearestObj(LIST);
            nObj = nearestObj.get(this, LIST);
            if(nObj.isHit) {
                // 触れていれば交尾
                this.copulate( LIST.get(nObj.idx) );
            } else if(nObj.distance < VIEW_RANGE) {
                // 視界内ならば同族種に向かう
                super.goToTarget( LIST.get(nObj.idx) );
            }
        } else if(super.isHungry) {
            // 捕食可能
            // 草食、水を混ぜたリストを作成
            ArrayList<MainObj> tmpList = new ArrayList<MainObj>();
            tmpList.addAll(Water.LIST);
            tmpList.addAll(PlantEater.LIST);
            
//            NearestObj nearestObj = super.getNearestObj(tmpList);
            nObj = nearestObj.get(this, tmpList);
            // idxから草食か水か判定
            MainObj target = null;
            if(nObj.idx < 0) {
                // 近くになし
            } else if(nObj.idx < Water.LIST.size()) {
                target = Water.LIST.get(nObj.idx);
            } else {
                target = PlantEater.LIST.get(nObj.idx - Water.LIST.size());
            }
            if(nObj.isHit) {
                // 触れていれば捕食
                super.eat(target);
            } else if(super.isLimit && nObj.distance < VIEW_RANGE) {
                // 視界内ならば捕食対象へ向かう
                super.goToTarget(target);
            }
        } else {
            // ランダムウォーク
            // 水中かどうか
//            NearestObj nearestObj = super.getNearestObj(Water.LIST);
            nObj = nearestObj.get(this, Water.LIST);
            if(nObj.isHit) {
                super.life++;
                super.untilEat++;
            }
        }
        
        super.walk();
    }
    
    
    @Override
    public void changeSeed() {
        LIST.remove(this);
        super.changeSeed();
    }
    
    
    @Override
    public void copulate(MainObj target) {
        int len = super.untilCopulate * target.untilCopulate + 1;
        MeatEater newObj;
        for(int i = 0; i < len; i++) {
            newObj = new MeatEater();
            newObj.x = super.x;
            newObj.y = super.y;
        }
        super.copulate(target);
    }
    
    public void draw(Graphics g) {
        super.draw(g);
        super.drawEx(g, MeatEater.LIST, PlantEater.LIST);
    }
}
