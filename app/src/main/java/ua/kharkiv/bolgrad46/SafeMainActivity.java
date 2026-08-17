package ua.kharkiv.bolgrad46;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SafeMainActivity extends Activity {
    private static final int BG = Color.rgb(243, 246, 252);
    private static final int BLUE = Color.rgb(21, 114, 245);
    private static final int BLUE_2 = Color.rgb(0, 88, 230);
    private static final int BLUE_3 = Color.rgb(57, 150, 255);
    private static final int NAVY = Color.rgb(9, 32, 53);
    private static final int NAVY_2 = Color.rgb(16, 45, 67);
    private static final int TEXT = Color.rgb(244, 247, 252);
    private static final int DARK_TEXT = Color.rgb(29, 46, 77);
    private static final int MUTED = Color.rgb(103, 129, 182);
    private static final int GREEN = Color.rgb(20, 207, 119);

    private FrameLayout content;
    private final TextView[] navLabels = new TextView[4];
    private final AppIconView[] navIcons = new AppIconView[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);

        if (Build.VERSION.SDK_INT >= 23) {
            window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }

        if (Build.VERSION.SDK_INT >= 29) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        setContentView(buildShell());
        showPage(0);
    }

    private View buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        ));

        root.addView(bottomNavigation(), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(82)
        ));

        return root;
    }

    private View bottomNavigation() {
        FrameLayout shell = new FrameLayout(this);
        shell.setPadding(dp(14), dp(5), dp(14), dp(8));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(dp(4), dp(5), dp(4), dp(4));
        bar.setBackground(lightCardBackground());
        shell.addView(bar, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        String[] labels = {"Главная", "События", "Заявки", "Профиль"};
        int[] types = {
            AppIconView.HOME,
            AppIconView.CLOCK,
            AppIconView.CLIPBOARD,
            AppIconView.PERSON
        };

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setClickable(true);
            item.setOnClickListener(v -> showPage(index));

            AppIconView icon = new AppIconView(this, types[i]);
            icon.setIconColor(MUTED);
            item.addView(icon, new LinearLayout.LayoutParams(dp(29), dp(29)));

            TextView label = text(labels[i], 12, MUTED, false);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            labelLp.topMargin = dp(2);
            item.addView(label, labelLp);

            bar.addView(item, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            navLabels[i] = label;
            navIcons[i] = icon;
        }

        return shell;
    }

    private void showPage(int index) {
        content.removeAllViews();

        View page;
        if (index == 0) {
            page = homePage();
        } else {
            page = secondaryPage(index);
        }

        content.addView(page, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        for (int i = 0; i < navLabels.length; i++) {
            int color = i == index ? BLUE : MUTED;
            navLabels[i].setTextColor(color);
            navIcons[i].setIconColor(color);
        }
    }

    private View homePage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setClipToPadding(false);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(12), dp(14), dp(12));
        scroll.addView(body, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        body.addView(headerCard(), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(112)
        ));

        addSpace(body, 14);
        body.addView(heroCard(), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(326)
        ));

        addSpace(body, 18);
        DoorOpenCard openCard = new DoorOpenCard(this);
        body.addView(openCard, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(166)
        ));

        addSpace(body, 10);
        return scroll;
    }

    private View headerCard() {
        BlueWaveCard card = new BlueWaveCard(this, 28f);
        card.setPadding(dp(18), dp(12), dp(14), dp(12));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(row, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        AppIconView house = new AppIconView(this, AppIconView.BUILDING);
        house.setIconColor(Color.WHITE);
        row.addView(house, new LinearLayout.LayoutParams(dp(94), dp(84)));

        TextView address = text("Болградская, 46", 27, Color.WHITE, true);
        address.setGravity(Gravity.CENTER);
        address.setSingleLine(true);
        row.addView(address, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        FrameLayout notify = new FrameLayout(this);
        notify.setBackground(glassSquareBackground());
        AppIconView bell = new AppIconView(this, AppIconView.BELL);
        bell.setIconColor(Color.WHITE);
        notify.addView(bell, new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.CENTER));

        View dot = new View(this);
        dot.setBackground(circle(GREEN_BLUE));
        FrameLayout.LayoutParams dotLp = new FrameLayout.LayoutParams(dp(13), dp(13), Gravity.TOP | Gravity.RIGHT);
        dotLp.setMargins(0, dp(14), dp(14), 0);
        notify.addView(dot, dotLp);

        LinearLayout.LayoutParams notifyLp = new LinearLayout.LayoutParams(dp(74), dp(78));
        notifyLp.leftMargin = dp(10);
        row.addView(notify, notifyLp);

        return card;
    }

    private static final int GREEN_BLUE = Color.rgb(67, 197, 251);

    private View heroCard() {
        RoundedFrame hero = new RoundedFrame(this, 28f);
        hero.setBackgroundColor(NAVY);

        ImageView image = new ImageView(this);
        image.setImageResource(getResources().getIdentifier("house46_target", "drawable", getPackageName()));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        hero.addView(image, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout online = pill("Онлайн", GREEN, TEXT);
        FrameLayout.LayoutParams onlineLp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            dp(45),
            Gravity.TOP | Gravity.LEFT
        );
        onlineLp.setMargins(dp(17), dp(16), 0, 0);
        hero.addView(online, onlineLp);

        TemperatureCard inside = new TemperatureCard(this, "В помещении", "22°C", false);
        FrameLayout.LayoutParams insideLp = new FrameLayout.LayoutParams(dp(150), dp(84), Gravity.LEFT | Gravity.BOTTOM);
        insideLp.setMargins(dp(13), 0, 0, dp(84));
        hero.addView(inside, insideLp);

        TemperatureCard outside = new TemperatureCard(this, "На улице", "18°C", true);
        FrameLayout.LayoutParams outsideLp = new FrameLayout.LayoutParams(dp(145), dp(84), Gravity.RIGHT | Gravity.BOTTOM);
        outsideLp.setMargins(0, 0, dp(13), dp(84));
        hero.addView(outside, outsideLp);

        View status = heroStatusBar();
        FrameLayout.LayoutParams statusLp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(76),
            Gravity.BOTTOM
        );
        hero.addView(status, statusLp);

        return hero;
    }

    private View heroStatusBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(12), dp(7), dp(12), dp(7));
        bar.setBackground(translucentStatusBackground());

        bar.addView(statusCell(AppIconView.LOCK, "Замок", "Онлайн", true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        bar.addView(verticalDivider(), new LinearLayout.LayoutParams(dp(1), dp(50)));
        bar.addView(statusCell(AppIconView.GLOBE, "Интернет", "Подключено", true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        bar.addView(verticalDivider(), new LinearLayout.LayoutParams(dp(1), dp(50)));
        bar.addView(statusCell(AppIconView.BLUETOOTH, "Bluetooth", "Подключено", true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        return bar;
    }

    private View statusCell(int iconType, String titleValue, String subValue, boolean ok) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.HORIZONTAL);
        cell.setGravity(Gravity.CENTER);

        AppIconView icon = new AppIconView(this, iconType);
        icon.setIconColor(Color.WHITE);
        cell.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(6), 0, 0, 0);

        TextView title = text(titleValue, 13, Color.WHITE, false);
        TextView sub = text(subValue, 11, ok ? GREEN : MUTED, false);
        texts.addView(title);
        texts.addView(sub);
        cell.addView(texts);

        return cell;
    }

    private View secondaryPage(int index) {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(28), dp(20), dp(20));
        page.setBackgroundColor(BG);

        String[] titles = {"", "События", "Заявки", "Профиль"};
        String[] subs = {
            "",
            "История доступа и уведомлений",
            "Ваши обращения и их статусы",
            "Настройки пользователя"
        };

        TextView title = text(titles[index], 30, DARK_TEXT, true);
        page.addView(title);
        TextView sub = text(subs[index], 16, MUTED, false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subLp.topMargin = dp(5);
        page.addView(sub, subLp);

        View card = new View(this);
        card.setBackground(lightCardBackground());
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(150)
        );
        cardLp.topMargin = dp(22);
        page.addView(card, cardLp);

        return page;
    }

    private LinearLayout pill(String label, int dotColor, int textColor) {
        LinearLayout pill = new LinearLayout(this);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        pill.setPadding(dp(13), 0, dp(15), 0);
        pill.setBackground(pillBackground());

        View dot = new View(this);
        dot.setBackground(circle(dotColor));
        pill.addView(dot, new LinearLayout.LayoutParams(dp(11), dp(11)));

        TextView text = text(label, 14, textColor, false);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textLp.leftMargin = dp(8);
        pill.addView(text, textLp);
        return pill;
    }

    private View verticalDivider() {
        View v = new View(this);
        v.setBackgroundColor(Color.argb(90, 220, 233, 255));
        return v;
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        t.setIncludeFontPadding(false);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }

    private void addSpace(LinearLayout parent, int height) {
        parent.addView(new View(this), new LinearLayout.LayoutParams(dp(1), dp(height)));
    }

    private GradientDrawable lightCardBackground() {
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.rgb(252, 253, 255), Color.rgb(233, 239, 250)}
        );
        bg.setCornerRadius(dp(25));
        bg.setStroke(dp(1), Color.rgb(221, 230, 247));
        return bg;
    }

    private GradientDrawable glassSquareBackground() {
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.argb(235, 82, 157, 255), Color.argb(245, 7, 86, 208)}
        );
        bg.setCornerRadius(dp(24));
        bg.setStroke(dp(1), Color.argb(220, 223, 239, 255));
        return bg;
    }

    private GradientDrawable pillBackground() {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(205, 8, 35, 61));
        bg.setCornerRadius(dp(24));
        return bg;
    }

    private GradientDrawable translucentStatusBackground() {
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.argb(235, 20, 60, 87), Color.argb(248, 9, 32, 52)}
        );
        return bg;
    }

    private GradientDrawable circle(int color) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        return bg;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class RoundedFrame extends FrameLayout {
        private final Paint mask = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path clip = new Path();
        private final float radius;

        RoundedFrame(Context context, float radiusDp) {
            super(context);
            radius = radiusDp * getResources().getDisplayMetrics().density;
            setWillNotDraw(false);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            clip.reset();
            clip.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), radius, radius, Path.Direction.CW);
            int save = canvas.save();
            canvas.clipPath(clip);
            super.dispatchDraw(canvas);
            canvas.restoreToCount(save);
        }
    }

    private static final class BlueWaveCard extends FrameLayout {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint wave = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint highlight = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final float radius;
        private final float density;

        BlueWaveCard(Context context, float radiusDp) {
            super(context);
            density = getResources().getDisplayMetrics().density;
            radius = radiusDp * density;
            setWillNotDraw(false);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setClipChildren(false);
            setClipToPadding(false);
            paint.setShadowLayer(14f * density, 0f, 6f * density, Color.argb(95, 48, 113, 229));
            wave.setColor(Color.argb(75, 115, 188, 255));
            highlight.setStyle(Paint.Style.STROKE);
            highlight.setStrokeWidth(1.1f * density);
            highlight.setColor(Color.argb(200, 220, 241, 255));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            RectF r = new RectF(dpLocal(3), dpLocal(3), getWidth() - dpLocal(3), getHeight() - dpLocal(3));
            paint.setShader(new LinearGradient(0, 0, getWidth(), getHeight(),
                new int[]{Color.rgb(74, 163, 255), Color.rgb(0, 96, 238), Color.rgb(12, 123, 246)},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(r, radius, radius, paint);

            path.reset();
            path.moveTo(0, getHeight() * 0.72f);
            path.cubicTo(getWidth() * 0.26f, getHeight() * 0.93f, getWidth() * 0.56f, getHeight() * 0.47f, getWidth(), getHeight() * 0.08f);
            path.lineTo(getWidth(), getHeight());
            path.lineTo(0, getHeight());
            path.close();
            canvas.drawPath(path, wave);

            canvas.drawRoundRect(r, radius, radius, highlight);
            super.onDraw(canvas);
        }

        private float dpLocal(float v) {
            return v * density;
        }
    }

    private static final class TemperatureCard extends LinearLayout {
        TemperatureCard(Context context, String titleValue, String value, boolean outside) {
            super(context);
            float density = getResources().getDisplayMetrics().density;
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER);
            setPadding(Math.round(11 * density), Math.round(7 * density), Math.round(11 * density), Math.round(7 * density));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.argb(226, 34, 62, 79), Color.argb(239, 13, 37, 55)}
            );
            bg.setCornerRadius(22 * density);
            bg.setStroke(Math.max(1, Math.round(density)), Color.argb(210, 229, 239, 250));
            setBackground(bg);

            AppIconView icon = new AppIconView(context, outside ? AppIconView.THERMOMETER_CLOUD : AppIconView.THERMOMETER_HOME);
            icon.setIconColor(Color.WHITE);
            addView(icon, new LinearLayout.LayoutParams(Math.round(43 * density), Math.round(43 * density)));

            LinearLayout texts = new LinearLayout(context);
            texts.setOrientation(VERTICAL);
            texts.setPadding(Math.round(7 * density), 0, 0, 0);

            TextView title = new TextView(context);
            title.setText(titleValue);
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            title.setIncludeFontPadding(false);
            texts.addView(title);

            TextView temp = new TextView(context);
            temp.setText(value);
            temp.setTextColor(Color.WHITE);
            temp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            temp.setTypeface(Typeface.DEFAULT_BOLD);
            temp.setIncludeFontPadding(false);
            texts.addView(temp);

            addView(texts);
        }
    }

    private static final class DoorOpenCard extends View {
        private static final long HOLD_MS = 900;
        private final float density;
        private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint wave = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint icon = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint title = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint sub = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path lock = new Path();
        private final Path wavePath = new Path();
        private float progress;
        private boolean opened;
        private ValueAnimator holdAnimator;

        DoorOpenCard(Context context) {
            super(context);
            density = getResources().getDisplayMetrics().density;
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setClickable(true);

            bg.setShadowLayer(dpLocal(14), 0f, dpLocal(6), Color.argb(95, 48, 113, 229));
            wave.setColor(Color.argb(70, 123, 190, 255));

            ring.setStyle(Paint.Style.STROKE);
            ring.setStrokeWidth(dpLocal(1.2f));
            ring.setColor(Color.WHITE);
            ring.setShadowLayer(dpLocal(10), 0f, 0f, Color.WHITE);

            progressPaint.setStyle(Paint.Style.STROKE);
            progressPaint.setStrokeCap(Paint.Cap.ROUND);
            progressPaint.setStrokeWidth(dpLocal(5));
            progressPaint.setColor(Color.WHITE);

            icon.setStyle(Paint.Style.STROKE);
            icon.setStrokeWidth(dpLocal(3));
            icon.setStrokeCap(Paint.Cap.ROUND);
            icon.setStrokeJoin(Paint.Join.ROUND);
            icon.setColor(Color.WHITE);

            title.setColor(Color.WHITE);
            title.setTextSize(sp(28));
            title.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));

            sub.setColor(Color.rgb(165, 195, 244));
            sub.setTextSize(sp(15));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            RectF card = new RectF(dpLocal(3), dpLocal(3), getWidth() - dpLocal(3), getHeight() - dpLocal(3));
            bg.setShader(new LinearGradient(0, 0, getWidth(), getHeight(),
                new int[]{Color.rgb(70, 158, 255), Color.rgb(0, 91, 231), Color.rgb(16, 125, 246)},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(card, dpLocal(28), dpLocal(28), bg);

            wavePath.reset();
            wavePath.moveTo(getWidth() * 0.6f, getHeight());
            wavePath.cubicTo(getWidth() * 0.76f, getHeight() * 0.85f, getWidth() * 0.88f, getHeight() * 0.52f, getWidth(), getHeight() * 0.2f);
            wavePath.lineTo(getWidth(), getHeight());
            wavePath.close();
            canvas.drawPath(wavePath, wave);

            float cx = dpLocal(89);
            float cy = getHeight() / 2f;
            float radius = dpLocal(61);
            canvas.drawCircle(cx, cy, radius, ring);

            if (progress > 0f && !opened) {
                RectF arc = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
                canvas.drawArc(arc, -90, 360f * progress, false, progressPaint);
            }

            drawLock(canvas, cx, cy);

            if (opened) {
                title.setTextSize(sp(27));
                canvas.drawText("Дверь открыта", dpLocal(170), cy - dpLocal(5), title);
                canvas.drawText("Команда выполнена", dpLocal(172), cy + dpLocal(27), sub);
            } else {
                title.setTextSize(sp(29));
                canvas.drawText("Открыть дверь", dpLocal(170), cy - dpLocal(4), title);
                canvas.drawText("Удерживайте для открытия", dpLocal(172), cy + dpLocal(28), sub);
            }
        }

        private void drawLock(Canvas canvas, float cx, float cy) {
            RectF shackle = new RectF(cx - dpLocal(23), cy - dpLocal(39), cx + dpLocal(23), cy + dpLocal(8));
            canvas.drawArc(shackle, 180, 180, false, icon);
            RectF body = new RectF(cx - dpLocal(30), cy - dpLocal(10), cx + dpLocal(30), cy + dpLocal(39));
            canvas.drawRoundRect(body, dpLocal(4), dpLocal(4), icon);
            canvas.drawCircle(cx, cy + dpLocal(8), dpLocal(4), icon);
            canvas.drawLine(cx, cy + dpLocal(12), cx, cy + dpLocal(22), icon);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (opened) {
                return true;
            }

            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                getParent().requestDisallowInterceptTouchEvent(true);
                startHold();
                return true;
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                getParent().requestDisallowInterceptTouchEvent(false);
                if (!opened) {
                    cancelHold();
                }
                return true;
            }

            return true;
        }

        private void startHold() {
            cancelAnimatorOnly();
            progress = 0f;
            holdAnimator = ValueAnimator.ofFloat(0f, 1f);
            holdAnimator.setDuration(HOLD_MS);
            holdAnimator.addUpdateListener(a -> {
                progress = (float) a.getAnimatedValue();
                invalidate();
            });
            holdAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (progress >= 0.99f) {
                        openDoor();
                    }
                }
            });
            holdAnimator.start();
        }

        private void cancelHold() {
            cancelAnimatorOnly();
            ValueAnimator back = ValueAnimator.ofFloat(progress, 0f);
            back.setDuration(140);
            back.addUpdateListener(a -> {
                progress = (float) a.getAnimatedValue();
                invalidate();
            });
            back.start();
        }

        private void cancelAnimatorOnly() {
            if (holdAnimator != null) {
                holdAnimator.removeAllListeners();
                holdAnimator.cancel();
                holdAnimator = null;
            }
        }

        private void openDoor() {
            opened = true;
            progress = 1f;
            vibrate();
            invalidate();
            postDelayed(() -> {
                opened = false;
                progress = 0f;
                invalidate();
            }, 1600);
        }

        private void vibrate() {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(45);
                }
            }
        }

        private float dpLocal(float v) {
            return v * density;
        }

        private float sp(float v) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
        }
    }

    private static final class AppIconView extends View {
        static final int BELL = 1;
        static final int BUILDING = 2;
        static final int LOCK = 3;
        static final int GLOBE = 4;
        static final int BLUETOOTH = 5;
        static final int HOME = 6;
        static final int CLOCK = 7;
        static final int CLIPBOARD = 8;
        static final int PERSON = 9;
        static final int THERMOMETER_HOME = 10;
        static final int THERMOMETER_CLOUD = 11;

        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final int type;
        private int iconColor = MUTED;

        AppIconView(Context context, int type) {
            super(context);
            this.type = type;
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            fill.setStyle(Paint.Style.FILL);
        }

        void setIconColor(int color) {
            iconColor = color;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float cy = h / 2f;
            float u = Math.min(w, h) / 32f;
            stroke.setColor(iconColor);
            fill.setColor(iconColor);
            stroke.setStrokeWidth(Math.max(2f, u * 1.8f));

            if (type == BELL) {
                RectF bell = new RectF(cx - 8*u, cy - 10*u, cx + 8*u, cy + 7*u);
                canvas.drawArc(bell, 195, 150, false, stroke);
                canvas.drawLine(cx-8*u, cy+1*u, cx-8*u, cy+6*u, stroke);
                canvas.drawLine(cx+8*u, cy+1*u, cx+8*u, cy+6*u, stroke);
                canvas.drawLine(cx-8*u, cy+6*u, cx+8*u, cy+6*u, stroke);
                canvas.drawCircle(cx, cy+10*u, 1.8f*u, fill);
            } else if (type == BUILDING) {
                path.reset();
                path.moveTo(cx-11*u, cy-5*u);
                path.lineTo(cx, cy-13*u);
                path.lineTo(cx+11*u, cy-5*u);
                path.moveTo(cx-8*u, cy-6*u);
                path.lineTo(cx-8*u, cy+12*u);
                path.lineTo(cx+8*u, cy+12*u);
                path.lineTo(cx+8*u, cy-6*u);
                canvas.drawPath(path, stroke);
                for (int iy=-1; iy<=1; iy++) {
                    for (int ix=-1; ix<=1; ix+=2) {
                        RectF window = new RectF(cx+ix*4*u-1.6f*u, cy+iy*5*u-1.6f*u, cx+ix*4*u+1.6f*u, cy+iy*5*u+1.6f*u);
                        canvas.drawRect(window, stroke);
                    }
                }
                canvas.drawRect(cx-2*u, cy+5*u, cx+2*u, cy+12*u, stroke);
                canvas.drawLine(cx-14*u, cy+13*u, cx+14*u, cy+13*u, stroke);
            } else if (type == LOCK) {
                RectF sh = new RectF(cx-7*u, cy-12*u, cx+7*u, cy+2*u);
                canvas.drawArc(sh, 180, 180, false, stroke);
                RectF body = new RectF(cx-10*u, cy-4*u, cx+10*u, cy+12*u);
                canvas.drawRoundRect(body, 2*u, 2*u, stroke);
                canvas.drawCircle(cx, cy+3*u, 1.7f*u, stroke);
                canvas.drawLine(cx, cy+5*u, cx, cy+9*u, stroke);
            } else if (type == GLOBE) {
                canvas.drawCircle(cx, cy, 12*u, stroke);
                canvas.drawOval(new RectF(cx-6*u, cy-12*u, cx+6*u, cy+12*u), stroke);
                canvas.drawLine(cx-12*u, cy, cx+12*u, cy, stroke);
                canvas.drawArc(new RectF(cx-12*u, cy-7*u, cx+12*u, cy+7*u), 180, 180, false, stroke);
                canvas.drawArc(new RectF(cx-12*u, cy-7*u, cx+12*u, cy+7*u), 0, 180, false, stroke);
            } else if (type == BLUETOOTH) {
                path.reset();
                path.moveTo(cx, cy-13*u);
                path.lineTo(cx+7*u, cy-6*u);
                path.lineTo(cx-6*u, cy+7*u);
                path.moveTo(cx, cy+13*u);
                path.lineTo(cx+7*u, cy+6*u);
                path.lineTo(cx-6*u, cy-7*u);
                path.moveTo(cx, cy-13*u);
                path.lineTo(cx, cy+13*u);
                canvas.drawPath(path, stroke);
            } else if (type == HOME) {
                path.reset();
                path.moveTo(cx-11*u, cy-1*u);
                path.lineTo(cx, cy-11*u);
                path.lineTo(cx+11*u, cy-1*u);
                path.moveTo(cx-8*u, cy-3*u);
                path.lineTo(cx-8*u, cy+11*u);
                path.lineTo(cx+8*u, cy+11*u);
                path.lineTo(cx+8*u, cy-3*u);
                canvas.drawPath(path, stroke);
                canvas.drawRect(cx-2*u, cy+4*u, cx+2*u, cy+11*u, stroke);
            } else if (type == CLOCK) {
                canvas.drawCircle(cx, cy, 11*u, stroke);
                canvas.drawLine(cx, cy, cx, cy-7*u, stroke);
                canvas.drawLine(cx, cy, cx+6*u, cy+3*u, stroke);
            } else if (type == CLIPBOARD) {
                RectF box = new RectF(cx-8*u, cy-9*u, cx+8*u, cy+12*u);
                canvas.drawRoundRect(box, 2*u, 2*u, stroke);
                RectF clip = new RectF(cx-4*u, cy-12*u, cx+4*u, cy-7*u);
                canvas.drawRoundRect(clip, 1.5f*u, 1.5f*u, stroke);
                canvas.drawLine(cx-4*u, cy-2*u, cx+4*u, cy-2*u, stroke);
                canvas.drawLine(cx-4*u, cy+3*u, cx+4*u, cy+3*u, stroke);
                canvas.drawLine(cx-4*u, cy+8*u, cx+4*u, cy+8*u, stroke);
            } else if (type == PERSON) {
                canvas.drawCircle(cx, cy-6*u, 5*u, stroke);
                canvas.drawArc(new RectF(cx-9*u, cy+1*u, cx+9*u, cy+16*u), 190, 160, false, stroke);
            } else if (type == THERMOMETER_HOME || type == THERMOMETER_CLOUD) {
                canvas.drawCircle(cx-4*u, cy+7*u, 4*u, stroke);
                canvas.drawRoundRect(new RectF(cx-6*u, cy-12*u, cx-2*u, cy+8*u), 2*u, 2*u, stroke);
                canvas.drawLine(cx-4*u, cy-5*u, cx-4*u, cy+7*u, stroke);
                if (type == THERMOMETER_HOME) {
                    path.reset();
                    path.moveTo(cx+1*u, cy-4*u);
                    path.lineTo(cx+6*u, cy-9*u);
                    path.lineTo(cx+11*u, cy-4*u);
                    canvas.drawPath(path, stroke);
                } else {
                    canvas.drawCircle(cx+7*u, cy-3*u, 4*u, stroke);
                }
            }
        }
    }
}
