package ecosystem;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class MainObj {
    private static final int STAGE_WIDTH = Ecosystem.STAGE_WIDTH;
    private static final int STAGE_HEIGHT = Ecosystem.STAGE_HEIGHT;
    private static final int ENABLE_WIDTH = Ecosystem.ENABLE_WIDTH;
    private static final int ENABLE_HEIGHT = Ecosystem.ENABLE_HEIGHT;
    
    public static final int OBJ_SIZE = Ecosystem.OBJ_SIZE;
    public static final int HALF_OBJ_SIZE = Ecosystem.HALF_OBJ_SIZE;
    public static final int VIEW_RANGE = Ecosystem.VIEW_RANGE;
    
    public static final int REPEAT_MAX = 100;    // 移動量
    public static final int UNTIL_EAT = 100;    // 捕食までのフレーム
    public static final int UNTIL_COPULATE = 3;    // 交尾までの捕食数
    public static final int VWALK = 1;    // 歩行速度
    public static final int VRUN = 2;    // 走る
    public static final int LIFE = 540;    // 生命力
    public static final int WAIT = 127;    // ウェイト
    
    public float x, y;    // 座標
    public int r, g, b;    // カラー
    public Color col;
    public int direction, repeat;    // 方向・回数
    public float dx, dy;    // 移動量
    public int life, max;    // 生命力
    public int hungry;    // 空腹
    public Boolean isHungry;    // 食事可能
    public Boolean isLimit;    // 空腹の限界
    public Boolean isDead;    // 死
    public int wait;    // 種子から植物へのウェイト
    public int ate;    // 捕食数
    public int untilEat;    // 捕食可能フレーム
    public int untilCopulate;    // 交尾までの捕食数
    
    
    /**
     * コンストラクタ
     */
    public MainObj() {
        // 初期座標
        this.x = (float)(Math.random() * ENABLE_WIDTH);
        this.y = (float)(Math.random() * ENABLE_HEIGHT);
        
        // 初期方向・繰り返し回数
        this.changeDirection(true, false);
        
        // 移動量
        this.dx = this.dy = VWALK;
        
        // 初期ライフ
        this.life = LIFE + (int)(Math.random() * LIFE / 2);
        this.max = this.life;
        this.hungry = this.life / 4;
        this.isHungry = this.isLimit = this.isDead = false;
        this.untilEat = UNTIL_EAT;
        this.untilCopulate = UNTIL_COPULATE;
        this.wait = WAIT + (int)(Math.random() * 64);
    }
    
    
    /**
     * 方向変更
     * @param allDirections 9方向か5方向か
     * @param except0 静止状態を除くか
     */
    public void changeDirection(Boolean allDirections, Boolean except0) {
        int n = allDirections ? 9 : 5;
        int o = 0;
        if(except0) {
            n--;
            o++;
        }
        this.direction = (int)(Math.random() * n) + o;
        this.repeat = (int)(Math.random() * REPEAT_MAX);
    }
    
    
    /**
     * 空腹限界状態にセット
     */
    public void setLimit() {
        this.isHungry = this.isLimit = true;
        this.dx = this.dy = VRUN;
    }
    
    
    /**
     * 食べる
     * @param target 対象オブジェクト
     */
    public void eat(MainObj target) {
        target.life = 0;
        this.ate++;
        this.untilCopulate--;
        this.untilEat = UNTIL_EAT;
        this.isHungry = isLimit = false;
        
        float n = (target.ate > 0) ? 0.8f : 0.4f;
        this.life += this.max * n;
        if(this.life > this.max) {
            this.life = this.max;
        }
        
        this.dx = this.dy = VWALK;
    }
    
    
    /**
     * 交尾(子クラスから呼び出し)
     * @param target 対象オブジェクト
     */
    public void copulate(MainObj target) {
        this.col = new Color(this.r, this.g += 10, this.b);
        target.col = new Color(target.r, target.g += 10, target.b);
        
        this.untilCopulate = target.untilCopulate = UNTIL_COPULATE;
        if(Ecosystem.HIGH_LOAD) {
            // FPSを保てなくなった
            int diff = Ecosystem.FPS - Integer.parseInt(Ecosystem.NOW_FPS);
            this.untilCopulate = target.untilCopulate += diff;
        }
        return;
    }
    
    
    /**
     * 移動
     */
    public void walk() {
        float enableX = ENABLE_WIDTH - this.dx;
        float enableY = ENABLE_HEIGHT - this.dy;
        switch(this.direction) {
            case 0:
                // 留まる
                break;
            case 1:
                // 上
                if(this.y > this.dy) {
                    this.y -= this.dy;
                } else {
                    // 端
                    this.repeat /= 2;
                }
                break;
            case 5:
                // 右上
                if(this.y > this.dy) {
                    this.y -= Math.sqrt(this.dy) / 2;
                } else {
                    this.repeat /= 2;
                }
                if(this.x < enableX) {
                    this.x += Math.sqrt(this.dx) / 2;
                } else {
                    this.repeat /= 2;
                }
                break;
            case 2:
                // 右
                if(this.x < enableX) {
                    this.x += this.dx;
                } else {
                    this.repeat /= 2;
                }
                break;
            case 6:
                // 右下
                if(this.x < enableX) {
                    this.x += Math.sqrt(this.dx) / 2;
                } else {
                    this.repeat /= 2;
                }
                if(this.y < enableY) {
                    this.y += Math.sqrt(this.dy) / 2;
                } else {
                    this.repeat /= 2;
                }
                break;
            case 3:
                // 下
                if(this.y < enableY) {
                    this.y += this.dy;
                } else {
                    this.repeat /= 2;
                }
                break;
            case 7:
                // 左下
                if(this.y < enableY) {
                    this.y += Math.sqrt(this.dy) / 2;
                } else {
                    this.repeat /= 2;
                }
                if(this.x > this.dx) {
                    this.x -= Math.sqrt(this.dx) / 2;
                } else {
                    this.repeat /= 2;
                }
                break;
            case 4:
                // 左
                if(this.x > this.dx) {
                    this.x -= this.dx;
                } else {
                    this.repeat /= 2;
                }
                break;
            case 8:
                // 左上
                if(this.x > this.dx) {
                    this.x -= Math.sqrt(this.dx) / 2;
                } else {
                    this.repeat /= 2;
                }
                if(this.y > this.dy) {
                    this.y -= Math.sqrt(this.dy) / 2;
                } else {
                    this.repeat /= 2;
                }
                break;
        }
    }
    
    
    /**
     * 毎フレーム呼び出される処理
     */
    public void onEnterFrame() {
        return;
    }
    
    
    /**
     * targetに向かう
     * @param target 対象オブジェクト
     */
    public void goToTarget(MainObj target) {
        this.repeat = (int)(Math.random() * REPEAT_MAX);
        
        float diffX = this.x - target.x;
        float diffY = this.y - target.y;
        if(Math.abs(diffX) >= Math.abs(diffY)) {
            // 横移動が多い
            this.direction = diffX >= 0 ? 4 : 2;
        } else {
            // 縦移動が多い
            this.direction = diffY >= 0 ? 1 : 3;
        }
        return;
    }
        
    
    /**
     * 最も近いオブジェクトを探索
     * @param targetList 対象オブジェクトのArrayList
     * @return オブジェクト情報
     */
    public NearestObj getNearestObj(ArrayList<? extends MainObj> targetList) {
        NearestObj nearestObj = new NearestObj();
        if(targetList.size() == 0) {
            // 対象オブジェクトなし
            return nearestObj;
        }
        
        MainObj target;
        double distance;
        // 同クラスの検索かどうか
        Boolean flag = (this.getClass() == targetList.get(0).getClass());
        for(int i = targetList.size() - 1; i >= 0; i--) {
            target = targetList.get(i);
            if(flag && (target.untilCopulate > 0 || target.isLimit)) {
                continue;
            }
            distance = Math.pow(this.x - target.x, 2) + Math.pow(this.y - target.y, 2);
            if(distance < Ecosystem.HIT_RANGE) {
                // 触れている
                if(target.equals(this)) {
                    // own
                    continue;
                }
                nearestObj.distance = distance;
                nearestObj.idx = i;
                nearestObj.isHit = true;
                break;
            } else if(distance < nearestObj.distance) {
                // 範囲内で最も近い
                nearestObj.distance = distance;
                nearestObj.idx = i;
            }
        }
        return nearestObj;
    }
    
    
    /**
     * 最も近いオブジェクトを全て探索
     * @param targetList 対象オブジェクトのArrayList
     * @return オブジェクト情報の入ったArraList
     */
    public ArrayList<NearestObj> getNearestObjAll(ArrayList<? extends MainObj> targetList) {
        ArrayList<NearestObj> nearestObjList = new ArrayList<NearestObj>();
        NearestObj nearestObj = new NearestObj();
        if(targetList.size() == 0) {
            // 対象オブジェクトなし
            return nearestObjList;
        }
        
        MainObj target;
        double distance;
        // 同クラスの検索かどうか
        Boolean flag = (this.getClass() == targetList.get(0).getClass());
        for(int i = targetList.size() - 1; i >= 0; i--) {
            target = targetList.get(i);
            if(flag && (target.untilCopulate > 0 || target.isLimit)) {
                continue;
            }
            distance = Math.pow(this.x - target.x, 2) + Math.pow(this.y - target.y, 2);
            if(distance < Ecosystem.HIT_RANGE) {
                // 触れている
                if(target.equals(this)) {
                    // own
                    continue;
                }
                nearestObj.distance = distance;
                nearestObj.idx = i;
                nearestObj.isHit = true;
                nearestObjList.add(nearestObj);
            } else if(distance < Ecosystem.VIEW_RANGE) {
                // 最も近い
                nearestObj.distance = distance;
                nearestObj.idx = i;
                nearestObjList.add(nearestObj);
            }
        }
        return nearestObjList;
    }
    
    
    /**
     * 種子に変わる
     */
    public void changeSeed() {
        Seed newObj = new Seed();
        newObj.x = this.x;
        newObj.y = this.y;
        
        if(Ecosystem.HIGH_LOAD || this.ate <= 1) {
            return;
        }
        
        // 周りの座標
        int addLen = this.ate - 1;
        float rndX, rndY, newX, newY;
        int nowLen = Seed.LIST.size();
        while(Seed.LIST.size() < nowLen + addLen) {
            // -1 to +1
            rndX = (float)(Math.random() * 2 - 1);
            rndY = (float)(Math.random() * 2 - 1);
            newX = this.x + rndX * OBJ_SIZE;
            newY = this.y + rndY * OBJ_SIZE;
            if(newX < 0 || newX > ENABLE_WIDTH || newY < 0 || newY > ENABLE_HEIGHT) {
                // ステージ外
                continue;
            }
            newObj = new Seed();
            newObj.x = newX;
            newObj.y = newY;
        }
        return;
    }
    
    
    /**
     * 実体を描画
     * @param g Graphicsオブジェクト
     */
    public void draw(Graphics g) {
        float objectSize = OBJ_SIZE * Ecosystem.OBJ_RATIO;
        Point2D.Float point = Ecosystem.STAGE.getPoint(this.x + Ecosystem.HALF_OBJ_SIZE, this.y + Ecosystem.HALF_OBJ_SIZE);
        
        if(point.x > 0 && point.x < STAGE_WIDTH && point.y > 0 && point.y < STAGE_HEIGHT) {
            g.setColor(this.col);
            g.fillOval((int)(point.x - objectSize / 2), (int)(point.y - objectSize / 2), (int)objectSize, (int)objectSize);
        }
    }
    
    
    /**
     * 視界など実体以外を描画
     * @param g Graphicsオブジェクト
     * @param ownList 同族種のArrayList
     * @param foodList 食事対象のArrayList
     */
    public void drawEx(Graphics g, ArrayList<? extends MainObj> ownList, ArrayList<? extends MainObj> foodList) {
        if(Ecosystem.HIGH_LOAD) {
            // 処理落ち
            return;
        }
        
        Stage stage = Ecosystem.STAGE;
        Point2D.Float point = stage.getPoint(this.x + HALF_OBJ_SIZE, this.y + HALF_OBJ_SIZE);
        int yRatio = Ecosystem.QUARTER_VIEW.compareTo(false) + 1;    // QUARTER: 2, 2D: 1
        
        if(this.untilCopulate <= 0) {
            // 視界描画
            g.setColor(new Color(255 - this.col.getRed(), 255 - this.col.getGreen(), 255 - this.col.getBlue(), 64));
            float range  = (float)( Math.sqrt(VIEW_RANGE) * Ecosystem.OBJ_RATIO );
            g.fillOval((int)(point.x - range / 2), (int)(point.y - range / 2 / yRatio), (int)range, (int)(range / yRatio));
            
            NearestObj nearestObj = this.getNearestObj(ownList);
            if(nearestObj.idx < 0 || nearestObj.distance > VIEW_RANGE) {
                return;
            }
            MainObj target = ownList.get(nearestObj.idx);
            Point2D.Float targetPoint = stage.getPoint(target.x + HALF_OBJ_SIZE, target.y + HALF_OBJ_SIZE);
            
            g.setColor(Color.pink);
            g.drawLine((int)point.x, (int)point.y, (int)targetPoint.x, (int)targetPoint.y);
        }
        
        if(this.isLimit) {
            NearestObj nearestObj = this.getNearestObj(foodList);
            if(nearestObj.idx < 0 || nearestObj.distance > VIEW_RANGE) {
                return;
            }
            MainObj target = foodList.get(nearestObj.idx);
            Point2D.Float targetPoint = stage.getPoint(target.x + HALF_OBJ_SIZE, target.y + HALF_OBJ_SIZE);
            
            g.setColor(Color.white);
            g.drawLine((int)point.x, (int)point.y, (int)targetPoint.x, (int)targetPoint.y);
        }
    }
}
