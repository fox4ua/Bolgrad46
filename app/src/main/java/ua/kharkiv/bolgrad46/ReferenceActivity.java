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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;

public class ReferenceActivity extends Activity {
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

        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                int appearance = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(appearance, appearance);
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        ReferenceView view = new ReferenceView(this);
        root.addView(view, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        ));

        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(0, bars.top, 0, bars.bottom);
                return insets;
            });
        } else {
            root.setFitsSystemWindows(true);
        }

        setContentView(root);
    }

    private static final class ReferenceView extends View {
        private static final float DESIGN_W = 941f;
        private static final float DESIGN_H = 1574f;

        private static final int BLUE = Color.rgb(24, 119, 247);
        private static final int BLUE_DARK = Color.rgb(0, 77, 214);
        private static final int NAVY = Color.rgb(7, 31, 52);
        private static final int NAVY_2 = Color.rgb(15, 55, 78);
        private static final int TEXT = Color.rgb(35, 54, 91);
        private static final int MUTED = Color.rgb(99, 126, 181);
        private static final int GREEN = Color.rgb(17, 198, 113);
        private static final int WHITE = Color.rgb(250, 252, 255);

        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF rect = new RectF();
        private final RectF doorHit = new RectF();
        private final RectF navHit = new RectF();

        private final Bitmap hero;
        private int selectedTab;
        private boolean holding;
        private boolean opened;
        private float holdProgress;
        private ValueAnimator holdAnimator;

        private float sx;
        private float sy;

        ReferenceView(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setClickable(true);
            setFocusable(true);
            hero = BitmapFactory.decodeResource(context.getResources(), R.drawable.house46_target);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            sx = getWidth() / DESIGN_W;
            float heightScale = getHeight() / DESIGN_H;
            sy = Math.min(heightScale, sx * 1.22f);

            drawBackground(canvas);

            if (selectedTab == 0) {
                drawHeader(canvas);
                drawHouseCard(canvas);
                drawDoorCard(canvas);
            } else {
                drawSecondary(canvas);
            }

            drawNavigation(canvas);
        }

        private float X(float v) { return v * sx; }
        private float Y(float v) { return v * sy; }
        private float H(float v) { return v * sy; }

        private void drawBackground(Canvas canvas) {
            fill.setShader(new LinearGradient(
                0f, 0f, getWidth(), getHeight(),
                new int[]{Color.rgb(250, 251, 255), Color.rgb(241, 245, 252), Color.rgb(248, 250, 255)},
                new float[]{0f, 0.56f, 1f},
                Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, 0, getWidth(), getHeight(), fill);
            fill.setShader(null);
        }

        private void drawHeader(Canvas canvas) {
            float x = X(27);
            float y = Y(20);
            float w = X(887);
            float h = H(238);
            float radius = X(38);

            blueGradient(x, y, x + w, y + h);
            shadow(fill, X(11), Color.argb(75, 62, 113, 215));
            rect.set(x, y, x + w, y + h);
            canvas.drawRoundRect(rect, radius, radius, fill);
            clearShadow(fill);
            fill.setShader(null);

            drawWave(canvas, x, y, w, h);

            float buildingCx = x + X(111);
            drawBuilding(canvas, buildingCx, y + h / 2f, X(148), Color.WHITE);

            float bellW = X(136);
            float bellH = H(154);
            float bellX = x + w - bellW - X(28);
            float bellY = y + (h - bellH) / 2f;
            drawBellCard(canvas, bellX, bellY, bellW, bellH);

            float left = x + X(238);
            float right = bellX - X(18);
            drawFittedText(canvas, "Болградская, 46", left, right, y + h / 2f + H(20), X(54), X(34), Color.WHITE, true, Paint.Align.LEFT);
        }

        private void drawHouseCard(Canvas canvas) {
            float x = X(27);
            float y = Y(306);
            float w = X(887);
            float h = H(576);
            float radius = X(34);
            float statusH = H(153);
            float imageBottom = y + h - statusH;

            rect.set(x, y, x + w, y + h);
            fill.setColor(NAVY);
            shadow(fill, X(9), Color.argb(55, 45, 80, 130));
            canvas.drawRoundRect(rect, radius, radius, fill);
            clearShadow(fill);

            canvas.save();
            path.reset();
            path.addRoundRect(rect, radius, radius, Path.Direction.CW);
            canvas.clipPath(path);

            if (hero != null) {
                drawBitmapCenterCrop(canvas, hero, new RectF(x, y, x + w, imageBottom));
            } else {
                fill.setColor(Color.rgb(8, 30, 48));
                canvas.drawRect(x, y, x + w, imageBottom, fill);
            }

            drawOnline(canvas, x + X(21), y + H(18));

            float tempY = imageBottom - H(174);
            drawTemperature(canvas, x + X(20), tempY, X(276), H(139), "В помещении", "22°C", false);
            drawTemperature(canvas, x + w - X(296), tempY, X(276), H(139), "На улице", "18°C", true);

            fill.setShader(new LinearGradient(
                x, imageBottom, x, y + h,
                new int[]{Color.rgb(23, 64, 88), Color.rgb(7, 28, 48)},
                null,
                Shader.TileMode.CLAMP
            ));
            canvas.drawRect(x, imageBottom, x + w, y + h, fill);
            fill.setShader(null);

            drawStatusCells(canvas, x, imageBottom, w, statusH);
            canvas.restore();
        }

        private void drawBitmapCenterCrop(Canvas canvas, Bitmap bitmap, RectF dst) {
            float srcRatio = bitmap.getWidth() / (float) bitmap.getHeight();
            float dstRatio = dst.width() / dst.height();
            Rect src;

            if (srcRatio > dstRatio) {
                int neededWidth = Math.round(bitmap.getHeight() * dstRatio);
                int left = (bitmap.getWidth() - neededWidth) / 2;
                src = new Rect(left, 0, left + neededWidth, bitmap.getHeight());
            } else {
                int neededHeight = Math.round(bitmap.getWidth() / dstRatio);
                int top = Math.max(0, (bitmap.getHeight() - neededHeight) / 2);
                src = new Rect(0, top, bitmap.getWidth(), Math.min(bitmap.getHeight(), top + neededHeight));
            }

            canvas.drawBitmap(bitmap, src, dst, fill);
        }

        private void drawOnline(Canvas canvas, float x, float y) {
            float w = X(170);
            float h = H(58);
            fill.setColor(Color.argb(230, 6, 32, 58));
            rect.set(x, y, x + w, y + h);
            canvas.drawRoundRect(rect, X(29), X(29), fill);
            fill.setColor(Color.rgb(18, 213, 134));
            canvas.drawCircle(x + X(28), y + h / 2f, X(10), fill);
            drawText(canvas, "Онлайн", x + X(54), y + h / 2f + H(9), X(25), WHITE, false, Paint.Align.LEFT);
        }

        private void drawTemperature(Canvas canvas, float x, float y, float w, float h, String label, String value, boolean outside) {
            fill.setColor(Color.argb(225, 25, 48, 66));
            shadow(fill, X(5), Color.argb(75, 0, 12, 25));
            rect.set(x, y, x + w, y + h);
            canvas.drawRoundRect(rect, X(28), X(28), fill);
            clearShadow(fill);

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(X(1.8f));
            stroke.setColor(Color.argb(230, 227, 237, 250));
            canvas.drawRoundRect(rect, X(28), X(28), stroke);

            float iconX = x + X(56);
            float centerY = y + h / 2f + H(6);
            drawThermometer(canvas, iconX, centerY, X(47), Color.WHITE, outside);

            float tx = x + X(96);
            drawFittedText(canvas, label, tx, x + w - X(16), y + H(53), X(24), X(20), WHITE, false, Paint.Align.LEFT);
            drawFittedText(canvas, value, tx, x + w - X(16), y + H(104), X(45), X(36), Color.WHITE, true, Paint.Align.LEFT);
        }

        private void drawStatusCells(Canvas canvas, float x, float y, float w, float h) {
            float cellW = w / 3f;
            fill.setColor(Color.argb(100, 218, 236, 255));
            canvas.drawRect(x + cellW - X(1), y + H(28), x + cellW + X(1), y + h - H(28), fill);
            canvas.drawRect(x + cellW * 2f - X(1), y + H(28), x + cellW * 2f + X(1), y + h - H(28), fill);

            drawStatusCell(canvas, x, y, cellW, h, 0, "Замок", "Онлайн");
            drawStatusCell(canvas, x + cellW, y, cellW, h, 1, "Интернет", "Подключено");
            drawStatusCell(canvas, x + cellW * 2f, y, cellW, h, 2, "Bluetooth", "Подключено");
        }

        private void drawStatusCell(Canvas canvas, float x, float y, float w, float h, int type, String title, String sub) {
            float iconX = x + X(68);
            float cy = y + h / 2f;

            if (type == 0) drawLock(canvas, iconX, cy, X(60), Color.WHITE);
            if (type == 1) drawGlobe(canvas, iconX, cy, X(60), Color.WHITE);
            if (type == 2) drawBluetooth(canvas, iconX, cy, X(62), Color.WHITE);

            float tx = x + X(134);
            float right = x + w - X(12);
            drawFittedText(canvas, title, tx, right, y + H(66), X(23), X(18), WHITE, false, Paint.Align.LEFT);
            drawFittedText(canvas, sub, tx, right, y + H(111), X(20), X(15), GREEN, false, Paint.Align.LEFT);
        }

        private void drawDoorCard(Canvas canvas) {
            float x = X(27);
            float y = Y(934);
            float w = X(887);
            float h = H(353);
            float radius = X(38);

            blueGradient(x, y, x + w, y + h);
            shadow(fill, X(10), Color.argb(70, 47, 101, 213));
            rect.set(x, y, x + w, y + h);
            canvas.drawRoundRect(rect, radius, radius, fill);
            clearShadow(fill);
            fill.setShader(null);
            drawWave(canvas, x, y, w, h);

            float circleCx = x + X(168);
            float circleCy = y + h / 2f;
            float circleR = X(108);

            fill.setColor(Color.argb(26, 255, 255, 255));
            shadow(fill, X(8), Color.argb(95, 255, 255, 255));
            canvas.drawCircle(circleCx, circleCy, circleR, fill);
            clearShadow(fill);

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(X(1.8f));
            stroke.setColor(Color.WHITE);
            canvas.drawCircle(circleCx, circleCy, circleR, stroke);

            if (holding && !opened) {
                stroke.setStrokeWidth(X(7));
                stroke.setStrokeCap(Paint.Cap.ROUND);
                stroke.setColor(Color.rgb(123, 221, 255));
                rect.set(circleCx - circleR - X(5), circleCy - circleR - X(5), circleCx + circleR + X(5), circleCy + circleR + X(5));
                canvas.drawArc(rect, -90, 360f * holdProgress, false, stroke);
            }

            float tx = x + X(338);
            float right = x + w - X(42);

            if (!opened) {
                drawLock(canvas, circleCx, circleCy, X(96), Color.WHITE);
                drawFittedText(canvas, "Открыть дверь", tx, right, y + H(185), X(58), X(39), Color.WHITE, true, Paint.Align.LEFT);
                String hint = holding ? "Продолжайте удерживать" : "Удерживайте для открытия";
                drawFittedText(canvas, hint, tx, right, y + H(249), X(29), X(21), Color.rgb(150, 190, 255), false, Paint.Align.LEFT);
            } else {
                drawCheck(canvas, circleCx, circleCy, X(79), Color.WHITE);
                drawFittedText(canvas, "Дверь открыта", tx, right, y + H(184), X(54), X(38), Color.WHITE, true, Paint.Align.LEFT);
                drawFittedText(canvas, "Команда выполнена", tx, right, y + H(248), X(28), X(21), Color.rgb(180, 214, 255), false, Paint.Align.LEFT);
            }

            doorHit.set(x, y, x + w, y + h);
        }

        private void drawNavigation(Canvas canvas) {
            float x = X(27);
            float w = X(887);
            float h = H(113);
            float bottomMargin = H(18);
            float y = getHeight() - bottomMargin - h;

            fill.setShader(new LinearGradient(
                x, y, x, y + h,
                new int[]{Color.rgb(254, 254, 255), Color.rgb(238, 242, 251)},
                null,
                Shader.TileMode.CLAMP
            ));
            shadow(fill, X(8), Color.argb(55, 75, 106, 170));
            rect.set(x, y, x + w, y + h);
            canvas.drawRoundRect(rect, X(34), X(34), fill);
            clearShadow(fill);
            fill.setShader(null);

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(X(1));
            stroke.setColor(Color.rgb(218, 226, 243));
            canvas.drawRoundRect(rect, X(34), X(34), stroke);

            String[] labels = {"Главная", "События", "Заявки", "Профиль"};
            float cellW = w / 4f;
            for (int i = 0; i < 4; i++) {
                float cx = x + cellW * (i + 0.5f);
                int color = selectedTab == i ? BLUE : MUTED;
                drawNavIcon(canvas, i, cx, y + H(43), X(42), color);
                drawFittedText(canvas, labels[i], x + cellW * i + X(8), x + cellW * (i + 1) - X(8), y + H(91), X(21), X(17), color, selectedTab == i, Paint.Align.CENTER);
            }

            navHit.set(x, y, x + w, y + h);
        }

        private void drawSecondary(Canvas canvas) {
            String[] titles = {"", "События", "Заявки", "Профиль"};
            String[] subtitles = {"", "История доступа и уведомлений", "Ваши обращения и их статусы", "Настройки пользователя"};

            float x = X(27);
            float y = Y(28);
            float w = X(887);
            float h = H(220);
            blueGradient(x, y, x + w, y + h);
            rect.set(x, y, x + w, y + h);
            canvas.drawRoundRect(rect, X(38), X(38), fill);
            fill.setShader(null);

            drawText(canvas, titles[selectedTab], x + X(45), y + H(102), X(50), Color.WHITE, true, Paint.Align.LEFT);
            drawFittedText(canvas, subtitles[selectedTab], x + X(45), x + w - X(45), y + H(157), X(24), X(18), Color.rgb(195, 219, 255), false, Paint.Align.LEFT);

            float cardY = Y(310);
            fill.setColor(Color.WHITE);
            shadow(fill, X(6), Color.argb(45, 60, 94, 150));
            rect.set(x, cardY, x + w, cardY + H(330));
            canvas.drawRoundRect(rect, X(32), X(32), fill);
            clearShadow(fill);
            drawText(canvas, "Раздел готов к подключению данных", x + X(45), cardY + H(95), X(29), TEXT, true, Paint.Align.LEFT);
            drawFittedText(canvas, "Навигация работает. Наполнение подключим следующим этапом.", x + X(45), x + w - X(45), cardY + H(150), X(22), X(17), MUTED, false, Paint.Align.LEFT);
        }

        private void blueGradient(float x1, float y1, float x2, float y2) {
            fill.setShader(new LinearGradient(
                x1, y1, x2, y2,
                new int[]{Color.rgb(82, 171, 255), BLUE, BLUE_DARK},
                new float[]{0f, 0.52f, 1f},
                Shader.TileMode.CLAMP
            ));
        }

        private void drawWave(Canvas canvas, float x, float y, float w, float h) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeWidth(Math.max(1f, X(1.7f)));
            stroke.setShader(new LinearGradient(
                x + w * 0.35f, y, x + w, y + h,
                new int[]{Color.argb(0, 255, 255, 255), Color.argb(170, 255, 255, 255), Color.argb(0, 255, 255, 255)},
                null,
                Shader.TileMode.CLAMP
            ));
            path.reset();
            path.moveTo(x + w * 0.34f, y + h * 0.72f);
            path.cubicTo(x + w * 0.57f, y + h * 0.70f, x + w * 0.68f, y + h * 0.23f, x + w * 0.98f, y + h * 0.15f);
            canvas.drawPath(path, stroke);
            stroke.setShader(null);
        }

        private void drawBellCard(Canvas canvas, float x, float y, float w, float h) {
            fill.setShader(new LinearGradient(x, y, x + w, y + h, new int[]{Color.rgb(92, 174, 255), Color.rgb(4, 83, 207)}, null, Shader.TileMode.CLAMP));
            shadow(fill, X(9), Color.argb(110, 5, 65, 172));
            rect.set(x, y, x + w, y + h);
            canvas.drawRoundRect(rect, X(34), X(34), fill);
            clearShadow(fill);
            fill.setShader(null);

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(X(5));
            stroke.setColor(Color.WHITE);
            float cx = x + w / 2f;
            float cy = y + h / 2f;
            float bw = X(50);
            float bh = H(61);
            rect.set(cx - bw / 2f, cy - bh / 2f, cx + bw / 2f, cy + bh / 2f);
            canvas.drawArc(rect, 195, 150, false, stroke);
            canvas.drawLine(cx - bw / 2f, cy + H(6), cx - bw / 2f, cy + H(25), stroke);
            canvas.drawLine(cx + bw / 2f, cy + H(6), cx + bw / 2f, cy + H(25), stroke);
            canvas.drawLine(cx - bw / 2f, cy + H(25), cx + bw / 2f, cy + H(25), stroke);
            canvas.drawCircle(cx, cy + H(38), X(5), stroke);
            fill.setColor(Color.rgb(75, 202, 255));
            canvas.drawCircle(x + w - X(28), y + H(27), X(12), fill);
        }

        private void drawBuilding(Canvas canvas, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(Math.max(X(2.2f), size * 0.018f));
            stroke.setColor(color);
            float left = cx - size * 0.25f;
            float right = cx + size * 0.25f;
            float top = cy - size * 0.38f;
            float bottom = cy + size * 0.38f;
            path.reset();
            path.moveTo(left - size * 0.06f, top + size * 0.10f);
            path.lineTo(cx, top - size * 0.08f);
            path.lineTo(right + size * 0.06f, top + size * 0.10f);
            canvas.drawPath(path, stroke);
            canvas.drawRect(left, top + size * 0.10f, right, bottom, stroke);
            for (int rr = 0; rr < 4; rr++) {
                for (int cc = 0; cc < 2; cc++) {
                    float wx = cx + (cc == 0 ? -size * 0.125f : size * 0.125f);
                    float wy = top + size * (0.24f + rr * 0.14f);
                    canvas.drawRect(wx - size * 0.042f, wy - size * 0.042f, wx + size * 0.042f, wy + size * 0.042f, stroke);
                }
            }
            rect.set(cx - size * 0.07f, bottom - size * 0.21f, cx + size * 0.07f, bottom);
            canvas.drawRoundRect(rect, size * 0.04f, size * 0.04f, stroke);
            canvas.drawLine(cx - size * 0.48f, bottom, cx + size * 0.48f, bottom, stroke);
            canvas.drawLine(cx - size * 0.42f, bottom, cx - size * 0.42f, bottom - size * 0.25f, stroke);
            canvas.drawCircle(cx - size * 0.42f, bottom - size * 0.29f, size * 0.045f, stroke);
            canvas.drawLine(cx + size * 0.42f, bottom, cx + size * 0.42f, bottom - size * 0.25f, stroke);
            canvas.drawCircle(cx + size * 0.42f, bottom - size * 0.29f, size * 0.045f, stroke);
        }

        private void drawThermometer(Canvas canvas, float cx, float cy, float size, int color, boolean outside) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(size * 0.08f);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setColor(color);
            canvas.drawLine(cx, cy - size * 0.34f, cx, cy + size * 0.15f, stroke);
            canvas.drawCircle(cx, cy + size * 0.27f, size * 0.17f, stroke);
            canvas.drawLine(cx, cy - size * 0.20f, cx, cy + size * 0.23f, stroke);
            if (outside) {
                canvas.drawCircle(cx + size * 0.43f, cy - size * 0.07f, size * 0.12f, stroke);
            } else {
                path.reset();
                path.moveTo(cx + size * 0.32f, cy - size * 0.30f);
                path.lineTo(cx + size * 0.52f, cy - size * 0.48f);
                path.lineTo(cx + size * 0.72f, cy - size * 0.30f);
                canvas.drawPath(path, stroke);
            }
        }

        private void drawLock(Canvas canvas, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(size * 0.055f);
            stroke.setColor(color);
            float bodyW = size * 0.62f;
            float bodyH = size * 0.48f;
            float top = cy - size * 0.03f;
            rect.set(cx - bodyW / 2f, top, cx + bodyW / 2f, top + bodyH);
            canvas.drawRoundRect(rect, size * 0.055f, size * 0.055f, stroke);
            rect.set(cx - size * 0.22f, cy - size * 0.40f, cx + size * 0.22f, cy + size * 0.05f);
            canvas.drawArc(rect, 180, 180, false, stroke);
            canvas.drawCircle(cx, cy + size * 0.18f, size * 0.055f, stroke);
            canvas.drawLine(cx, cy + size * 0.24f, cx, cy + size * 0.33f, stroke);
        }

        private void drawGlobe(Canvas canvas, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(size * 0.045f);
            stroke.setColor(color);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            float radius = size * 0.43f;
            canvas.drawCircle(cx, cy, radius, stroke);
            rect.set(cx - radius * 0.55f, cy - radius, cx + radius * 0.55f, cy + radius);
            canvas.drawOval(rect, stroke);
            canvas.drawLine(cx - radius, cy, cx + radius, cy, stroke);
            canvas.drawArc(new RectF(cx - radius, cy - radius * 0.50f, cx + radius, cy + radius * 0.50f), 0, 180, false, stroke);
            canvas.drawArc(new RectF(cx - radius, cy - radius * 0.50f, cx + radius, cy + radius * 0.50f), 180, 180, false, stroke);
        }

        private void drawBluetooth(Canvas canvas, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(size * 0.05f);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setColor(color);
            float top = cy - size * 0.42f;
            float bottom = cy + size * 0.42f;
            path.reset();
            path.moveTo(cx, top);
            path.lineTo(cx + size * 0.26f, cy - size * 0.18f);
            path.lineTo(cx - size * 0.20f, cy + size * 0.18f);
            path.lineTo(cx + size * 0.26f, bottom);
            path.lineTo(cx, cy + size * 0.03f);
            path.lineTo(cx, top);
            canvas.drawPath(path, stroke);
            canvas.drawLine(cx - size * 0.32f, cy - size * 0.31f, cx + size * 0.26f, cy + size * 0.22f, stroke);
            canvas.drawLine(cx - size * 0.32f, cy + size * 0.31f, cx + size * 0.26f, cy - size * 0.22f, stroke);
        }

        private void drawCheck(Canvas canvas, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(size * 0.09f);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setColor(color);
            canvas.drawLine(cx - size * 0.33f, cy, cx - size * 0.08f, cy + size * 0.25f, stroke);
            canvas.drawLine(cx - size * 0.08f, cy + size * 0.25f, cx + size * 0.37f, cy - size * 0.30f, stroke);
        }

        private void drawNavIcon(Canvas canvas, int type, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(size * 0.06f);
            stroke.setColor(color);

            if (type == 0) {
                path.reset();
                path.moveTo(cx - size * 0.35f, cy - size * 0.04f);
                path.lineTo(cx, cy - size * 0.34f);
                path.lineTo(cx + size * 0.35f, cy - size * 0.04f);
                canvas.drawPath(path, stroke);
                canvas.drawRect(cx - size * 0.27f, cy - size * 0.04f, cx + size * 0.27f, cy + size * 0.32f, stroke);
                canvas.drawRect(cx - size * 0.07f, cy + size * 0.10f, cx + size * 0.07f, cy + size * 0.32f, stroke);
            } else if (type == 1) {
                canvas.drawCircle(cx, cy, size * 0.34f, stroke);
                canvas.drawLine(cx, cy, cx, cy - size * 0.20f, stroke);
                canvas.drawLine(cx, cy, cx + size * 0.17f, cy + size * 0.10f, stroke);
            } else if (type == 2) {
                rect.set(cx - size * 0.24f, cy - size * 0.34f, cx + size * 0.24f, cy + size * 0.34f);
                canvas.drawRoundRect(rect, size * 0.04f, size * 0.04f, stroke);
                canvas.drawLine(cx - size * 0.10f, cy - size * 0.34f, cx - size * 0.10f, cy - size * 0.43f, stroke);
                canvas.drawLine(cx + size * 0.10f, cy - size * 0.34f, cx + size * 0.10f, cy - size * 0.43f, stroke);
                canvas.drawLine(cx - size * 0.12f, cy - size * 0.14f, cx + size * 0.12f, cy - size * 0.14f, stroke);
                canvas.drawLine(cx - size * 0.12f, cy, cx + size * 0.12f, cy, stroke);
                canvas.drawLine(cx - size * 0.12f, cy + size * 0.14f, cx + size * 0.12f, cy + size * 0.14f, stroke);
            } else {
                canvas.drawCircle(cx, cy - size * 0.17f, size * 0.16f, stroke);
                rect.set(cx - size * 0.27f, cy + size * 0.02f, cx + size * 0.27f, cy + size * 0.40f);
                canvas.drawArc(rect, 190, 160, false, stroke);
            }
        }

        private void drawText(Canvas canvas, String value, float x, float baseline, float size, int color, boolean bold, Paint.Align align) {
            text.setShader(null);
            text.setColor(color);
            text.setTextSize(size);
            text.setTextAlign(align);
            text.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
            canvas.drawText(value, x, baseline, text);
        }

        private void drawFittedText(Canvas canvas, String value, float left, float right, float baseline, float maxSize, float minSize, int color, boolean bold, Paint.Align align) {
            float width = Math.max(1f, right - left);
            float size = maxSize;
            text.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
            text.setTextAlign(align);
            text.setColor(color);
            text.setShader(null);
            text.setTextSize(size);

            while (text.measureText(value) > width && size > minSize) {
                size -= Math.max(0.5f, sx);
                text.setTextSize(size);
            }

            float x;
            if (align == Paint.Align.CENTER) {
                x = left + width / 2f;
            } else if (align == Paint.Align.RIGHT) {
                x = right;
            } else {
                x = left;
            }
            canvas.drawText(value, x, baseline, text);
        }

        private void shadow(Paint paint, float radius, int color) {
            paint.setShadowLayer(radius, 0f, radius * 0.45f, color);
        }

        private void clearShadow(Paint paint) {
            paint.clearShadowLayer();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (selectedTab == 0 && doorHit.contains(x, y)) {
                    startHold();
                    return true;
                }
                return true;
            }

            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                if (holding && !doorHit.contains(x, y)) {
                    cancelHold();
                }
                return true;
            }

            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                if (holding) {
                    cancelHold();
                    return true;
                }

                if (navHit.contains(x, y)) {
                    float cell = navHit.width() / 4f;
                    int index = Math.min(3, Math.max(0, (int) ((x - navHit.left) / cell)));
                    selectedTab = index;
                    invalidate();
                }
                performClick();
                return true;
            }

            if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                cancelHold();
                return true;
            }

            return true;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        private void startHold() {
            if (opened || holding) return;
            holding = true;
            holdProgress = 0f;
            if (holdAnimator != null) holdAnimator.cancel();
            holdAnimator = ValueAnimator.ofFloat(0f, 1f);
            holdAnimator.setDuration(850);
            holdAnimator.addUpdateListener(animation -> {
                holdProgress = (float) animation.getAnimatedValue();
                invalidate();
            });
            holdAnimator.addListener(new AnimatorListenerAdapter() {
                private boolean cancelled;

                @Override
                public void onAnimationCancel(Animator animation) {
                    cancelled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!cancelled && holding && holdProgress >= 0.99f) {
                        holding = false;
                        opened = true;
                        vibrate();
                        invalidate();
                        postDelayed(() -> {
                            opened = false;
                            holdProgress = 0f;
                            invalidate();
                        }, 1600);
                    }
                }
            });
            holdAnimator.start();
            invalidate();
        }

        private void cancelHold() {
            if (!holding) return;
            holding = false;
            if (holdAnimator != null) holdAnimator.cancel();
            holdProgress = 0f;
            invalidate();
        }

        private void vibrate() {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null) return;
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(55, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(55);
            }
        }
    }
}
