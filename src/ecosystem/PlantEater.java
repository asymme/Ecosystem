package ecosystem;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class PlantEater extends MainObj {
	public static ArrayList<PlantEater> LIST = new ArrayList<PlantEater>();
	
	public PlantEater() {
		super();
		super.b = 200;
		super.r = super.g = 64;
		super.col = new Color(super.r, super.g, super.b);
		LIST.add(this);
	}
	
	
	/**
	 * targetから逃げる
	 * @param target 対象オブジェクト
	 */
	public void runawayFromTarget(MainObj target) {
		super.repeat = (int)(Math.random() * REPEAT_MAX);
		
		float diffX = super.x - target.x;
		float diffY = super.y - target.y;
		if(diffX >= 0 && diffY >= 0) {
			// 右下に逃げる
			super.direction = 6;
		} else if(diffX >= 0 && diffY < 0) {
			// 右上
			super.direction = 5;
		} else if(diffX < 0 && diffY >= 0) {
			// 左下
			super.direction = 7;
		} else if(diffX < 0 && diffY < 0) {
			// 左上
			super.direction = 8;
		}
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
			} else {
				// 視界内にいなければ肉食から逃げる判断
				nearestObj = super.getNearestObj(MeatEater.LIST);
				if(nearestObj.distance < VIEW_RANGE) {
					this.runawayFromTarget( MeatEater.LIST.get(nearestObj.idx) );
				}
			}
		} else if(super.isHungry || super.isLimit) {
			// 捕食可能
			// 植物、水を混ぜたリストを作成
			ArrayList<MainObj> tmpList = new ArrayList<MainObj>();
			tmpList.addAll(Plant.LIST);
			tmpList.addAll(Water.LIST);
			
			NearestObj nearestObj = super.getNearestObj(tmpList);
			// idxから植物か水か判定
			MainObj target = null;
			if(nearestObj.idx < 0) {
				// 近くになし
			} else if(nearestObj.idx < Plant.LIST.size()) {
				target = Plant.LIST.get(nearestObj.idx);
			} else {
				target = Water.LIST.get(nearestObj.idx - Plant.LIST.size());
			}
			if(nearestObj.isHit) {
				// 触れていれば捕食
				super.eat(target);
				//target.life = 0;
			} else if(super.isLimit && nearestObj.distance < VIEW_RANGE) {
				// 視界内ならば捕食対象へ向かう
				super.goToTarget(target);
			} else if(!super.isLimit && super.isHungry) {
				// 視界内になければ肉食から逃げる判断
				nearestObj = super.getNearestObj(MeatEater.LIST);
				if(nearestObj.distance < VIEW_RANGE) {
					this.runawayFromTarget( MeatEater.LIST.get(nearestObj.idx) );
				}
			}
		} else {
			// ランダムウォーク
			// 水中かどうか
			NearestObj nearestObj = super.getNearestObj(Water.LIST);
			if(nearestObj.isHit) {
				super.life++;
				super.untilEat++;
			}
			
			// 肉食から逃げる判断
			nearestObj = super.getNearestObj(MeatEater.LIST);
			if(nearestObj.distance < VIEW_RANGE) {
				this.runawayFromTarget( MeatEater.LIST.get(nearestObj.idx) );
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
		PlantEater newObj;
		for(int i = 0; i < len; i++) {
			newObj = new PlantEater();
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
		super.drawEx(g, PlantEater.LIST, Plant.LIST);
	}
}
