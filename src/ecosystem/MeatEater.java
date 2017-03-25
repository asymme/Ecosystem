package ecosystem;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class MeatEater extends MainObj {
	public static ArrayList<MeatEater> LIST = new ArrayList<MeatEater>();
	
	public MeatEater() {
		super();
		super.r = 200;
		super.g = super.b = 64;
		super.col = new Color(super.r, super.g, super.b);
		super.untilCopulate = 10;
		LIST.add(this);
	}
	
	
	@Override
	public void onEnterFrame() {
		if(--super.life <= 0) {
			// 死
			super.isDead = true;
			this.changeSeed();
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
			Boolean b1 = true;
			Boolean b2 = false;
			if(super.isLimit) {
				b1 = false;
				b2 = true;
			}
			super.changeDirection(b1, b2);
		}
		
		if(super.untilCopulate <= 0 && !super.isLimit) {
			// 交尾可能
			NearestObj nearestObj = super.getNearestObj(LIST);
			if(nearestObj.isHit) {
				// 触れていれば交尾
				this.copulate( LIST.get(nearestObj.idx) );
			} else if(nearestObj.distance < VIEW_RANGE) {
				// 視界内ならば同族種に向かう
				super.goToTarget( LIST.get(nearestObj.idx) );
			}
		} else if(super.isHungry || super.isLimit) {
			// 捕食可能
			// 草食、水を混ぜたリストを作成
			ArrayList<MainObj> tmpList = new ArrayList<MainObj>();
			tmpList.addAll(Water.LIST);
			tmpList.addAll(PlantEater.LIST);
			
			NearestObj nearestObj = super.getNearestObj(tmpList);
			// idxから草食か水か判定
			MainObj target = null;
			if(nearestObj.idx < 0) {
				// 近くになし
			} else if(nearestObj.idx < Water.LIST.size()) {
				target = Water.LIST.get(nearestObj.idx);
			} else {
				target = PlantEater.LIST.get(nearestObj.idx - Water.LIST.size());
			}
			if(nearestObj.isHit) {
				// 触れていれば捕食
				super.eat(target);
			} else if(super.isLimit && nearestObj.distance < VIEW_RANGE) {
				// 視界内ならば捕食対象へ向かう
				super.goToTarget(target);
			}
		} else {
			// ランダムウォーク
			// 水中かどうか
			NearestObj nearestObj = super.getNearestObj(Water.LIST);
			if(nearestObj.isHit) {
				super.life++;
				super.untilEat++;
			}
		}
		
		super.walk();
	}
	
	
	@Override
	public void changeSeed() {
		LIST.remove(this);
		
		// 種子に変わる
		Seed newObj = new Seed();
		newObj.x = super.x;
		newObj.y = super.y;
		
		if(Ecosystem.HIGH_LOAD || super.ate <= 1) {
			return;
		}
		
		// 周りの座標
		int len = (int)Math.ceil(super.ate - 1);
		float rndX, rndY;
		for(int i = 0; i < len; i++) {
			// -1 ～ +1
			rndX = (float)(Math.random() * 2 - 1);
			rndY = (float)(Math.random() * 2 - 1);
			newObj = new Seed();
			newObj.x = super.x + rndX * OBJ_SIZE;
			newObj.y = super.y + rndY * OBJ_SIZE;
		}
	}
	
	
	@Override
	public void copulate(MainObj target) {
		int len = super.untilCopulate * target.untilCopulate + 1;
		MeatEater newObj;
		for(int i = 0; i < len; i++) {
			newObj = new MeatEater();
			newObj.x = super.x;
			newObj.y = super.y;
			if(len == 1) {
				newObj.direction = 0;
				newObj.repeat = 10;
			}
		}
		super.copulate(target);
	}
	
	public void draw(Graphics g) {
		super.draw(g);
		super.drawEx(g, MeatEater.LIST, PlantEater.LIST);
	}
}
