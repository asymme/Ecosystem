package ecosystem;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JComponent;


@SuppressWarnings("serial")
public class Ecosystem extends JPanel implements ActionListener, Runnable, MouseWheelListener, MouseListener, MouseMotionListener, ChangeListener {
    // フレームサイズ
    private static final int FRAME_WIDTH = 900;
    private static final int FRAME_HEIGHT = FRAME_WIDTH;
    
    // 理想FPS
    public static int FPS = 60;
    
    // 初期オブジェクト数
    private static final int MEAT_EATER = 20;
    private static final int PLANT_EATER = 200;
    private static final int PLANT = 1500;
    private static final int WATER = 1000;
    
    // オブジェクトサイズ
    public static final int OBJ_SIZE = FRAME_WIDTH / 27;
    public static final int HALF_OBJ_SIZE = OBJ_SIZE / 2;
    
    // 視野
    public static final int VIEW_RANGE = (int)Math.pow(OBJ_SIZE * 6.5, 2);
    
    // 当たり判定用
    public static final int HIT_RANGE = OBJ_SIZE * OBJ_SIZE;
    
    // グラフ高さ
    public static final int GRAPH_HEIGHT = 5;
    
    // クォータービュー/2D
    public static Boolean QUARTER_VIEW = true;
    
    // オブジェクト倍率
    public static float OBJ_RATIO = (float)(Math.sqrt(2) / 2);
    
    // ステージオブジェクト用
    public static Stage STAGE;
    
    // ステージ幅/高さ
    public static int STAGE_WIDTH, STAGE_HEIGHT;
    // 実際に表示されている高さ
    private static int DISP_HEIGHT;
    
    // 現在のFPS
    public static String NOW_FPS;
    
    // 処理落ち
    public static Boolean HIGH_LOAD = false;
    
    // 設定パネル用
    private static JButton changeViewBtn;
    private static JButton startBtn;
    private static JTextField[] numField;
    
    private static int BORDER_X, BORDER_Y, GRAPH_Y;
    
    private static JLabel statLabel;
    private static JSlider fpsSlider;
    
    // 理想的な処理時間
    private static long SPF = 1000000L / FPS;
    
    // スレッド
    private static Timer TIMER;
    private static Thread THREAD;
    
    // FPS計測用
    private static long BASE_TIME;
    private static int COUNTER;
    private static final int[] FPS_LIST = new int[FPS];
    private static String AVERAGE_FPS;
    
