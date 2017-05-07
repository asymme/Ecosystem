package ecosystem;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class MainObj {
    private static final int STAGE_WIDTH = Ecosystem.STAGE_WIDTH;
    private static final int DISP_HEIGHT = Ecosystem.DISP_HEIGHT;
    private static final int ENABLE_WIDTH = Ecosystem.ENABLE_WIDTH;
    private static final int ENABLE_HEIGHT = Ecosystem.ENABLE_HEIGHT;
    private static final Stage STAGE = Ecosystem.STAGE;
    private static NearestObj NEAREST_OBJ = new NearestObj();
    
    public static final int OBJ_SIZE = Ecosystem.OBJ_SIZE;
    public static final int HALF_OBJ_SIZE = Ecosystem.HALF_OBJ_SIZE;
    public static final int VIEW_RANGE = Ecosystem.VIEW_RANGE;
    
    public static final int REPEAT_MAX = 100;    // 移動量
    public static final int UNTIL_EAT = 100;    // 捕食までのフレーム
    public static final int UNTIL_COPULATE = 3;    // 交尾までの捕食数
    public static final int VWALK = 1;    // 歩行速度
    public static final int VRUN = 2;    // 走る
    public static final int LIFE = 540;    // 生命力
    
    public float x, y;    // 座標
    public int r, g, b;    // カラー
    public Color col;
    public int degree, repeat;    // 方向・回数
    public float dx, dy;    // 移動量
    public int life, max;    // 生命力
    public int hungry;    // 空腹
    public Boolean isHungry;    // 食事可能
    public Boolean isLimit;    // 空腹の限界
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
        this.changeDirection(false);
        
        // 移動量
        this.dx = this.dy = VWALK;
        
        // 初期ライフ
        this.life = LIFE + (int)(Math.random() * LIFE / 2);
        this.max = this.life;
        this.hungry = this.life / 4;
        this.isHungry = this.isLimit = false;
        this.untilEat = UNTIL_EAT;
        this.untilCopulate = UNTIL_COPULATE;
    }
    
    
    /**
     * 方向変更
     * @param except0 静止状態を除くか
     */
    public void changeDirection(Boolean except0) {
        int n = (except0) ? 360 : 400;
        this.degree = (int)(Math.random() * n);
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
        if(this.degree >= 360) {
            return;
        }
        double radian = this.degree * Math.PI / 180.0d;
        float newX = (float)Math.cos(radian) * this.dx + this.x;
        float newY = (float)Math.sin(radian) * this.dy + this.y;
        if(newX > 0 && newX < ENABLE_WIDTH) {
            this.x = newX;
        } else {
            this.repeat /= 2;
        }
        if(newY > 0 && newY < ENABLE_HEIGHT) {
            this.y = newY;
        } else {
            this.repeat /= 2;
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
    public void goToTarget(float targetX, float targetY) {
        double radian = Math.atan2(targetY - this.y, targetX - this.x);
        this.degree = (int)Math.round(radian * 180.0d / Math.PI);
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
        float halfSize = objectSize / 2;
        Point2D.Float point = STAGE.getPoint(this.x + HALF_OBJ_SIZE, this.y + HALF_OBJ_SIZE);
        // 映っている範囲内のみ描画
        if(point.x > 0 && point.x < STAGE_WIDTH && point.y > 0 && point.y < DISP_HEIGHT) {
            g.setColor(this.col);
            g.fillOval((int)(point.x - halfSize), (int)(point.y - halfSize), (int)objectSize, (int)objectSize);
        }
    }
    
    
    /**
     * 視界など実体以外を描画
     * @param g Graphicsオブジェクト
     * @param ownList 同族種のArrayList
     * @param foodList 食事対象のArrayList
     */
    public Point2D.Float drawEx(Graphics g, ArrayList<? extends MainObj> ownList, ArrayList<? extends MainObj> foodList) {
        if(Ecosystem.HIGH_LOAD) {
            // 処理落ち
            return null;
        }
        
        NearestObj nObj;
        Point2D.Float point = STAGE.getPoint(this.x + HALF_OBJ_SIZE, this.y + HALF_OBJ_SIZE);
        if(this.untilCopulate <= 0) {
            // 視界描画
//            int yRatio = Ecosystem.QUARTER_VIEW.compareTo(false) + 1;    // QUARTER: 2, 2D: 1
//            g.setColor(new Color(255 - this.col.getRed(), 255 - this.col.getGreen(), 255 - this.col.getBlue(), 64));
//            float range  = (float)( Math.sqrt(VIEW_RANGE) * Ecosystem.OBJ_RATIO );
//            g.fillOval((int)(point.x - range / 2), (int)(point.y - range / 2 / yRatio), (int)range, (int)(range / yRatio));
            
            nObj = NEAREST_OBJ.get(this, ownList);
            if(nObj.idx < 0 || nObj.distance > VIEW_RANGE) {
                return point;
            }
            MainObj target = ownList.get(nObj.idx);
            Point2D.Float targetPoint = STAGE.getPoint(target.x + HALF_OBJ_SIZE, target.y + HALF_OBJ_SIZE);
            
            g.setColor(Color.pink);
            g.drawLine((int)point.x, (int)point.y, (int)targetPoint.x, (int)targetPoint.y);
        }
        
        if(this.isLimit) {
            nObj = NEAREST_OBJ.get(this, foodList);
            if(nObj.idx < 0 || nObj.distance > VIEW_RANGE) {
                return point;
            }
            MainObj target = foodList.get(nObj.idx);
            Point2D.Float targetPoint = STAGE.getPoint(target.x + HALF_OBJ_SIZE, target.y + HALF_OBJ_SIZE);
            
            g.setColor(Color.white);
            g.drawLine((int)point.x, (int)point.y, (int)targetPoint.x, (int)targetPoint.y);
        }
        return point;
    }
}
