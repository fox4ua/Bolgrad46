package ua.kharkiv.bolgrad46;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PixelPerfectActivity extends Activity {
    private static final int BG = Color.rgb(247, 249, 253);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        if (Build.VERSION.SDK_INT >= 29) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        root.addView(new HomeView(this), new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        setContentView(root);
    }

    private static final class HomeView extends View {
        private static final float DW = 941f;
        private static final float DH = 1495f;

        private static final int BLUE = Color.rgb(0, 111, 255);
        private static final int BLUE_DARK = Color.rgb(0, 69, 218);
        private static final int BLUE_LIGHT = Color.rgb(72, 166, 255);
        private static final int NAVY = Color.rgb(6, 29, 50);
        private static final int WHITE = Color.rgb(250, 252, 255);
        private static final int MUTED = Color.rgb(94, 121, 179);
        private static final int GREEN = Color.rgb(15, 202, 118);

        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint s = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF rect = new RectF();
        private final RectF doorHit = new RectF();
        private final RectF navHit = new RectF();

        private Bitmap hero;
        private float sx;
        private float sy;
        private int tab;
        private boolean holding;
        private boolean opened;
        private float progress;
        private ValueAnimator animator;

        HomeView(Context context) {
            super(context);
            setClickable(true);
            setFocusable(true);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            hero = loadHero(context);
        }

        private Bitmap loadHero(Context context) {
            try {
                InputStream input = context.getResources().openRawResource(R.raw.house46_hero_base64);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int n;
                while ((n = input.read(buffer)) >= 0) {
                    output.write(buffer, 0, n);
                }
                input.close();
                String encoded = new String(output.toByteArray(), StandardCharsets.US_ASCII).trim();
                byte[] raw = Base64.decode(encoded, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(raw, 0, raw.length);
            } catch (Exception ignored) {
                return null;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            sx = getWidth() / DW;
            sy = getHeight() / DH;
            background(canvas);
            if (tab == 0) {
                header(canvas);
                house(canvas);
                door(canvas);
            } else {
                secondary(canvas);
            }
            navigation(canvas);
        }

        private float X(float v) { return v * sx; }
        private float Y(float v) { return v * sy; }
        private float H(float v) { return v * sy; }

        private void background(Canvas c) {
            p.setShader(new LinearGradient(0, 0, getWidth(), getHeight(),
                new int[]{Color.rgb(251,252,255), Color.rgb(242,246,253), Color.rgb(249,251,255)},
                new float[]{0f, .58f, 1f}, Shader.TileMode.CLAMP));
            c.drawRect(0,0,getWidth(),getHeight(),p);
            p.setShader(null);
        }

        private void header(Canvas c) {
            float x=X(27), y=Y(38), w=X(887), h=H(238), r=X(38);
            blueGradient(x,y,x+w,y+h);
            shadow(p,X(12),Color.argb(90,35,100,220));
            rect.set(x,y,x+w,y+h); c.drawRoundRect(rect,r,r,p); clear(p); p.setShader(null);
            wave(c,x,y,w,h);

            building(c,x+X(112),y+h/2,X(150),Color.WHITE);
            float bw=X(136), bh=H(154), bx=x+w-bw-X(29), by=y+(h-bh)/2;
            bellCard(c,bx,by,bw,bh);

            fitText(c,"Болградская, 46",x+X(238),bx-X(18),y+h/2+H(20),X(56),X(34),Color.WHITE,true,Paint.Align.LEFT);
        }

        private void house(Canvas c) {
            float x=X(27), y=Y(323), w=X(887), h=H(576), radius=X(34);
            float statusH=H(153), imageBottom=y+h-statusH;
            rect.set(x,y,x+w,y+h);
            p.setColor(NAVY); shadow(p,X(10),Color.argb(60,20,62,108)); c.drawRoundRect(rect,radius,radius,p); clear(p);

            c.save();
            path.reset(); path.addRoundRect(rect,radius,radius,Path.Direction.CW); c.clipPath(path);
            if (hero != null) {
                centerCrop(c,hero,new RectF(x,y,x+w,imageBottom));
            } else {
                p.setShader(new LinearGradient(x,y,x+w,imageBottom,
                    new int[]{Color.rgb(10,45,71),Color.rgb(3,18,33)},null,Shader.TileMode.CLAMP));
                c.drawRect(x,y,x+w,imageBottom,p); p.setShader(null);
                fitText(c,"Фото дома не загрузилось",x+X(50),x+w-X(50),y+H(210),X(30),X(22),Color.WHITE,false,Paint.Align.CENTER);
            }

            online(c,x+X(20),y+H(18));
            float ty=imageBottom-H(174);
            temp(c,x+X(20),ty,X(276),H(139),"В помещении","22°C",false);
            temp(c,x+w-X(296),ty,X(276),H(139),"На улице","18°C",true);

            p.setShader(new LinearGradient(x,imageBottom,x,y+h,
                new int[]{Color.rgb(22,65,91),Color.rgb(7,28,49)},null,Shader.TileMode.CLAMP));
            c.drawRect(x,imageBottom,x+w,y+h,p); p.setShader(null);
            statusCells(c,x,imageBottom,w,statusH);
            c.restore();
        }

        private void centerCrop(Canvas c, Bitmap b, RectF dst) {
            float br=b.getWidth()/(float)b.getHeight();
            float dr=dst.width()/dst.height();
            Rect src;
            if (br>dr) {
                int nw=Math.round(b.getHeight()*dr); int l=(b.getWidth()-nw)/2;
                src=new Rect(l,0,l+nw,b.getHeight());
            } else {
                int nh=Math.round(b.getWidth()/dr); int t=Math.max(0,(b.getHeight()-nh)/2);
                src=new Rect(0,t,b.getWidth(),Math.min(b.getHeight(),t+nh));
            }
            p.setAlpha(255); c.drawBitmap(b,src,dst,p);
        }

        private void online(Canvas c,float x,float y) {
            float w=X(170),h=H(58);
            p.setColor(Color.argb(230,5,31,58)); rect.set(x,y,x+w,y+h); c.drawRoundRect(rect,X(29),X(29),p);
            p.setColor(Color.rgb(20,215,136)); c.drawCircle(x+X(29),y+h/2,X(10),p);
            text(c,"Онлайн",x+X(54),y+h/2+H(9),X(25),WHITE,false,Paint.Align.LEFT);
        }

        private void temp(Canvas c,float x,float y,float w,float h,String label,String value,boolean outside) {
            p.setColor(Color.argb(225,21,47,66)); shadow(p,X(5),Color.argb(80,0,12,25));
            rect.set(x,y,x+w,y+h); c.drawRoundRect(rect,X(28),X(28),p); clear(p);
            s.setStyle(Paint.Style.STROKE); s.setStrokeWidth(X(1.8f)); s.setColor(Color.argb(235,230,239,252));
            c.drawRoundRect(rect,X(28),X(28),s);
            thermometer(c,x+X(56),y+h/2+H(6),X(47),Color.WHITE,outside);
            fitText(c,label,x+X(96),x+w-X(14),y+H(54),X(24),X(19),WHITE,false,Paint.Align.LEFT);
            fitText(c,value,x+X(96),x+w-X(14),y+H(104),X(46),X(36),Color.WHITE,true,Paint.Align.LEFT);
        }

        private void statusCells(Canvas c,float x,float y,float w,float h) {
            float cw=w/3f;
            p.setColor(Color.argb(105,218,236,255));
            c.drawRect(x+cw-X(1),y+H(28),x+cw+X(1),y+h-H(28),p);
            c.drawRect(x+2*cw-X(1),y+H(28),x+2*cw+X(1),y+h-H(28),p);
            status(c,x,y,cw,h,0,"Замок","Онлайн");
            status(c,x+cw,y,cw,h,1,"Интернет","Подключено");
            status(c,x+2*cw,y,cw,h,2,"Bluetooth","Подключено");
        }

        private void status(Canvas c,float x,float y,float w,float h,int type,String title,String sub) {
            float ix=x+X(68), cy=y+h/2;
            if(type==0) lock(c,ix,cy,X(62),Color.WHITE);
            if(type==1) globe(c,ix,cy,X(64),Color.WHITE);
            if(type==2) bluetooth(c,ix,cy,X(64),Color.WHITE);
            float tx=x+X(134), right=x+w-X(10);
            fitText(c,title,tx,right,y+H(66),X(24),X(18),WHITE,false,Paint.Align.LEFT);
            fitText(c,sub,tx,right,y+H(112),X(21),X(15),GREEN,false,Paint.Align.LEFT);
        }

        private void door(Canvas c) {
            float x=X(27), y=Y(955), w=X(887), h=H(354), radius=X(38);
            blueGradient(x,y,x+w,y+h);
            shadow(p,X(12),Color.argb(90,38,97,215)); rect.set(x,y,x+w,y+h); c.drawRoundRect(rect,radius,radius,p); clear(p); p.setShader(null);
            wave(c,x,y,w,h);

            float cx=x+X(168), cy=y+h/2, cr=X(108);
            p.setColor(Color.argb(36,255,255,255)); shadow(p,X(12),Color.argb(125,255,255,255)); c.drawCircle(cx,cy,cr,p); clear(p);
            s.setStyle(Paint.Style.STROKE); s.setStrokeWidth(X(2)); s.setColor(Color.WHITE); c.drawCircle(cx,cy,cr,s);
            if(holding && !opened){
                s.setStrokeWidth(X(8)); s.setStrokeCap(Paint.Cap.ROUND); s.setColor(Color.rgb(104,220,255));
                rect.set(cx-cr-X(6),cy-cr-X(6),cx+cr+X(6),cy+cr+X(6)); c.drawArc(rect,-90,360*progress,false,s);
            }

            float tx=x+X(338), right=x+w-X(40);
            if(!opened){
                lock(c,cx,cy,X(98),Color.WHITE);
                fitText(c,"Открыть дверь",tx,right,y+H(185),X(60),X(39),Color.WHITE,true,Paint.Align.LEFT);
                fitText(c,holding?"Продолжайте удерживать":"Удерживайте для открытия",tx,right,y+H(250),X(29),X(21),Color.rgb(157,194,255),false,Paint.Align.LEFT);
            } else {
                check(c,cx,cy,X(80),Color.WHITE);
                fitText(c,"Дверь открыта",tx,right,y+H(184),X(55),X(38),Color.WHITE,true,Paint.Align.LEFT);
                fitText(c,"Команда выполнена",tx,right,y+H(249),X(28),X(21),Color.rgb(183,214,255),false,Paint.Align.LEFT);
            }
            doorHit.set(x,y,x+w,y+h);
        }

        private void navigation(Canvas c) {
            float x=X(27), w=X(887), h=H(120), y=getHeight()-H(18)-h;
            p.setShader(new LinearGradient(x,y,x,y+h,
                new int[]{Color.rgb(254,254,255),Color.rgb(236,241,251)},null,Shader.TileMode.CLAMP));
            shadow(p,X(9),Color.argb(70,74,106,178)); rect.set(x,y,x+w,y+h); c.drawRoundRect(rect,X(34),X(34),p); clear(p); p.setShader(null);
            s.setStyle(Paint.Style.STROKE); s.setStrokeWidth(X(1)); s.setColor(Color.rgb(217,226,244)); c.drawRoundRect(rect,X(34),X(34),s);
            String[] labels={"Главная","События","Заявки","Профиль"}; float cw=w/4f;
            for(int i=0;i<4;i++){
                float cx=x+cw*(i+.5f); int color=tab==i?BLUE:MUTED;
                navIcon(c,i,cx,y+H(46),X(43),color);
                fitText(c,labels[i],x+cw*i+X(7),x+cw*(i+1)-X(7),y+H(96),X(22),X(17),color,tab==i,Paint.Align.CENTER);
            }
            navHit.set(x,y,x+w,y+h);
        }

        private void secondary(Canvas c) {
            String[] titles={"","События","Заявки","Профиль"};
            String[] subs={"","История доступа и уведомлений","Ваши обращения и их статусы","Настройки пользователя"};
            float x=X(27),y=Y(38),w=X(887),h=H(238);
            blueGradient(x,y,x+w,y+h); rect.set(x,y,x+w,y+h); c.drawRoundRect(rect,X(38),X(38),p); p.setShader(null);
            text(c,titles[tab],x+X(42),y+H(108),X(52),Color.WHITE,true,Paint.Align.LEFT);
            fitText(c,subs[tab],x+X(42),x+w-X(42),y+H(164),X(25),X(18),Color.rgb(198,221,255),false,Paint.Align.LEFT);
            p.setColor(Color.WHITE); rect.set(x,Y(325),x+w,Y(690)); shadow(p,X(8),Color.argb(45,60,94,150)); c.drawRoundRect(rect,X(32),X(32),p); clear(p);
            fitText(c,"Раздел готов к подключению данных",x+X(45),x+w-X(45),Y(425),X(31),X(22),Color.rgb(35,54,91),true,Paint.Align.LEFT);
        }

        private void blueGradient(float x1,float y1,float x2,float y2){
            p.setAlpha(255);
            p.setShader(new LinearGradient(x1,y1,x2,y2,
                new int[]{BLUE_LIGHT,BLUE,BLUE_DARK},new float[]{0f,.50f,1f},Shader.TileMode.CLAMP));
        }

        private void wave(Canvas c,float x,float y,float w,float h){
            s.setStyle(Paint.Style.STROKE); s.setStrokeCap(Paint.Cap.ROUND); s.setStrokeWidth(Math.max(1f,X(1.7f)));
            s.setShader(new LinearGradient(x+w*.30f,y,x+w,y+h,
                new int[]{Color.argb(0,255,255,255),Color.argb(195,255,255,255),Color.argb(0,255,255,255)},null,Shader.TileMode.CLAMP));
            path.reset(); path.moveTo(x+w*.29f,y+h*.73f); path.cubicTo(x+w*.54f,y+h*.71f,x+w*.68f,y+h*.22f,x+w*.99f,y+h*.14f); c.drawPath(path,s);
            s.setShader(null);
        }

        private void bellCard(Canvas c,float x,float y,float w,float h){
            p.setAlpha(255); p.setShader(new LinearGradient(x,y,x+w,y+h,
                new int[]{Color.rgb(87,174,255),Color.rgb(1,82,210)},null,Shader.TileMode.CLAMP));
            shadow(p,X(12),Color.argb(125,0,61,170)); rect.set(x,y,x+w,y+h); c.drawRoundRect(rect,X(34),X(34),p); clear(p); p.setShader(null);
            s.setStyle(Paint.Style.STROKE); s.setStrokeCap(Paint.Cap.ROUND); s.setStrokeJoin(Paint.Join.ROUND); s.setStrokeWidth(X(5)); s.setColor(Color.WHITE);
            float cx=x+w/2,cy=y+h/2,bw=X(50),bh=H(61); rect.set(cx-bw/2,cy-bh/2,cx+bw/2,cy+bh/2); c.drawArc(rect,195,150,false,s);
            c.drawLine(cx-bw/2,cy+H(6),cx-bw/2,cy+H(25),s); c.drawLine(cx+bw/2,cy+H(6),cx+bw/2,cy+H(25),s); c.drawLine(cx-bw/2,cy+H(25),cx+bw/2,cy+H(25),s); c.drawCircle(cx,cy+H(38),X(5),s);
            p.setColor(Color.rgb(72,203,255)); c.drawCircle(x+w-X(28),y+H(27),X(12),p);
        }

        private void building(Canvas c,float cx,float cy,float size,int color){
            s.setStyle(Paint.Style.STROKE); s.setStrokeCap(Paint.Cap.ROUND); s.setStrokeJoin(Paint.Join.ROUND); s.setStrokeWidth(Math.max(X(2.2f),size*.018f)); s.setColor(color);
            float left=cx-size*.25f,right=cx+size*.25f,top=cy-size*.38f,bottom=cy+size*.38f;
            path.reset(); path.moveTo(left-size*.06f,top+size*.1f); path.lineTo(cx,top-size*.08f); path.lineTo(right+size*.06f,top+size*.1f); c.drawPath(path,s); c.drawRect(left,top+size*.1f,right,bottom,s);
            for(int rr=0;rr<4;rr++) for(int cc=0;cc<2;cc++){ float wx=cx+(cc==0?-size*.125f:size*.125f),wy=top+size*(.24f+rr*.14f); c.drawRect(wx-size*.042f,wy-size*.042f,wx+size*.042f,wy+size*.042f,s); }
            rect.set(cx-size*.07f,bottom-size*.21f,cx+size*.07f,bottom); c.drawRoundRect(rect,size*.04f,size*.04f,s); c.drawLine(cx-size*.48f,bottom,cx+size*.48f,bottom,s);
            c.drawLine(cx-size*.42f,bottom,cx-size*.42f,bottom-size*.25f,s); c.drawCircle(cx-size*.42f,bottom-size*.29f,size*.045f,s); c.drawLine(cx+size*.42f,bottom,cx+size*.42f,bottom-size*.25f,s); c.drawCircle(cx+size*.42f,bottom-size*.29f,size*.045f,s);
        }

        private void thermometer(Canvas c,float cx,float cy,float size,int color,boolean outside){
            s.setStyle(Paint.Style.STROKE); s.setStrokeCap(Paint.Cap.ROUND); s.setStrokeWidth(size*.08f); s.setColor(color);
            c.drawLine(cx,cy-size*.34f,cx,cy+size*.15f,s); c.drawCircle(cx,cy+size*.27f,size*.17f,s); c.drawLine(cx,cy-size*.34f,cx,cy+size*.27f,s);
            if(outside) c.drawCircle(cx+size*.45f,cy-size*.10f,size*.10f,s); else { path.reset(); path.moveTo(cx+size*.30f,cy-size*.24f); path.lineTo(cx+size*.48f,cy-size*.40f); path.lineTo(cx+size*.65f,cy-size*.24f); c.drawPath(path,s); }
        }

        private void lock(Canvas c,float cx,float cy,float size,int color){
            s.setStyle(Paint.Style.STROKE); s.setStrokeCap(Paint.Cap.ROUND); s.setStrokeJoin(Paint.Join.ROUND); s.setStrokeWidth(size*.055f); s.setColor(color);
            float bw=size*.62f,bh=size*.53f; rect.set(cx-bw/2,cy-size*.02f,cx+bw/2,cy-size*.02f+bh); c.drawRoundRect(rect,size*.05f,size*.05f,s);
            rect.set(cx-size*.24f,cy-size*.46f,cx+size*.24f,cy+size*.10f); c.drawArc(rect,180,-180,false,s); c.drawCircle(cx,cy+size*.19f,size*.055f,s); c.drawLine(cx,cy+size*.245f,cx,cy+size*.35f,s);
        }

        private void globe(Canvas c,float cx,float cy,float size,int color){
            s.setStyle(Paint.Style.STROKE); s.setStrokeWidth(size*.045f); s.setColor(color); c.drawCircle(cx,cy,size*.40f,s); rect.set(cx-size*.20f,cy-size*.40f,cx+size*.20f,cy+size*.40f); c.drawOval(rect,s); c.drawLine(cx-size*.40f,cy,cx+size*.40f,cy,s); c.drawLine(cx-size*.35f,cy-size*.18f,cx+size*.35f,cy-size*.18f,s); c.drawLine(cx-size*.35f,cy+size*.18f,cx+size*.35f,cy+size*.18f,s);
        }

        private void bluetooth(Canvas c,float cx,float cy,float size,int color){
            s.setStyle(Paint.Style.STROKE); s.setStrokeCap(Paint.Cap.ROUND); s.setStrokeJoin(Paint.Join.ROUND); s.setStrokeWidth(size*.055f); s.setColor(color); path.reset(); path.moveTo(cx,cy-size*.45f); path.lineTo(cx+size*.26f,cy-size*.19f); path.lineTo(cx-size*.22f,cy+size*.20f); path.lineTo(cx+size*.26f,cy+size*.45f); path.lineTo(cx+size*.26f,cy+size*.19f); path.lineTo(cx-size*.22f,cy-size*.20f); c.drawPath(path,s);
        }

        private void check(Canvas c,float cx,float cy,float size,int color){
            s.setStyle(Paint.Style.STROKE); s.setStrokeCap(Paint.Cap.ROUND); s.setStrokeJoin(Paint.Join.ROUND); s.setStrokeWidth(size*.09f); s.setColor(color); path.reset(); path.moveTo(cx-size*.34f,cy); path.lineTo(cx-size*.08f,cy+size*.27f); path.lineTo(cx+size*.40f,cy-size*.32f); c.drawPath(path,s);
        }

        private void navIcon(Canvas c,int type,float cx,float cy,float size,int color){
            s.setStyle(Paint.Style.STROKE); s.setStrokeCap(Paint.Cap.ROUND); s.setStrokeJoin(Paint.Join.ROUND); s.setStrokeWidth(size*.065f); s.setColor(color);
            if(type==0){ path.reset(); path.moveTo(cx-size*.40f,cy-size*.05f); path.lineTo(cx,cy-size*.40f); path.lineTo(cx+size*.40f,cy-size*.05f); c.drawPath(path,s); c.drawRect(cx-size*.30f,cy-size*.05f,cx+size*.30f,cy+size*.34f,s); }
            else if(type==1){ c.drawCircle(cx,cy,size*.39f,s); c.drawLine(cx,cy,cx,cy-size*.22f,s); c.drawLine(cx,cy,cx+size*.20f,cy+size*.10f,s); }
            else if(type==2){ rect.set(cx-size*.28f,cy-size*.36f,cx+size*.28f,cy+size*.40f); c.drawRoundRect(rect,size*.05f,size*.05f,s); c.drawLine(cx-size*.12f,cy-size*.45f,cx+size*.12f,cy-size*.45f,s); for(int i=-1;i<=1;i++) c.drawLine(cx-size*.13f,cy+i*size*.15f,cx+size*.13f,cy+i*size*.15f,s); }
            else { c.drawCircle(cx,cy-size*.20f,size*.17f,s); rect.set(cx-size*.32f,cy+size*.02f,cx+size*.32f,cy+size*.45f); c.drawArc(rect,200,140,false,s); }
        }

        private void text(Canvas c,String value,float x,float baseline,float size,int color,boolean bold,Paint.Align align){
            p.setShader(null); p.setAlpha(255); p.setStyle(Paint.Style.FILL); p.setColor(color); p.setTextAlign(align); p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL)); p.setTextSize(size); c.drawText(value,x,baseline,p);
        }

        private void fitText(Canvas c,String value,float left,float right,float baseline,float max,float min,int color,boolean bold,Paint.Align align){
            p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL)); p.setTextSize(max); float allowed=right-left; while(p.measureText(value)>allowed && p.getTextSize()>min) p.setTextSize(p.getTextSize()-X(1)); float x=align==Paint.Align.CENTER?(left+right)/2:left; p.setShader(null); p.setAlpha(255); p.setStyle(Paint.Style.FILL); p.setColor(color); p.setTextAlign(align); c.drawText(value,x,baseline,p);
        }

        private void shadow(Paint paint,float radius,int color){ paint.setShadowLayer(radius,0,radius*.40f,color); }
        private void clear(Paint paint){ paint.clearShadowLayer(); }

        @Override
        public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                if(doorHit.contains(x,y) && tab==0){ startHold(); return true; }
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE){ if(holding && !doorHit.contains(x,y)) cancelHold(); return true; }
            if(e.getAction()==MotionEvent.ACTION_UP){
                if(holding) cancelHold();
                if(navHit.contains(x,y)){ float cw=navHit.width()/4f; int next=(int)((x-navHit.left)/cw); if(next<0)next=0; if(next>3)next=3; tab=next; opened=false; invalidate(); }
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_CANCEL){ cancelHold(); return true; }
            return true;
        }

        private void startHold(){
            if(opened||holding)return; holding=true; progress=0f; if(animator!=null)animator.cancel(); animator=ValueAnimator.ofFloat(0f,1f); animator.setDuration(850); animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();}); animator.addListener(new AnimatorListenerAdapter(){@Override public void onAnimationEnd(Animator a){ if(holding && progress>=.99f){ holding=false; opened=true; progress=1f; vibrate(); invalidate(); postDelayed(()->{opened=false;progress=0f;invalidate();},1700); } }}); animator.start(); invalidate();
        }
        private void cancelHold(){ if(!holding)return; holding=false; if(animator!=null)animator.cancel(); progress=0f; invalidate(); }
        private void vibrate(){ try{ Vibrator v=(Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE); if(v==null)return; if(Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(55,VibrationEffect.DEFAULT_AMPLITUDE)); else v.vibrate(55); }catch(Exception ignored){} }
    }
}