    // マウス座標
    private static int mousePressedX, mousePressedY;
    
    
    /**
     * コンストラクタ
     */
    public Ecosystem() {
        JFrame mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        
        TIMER = new Timer(1000 / FPS, this);
        THREAD = new Thread(this);
        
        changeViewBtn = new JButton("Change View");
        changeViewBtn.addActionListener(this);
        startBtn = new JButton("Start");
        startBtn.addActionListener(this);
        
        this.setBackground(new Color(237, 237, 237));
        this.setLayout(new BorderLayout());
        
        JPanel configPanel = new JPanel();
        configPanel.setPreferredSize(new Dimension(FRAME_WIDTH, 50));
        configPanel.setBackground(Color.white);
        configPanel.add(changeViewBtn);
        
        JLabel[] label= new JLabel[3];
        label[0] = new JLabel("肉食：");
        label[1] = new JLabel("草食：");
        label[2] = new JLabel("植物：");
        
        numField = new JTextField[3];
        numField[0] = new JTextField(String.valueOf(MEAT_EATER), 5);
        numField[1] = new JTextField(String.valueOf(PLANT_EATER), 5);
        numField[2] = new JTextField(String.valueOf(PLANT), 5);
        
        for(int i = 0; i < numField.length; i++) {
            configPanel.add(label[i]);
            configPanel.add(numField[i]);
        }
        configPanel.add(startBtn);
        
        Hashtable<Integer, JComponent> table = new Hashtable<Integer, JComponent>();
        table.put(new Integer(1), new JLabel("1"));
        table.put(new Integer(FPS), new JLabel(String.valueOf(FPS) + "(fps)"));
        fpsSlider = new JSlider(1, FPS, FPS);
        fpsSlider.addChangeListener(this);
        fpsSlider.setLabelTable(table);
        fpsSlider.setPaintLabels(true);
        configPanel.add(fpsSlider);
        
        JPanel statPanel = new JPanel();
        statPanel.setPreferredSize(new Dimension(100, FRAME_HEIGHT));
        statPanel.setBackground(Color.white);
        statLabel = new JLabel();
        statPanel.add(statLabel);
        
        Container contentPane = mainFrame.getContentPane();
        contentPane.add(this, BorderLayout.CENTER);
        contentPane.add(configPanel, BorderLayout.SOUTH);
        contentPane.add(statPanel, BorderLayout.EAST);
        mainFrame.setVisible(true);
        
        // 内部マップが正方形になるように再整形
        int w = this.getWidth();
        int h = this.getHeight();
        int diff = w - h;
        this.setSize(h, h);
        STAGE_WIDTH = this.getWidth();
        STAGE_HEIGHT = this.getHeight();
        
        STAGE = new Stage(0, 0, STAGE_WIDTH, STAGE_HEIGHT);
        DISP_HEIGHT = Stage.yPoints_qv[2];
        mainFrame.setSize(FRAME_WIDTH - diff, DISP_HEIGHT + FRAME_HEIGHT - STAGE_HEIGHT + GRAPH_HEIGHT);
        
        this.addMouseWheelListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }
    
    
    /**
     * マウスイペント
     */
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    
    /**
     * クリック時の座標を取得
     */
    public void mousePressed(MouseEvent e) {
        mousePressedX = e.getX();
        mousePressedY = e.getY();
    }
    
