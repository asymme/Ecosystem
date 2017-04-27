package ecosystem;

import java.util.ArrayList;

public class NearestObj {
    public Boolean isHit;
    public double distance;
    public int idx;
    public NearestObj() {
        // コンストラクタ
        this.isHit = false;
        this.distance = Double.MAX_VALUE;
        this.idx = -1;
    }
    
    /**
     * 最も近いオブジェクトを取得
     * @param obj
     * @param targetList 対象ArrayList
     * @return オブジェクト情報
     */
    public NearestObj get(MainObj obj, ArrayList<? extends MainObj> targetList) {
        if(targetList.size() == 0) {
            // 対象オブジェクトリストなし
            return this;
        }
        
        MainObj target;
        double distance;
        // 同クラスの検索かどうか
        Boolean flag = (obj.getClass() == targetList.get(0).getClass());
        for(int i = targetList.size() - 1; i >= 0; i--) {
            target = targetList.get(i);
            if(flag && (target.equals(obj) || target.untilCopulate > 0 || target.isLimit)) {
                continue;
            }
            distance = Math.pow(obj.x - target.x, 2) + Math.pow(obj.y - target.y, 2);
            if(distance < Ecosystem.HIT_RANGE) {
                // 触れている
                this.distance = distance;
                this.idx = i;
                this.isHit = true;
                break;
            } else if(distance < this.distance) {
                // 範囲内で最も近い
                this.distance = distance;
                this.idx = i;
            }
        }
        return this;
    }
}
