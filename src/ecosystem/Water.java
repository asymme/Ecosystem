package ecosystem;

import java.awt.Color;
import java.util.ArrayList;

public class Water extends MainObj {
    public static ArrayList<Water> LIST = new ArrayList<Water>();
    public Water() {
        super();
        super.r = 0;
        super.g = super.b = 255;
        super.col = new Color(super.r, super.g, super.b, 127);
        super.ate = 1;
        LIST.add(this);
    }
    
    @Override
    public void onEnterFrame() {
        if(super.life <= 0) {
            // 死
            LIST.remove(this);
            return;
        }
    }
}
