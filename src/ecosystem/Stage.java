package ecosystem;

import java.awt.geom.Point2D;
import java.awt.Graphics;
import java.awt.Color;

public class Stage {
	// クォータービューオフセット
	public static Point2D.Float QUARTER_OFFSET;
	
	// 2Dオフセット
	public static Point2D.Float OFFSET;
	
	// 座標配列
	private static final int[] xPoints = new int[4];
	private static final int[] yPoints = new int[4];
	
	// オフセット込みの座標配列
	public static final int[] xPoints_qv = new int[4];
	public static final int[] yPoints_qv = new int[4];
	public static final int[] xPoints_2d = new int[4];
	public static final int[] yPoints_2d = new int[4];
	
	// ステージカラー
	private static final Color col = new Color(255, 255, 196);
	
	// 基準オフセットY座標
	public static int baseLX, baseRX;
	public static int baseX, baseY;
	
	/**
	 * 地面オブジェクト
	 * @param startX 左上X座標
	 * @param startY 左上Y
	 * @param stageWidth ステージ幅
	 * @param stageHeight ステージ高さ
	 */
	public Stage(int startX, int startY, int stageWidth, int stageHeight) {
		OFFSET = new Point2D.Float(0.0f, 0.0f);
		QUARTER_OFFSET = new Point2D.Float(stageWidth / 2, Ecosystem.GRAPH_HEIGHT);
		
		// 左上
		xPoints[0] = startX;
		yPoints[0] = startY;
		// 右上
		xPoints[1] = startX + stageWidth;
		yPoints[1] = startY;
		// 右下
		xPoints[2] = startX + stageWidth;
		yPoints[2] = startY + stageHeight;
		// 左下
		xPoints[3] = startX;
		yPoints[3] = startY + stageHeight;
		
		// クォータービュー座標
		Point2D.Float point = new Point2D.Float();
		for(int i = 0; i < 4; i++){
			point = this.getQuarterPoint(xPoints[i], yPoints[i]);
			xPoints_qv[i] = (int)point.x;
			yPoints_qv[i] = (int)point.y;
		}
		baseX = xPoints_qv[0];
		baseY = yPoints_qv[1];
		baseLX = xPoints_qv[3];
		baseRX = xPoints_qv[1];
	}
	
	
	/**
	 * 座標取得
	 * @param x 2D X座標
	 * @param y 2D Y座標
	 * @return クォータービュー/2D座標
	 */
	public Point2D.Float getPoint(float x, float y) {
		return (Ecosystem.QUARTER_VIEW) ? this.getQuarterPoint(x, y) : this.get2DPoint(x, y);
	}
	
	
	/**
	 * 2D座標を取得
	 * @param x
	 * @param y
	 * @return
	 */
	public Point2D.Float get2DPoint(float x, float y) {
		Point2D.Float point = new Point2D.Float();
		point.x = x + OFFSET.x;
		point.y = y + OFFSET.y;
		return point;
	}
	
	
	/**
	 * クォータービュー座標を取得
	 * @param x: 2D X座標
	 * @param y: 2D Y座標
	 * @return: クォータービュー座標
	 */
	public Point2D.Float getQuarterPoint(float x, float y) {
		double xyRadian = 45 * Math.PI / 180;
		double yzRadian = 30 * Math.PI / 180;
		double cosR = Math.cos(xyRadian);
		double sinR = Math.sin(xyRadian);
		
		// Z軸回転
		double newX = x * cosR - y * sinR;
		double newY = x * sinR + y * cosR;
		
		// X軸回転
		newY = newY * Math.sin(yzRadian);
		
		Point2D.Float point = new Point2D.Float();
		point.x = (float)(newX * Ecosystem.OBJ_RATIO) + QUARTER_OFFSET.x;
		point.y = (float)(newY * Ecosystem.OBJ_RATIO) + QUARTER_OFFSET.y;
		return point;
	}
	
	
	/**
	 * 描画
	 * @param g Graphicsオブジェクト
	 */
	public void draw(Graphics g) {
		Point2D.Float point;
		g.setColor(col);
		if(!Ecosystem.QUARTER_VIEW) {
			// 2D
			for(int i = 0; i < 4; i++) {
				point = this.get2DPoint(xPoints[i], yPoints[i]);
				xPoints_2d[i] = (int)point.x;
				yPoints_2d[i] = (int)point.y;
			}
			g.fillPolygon(xPoints_2d, yPoints_2d, 4);
		} else {
			// クォータービュー
			for(int i = 0; i < 4; i++){
				point = this.getQuarterPoint(xPoints[i], yPoints[i]);
				xPoints_qv[i] = (int)point.x;
				yPoints_qv[i] = (int)point.y;
			}
			g.fillPolygon(xPoints_qv, yPoints_qv, 4);
		}
	}
}
