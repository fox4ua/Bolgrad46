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
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SafeMainActivity extends Activity {
    private static final int BG = Color.rgb(244, 247, 253);

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
                controller.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackground(new ColorDrawable(BG));

        ReferenceScreen screen = new ReferenceScreen(this);
        root.addView(screen, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        ));

        setContentView(root);
    }

    private static final class ReferenceScreen extends View {
        private static final float DESIGN_W = 924f;
        private static final float DESIGN_H = 1470f;

        private static final int BLUE = Color.rgb(22, 115, 247);
        private static final int BLUE_DARK = Color.rgb(0, 79, 218);
        private static final int NAVY = Color.rgb(8, 31, 53);
        private static final int TEXT_DARK = Color.rgb(32, 46, 75);
        private static final int MUTED = Color.rgb(100, 128, 184);
        private static final int GREEN = Color.rgb(17, 201, 116);
        private static final int WHITE = Color.rgb(247, 250, 255);

        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF r = new RectF();

        private Bitmap hero;
        private int selectedTab;
        private boolean opened;
        private float holdProgress;
        private boolean holding;
        private ValueAnimator holdAnimator;

        private float sx;
        private float sy;
        private float oy;

        private final RectF doorHit = new RectF();
        private final RectF navHit = new RectF();

        ReferenceScreen(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setClickable(true);
            setFocusable(true);
            hero = loadHero(context);
        }

        private Bitmap loadHero(Context context) {
            Bitmap bitmap = null;
            try {
                InputStream input = context.getResources().openRawResource(R.raw.house46_hero_base64);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }
                input.close();
                String encoded = new String(output.toByteArray(), StandardCharsets.US_ASCII).trim();
                byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (Exception ignored) {
                bitmap = null;
            }
            return bitmap;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            sx = getWidth() / DESIGN_W;
            float fillY = getHeight() / DESIGN_H;
            sy = Math.min(fillY, sx * 1.16f);
            float contentHeight = DESIGN_H * sy;
            oy = Math.max(0f, (getHeight() - contentHeight) * 0.38f);

            drawBackground(canvas);

            if (selectedTab == 0) {
                drawHome(canvas);
            } else {
                drawSecondary(canvas);
            }

            drawNavigation(canvas);
        }

        private void drawBackground(Canvas canvas) {
            p.setShader(new LinearGradient(
                0, 0, getWidth(), getHeight(),
                new int[]{
                    Color.rgb(249, 250, 255),
                    Color.rgb(238, 243, 252),
                    Color.rgb(249, 250, 255)
                },
                new float[]{0f, 0.58f, 1f},
                Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, 0, getWidth(), getHeight(), p);
            p.setShader(null);
        }

        private void drawHome(Canvas canvas) {
            drawHeader(canvas);
            drawHero(canvas);
            drawDoorCard(canvas);
        }

        private void drawHeader(Canvas canvas) {
            float x = X(26);
            float y = Y(24);
            float w = X(872);
            float h = H(236);
            float radius = X(40);

            setBlueGradient(x, y, x + w, y + h);
            shadow(0, X(13), Color.argb(70, 70, 127, 230));
            r.set(x, y, x + w, y + h);
            canvas.drawRoundRect(r, radius, radius, p);
            clearShadow();
            p.setShader(null);

            drawWave(canvas, x, y, w, h);
            drawBuildingIcon(canvas, x + X(110), y + h / 2f, X(150), Color.WHITE);

            float bellW = X(136);
            float bellH = H(154);
            float bellX = x + w - bellW - X(28);
            float bellY = y + (h - bellH) / 2f;
            drawBellCard(canvas, bellX, bellY, bellW, bellH);

            float textLeft = x + X(236);
            float textRight = bellX - X(22);
            drawFittedText(
                canvas,
                "Болградская, 46",
                textLeft,
                textRight,
                y + h / 2f + H(19),
                X(59),
                X(39),
                Color.WHITE,
                true,
                Paint.Align.LEFT
            );
        }

        private void drawWave(Canvas canvas, float x, float y, float w, float h) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.2f, X(2)));
            p.setShader(new LinearGradient(
                x + w * 0.35f, y + h * 0.25f,
                x + w * 0.9f, y + h * 0.8f,
                new int[]{Color.argb(0, 255, 255, 255), Color.argb(180, 255, 255, 255), Color.argb(0, 255, 255, 255)},
                null,
                Shader.TileMode.CLAMP
            ));
            path.reset();
            path.moveTo(x + w * 0.33f, y + h * 0.72f);
            path.cubicTo(
                x + w * 0.55f, y + h * 0.70f,
                x + w * 0.63f, y + h * 0.22f,
                x + w * 0.98f, y + h * 0.16f
            );
            canvas.drawPath(path, p);

            p.setStrokeWidth(Math.max(1f, X(1.2f)));
            p.setShader(new LinearGradient(
                x + w * 0.02f, y + h * 0.85f,
                x + w * 0.82f, y + h * 0.45f,
                new int[]{Color.argb(0, 255, 255, 255), Color.argb(100, 255, 255, 255), Color.argb(0, 255, 255, 255)},
                null,
                Shader.TileMode.CLAMP
            ));
            path.reset();
            path.moveTo(x + w * 0.03f, y + h * 0.87f);
            path.cubicTo(
                x + w * 0.31f, y + h * 1.02f,
                x + w * 0.61f, y + h * 0.48f,
                x + w * 0.98f, y + h * 0.40f
            );
            canvas.drawPath(path, p);

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
        }

        private void drawBellCard(Canvas canvas, float x, float y, float w, float h) {
            p.setShader(new LinearGradient(
                x, y, x + w, y + h,
                new int[]{Color.rgb(86, 166, 255), Color.rgb(3, 83, 209)},
                null,
                Shader.TileMode.CLAMP
            ));
            shadow(0, X(10), Color.argb(110, 5, 70, 180));
            r.set(x, y, x + w, y + h);
            canvas.drawRoundRect(r, X(34), X(34), p);
            clearShadow();
            p.setShader(null);

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(X(5));
            stroke.setColor(Color.WHITE);

            float cx = x + w / 2f;
            float cy = y + h / 2f + H(2);
            float bw = X(50);
            float bh = H(62);
            r.set(cx - bw / 2f, cy - bh / 2f, cx + bw / 2f, cy + bh / 2f);
            canvas.drawArc(r, 195, 150, false, stroke);
            canvas.drawLine(cx - bw / 2f, cy + H(6), cx - bw / 2f, cy + H(25), stroke);
            canvas.drawLine(cx + bw / 2f, cy + H(6), cx + bw / 2f, cy + H(25), stroke);
            canvas.drawLine(cx - bw / 2f, cy + H(25), cx + bw / 2f, cy + H(25), stroke);
            canvas.drawCircle(cx, cy + H(38), X(5), stroke);

            p.setColor(Color.rgb(77, 200, 255));
            canvas.drawCircle(x + w - X(28), y + H(27), X(12), p);
        }

        private void drawHero(Canvas canvas) {
            float x = X(26);
            float y = Y(309);
            float w = X(872);
            float h = H(576);
            float radius = X(34);

            r.set(x, y, x + w, y + h);
            shadow(0, X(9), Color.argb(60, 45, 85, 140));
            p.setColor(NAVY);
            canvas.drawRoundRect(r, radius, radius, p);
            clearShadow();

            canvas.save();
            path.reset();
            path.addRoundRect(r, radius, radius, Path.Direction.CW);
            canvas.clipPath(path);

            float statusH = H(153);
            float imageBottom = y + h - statusH;

            if (hero != null) {
                Rect src = new Rect(0, 0, hero.getWidth(), hero.getHeight());
                RectF dst = new RectF(x, y, x + w, imageBottom);
                p.setAlpha(255);
                canvas.drawBitmap(hero, src, dst, p);
            } else {
                p.setShader(new LinearGradient(
                    x, y, x + w, imageBottom,
                    Color.rgb(14, 48, 75),
                    Color.rgb(4, 20, 36),
                    Shader.TileMode.CLAMP
                ));
                canvas.drawRect(x, y, x + w, imageBottom, p);
                p.setShader(null);
            }

            drawOnlinePill(canvas, x + X(22), y + H(18));

            float tempY = y + h - statusH - H(174);
            drawTemperature(canvas, x + X(19), tempY, X(276), H(139), "В помещении", "22°C", false);
            drawTemperature(canvas, x + w - X(276) - X(19), tempY, X(276), H(139), "На улице", "18°C", true);

            p.setShader(new LinearGradient(
                x, imageBottom, x, y + h,
                new int[]{Color.rgb(22, 64, 89), Color.rgb(7, 29, 49)},
                null,
                Shader.TileMode.CLAMP
            ));
            canvas.drawRect(x, imageBottom, x + w, y + h, p);
            p.setShader(null);

            drawStatusCells(canvas, x, imageBottom, w, statusH);
            canvas.restore();
        }

        private void drawOnlinePill(Canvas canvas, float x, float y) {
            float w = X(170);
            float h = H(58);
            p.setColor(Color.argb(225, 5, 33, 59));
            r.set(x, y, x + w, y + h);
            canvas.drawRoundRect(r, X(30), X(30), p);

            p.setColor(Color.rgb(21, 215, 137));
            canvas.drawCircle(x + X(29), y + h / 2f, X(10), p);

            drawText(canvas, "Онлайн", x + X(54), y + h / 2f + H(9), X(26), WHITE, false, Paint.Align.LEFT);
        }

        private void drawTemperature(Canvas canvas, float x, float y, float w, float h, String label, String value, boolean outside) {
            p.setColor(Color.argb(222, 22, 48, 68));
            shadow(0, X(5), Color.argb(75, 2, 13, 29));
            r.set(x, y, x + w, y + h);
            canvas.drawRoundRect(r, X(28), X(28), p);
            clearShadow();

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(X(2));
            stroke.setColor(Color.argb(220, 226, 238, 251));
            canvas.drawRoundRect(r, X(28), X(28), stroke);

            float icx = x + X(55);
            float icy = y + h / 2f + H(6);
            drawThermometer(canvas, icx, icy, X(46), Color.WHITE, outside);

            drawText(canvas, label, x + X(96), y + H(53), X(24), WHITE, false, Paint.Align.LEFT);
            drawText(canvas, value, x + X(96), y + H(103), X(45), Color.WHITE, true, Paint.Align.LEFT);
        }

        private void drawStatusCells(Canvas canvas, float x, float y, float w, float h) {
            float cellW = w / 3f;

            for (int i = 1; i <= 2; i++) {
                p.setColor(Color.argb(100, 218, 236, 255));
                canvas.drawRect(x + cellW * i - X(1), y + H(28), x + cellW * i + X(1), y + h - H(28), p);
            }

            drawStatusCell(canvas, x, y, cellW, h, 0, "Замок", "Онлайн");
            drawStatusCell(canvas, x + cellW, y, cellW, h, 1, "Интернет", "Подключено");
            drawStatusCell(canvas, x + cellW * 2f, y, cellW, h, 2, "Bluetooth", "Подключено");
        }

        private void drawStatusCell(Canvas canvas, float x, float y, float w, float h, int type, String title, String sub) {
            float iconX = x + X(67);
            float cy = y + h / 2f;

            if (type == 0) {
                drawLock(canvas, iconX, cy, X(60), Color.WHITE);
            } else if (type == 1) {
                drawGlobe(canvas, iconX, cy, X(60), Color.WHITE);
            } else {
                drawBluetooth(canvas, iconX, cy, X(62), Color.WHITE);
            }

            float textX = x + X(135);
            drawText(canvas, title, textX, y + H(66), X(24), WHITE, false, Paint.Align.LEFT);
            drawText(canvas, sub, textX, y + H(113), X(21), GREEN, false, Paint.Align.LEFT);
        }

        private void drawDoorCard(Canvas canvas) {
            float x = X(26);
            float y = Y(936);
            float w = X(872);
            float h = H(353);
            float radius = X(38);

            setBlueGradient(x, y, x + w, y + h);
            shadow(0, X(10), Color.argb(75, 47, 104, 220));
            r.set(x, y, x + w, y + h);
            canvas.drawRoundRect(r, radius, radius, p);
            clearShadow();
            p.setShader(null);

            drawWave(canvas, x, y, w, h);

            float circleCx = x + X(166);
            float circleCy = y + h / 2f;
            float circleR = X(109);

            p.setColor(Color.argb(32, 255, 255, 255));
            shadow(0, X(8), Color.argb(110, 255, 255, 255));
            canvas.drawCircle(circleCx, circleCy, circleR, p);
            clearShadow();

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(X(2));
            stroke.setColor(Color.WHITE);
            canvas.drawCircle(circleCx, circleCy, circleR, stroke);

            if (holding && !opened) {
                stroke.setStrokeWidth(X(7));
                stroke.setStrokeCap(Paint.Cap.ROUND);
                stroke.setColor(Color.rgb(123, 221, 255));
                r.set(circleCx - circleR - X(5), circleCy - circleR - X(5), circleCx + circleR + X(5), circleCy + circleR + X(5));
                canvas.drawArc(r, -90, 360f * holdProgress, false, stroke);
            }

            if (!opened) {
                drawLock(canvas, circleCx, circleCy, X(95), Color.WHITE);
                float tx = x + X(334);
                drawFittedText(
                    canvas,
                    "Открыть дверь",
                    tx,
                    x + w - X(45),
                    y + H(187),
                    X(61),
                    X(39),
                    Color.WHITE,
                    true,
                    Paint.Align.LEFT
                );
                drawFittedText(
                    canvas,
                    holding ? "Продолжайте удерживать" : "Удерживайте для открытия",
                    tx,
                    x + w - X(45),
                    y + H(251),
                    X(30),
                    X(22),
                    Color.rgb(145, 187, 255),
                    false,
                    Paint.Align.LEFT
                );
            } else {
                drawCheck(canvas, circleCx, circleCy, X(78), Color.WHITE);
                float tx = x + X(334);
                drawFittedText(
                    canvas,
                    "Дверь открыта",
                    tx,
                    x + w - X(45),
                    y + H(184),
                    X(56),
                    X(38),
                    Color.WHITE,
                    true,
                    Paint.Align.LEFT
                );
                drawText(canvas, "Команда выполнена", tx, y + H(248), X(28), Color.rgb(176, 211, 255), false, Paint.Align.LEFT);
            }

            doorHit.set(x, y, x + w, y + h);
        }

        private void drawNavigation(Canvas canvas) {
            float x = X(26);
            float y = Y(1337);
            float w = X(872);
            float h = H(113);

            p.setShader(new LinearGradient(
                x, y, x, y + h,
                new int[]{Color.rgb(253, 254, 255), Color.rgb(235, 240, 251)},
                null,
                Shader.TileMode.CLAMP
            ));
            shadow(0, X(8), Color.argb(60, 74, 107, 178));
            r.set(x, y, x + w, y + h);
            canvas.drawRoundRect(r, X(34), X(34), p);
            clearShadow();
            p.setShader(null);

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(X(1));
            stroke.setColor(Color.rgb(220, 228, 245));
            canvas.drawRoundRect(r, X(34), X(34), stroke);

            String[] labels = {"Главная", "События", "Заявки", "Профиль"};
            float cellW = w / 4f;

            for (int i = 0; i < 4; i++) {
                float cx = x + cellW * (i + 0.5f);
                int color = selectedTab == i ? BLUE : MUTED;
                drawNavIcon(canvas, i, cx, y + H(43), X(43), color);
                drawText(canvas, labels[i], cx, y + H(90), X(22), color, selectedTab == i, Paint.Align.CENTER);
            }

            navHit.set(x, y, x + w, y + h);
        }

        private void drawSecondary(Canvas canvas) {
            String[] titles = {"", "События", "Заявки", "Профиль"};
            String[] subs = {
                "",
                "История доступа и уведомлений",
                "Ваши обращения и их статусы",
                "Настройки пользователя"
            };

            float x = X(26);
            float y = Y(48);
            float w = X(872);
            float h = H(220);
            setBlueGradient(x, y, x + w, y + h);
            r.set(x, y, x + w, y + h);
            canvas.drawRoundRect(r, X(38), X(38), p);
            p.setShader(null);

            drawText(canvas, titles[selectedTab], x + X(45), y + H(100), X(52), Color.WHITE, true, Paint.Align.LEFT);
            drawText(canvas, subs[selectedTab], x + X(45), y + H(152), X(25), Color.rgb(191, 218, 255), false, Paint.Align.LEFT);

            p.setColor(Color.rgb(252, 253, 255));
            shadow(0, X(6), Color.argb(45, 60, 95, 150));
            r.set(x, Y(315), x + w, Y(720));
            canvas.drawRoundRect(r, X(32), X(32), p);
            clearShadow();

            drawText(canvas, "Раздел готов к подключению данных", x + X(48), Y(410), X(30), TEXT_DARK, true, Paint.Align.LEFT);
            drawText(canvas, "Навигация работает. Наполнение подключим следующим этапом.", x + X(48), Y(466), X(23), MUTED, false, Paint.Align.LEFT);
        }

        private void drawBuildingIcon(Canvas canvas, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(Math.max(X(2), size * 0.018f));
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setColor(color);

            float w = size * 0.52f;
            float h = size * 0.76f;
            float left = cx - w / 2f;
            float top = cy - h / 2f;
            float right = cx + w / 2f;
            float bottom = cy + h / 2f;

            path.reset();
            path.moveTo(left - size * 0.07f, top + size * 0.10f);
            path.lineTo(cx, top - size * 0.09f);
            path.lineTo(right + size * 0.07f, top + size * 0.10f);
            canvas.drawPath(path, stroke);
            canvas.drawRect(left, top + size * 0.10f, right, bottom, stroke);

            int rows = 4;
            int cols = 2;
            float ww = size * 0.085f;
            float wh = size * 0.085f;
            for (int rr = 0; rr < rows; rr++) {
                for (int cc = 0; cc < cols; cc++) {
                    float wx = cx + (cc == 0 ? -size * 0.13f : size * 0.13f);
                    float wy = top + size * (0.24f + rr * 0.14f);
                    canvas.drawRect(wx - ww / 2f, wy - wh / 2f, wx + ww / 2f, wy + wh / 2f, stroke);
                }
            }

            r.set(cx - size * 0.07f, bottom - size * 0.21f, cx + size * 0.07f, bottom);
            canvas.drawRoundRect(r, size * 0.04f, size * 0.04f, stroke);
            canvas.drawLine(cx - size * 0.48f, bottom + size * 0.01f, cx + size * 0.48f, bottom + size * 0.01f, stroke);
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

            canvas.drawLine(cx, cy - size * 0.34f, cx, cy + size * 0.16f, stroke);
            canvas.drawCircle(cx, cy + size * 0.27f, size * 0.17f, stroke);
            canvas.drawLine(cx + size * 0.16f, cy - size * 0.28f, cx + size * 0.28f, cy - size * 0.28f, stroke);

            if (outside) {
                canvas.drawCircle(cx + size * 0.42f, cy - size * 0.11f, size * 0.12f, stroke);
            } else {
                path.reset();
                path.moveTo(cx + size * 0.20f, cy - size * 0.21f);
                path.lineTo(cx + size * 0.44f, cy - size * 0.42f);
                path.lineTo(cx + size * 0.67f, cy - size * 0.21f);
                canvas.drawPath(path, stroke);
            }
        }

        private void drawLock(Canvas canvas, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(Math.max(X(3), size * 0.065f));
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setColor(color);

            float bodyW = size * 0.68f;
            float bodyH = size * 0.52f;
            float bodyTop = cy - size * 0.02f;
            r.set(cx - bodyW / 2f, bodyTop, cx + bodyW / 2f, bodyTop + bodyH);
            canvas.drawRoundRect(r, size * 0.07f, size * 0.07f, stroke);

            r.set(cx - size * 0.24f, cy - size * 0.44f, cx + size * 0.24f, cy + size * 0.10f);
            canvas.drawArc(r, 180, 180, false, stroke);
            canvas.drawCircle(cx, cy + size * 0.19f, size * 0.055f, stroke);
            canvas.drawLine(cx, cy + size * 0.245f, cx, cy + size * 0.36f, stroke);
        }

        private void drawGlobe(Canvas canvas, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(size * 0.055f);
            stroke.setColor(color);
            r.set(cx - size / 2f, cy - size / 2f, cx + size / 2f, cy + size / 2f);
            canvas.drawOval(r, stroke);
            r.set(cx - size * 0.22f, cy - size / 2f, cx + size * 0.22f, cy + size / 2f);
            canvas.drawOval(r, stroke);
            canvas.drawLine(cx - size / 2f, cy, cx + size / 2f, cy, stroke);
            canvas.drawLine(cx - size * 0.42f, cy - size * 0.22f, cx + size * 0.42f, cy - size * 0.22f, stroke);
            canvas.drawLine(cx - size * 0.42f, cy + size * 0.22f, cx + size * 0.42f, cy + size * 0.22f, stroke);
        }

        private void drawBluetooth(Canvas canvas, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(size * 0.065f);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setColor(color);
            path.reset();
            path.moveTo(cx, cy - size * 0.50f);
            path.lineTo(cx + size * 0.28f, cy - size * 0.24f);
            path.lineTo(cx - size * 0.27f, cy + size * 0.28f);
            path.lineTo(cx + size * 0.28f, cy + size * 0.50f);
            path.lineTo(cx, cy + size * 0.73f);
            path.lineTo(cx, cy - size * 0.50f);
            canvas.drawPath(path, stroke);
            canvas.drawLine(cx - size * 0.30f, cy - size * 0.30f, cx + size * 0.28f, cy + size * 0.50f, stroke);
        }

        private void drawCheck(Canvas canvas, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(size * 0.10f);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setColor(color);
            path.reset();
            path.moveTo(cx - size * 0.34f, cy);
            path.lineTo(cx - size * 0.08f, cy + size * 0.25f);
            path.lineTo(cx + size * 0.38f, cy - size * 0.30f);
            canvas.drawPath(path, stroke);
        }

        private void drawNavIcon(Canvas canvas, int type, float cx, float cy, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(size * 0.07f);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setColor(color);

            if (type == 0) {
                path.reset();
                path.moveTo(cx - size * 0.38f, cy);
                path.lineTo(cx, cy - size * 0.32f);
                path.lineTo(cx + size * 0.38f, cy);
                canvas.drawPath(path, stroke);
                r.set(cx - size * 0.28f, cy, cx + size * 0.28f, cy + size * 0.34f);
                canvas.drawRect(r, stroke);
            } else if (type == 1) {
                canvas.drawCircle(cx, cy, size * 0.36f, stroke);
                canvas.drawLine(cx, cy, cx, cy - size * 0.22f, stroke);
                canvas.drawLine(cx, cy, cx + size * 0.18f, cy + size * 0.12f, stroke);
            } else if (type == 2) {
                r.set(cx - size * 0.26f, cy - size * 0.32f, cx + size * 0.26f, cy + size * 0.34f);
                canvas.drawRoundRect(r, size * 0.05f, size * 0.05f, stroke);
                canvas.drawLine(cx - size * 0.12f, cy - size * 0.05f, cx + size * 0.12f, cy - size * 0.05f, stroke);
                canvas.drawLine(cx - size * 0.12f, cy + size * 0.09f, cx + size * 0.12f, cy + size * 0.09f, stroke);
            } else {
                canvas.drawCircle(cx, cy - size * 0.18f, size * 0.16f, stroke);
                r.set(cx - size * 0.28f, cy + size * 0.04f, cx + size * 0.28f, cy + size * 0.44f);
                canvas.drawArc(r, 190, 160, false, stroke);
            }
        }

        private void setBlueGradient(float left, float top, float right, float bottom) {
            p.setShader(new LinearGradient(
                left, top, right, bottom,
                new int[]{Color.rgb(86, 175, 255), BLUE, BLUE_DARK},
                new float[]{0f, 0.43f, 1f},
                Shader.TileMode.CLAMP
            ));
        }

        private void drawText(Canvas canvas, String value, float x, float baseline, float size, int color, boolean bold, Paint.Align align) {
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setTextSize(size);
            p.setTextAlign(align);
            p.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
            canvas.drawText(value, x, baseline, p);
        }

        private void drawFittedText(Canvas canvas, String value, float left, float right, float baseline, float preferred, float minimum, int color, boolean bold, Paint.Align align) {
            p.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
            p.setTextAlign(align);
            p.setColor(color);
            p.setShader(null);
            float size = preferred;
            p.setTextSize(size);
            float available = Math.max(1f, right - left);
            while (p.measureText(value) > available && size > minimum) {
                size -= X(1.2f);
                p.setTextSize(size);
            }
            float x = align == Paint.Align.RIGHT ? right : left;
            canvas.drawText(value, x, baseline, p);
        }

        private void shadow(float dx, float radius, int color) {
            p.setShadowLayer(radius, dx, radius * 0.25f, color);
        }

        private void clearShadow() {
            p.clearShadowLayer();
        }

        private float X(float value) {
            return value * sx;
        }

        private float Y(float value) {
            return oy + value * sy;
        }

        private float H(float value) {
            return value * sy;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (selectedTab == 0 && doorHit.contains(x, y) && !opened) {
                    holding = true;
                    startHold();
                    invalidate();
                    return true;
                }

                if (navHit.contains(x, y)) {
                    return true;
                }
            }

            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                if (holding && !doorHit.contains(x, y)) {
                    cancelHold();
                }
                return true;
            }

            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                if (holding) {
                    if (!opened) {
                        cancelHold();
                    }
                    return true;
                }

                if (navHit.contains(x, y)) {
                    int index = (int) ((x - navHit.left) / (navHit.width() / 4f));
                    index = Math.max(0, Math.min(3, index));
                    selectedTab = index;
                    invalidate();
                    performClick();
                    return true;
                }
            }

            if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                cancelHold();
                return true;
            }

            return true;
        }

        private void startHold() {
            if (holdAnimator != null) {
                holdAnimator.cancel();
            }

            holdProgress = 0f;
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
                    if (!cancelled && holding) {
                        holding = false;
                        opened = true;
                        holdProgress = 1f;
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
        }

        private void cancelHold() {
            holding = false;
            if (holdAnimator != null) {
                holdAnimator.cancel();
            }
            holdProgress = 0f;
            invalidate();
        }

        private void vibrate() {
            try {
                Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(45);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }
    }
}
