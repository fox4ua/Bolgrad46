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
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.FrameLayout;

public class TargetActivity extends Activity {
    private static final int BACKGROUND = Color.rgb(246, 248, 253);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(BACKGROUND);
        window.setNavigationBarColor(BACKGROUND);

        if (Build.VERSION.SDK_INT >= 29) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BACKGROUND);

        TargetView targetView = new TargetView(this);
        root.addView(targetView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int top;
            int bottom;

            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }

            view.setPadding(0, top, 0, bottom);
            return insets;
        });

        setContentView(root);
        root.requestApplyInsets();
    }

    private static final class TargetView extends View {
        private static final float DESIGN_WIDTH = 941f;

        private static final int BLUE_LEFT = Color.rgb(79, 170, 255);
        private static final int BLUE_MID = Color.rgb(19, 119, 252);
        private static final int BLUE_RIGHT = Color.rgb(0, 78, 221);
        private static final int NAVY = Color.rgb(5, 28, 49);
        private static final int NAVY_LIGHT = Color.rgb(17, 59, 82);
        private static final int WHITE = Color.rgb(249, 252, 255);
        private static final int MUTED = Color.rgb(102, 130, 188);
        private static final int GREEN = Color.rgb(17, 204, 119);
        private static final int ACTIVE_BLUE = Color.rgb(16, 116, 249);

        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF rect = new RectF();
        private final RectF doorHit = new RectF();
        private final RectF navHit = new RectF();

        private Bitmap houseBitmap;

        private float sx;
        private float sy;
        private float headerY;
        private float houseY;
        private float doorY;
        private float navY;

        private int selectedTab = 0;
        private boolean holding;
        private boolean opened;
        private float holdProgress;
        private ValueAnimator holdAnimator;

        TargetView(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setClickable(true);
            setFocusable(true);
            houseBitmap = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.house46_runtime
            );
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            sx = getWidth() / DESIGN_WIDTH;
            sy = sx * 1.10f;
            calculateLayout();
            drawBackground(canvas);

            if (selectedTab == 0) {
                drawHeader(canvas);
                drawHouseCard(canvas);
                drawDoorCard(canvas);
            } else {
                drawSecondaryPage(canvas);
            }

            drawNavigation(canvas);
        }

        private void calculateLayout() {
            float top = 14f * sy;
            float headerHeight = 238f * sy;
            float houseHeight = 576f * sy;
            float doorHeight = 354f * sy;
            float navHeight = 120f * sy;
            float bottom = 12f * sy;

            navY = getHeight() - bottom - navHeight;

            float baseGapOne = 42f * sy;
            float baseGapTwo = 50f * sy;
            float baseGapThree = 42f * sy;

            float used = top
                + headerHeight
                + baseGapOne
                + houseHeight
                + baseGapTwo
                + doorHeight
                + baseGapThree;

            float availableBeforeNavigation = navY;
            float extra = Math.max(0f, availableBeforeNavigation - used);

            float gapOne = baseGapOne + extra * 0.29f;
            float gapTwo = baseGapTwo + extra * 0.30f;
            float gapThree = baseGapThree + extra * 0.41f;

            headerY = top;
            houseY = headerY + headerHeight + gapOne;
            doorY = houseY + houseHeight + gapTwo;

            float calculatedDoorBottom = doorY + doorHeight;
            float maximumDoorBottom = navY - gapThree;

            if (calculatedDoorBottom > maximumDoorBottom) {
                doorY -= calculatedDoorBottom - maximumDoorBottom;
            }
        }

        private float x(float value) {
            return value * sx;
        }

        private float h(float value) {
            return value * sy;
        }

        private void drawBackground(Canvas canvas) {
            fill.setShader(new LinearGradient(
                0f,
                0f,
                getWidth(),
                getHeight(),
                new int[]{
                    Color.rgb(251, 252, 255),
                    Color.rgb(240, 244, 252),
                    Color.rgb(249, 250, 255)
                },
                new float[]{0f, 0.58f, 1f},
                Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0f, 0f, getWidth(), getHeight(), fill);
            fill.setShader(null);
        }

        private void drawHeader(Canvas canvas) {
            float left = x(27f);
            float top = headerY;
            float width = x(887f);
            float height = h(238f);
            float radius = x(39f);

            setBlueGradient(left, top, left + width, top + height);
            setShadow(fill, x(13f), Color.argb(80, 42, 105, 226));
            rect.set(left, top, left + width, top + height);
            canvas.drawRoundRect(rect, radius, radius, fill);
            clearShadow(fill);
            fill.setShader(null);

            drawWave(canvas, left, top, width, height);
            drawBuilding(canvas, left + x(111f), top + height * 0.50f, x(149f));

            float bellWidth = x(137f);
            float bellHeight = h(154f);
            float bellLeft = left + width - bellWidth - x(29f);
            float bellTop = top + (height - bellHeight) * 0.50f;
            drawBellButton(canvas, bellLeft, bellTop, bellWidth, bellHeight);

            drawFittedText(
                canvas,
                "Болградская, 46",
                left + x(239f),
                bellLeft - x(21f),
                top + height * 0.58f,
                x(52f),
                x(35f),
                WHITE,
                true,
                Paint.Align.LEFT
            );
        }

        private void drawHouseCard(Canvas canvas) {
            float left = x(27f);
            float top = houseY;
            float width = x(887f);
            float height = h(576f);
            float radius = x(35f);
            float statusHeight = h(153f);
            float imageBottom = top + height - statusHeight;

            rect.set(left, top, left + width, top + height);
            fill.setColor(NAVY);
            setShadow(fill, x(10f), Color.argb(55, 38, 82, 132));
            canvas.drawRoundRect(rect, radius, radius, fill);
            clearShadow(fill);

            canvas.save();
            path.reset();
            path.addRoundRect(rect, radius, radius, Path.Direction.CW);
            canvas.clipPath(path);

            if (houseBitmap != null) {
                drawCenterCrop(
                    canvas,
                    houseBitmap,
                    new RectF(left, top, left + width, imageBottom)
                );
            } else {
                fill.setShader(new LinearGradient(
                    left,
                    top,
                    left + width,
                    imageBottom,
                    Color.rgb(12, 48, 73),
                    Color.rgb(4, 22, 38),
                    Shader.TileMode.CLAMP
                ));
                canvas.drawRect(left, top, left + width, imageBottom, fill);
                fill.setShader(null);
            }

            drawOnlinePill(canvas, left + x(20f), top + h(18f));

            float temperatureTop = imageBottom - h(174f);
            drawTemperatureCard(
                canvas,
                left + x(20f),
                temperatureTop,
                x(276f),
                h(139f),
                "В помещении",
                "22°C",
                false
            );
            drawTemperatureCard(
                canvas,
                left + width - x(296f),
                temperatureTop,
                x(276f),
                h(139f),
                "На улице",
                "18°C",
                true
            );

            fill.setShader(new LinearGradient(
                left,
                imageBottom,
                left,
                top + height,
                new int[]{NAVY_LIGHT, NAVY},
                null,
                Shader.TileMode.CLAMP
            ));
            canvas.drawRect(left, imageBottom, left + width, top + height, fill);
            fill.setShader(null);

            drawStatusRow(canvas, left, imageBottom, width, statusHeight);
            canvas.restore();
        }

        private void drawCenterCrop(Canvas canvas, Bitmap bitmap, RectF destination) {
            float bitmapRatio = bitmap.getWidth() / (float) bitmap.getHeight();
            float destinationRatio = destination.width() / destination.height();
            Rect source;

            if (bitmapRatio > destinationRatio) {
                int sourceWidth = Math.round(bitmap.getHeight() * destinationRatio);
                int sourceLeft = (bitmap.getWidth() - sourceWidth) / 2;
                source = new Rect(
                    sourceLeft,
                    0,
                    sourceLeft + sourceWidth,
                    bitmap.getHeight()
                );
            } else {
                int sourceHeight = Math.round(bitmap.getWidth() / destinationRatio);
                int sourceTop = Math.max(0, (bitmap.getHeight() - sourceHeight) / 2);
                source = new Rect(
                    0,
                    sourceTop,
                    bitmap.getWidth(),
                    Math.min(bitmap.getHeight(), sourceTop + sourceHeight)
                );
            }

            fill.setAlpha(255);
            canvas.drawBitmap(bitmap, source, destination, fill);
        }

        private void drawOnlinePill(Canvas canvas, float left, float top) {
            float width = x(170f);
            float height = h(58f);

            fill.setColor(Color.argb(229, 4, 31, 57));
            rect.set(left, top, left + width, top + height);
            canvas.drawRoundRect(rect, x(29f), x(29f), fill);

            fill.setColor(Color.rgb(17, 214, 133));
            canvas.drawCircle(left + x(29f), top + height * 0.50f, x(10f), fill);

            drawText(
                canvas,
                "Онлайн",
                left + x(55f),
                top + height * 0.66f,
                x(25f),
                WHITE,
                false,
                Paint.Align.LEFT
            );
        }

        private void drawTemperatureCard(
            Canvas canvas,
            float left,
            float top,
            float width,
            float height,
            String title,
            String value,
            boolean outside
        ) {
            fill.setColor(Color.argb(224, 21, 46, 66));
            setShadow(fill, x(5f), Color.argb(80, 0, 12, 25));
            rect.set(left, top, left + width, top + height);
            canvas.drawRoundRect(rect, x(29f), x(29f), fill);
            clearShadow(fill);

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(x(1.8f));
            stroke.setColor(Color.argb(230, 229, 239, 252));
            canvas.drawRoundRect(rect, x(29f), x(29f), stroke);

            drawThermometer(
                canvas,
                left + x(55f),
                top + height * 0.55f,
                x(47f),
                outside
            );

            float textLeft = left + x(96f);
            drawFittedText(
                canvas,
                title,
                textLeft,
                left + width - x(15f),
                top + h(53f),
                x(24f),
                x(19f),
                WHITE,
                false,
                Paint.Align.LEFT
            );
            drawFittedText(
                canvas,
                value,
                textLeft,
                left + width - x(15f),
                top + h(104f),
                x(45f),
                x(36f),
                WHITE,
                true,
                Paint.Align.LEFT
            );
        }

        private void drawStatusRow(Canvas canvas, float left, float top, float width, float height) {
            float cellWidth = width / 3f;

            fill.setColor(Color.argb(95, 216, 235, 255));
            canvas.drawRect(
                left + cellWidth - x(1f),
                top + h(29f),
                left + cellWidth + x(1f),
                top + height - h(29f),
                fill
            );
            canvas.drawRect(
                left + cellWidth * 2f - x(1f),
                top + h(29f),
                left + cellWidth * 2f + x(1f),
                top + height - h(29f),
                fill
            );

            drawStatusCell(canvas, left, top, cellWidth, height, 0, "Замок", "Онлайн");
            drawStatusCell(canvas, left + cellWidth, top, cellWidth, height, 1, "Интернет", "Подключено");
            drawStatusCell(canvas, left + cellWidth * 2f, top, cellWidth, height, 2, "Bluetooth", "Подключено");
        }

        private void drawStatusCell(
            Canvas canvas,
            float left,
            float top,
            float width,
            float height,
            int type,
            String title,
            String subtitle
        ) {
            float iconX = left + x(68f);
            float centerY = top + height * 0.50f;

            if (type == 0) {
                drawLock(canvas, iconX, centerY, x(61f), WHITE);
            } else if (type == 1) {
                drawGlobe(canvas, iconX, centerY, x(62f), WHITE);
            } else {
                drawBluetooth(canvas, iconX, centerY, x(63f), WHITE);
            }

            float textLeft = left + x(134f);
            float textRight = left + width - x(11f);
            drawFittedText(
                canvas,
                title,
                textLeft,
                textRight,
                top + h(65f),
                x(24f),
                x(18f),
                WHITE,
                false,
                Paint.Align.LEFT
            );
            drawFittedText(
                canvas,
                subtitle,
                textLeft,
                textRight,
                top + h(112f),
                x(21f),
                x(15f),
                GREEN,
                false,
                Paint.Align.LEFT
            );
        }

        private void drawDoorCard(Canvas canvas) {
            float left = x(27f);
            float top = doorY;
            float width = x(887f);
            float height = h(354f);
            float radius = x(39f);

            setBlueGradient(left, top, left + width, top + height);
            setShadow(fill, x(12f), Color.argb(88, 39, 101, 219));
            rect.set(left, top, left + width, top + height);
            canvas.drawRoundRect(rect, radius, radius, fill);
            clearShadow(fill);
            fill.setShader(null);

            drawWave(canvas, left, top, width, height);

            float circleX = left + x(168f);
            float circleY = top + height * 0.50f;
            float circleRadius = x(108f);

            fill.setColor(Color.argb(30, 255, 255, 255));
            setShadow(fill, x(12f), Color.argb(115, 255, 255, 255));
            canvas.drawCircle(circleX, circleY, circleRadius, fill);
            clearShadow(fill);

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(x(2f));
            stroke.setColor(WHITE);
            canvas.drawCircle(circleX, circleY, circleRadius, stroke);

            if (holding && !opened) {
                stroke.setStrokeCap(Paint.Cap.ROUND);
                stroke.setStrokeWidth(x(8f));
                stroke.setColor(Color.rgb(112, 221, 255));
                rect.set(
                    circleX - circleRadius - x(6f),
                    circleY - circleRadius - x(6f),
                    circleX + circleRadius + x(6f),
                    circleY + circleRadius + x(6f)
                );
                canvas.drawArc(rect, -90f, 360f * holdProgress, false, stroke);
            }

            float textLeft = left + x(338f);
            float textRight = left + width - x(38f);

            if (opened) {
                drawCheck(canvas, circleX, circleY, x(80f), WHITE);
                drawFittedText(
                    canvas,
                    "Дверь открыта",
                    textLeft,
                    textRight,
                    top + h(184f),
                    x(55f),
                    x(38f),
                    WHITE,
                    true,
                    Paint.Align.LEFT
                );
                drawFittedText(
                    canvas,
                    "Команда выполнена",
                    textLeft,
                    textRight,
                    top + h(249f),
                    x(29f),
                    x(21f),
                    Color.rgb(178, 211, 255),
                    false,
                    Paint.Align.LEFT
                );
            } else {
                drawLock(canvas, circleX, circleY, x(98f), WHITE);
                drawFittedText(
                    canvas,
                    "Открыть дверь",
                    textLeft,
                    textRight,
                    top + h(185f),
                    x(59f),
                    x(39f),
                    WHITE,
                    true,
                    Paint.Align.LEFT
                );
                drawFittedText(
                    canvas,
                    holding ? "Продолжайте удерживать" : "Удерживайте для открытия",
                    textLeft,
                    textRight,
                    top + h(250f),
                    x(29f),
                    x(21f),
                    Color.rgb(154, 192, 255),
                    false,
                    Paint.Align.LEFT
                );
            }

            doorHit.set(left, top, left + width, top + height);
        }

        private void drawNavigation(Canvas canvas) {
            float left = x(27f);
            float width = x(887f);
            float height = h(120f);
            float top = navY;

            fill.setShader(new LinearGradient(
                left,
                top,
                left,
                top + height,
                new int[]{Color.rgb(254, 254, 255), Color.rgb(237, 242, 252)},
                null,
                Shader.TileMode.CLAMP
            ));
            setShadow(fill, x(9f), Color.argb(55, 76, 107, 177));
            rect.set(left, top, left + width, top + height);
            canvas.drawRoundRect(rect, x(35f), x(35f), fill);
            clearShadow(fill);
            fill.setShader(null);

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(x(1f));
            stroke.setColor(Color.rgb(219, 227, 244));
            canvas.drawRoundRect(rect, x(35f), x(35f), stroke);

            String[] labels = {"Главная", "События", "Заявки", "Профиль"};
            float cellWidth = width / 4f;

            for (int index = 0; index < labels.length; index++) {
                float centerX = left + cellWidth * (index + 0.5f);
                int color = selectedTab == index ? ACTIVE_BLUE : MUTED;

                drawNavIcon(
                    canvas,
                    index,
                    centerX,
                    top + h(45f),
                    x(43f),
                    color
                );
                drawFittedText(
                    canvas,
                    labels[index],
                    left + cellWidth * index + x(6f),
                    left + cellWidth * (index + 1) - x(6f),
                    top + h(96f),
                    x(22f),
                    x(17f),
                    color,
                    selectedTab == index,
                    Paint.Align.CENTER
                );
            }

            navHit.set(left, top, left + width, top + height);
        }

        private void drawSecondaryPage(Canvas canvas) {
            String[] titles = {"", "События", "Заявки", "Профиль"};
            String[] subtitles = {
                "",
                "История доступа и уведомлений",
                "Ваши обращения и их статусы",
                "Настройки пользователя"
            };

            float left = x(27f);
            float top = headerY;
            float width = x(887f);
            float height = h(238f);

            setBlueGradient(left, top, left + width, top + height);
            rect.set(left, top, left + width, top + height);
            canvas.drawRoundRect(rect, x(39f), x(39f), fill);
            fill.setShader(null);

            drawText(
                canvas,
                titles[selectedTab],
                left + x(44f),
                top + h(108f),
                x(51f),
                WHITE,
                true,
                Paint.Align.LEFT
            );
            drawFittedText(
                canvas,
                subtitles[selectedTab],
                left + x(44f),
                left + width - x(44f),
                top + h(164f),
                x(25f),
                x(18f),
                Color.rgb(198, 220, 255),
                false,
                Paint.Align.LEFT
            );

            float cardTop = top + height + h(48f);
            float cardHeight = h(300f);
            fill.setColor(Color.WHITE);
            setShadow(fill, x(7f), Color.argb(45, 64, 96, 150));
            rect.set(left, cardTop, left + width, cardTop + cardHeight);
            canvas.drawRoundRect(rect, x(32f), x(32f), fill);
            clearShadow(fill);

            drawFittedText(
                canvas,
                "Раздел готов к подключению данных",
                left + x(45f),
                left + width - x(45f),
                cardTop + h(100f),
                x(31f),
                x(22f),
                Color.rgb(38, 56, 91),
                true,
                Paint.Align.LEFT
            );
        }

        private void setBlueGradient(float left, float top, float right, float bottom) {
            fill.setAlpha(255);
            fill.setShader(new LinearGradient(
                left,
                top,
                right,
                bottom,
                new int[]{BLUE_LEFT, BLUE_MID, BLUE_RIGHT},
                new float[]{0f, 0.52f, 1f},
                Shader.TileMode.CLAMP
            ));
        }

        private void drawWave(Canvas canvas, float left, float top, float width, float height) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeWidth(Math.max(1f, x(1.6f)));
            stroke.setShader(new LinearGradient(
                left + width * 0.28f,
                top,
                left + width,
                top + height,
                new int[]{
                    Color.argb(0, 255, 255, 255),
                    Color.argb(185, 255, 255, 255),
                    Color.argb(0, 255, 255, 255)
                },
                null,
                Shader.TileMode.CLAMP
            ));
            path.reset();
            path.moveTo(left + width * 0.29f, top + height * 0.74f);
            path.cubicTo(
                left + width * 0.54f,
                top + height * 0.72f,
                left + width * 0.70f,
                top + height * 0.21f,
                left + width * 0.99f,
                top + height * 0.14f
            );
            canvas.drawPath(path, stroke);
            stroke.setShader(null);
        }

        private void drawBellButton(Canvas canvas, float left, float top, float width, float height) {
            fill.setShader(new LinearGradient(
                left,
                top,
                left + width,
                top + height,
                new int[]{Color.rgb(91, 175, 255), Color.rgb(2, 82, 208)},
                null,
                Shader.TileMode.CLAMP
            ));
            setShadow(fill, x(12f), Color.argb(115, 0, 62, 172));
            rect.set(left, top, left + width, top + height);
            canvas.drawRoundRect(rect, x(35f), x(35f), fill);
            clearShadow(fill);
            fill.setShader(null);

            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(x(5f));
            stroke.setColor(WHITE);

            float centerX = left + width * 0.50f;
            float centerY = top + height * 0.50f;
            float bellWidth = x(50f);
            float bellHeight = h(61f);

            rect.set(
                centerX - bellWidth * 0.50f,
                centerY - bellHeight * 0.50f,
                centerX + bellWidth * 0.50f,
                centerY + bellHeight * 0.50f
            );
            canvas.drawArc(rect, 195f, 150f, false, stroke);
            canvas.drawLine(
                centerX - bellWidth * 0.50f,
                centerY + h(6f),
                centerX - bellWidth * 0.50f,
                centerY + h(25f),
                stroke
            );
            canvas.drawLine(
                centerX + bellWidth * 0.50f,
                centerY + h(6f),
                centerX + bellWidth * 0.50f,
                centerY + h(25f),
                stroke
            );
            canvas.drawLine(
                centerX - bellWidth * 0.50f,
                centerY + h(25f),
                centerX + bellWidth * 0.50f,
                centerY + h(25f),
                stroke
            );
            canvas.drawCircle(centerX, centerY + h(38f), x(5f), stroke);

            fill.setColor(Color.rgb(72, 203, 255));
            canvas.drawCircle(left + width - x(28f), top + h(27f), x(12f), fill);
        }

        private void drawBuilding(Canvas canvas, float centerX, float centerY, float size) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(Math.max(x(2.2f), size * 0.018f));
            stroke.setColor(WHITE);

            float left = centerX - size * 0.25f;
            float right = centerX + size * 0.25f;
            float top = centerY - size * 0.38f;
            float bottom = centerY + size * 0.38f;

            path.reset();
            path.moveTo(left - size * 0.06f, top + size * 0.10f);
            path.lineTo(centerX, top - size * 0.08f);
            path.lineTo(right + size * 0.06f, top + size * 0.10f);
            canvas.drawPath(path, stroke);
            canvas.drawRect(left, top + size * 0.10f, right, bottom, stroke);

            for (int row = 0; row < 4; row++) {
                for (int column = 0; column < 2; column++) {
                    float windowX = centerX + (column == 0 ? -size * 0.125f : size * 0.125f);
                    float windowY = top + size * (0.24f + row * 0.14f);
                    canvas.drawRect(
                        windowX - size * 0.042f,
                        windowY - size * 0.042f,
                        windowX + size * 0.042f,
                        windowY + size * 0.042f,
                        stroke
                    );
                }
            }

            rect.set(
                centerX - size * 0.07f,
                bottom - size * 0.21f,
                centerX + size * 0.07f,
                bottom
            );
            canvas.drawRoundRect(rect, size * 0.04f, size * 0.04f, stroke);
            canvas.drawLine(centerX - size * 0.48f, bottom, centerX + size * 0.48f, bottom, stroke);
            canvas.drawLine(centerX - size * 0.42f, bottom, centerX - size * 0.42f, bottom - size * 0.25f, stroke);
            canvas.drawCircle(centerX - size * 0.42f, bottom - size * 0.29f, size * 0.045f, stroke);
            canvas.drawLine(centerX + size * 0.42f, bottom, centerX + size * 0.42f, bottom - size * 0.25f, stroke);
            canvas.drawCircle(centerX + size * 0.42f, bottom - size * 0.29f, size * 0.045f, stroke);
        }

        private void drawThermometer(Canvas canvas, float centerX, float centerY, float size, boolean outside) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeWidth(size * 0.08f);
            stroke.setColor(WHITE);

            canvas.drawLine(centerX, centerY - size * 0.34f, centerX, centerY + size * 0.15f, stroke);
            canvas.drawCircle(centerX, centerY + size * 0.27f, size * 0.17f, stroke);
            canvas.drawLine(centerX, centerY - size * 0.34f, centerX, centerY + size * 0.27f, stroke);

            if (outside) {
                canvas.drawCircle(centerX + size * 0.45f, centerY - size * 0.10f, size * 0.10f, stroke);
            } else {
                path.reset();
                path.moveTo(centerX + size * 0.30f, centerY - size * 0.24f);
                path.lineTo(centerX + size * 0.48f, centerY - size * 0.40f);
                path.lineTo(centerX + size * 0.65f, centerY - size * 0.24f);
                canvas.drawPath(path, stroke);
            }
        }

        private void drawLock(Canvas canvas, float centerX, float centerY, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(size * 0.055f);
            stroke.setColor(color);

            float bodyWidth = size * 0.62f;
            float bodyHeight = size * 0.53f;
            rect.set(
                centerX - bodyWidth * 0.50f,
                centerY - size * 0.02f,
                centerX + bodyWidth * 0.50f,
                centerY - size * 0.02f + bodyHeight
            );
            canvas.drawRoundRect(rect, size * 0.05f, size * 0.05f, stroke);

            rect.set(
                centerX - size * 0.24f,
                centerY - size * 0.46f,
                centerX + size * 0.24f,
                centerY + size * 0.10f
            );
            canvas.drawArc(rect, 180f, -180f, false, stroke);
            canvas.drawCircle(centerX, centerY + size * 0.19f, size * 0.055f, stroke);
            canvas.drawLine(
                centerX,
                centerY + size * 0.245f,
                centerX,
                centerY + size * 0.35f,
                stroke
            );
        }

        private void drawGlobe(Canvas canvas, float centerX, float centerY, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(size * 0.045f);
            stroke.setColor(color);

            canvas.drawCircle(centerX, centerY, size * 0.40f, stroke);
            rect.set(
                centerX - size * 0.20f,
                centerY - size * 0.40f,
                centerX + size * 0.20f,
                centerY + size * 0.40f
            );
            canvas.drawOval(rect, stroke);
            canvas.drawLine(centerX - size * 0.40f, centerY, centerX + size * 0.40f, centerY, stroke);
            canvas.drawLine(centerX - size * 0.35f, centerY - size * 0.18f, centerX + size * 0.35f, centerY - size * 0.18f, stroke);
            canvas.drawLine(centerX - size * 0.35f, centerY + size * 0.18f, centerX + size * 0.35f, centerY + size * 0.18f, stroke);
        }

        private void drawBluetooth(Canvas canvas, float centerX, float centerY, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(size * 0.055f);
            stroke.setColor(color);

            path.reset();
            path.moveTo(centerX, centerY - size * 0.45f);
            path.lineTo(centerX + size * 0.26f, centerY - size * 0.19f);
            path.lineTo(centerX - size * 0.22f, centerY + size * 0.20f);
            path.lineTo(centerX + size * 0.26f, centerY + size * 0.45f);
            path.lineTo(centerX + size * 0.26f, centerY + size * 0.19f);
            path.lineTo(centerX - size * 0.22f, centerY - size * 0.20f);
            canvas.drawPath(path, stroke);
        }

        private void drawCheck(Canvas canvas, float centerX, float centerY, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(size * 0.09f);
            stroke.setColor(color);

            path.reset();
            path.moveTo(centerX - size * 0.34f, centerY);
            path.lineTo(centerX - size * 0.08f, centerY + size * 0.27f);
            path.lineTo(centerX + size * 0.40f, centerY - size * 0.32f);
            canvas.drawPath(path, stroke);
        }

        private void drawNavIcon(Canvas canvas, int type, float centerX, float centerY, float size, int color) {
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setStrokeWidth(size * 0.065f);
            stroke.setColor(color);

            if (type == 0) {
                path.reset();
                path.moveTo(centerX - size * 0.40f, centerY - size * 0.05f);
                path.lineTo(centerX, centerY - size * 0.40f);
                path.lineTo(centerX + size * 0.40f, centerY - size * 0.05f);
                canvas.drawPath(path, stroke);
                canvas.drawRect(
                    centerX - size * 0.30f,
                    centerY - size * 0.05f,
                    centerX + size * 0.30f,
                    centerY + size * 0.34f,
                    stroke
                );
            } else if (type == 1) {
                canvas.drawCircle(centerX, centerY, size * 0.39f, stroke);
                canvas.drawLine(centerX, centerY, centerX, centerY - size * 0.22f, stroke);
                canvas.drawLine(centerX, centerY, centerX + size * 0.20f, centerY + size * 0.10f, stroke);
            } else if (type == 2) {
                rect.set(
                    centerX - size * 0.28f,
                    centerY - size * 0.36f,
                    centerX + size * 0.28f,
                    centerY + size * 0.40f
                );
                canvas.drawRoundRect(rect, size * 0.05f, size * 0.05f, stroke);
                canvas.drawLine(centerX - size * 0.12f, centerY - size * 0.45f, centerX + size * 0.12f, centerY - size * 0.45f, stroke);
                for (int row = -1; row <= 1; row++) {
                    canvas.drawLine(
                        centerX - size * 0.13f,
                        centerY + row * size * 0.15f,
                        centerX + size * 0.13f,
                        centerY + row * size * 0.15f,
                        stroke
                    );
                }
            } else {
                canvas.drawCircle(centerX, centerY - size * 0.20f, size * 0.17f, stroke);
                rect.set(
                    centerX - size * 0.32f,
                    centerY + size * 0.02f,
                    centerX + size * 0.32f,
                    centerY + size * 0.45f
                );
                canvas.drawArc(rect, 200f, 140f, false, stroke);
            }
        }

        private void drawText(
            Canvas canvas,
            String value,
            float x,
            float baseline,
            float size,
            int color,
            boolean bold,
            Paint.Align align
        ) {
            textPaint.setShader(null);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(color);
            textPaint.setTextAlign(align);
            textPaint.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
            textPaint.setTextSize(size);
            canvas.drawText(value, x, baseline, textPaint);
        }

        private void drawFittedText(
            Canvas canvas,
            String value,
            float left,
            float right,
            float baseline,
            float maximumSize,
            float minimumSize,
            int color,
            boolean bold,
            Paint.Align align
        ) {
            textPaint.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
            textPaint.setTextSize(maximumSize);
            float allowedWidth = Math.max(1f, right - left);

            while (textPaint.measureText(value) > allowedWidth && textPaint.getTextSize() > minimumSize) {
                textPaint.setTextSize(textPaint.getTextSize() - x(1f));
            }

            float drawX = align == Paint.Align.CENTER ? (left + right) * 0.50f : left;
            textPaint.setShader(null);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(color);
            textPaint.setTextAlign(align);
            canvas.drawText(value, drawX, baseline, textPaint);
        }

        private void setShadow(Paint paint, float radius, int color) {
            paint.setShadowLayer(radius, 0f, radius * 0.40f, color);
        }

        private void clearShadow(Paint paint) {
            paint.clearShadowLayer();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float touchX = event.getX();
            float touchY = event.getY();

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (selectedTab == 0 && doorHit.contains(touchX, touchY)) {
                    startHold();
                    return true;
                }

                if (navHit.contains(touchX, touchY)) {
                    selectNavigation(touchX);
                    return true;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (holding) {
                    cancelHold();
                    return true;
                }
            }

            return true;
        }

        private void selectNavigation(float touchX) {
            float cellWidth = navHit.width() / 4f;
            int index = (int) ((touchX - navHit.left) / cellWidth);
            index = Math.max(0, Math.min(3, index));
            selectedTab = index;
            invalidate();
        }

        private void startHold() {
            if (opened || holding) {
                return;
            }

            holding = true;
            holdProgress = 0f;

            holdAnimator = ValueAnimator.ofFloat(0f, 1f);
            holdAnimator.setDuration(850L);
            holdAnimator.addUpdateListener(animation -> {
                holdProgress = (float) animation.getAnimatedValue();
                invalidate();
            });
            holdAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (holding && holdProgress >= 0.99f) {
                        completeOpen();
                    }
                }
            });
            holdAnimator.start();
        }

        private void cancelHold() {
            holding = false;

            if (holdAnimator != null) {
                holdAnimator.cancel();
                holdAnimator = null;
            }

            holdProgress = 0f;
            invalidate();
        }

        private void completeOpen() {
            holding = false;
            opened = true;
            holdProgress = 1f;
            vibrate();
            invalidate();

            postDelayed(() -> {
                opened = false;
                holdProgress = 0f;
                invalidate();
            }, 1600L);
        }

        private void vibrate() {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

            if (vibrator == null) {
                return;
            }

            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(45L);
            }
        }
    }
}
