package ua.kharkiv.bolgrad46;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.util.Base64;
import android.view.*;
import android.widget.Toast;
import java.io.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(5,19,37));
        getWindow().setNavigationBarColor(Color.rgb(4,16,31));
        if (Build.VERSION.SDK_INT >= 29) {
            getWindow().setStatusBarContrastEnforced(false);
            getWindow().setNavigationBarContrastEnforced(false);
        }
        setContentView(new HomeView(this));
    }

    static class HomeView extends View {
        static final float W=750f;
        static final int BG=Color.rgb(5,19,37), BG2=Color.rgb(4,16,31),
                SUR=Color.rgb(8,28,50), BORDER=Color.rgb(25,60,94),
                BLUE=Color.rgb(37,133,255), BLUEL=Color.rgb(79,160,255),
                TXT=Color.rgb(246,249,255), MUT=Color.rgb(139,164,197),
                GREEN=Color.rgb(54,211,128);
        final Paint p=new Paint(3), s=new Paint(3);
        final Handler h=new Handler(Looper.getMainLooper());
        Bitmap hero;
        int topInset,bottomInset;
        float sc=1, vh=1500, drag=0;
        boolean dragging, opened;
        ValueAnimator anim;

        HomeView(Context c) {
            super(c);
            s.setStyle(Paint.Style.STROKE); s.setStrokeCap(Paint.Cap.ROUND); s.setStrokeJoin(Paint.Join.ROUND);
            hero=loadHero();
            setOnApplyWindowInsetsListener((v,i)->{
                topInset=i.getSystemWindowInsetTop();
                bottomInset=i.getSystemWindowInsetBottom();
                invalidate(); return i;
            });
            requestApplyInsets();
        }

        Bitmap loadHero() {
            try {
                InputStream in=getResources().openRawResource(R.raw.house46_hero_base64);
                ByteArrayOutputStream o=new ByteArrayOutputStream();
                byte[] b=new byte[4096]; int n;
                while((n=in.read(b))!=-1)o.write(b,0,n);
                in.close();
                byte[] d=Base64.decode(o.toByteArray(),Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(d,0,d.length);
            } catch(Exception e){ return null; }
        }

        @Override protected void onDraw(Canvas c) {
            sc=getWidth()/W;
            vh=Math.max(1,getHeight()-topInset-bottomInset)/sc;
            c.save(); c.translate(0,topInset); c.scale(sc,sc);
            p.setShader(new LinearGradient(0,0,0,vh,BG,BG2,Shader.TileMode.CLAMP));
            c.drawRect(0,0,W,vh,p); p.setShader(null);
            header(c); hero(c); swipe(c); status(c); events(c); nav(c);
            c.restore();
        }

        void header(Canvas c){
            txt(c,"Мой дом",34,78,45,TXT,true);
            chevronDown(c,294,60,MUT);
            txt(c,"Болградская, 46",34,119,23,MUT,false);
            card(c,649,31,716,101,20,SUR,BORDER);
            bell(c,683,62); circle(c,704,42,7,BLUE);
        }

        void hero(Canvas c){
            float l=31,t=148,r=719,b=544,rad=25;
            Path path=new Path(); path.addRoundRect(new RectF(l,t,r,b),rad,rad,Path.Direction.CW);
            c.save(); c.clipPath(path);
            if(hero!=null)c.drawBitmap(hero,new Rect(0,0,hero.getWidth(),hero.getHeight()),new RectF(l,t,r,b),p);
            else {p.setColor(Color.rgb(11,37,64)); c.drawRect(l,t,r,b,p);}
            c.restore(); rstroke(c,l,t,r,b,rad,BORDER,1.5f);
        }

        void swipe(Canvas c){
            card(c,31,564,719,758,28,SUR,Color.rgb(25,84,142));
            rstroke(c,53,584,697,738,76,Color.rgb(25,94,165),1.6f);
            float x=125+drag,y=661;
            circle(c,x,y,73,Color.rgb(12,49,91));
            rstroke(c,x-73,y-73,x+73,y+73,73,BLUEL,2.5f);
            door(c,x,y,Color.WHITE);
            if(!opened){
                arrow(c,246,y,Color.rgb(26,84,147)); arrow(c,272,y,Color.rgb(29,104,184)); arrow(c,298,y,BLUE);
                txt(c,"Свайп, чтобы открыть дверь",347,650,22,TXT,true);
                txt(c,"Потяните вправо",347,691,18,MUT,false);
            } else {
                txt(c,"Дверь открыта",347,650,23,GREEN,true);
                txt(c,"Команда выполнена",347,691,18,MUT,false);
            }
        }

        void status(Canvas c){
            float t=778;
            card(c,31,t,719,t+122,24,SUR,BORDER);
            shield(c,81,t+61);
            txt(c,"Система работает",135,t+51,23,TXT,true);
            txt(c,"Все сервисы активны",135,t+82,17,MUT,false);
            line(c,433,t+25,433,t+97,Color.rgb(27,58,86),1);
            line(c,575,t+25,575,t+97,Color.rgb(27,58,86),1);
            building(c,504,t+49,MUT); center(c,"9 этажей",504,t+91,16,MUT,false);
            smallDoor(c,645,t+49,MUT); center(c,"3 подъезда",645,t+91,16,MUT,false);
        }

        void events(Canvas c){
            txt(c,"Последние события",31,946,26,TXT,true);
            txt(c,"Все события",600,946,17,BLUE,false); right(c,707,939,BLUE);
            float t=971; card(c,31,t,719,1296,24,SUR,BORDER);
            row(c,t,0,"Дверь открыта","Сегодня, 08:37",BLUE);
            row(c,t,1,"Вход по коду","Сегодня, 08:31",GREEN);
            row(c,t,2,"Плановое обслуживание","Инженерная служба • Вчера, 16:42",BLUE);
        }

        void row(Canvas c,float t,int i,String a,String b,int col){
            float y=t+i*106;
            if(i>0)line(c,109,y,695,y,Color.rgb(23,53,79),1);
            rfill(c,48,y+21,93,y+66,12,Color.argb(48,Color.red(col),Color.green(col),Color.blue(col)));
            rstroke(c,48,y+21,93,y+66,12,Color.argb(100,Color.red(col),Color.green(col),Color.blue(col)),1);
            if(i==0)smallDoor(c,70,y+44,col); else if(i==1)person(c,70,y+45,col); else gear(c,70,y+45,col);
            txt(c,a,112,y+50,20,TXT,true); txt(c,b,112,y+78,i==2?14:16,MUT,false); right(c,690,y+48,MUT);
        }

        void nav(Canvas c){
            float t=Math.max(1320,vh-108);
            card(c,16,t,734,t+98,22,Color.rgb(5,24,43),Color.rgb(20,51,78));
            float[] x={95,280,468,655}; String[] q={"Главная","События","Заявки","Профиль"};
            home(c,x[0],t+36,BLUE); clock(c,x[1],t+36,MUT); clipboard(c,x[2],t+36,MUT); person(c,x[3],t+36,MUT);
            for(int i=0;i<4;i++)center(c,q[i],x[i],t+76,15,i==0?BLUE:MUT,i==0);
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX()/sc, y=(e.getY()-topInset)/sc;
            float dx=x-(125+drag),dy=y-661;
            if(e.getAction()==MotionEvent.ACTION_DOWN && dx*dx+dy*dy<92*92){dragging=true;return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE&&dragging){drag=Math.max(0,Math.min(500,x-125));invalidate();return true;}
            if((e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)&&dragging){
                dragging=false; animate(drag>=330?500:0,drag>=330); return true;
            }
            return true;
        }

        void animate(float to,boolean ok){
            if(anim!=null)anim.cancel();
            anim=ValueAnimator.ofFloat(drag,to); anim.setDuration(220);
            anim.addUpdateListener(a->{drag=(float)a.getAnimatedValue();invalidate();}); anim.start();
            if(ok){opened=true;Toast.makeText(getContext(),"Демо: команда открытия двери",Toast.LENGTH_SHORT).show();
                h.postDelayed(()->{opened=false;animate(0,false);},1300);}
        }

        void txt(Canvas c,String z,float x,float y,float sz,int col,boolean bold){
            p.setShader(null);p.setColor(col);p.setTextSize(sz);
            p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(z,x,y,p);
        }
        void center(Canvas c,String z,float x,float y,float sz,int col,boolean bold){
            p.setTextSize(sz);p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));
            txt(c,z,x-p.measureText(z)/2,y,sz,col,bold);
        }
        void card(Canvas c,float l,float t,float r,float b,float rad,int fill,int border){rfill(c,l,t,r,b,rad,fill);rstroke(c,l,t,r,b,rad,border,1.2f);}
        void rfill(Canvas c,float l,float t,float r,float b,float rad,int col){p.setShader(null);p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(l,t,r,b),rad,rad,p);}
        void rstroke(Canvas c,float l,float t,float r,float b,float rad,int col,float w){s.setColor(col);s.setStrokeWidth(w);c.drawRoundRect(new RectF(l,t,r,b),rad,rad,s);}
        void circle(Canvas c,float x,float y,float r,int col){p.setShader(null);p.setColor(col);c.drawCircle(x,y,r,p);}
        void line(Canvas c,float x1,float y1,float x2,float y2,int col,float w){s.setColor(col);s.setStrokeWidth(w);c.drawLine(x1,y1,x2,y2,s);}
        void chevronDown(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(3);c.drawLine(x-8,y-3,x,y+5,s);c.drawLine(x,y+5,x+8,y-3,s);}
        void right(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(3);c.drawLine(x-4,y-8,x+4,y,s);c.drawLine(x+4,y,x-4,y+8,s);}
        void arrow(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(4);c.drawLine(x-9,y-13,x+4,y,s);c.drawLine(x+4,y,x-9,y+13,s);}
        void bell(Canvas c,float x,float y){s.setColor(Color.WHITE);s.setStrokeWidth(2.5f);c.drawArc(new RectF(x-11,y-15,x+11,y+10),190,160,false,s);line(c,x-11,y+4,x-11,y+11,Color.WHITE,2.5f);line(c,x+11,y+4,x+11,y+11,Color.WHITE,2.5f);line(c,x-11,y+11,x+11,y+11,Color.WHITE,2.5f);circle(c,x,y+16,2.5f,Color.WHITE);}
        void door(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(3);c.drawRect(x-21,y-34,x+20,y+34,s);circle(c,x+9,y,3,col);line(c,x-30,y+35,x+29,y+35,col,3);}
        void smallDoor(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(2.5f);c.drawRect(x-10,y-18,x+10,y+16,s);circle(c,x+5,y,1.8f,col);}
        void shield(Canvas c,float x,float y){Path q=new Path();q.moveTo(x,y-35);q.lineTo(x+27,y-22);q.lineTo(x+22,y+22);q.lineTo(x,y+37);q.lineTo(x-22,y+22);q.lineTo(x-27,y-22);q.close();p.setColor(Color.rgb(9,55,41));c.drawPath(q,p);s.setColor(GREEN);s.setStrokeWidth(2);c.drawPath(q,s);line(c,x-10,y,x-2,y+8,GREEN,3);line(c,x-2,y+8,x+13,y-10,GREEN,3);}
        void building(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(2);c.drawRect(x-12,y-22,x+12,y+18,s);for(int a=0;a<2;a++)for(int b=0;b<2;b++)c.drawRect(x-7+b*10,y-15+a*12,x-2+b*10,y-10+a*12,s);c.drawRect(x-3,y+8,x+3,y+18,s);}
        void home(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(3);Path q=new Path();q.moveTo(x-18,y);q.lineTo(x,y-17);q.lineTo(x+18,y);q.lineTo(x+18,y+19);q.lineTo(x-18,y+19);q.close();c.drawPath(q,s);c.drawRect(x-4,y+7,x+4,y+19,s);}
        void clock(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(2.5f);c.drawCircle(x,y,18,s);line(c,x,y,x,y-10,col,2.5f);line(c,x,y,x+8,y+5,col,2.5f);}
        void clipboard(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(2.5f);c.drawRoundRect(new RectF(x-15,y-19,x+15,y+20),3,3,s);c.drawRect(x-7,y-24,x+7,y-17,s);line(c,x-8,y-6,x+8,y-6,col,2.5f);line(c,x-8,y+3,x+8,y+3,col,2.5f);}
        void person(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(2.5f);c.drawCircle(x,y-9,8,s);c.drawArc(new RectF(x-14,y+2,x+14,y+24),180,180,false,s);}
        void gear(Canvas c,float x,float y,int col){s.setColor(col);s.setStrokeWidth(2.3f);c.drawCircle(x,y,12,s);c.drawCircle(x,y,4,s);for(int i=0;i<8;i++){double a=Math.PI*2*i/8;line(c,x+(float)Math.cos(a)*15,y+(float)Math.sin(a)*15,x+(float)Math.cos(a)*20,y+(float)Math.sin(a)*20,col,2.3f);}}
    }
}
