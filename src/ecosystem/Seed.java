package ecosystem;

import java.awt.Color;
import java.util.ArrayList;

public class Seed extends MainObj {
    public static ArrayList<Seed> LIST = new ArrayList<Seed>();
    public Seed() {
        super();
        super.r = super.g = super.b = 222;
        super.col = new Color(super.r, super.g, super.b, 222);
        LIST.add(this);
    }
    
    @Override
    public void onEnterFrame() {
        if(--super.wait <= 0) {
            LIST.remove(this);
            
            // 植物に変わる
            Plant newObj = new Plant();
            newObj.x = super.x;
            newObj.y = super.y;
        }
        return;
    }
}