    /**
     * ドラッグ時にステージを移動
     */
    public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - mousePressedX;
        int dy = e.getY() - mousePressedY;
        if(!QUARTER_VIEW) {
            // 2D
            if((Stage.yPoints_2d[0] < 0 && dy > 0) || (Stage.yPoints_2d[2] > this.getHeight() - GRAPH_HEIGHT && dy < 0)) {
                Stage.OFFSET.y += dy;
                
                // 行き過ぎを止める
                int minOffsetY = this.getHeight() - STAGE_HEIGHT - GRAPH_HEIGHT;
                if(Stage.OFFSET.y > 0) {
                    Stage.OFFSET.y = 0;
                } else if(Stage.OFFSET.y < minOffsetY) {
                    Stage.OFFSET.y = minOffsetY;
                }
            }
        } else {
            // クォータービュー
            if((Stage.xPoints_qv[3] < 0 && dx > 0) || (Stage.xPoints_qv[1] > STAGE_WIDTH && dx < 0)) {
                Stage.QUARTER_OFFSET.x += dx;
                
                // 行き過ぎを止める
                int diffX = Stage.xPoints_qv[0] - Stage.xPoints_qv[3];
                int minOffsetX = STAGE_WIDTH - (Stage.xPoints_qv[1] - Stage.xPoints_qv[0]);
                if(Stage.QUARTER_OFFSET.x > diffX) {
                    Stage.QUARTER_OFFSET.x = diffX;
                } else if(Stage.QUARTER_OFFSET.x < minOffsetX) {
                    Stage.QUARTER_OFFSET.x = minOffsetX;
                }
            }
            
            if((Stage.yPoints_qv[0] < 0 && dy > 0) || (Stage.yPoints_qv[2] > this.getHeight() - GRAPH_HEIGHT && dy < 0)) {
                Stage.QUARTER_OFFSET.y += dy;
                
                // 行き過ぎを止める
                int minOffsetY = this.getHeight() - (Stage.yPoints_qv[2] - Stage.yPoints_qv[0]) - GRAPH_HEIGHT;
                if(Stage.QUARTER_OFFSET.y > 0) {
                    Stage.QUARTER_OFFSET.y = 0;
                } else if(Stage.QUARTER_OFFSET.y < minOffsetY) {
                    Stage.QUARTER_OFFSET.y = minOffsetY;
                }
            }
        }
        mousePressedX = e.getX();
        mousePressedY = e.getY();
        Stage.QUARTER_BASE.x = Stage.xPoints_qv[0];
        Stage.QUARTER_BASE.y = Stage.yPoints_qv[1];
        if(THREAD == null) {
            repaint();
        }
    }
    
    /**
     * マウスホイールでステージを拡大縮小
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        if(!QUARTER_VIEW) {
            return;
        }
        
        float ratio = (float)(0.01f * e.getWheelRotation());
        float max = 2.0f;
        float min = (float)(Math.sqrt(2) / 2);
        OBJ_RATIO += ratio;
        if(OBJ_RATIO > max) {
            // 最大よりは大きくしない
            OBJ_RATIO = max;
        } else if(OBJ_RATIO < min) {
            // 最小よりは小さくしない
            OBJ_RATIO = min;
        }
        
        // 縮小時、ステージが中心に戻るように基点を更新
        if(ratio < 0) {
            if(Stage.yPoints_qv[0] > 0) {
                Stage.QUARTER_BASE.y -= Stage.yPoints_qv[0];
            } else if(Stage.yPoints_qv[2] < DISP_HEIGHT) {
                Stage.QUARTER_BASE.y += DISP_HEIGHT - Stage.yPoints_qv[2];
            }
            if(Stage.xPoints_qv[1] < STAGE_WIDTH) {
                Stage.QUARTER_BASE.x += STAGE_WIDTH - Stage.xPoints_qv[1];
            } else if(Stage.xPoints_qv[3] > 0) {
                Stage.QUARTER_BASE.x -= Stage.xPoints_qv[3];
            }
        }
        
        // 基点から拡大縮小する
        Stage.QUARTER_OFFSET.x += Stage.QUARTER_BASE.x - Stage.xPoints_qv[0];
        Stage.QUARTER_OFFSET.y += Stage.QUARTER_BASE.y - Stage.yPoints_qv[1];
        
        if(THREAD == null) {
            // スタート前
            repaint();
        }
    }
    
    /**
     * Swingコンポーネント描画(repaintからの呼び出し)
     */
    @Override
    public void paintComponent(Graphics g) {
        // reload
        STAGE.draw(g);
        
        MainObj obj;
        // 種子
        for(int i = Seed.LIST.size() - 1; i >= 0; i--) {
            obj = Seed.LIST.get(i);
            obj.onEnterFrame();
            if(Seed.LIST.contains(obj)) {
                obj.draw(g);
            }
        }
        
        // 植物
        for(int i = Plant.LIST.size() - 1; i >= 0; i--) {
            obj = Plant.LIST.get(i);
            obj.onEnterFrame();
            if(Plant.LIST.contains(obj)) {
                obj.draw(g);
            }
        }
        
        // 水
        for(int i = Water.LIST.size() - 1; i >= 0; i--) {
            obj = Water.LIST.get(i);
            obj.onEnterFrame();
            if(Water.LIST.contains(obj)) {
                obj.draw(g);
            }
        }
        
        // 草食
        for(int i = PlantEater.LIST.size() - 1; i >= 0; i--) {
            obj = PlantEater.LIST.get(i);
            obj.onEnterFrame();
            if(PlantEater.LIST.contains(obj)) {
                obj.draw(g);
            }
        }
        
        // 肉食
        for(int i = MeatEater.LIST.size() - 1; i >= 0; i--) {
            obj = MeatEater.LIST.get(i);
            obj.onEnterFrame();
            if(MeatEater.LIST.contains(obj)) {
                obj.draw(g);
            }
        }
        
        int me = MeatEater.LIST.size();
        int pe = PlantEater.LIST.size();
        int pl = Plant.LIST.size();
        int sd = Seed.LIST.size();
        int wt = Water.LIST.size();
        int total = me + pe + pl + sd + wt;
        if(me + pe + pl == 0) {
            THREAD = null;
        }
        if(total == 0) {
            // 0除算回避
            total = 1;
            BORDER_X = this.getWidth() - 1;
            BORDER_Y = this.getHeight() - 1;
            GRAPH_Y = BORDER_Y - GRAPH_HEIGHT;
        }
        
        // グラフ
        g.setColor(Color.cyan);
        g.fillRect(0, GRAPH_Y, (me + pe + pl + sd + wt) * STAGE_WIDTH / total, GRAPH_HEIGHT);
        g.setColor(Color.lightGray);
        g.fillRect(0, GRAPH_Y, (me + pe + pl + sd) * STAGE_WIDTH / total, GRAPH_HEIGHT);
        g.setColor(Color.green);
        g.fillRect(0, GRAPH_Y, (me + pe + pl) * STAGE_WIDTH / total, GRAPH_HEIGHT);
        g.setColor(Color.blue);
        g.fillRect(0, GRAPH_Y, (me + pe) * STAGE_WIDTH / total, GRAPH_HEIGHT);
        g.setColor(Color.red);
        g.fillRect(0, GRAPH_Y, me * STAGE_WIDTH / total, GRAPH_HEIGHT);
        
        // 枠線
        g.setColor(Color.black);
        g.drawLine(0, BORDER_Y, BORDER_X, BORDER_Y);
        g.drawLine(BORDER_X, 0, BORDER_X, BORDER_Y);
        
        // ステータス更新
        COUNTER++;
        long nowTime = System.currentTimeMillis();
        if(nowTime - BASE_TIME >= 1000) {
            // 指定FPSより遅れていればフラグを立て、戻ったら下ろす
            if(COUNTER <= FPS * 0.833f && !HIGH_LOAD) {
                HIGH_LOAD = true;
            } else if(COUNTER > FPS * 0.9f && HIGH_LOAD) {
                HIGH_LOAD = false;
            }
            
            // FPS更新
            int sum = 0;
            int d = 0;
            for(int i = 0; i < FPS_LIST.length - 1; i++) {
                FPS_LIST[i] = FPS_LIST[i + 1];
                if(FPS_LIST[i]  > 0) {
                    sum += FPS_LIST[i];
                    d++;
                }
            }
            FPS_LIST[FPS_LIST.length - 1] = COUNTER;
            float average = (float)(sum + COUNTER) / ++d;
            average = Math.round(average * 1000f) / 1000f;
            AVERAGE_FPS = String.valueOf(average) + "000";
            AVERAGE_FPS = AVERAGE_FPS.substring(0, 6);
            
            NOW_FPS = String.valueOf(COUNTER);
            COUNTER = 0;
            BASE_TIME = nowTime;
        }
        if(COUNTER % 2 == 0 || COUNTER % 3 == 0) {
            return;
        }
        
        String str = "<html><body><p><b>";
        str += "<font color=red size=4>肉食：" + String.valueOf(MeatEater.LIST.size()) + "</font><br>";
        str += "<font color=blue size=4>草食：" + String.valueOf(PlantEater.LIST.size()) + "</font><br>";
        str += "<font color=green size=4>植物：" + String.valueOf(Plant.LIST.size()) + "</font><br>";
        str += "<font color=gray size=4>種子：" + String.valueOf(Seed.LIST.size()) + "</font><br>";
        str += "<font color=aqua size=4>　水：" + String.valueOf(Water.LIST.size()) + "</font><br><br>";
        str += "<font color=black size=4>" + NOW_FPS + "/" + String.valueOf(FPS) + " fps<font><br>";
        str += "<font color=black size=4>(" + AVERAGE_FPS + " fps)<font>";
        str += "</b></p></body></html>";
        statLabel.setText(str);
    }
    
    /**
     * AWTコンポーネントイベント処理
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == TIMER) {
            repaint();
        } else if(e.getSource() == startBtn) {
            // 初期化
            TIMER.stop();
            THREAD = null;
            
            Water.LIST.clear();
            Seed.LIST.clear();
            Plant.LIST.clear();
            PlantEater.LIST.clear();
            MeatEater.LIST.clear();
            
            int[] startLength = new int[numField.length];
            for(int l = 0; l < numField.length; l++) {
                startLength[l] = Integer.parseInt(numField[l].getText(), 10);
            }
            
            // 植物
            for(int k = 0; k < startLength[2]; k++) {
                new Plant();
            }
            
            // 水(集合させる)
            Water newObj = new Water();
            float rndX, rndY, nx, ny;
            int idx;
            while(Water.LIST.size() < WATER) {
                idx = (int)Math.floor( Math.random() * Water.LIST.size() );
                Water obj = Water.LIST.get(idx);
                // -1 ～ +1
                rndX = (float)(Math.random() * 2 - 1);
                rndY = (float)(Math.random() * 2 - 1);
                
                nx = obj.x + rndX * OBJ_SIZE;
                ny = obj.y + rndY * OBJ_SIZE;
                if(nx < 0 || ny < 0 || nx > STAGE_WIDTH || ny > STAGE_HEIGHT) {
                    continue;
                }
                
                newObj = new Water();
                newObj.x = nx;
                newObj.y = ny;
            }
            
            // 草食
            for(int j = 0; j < startLength[1]; j++) {
                new PlantEater();
            }
            // 肉食
            for(int i = 0; i < startLength[0]; i++) {
                new MeatEater();
            }
            
            COUNTER = 0;
            BASE_TIME = System.currentTimeMillis();
            NOW_FPS = String.valueOf(FPS);
            HIGH_LOAD = false;
            
            for(int i = 0; i < FPS_LIST.length; i++) {
                FPS_LIST[i] = -1;
            }
            AVERAGE_FPS = "--";
            
            //TIMER.start();
            this.start();
        } else if(e.getSource() == changeViewBtn) {
            OBJ_RATIO = (QUARTER_VIEW = !QUARTER_VIEW) ? (float)(Math.sqrt(2) / 2) : 1.0f;
            if(THREAD == null) {
                repaint();
            }
        }
    }
    
    /**
     * Swingコンポーネントイベント処理
     */
    public void stateChanged(ChangeEvent e) {
        if(e.getSource() == fpsSlider) {
            FPS = fpsSlider.getValue();
            SPF = 1000000L / FPS;
            if(THREAD == null) {
                repaint();
            }
        }
    }
    
    /**
     * スレッド開始
     */
    public void start() {
        try {
            Thread.sleep(100);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        if(THREAD == null) {
            THREAD = new Thread(this);
            THREAD.start();
        }
    }
    
    /**
     * スレッドメインループ
     */
    @Override
    public void run() {
        long processingTime, availableTime;
        long errorTime = 0L;
        long baseTime = System.currentTimeMillis();
        while(THREAD != null) {
            repaint();
            
            // 使える時間
            availableTime = SPF + errorTime;
            while(true) {
                try {
                    processingTime = (System.currentTimeMillis() - baseTime) * 1000L;
                    if(availableTime < 0) {
                        // 処理落ちが続いた時
                        Thread.sleep(1);
                        // 厳密なスリープ時間を加算
                        processingTime += (System.currentTimeMillis() - baseTime) * 1000L - processingTime;
                        break;
                    } else if(processingTime >= availableTime) {
                        // 理想時間が経過していたら次フレームへ
                        break;
                    } else {
                        Thread.sleep(1);
                    }
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 誤差
            errorTime = availableTime - processingTime;
            baseTime = System.currentTimeMillis();
        }
        repaint();
    }
    
    public static void main(String[] args) {
        // TODO 自動生成されたメソッド・スタブ
        // フルスピードモード
        // 植物が端にある時、草食動物がはまる
        // 草食動物が四隅に追い詰められる
        new Ecosystem();
    }

}
