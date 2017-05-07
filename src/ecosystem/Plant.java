package ecosystem;

import java.awt.Color;
import java.util.ArrayList;

public class Plant extends MainObj {
    public static ArrayList<Plant> LIST = new ArrayList<Plant>();
    private static NearestObj NEAREST_OBJ = new NearestObj();
    public Plant() {
        super();
        super.g = 196;
        super.b = super.r = 64;
        super.col = new Color(super.r, super.g, super.b, 222);
        super.life = super.max = (int)(super.life * 5);
        super.ate = 1;
        
        LIST.add(this);
    }
    
    
    @Override
    public void onEnterFrame() {
        if(--super.life <= 0) {
            // 死
            LIST.remove(this);
            return;
        } else if(super.life < super.max / 4 && !super.isHungry) {
            super.isHungry = true;
            super.col = new Color(222, 222, 127, 222);
        } else {
            // 水中かどうか
            NearestObj nObj = NEAREST_OBJ.get(this, Water.LIST);
            if(nObj.isHit) {
                super.life++;
                Water.LIST.get(nObj.idx).life -= (int)Math.round( Math.pow( Math.random(), Integer.parseInt(Ecosystem.NOW_FPS) ) );
            }
        }
    }
}
