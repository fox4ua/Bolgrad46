package ua.kharkiv.bolgrad46;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(4, 16, 31);
    private static final int SURFACE = Color.rgb(6, 24, 45);
    private static final int SURFACE_2 = Color.rgb(7, 30, 55);
    private static final int BORDER = Color.rgb(20, 48, 78);
    private static final int BLUE = Color.rgb(0, 119, 255);
    private static final int BLUE_SOFT = Color.rgb(38, 119, 225);
    private static final int TEXT = Color.rgb(247, 249, 253);
    private static final int MUTED = Color.rgb(134, 157, 197);
    private static final int GREEN = Color.rgb(43, 211, 119);

    private LinearLayout root;
    private FrameLayout content;
    private GlowCard nav;
    private final TextView[] navLabels = new TextView[4];
    private final IconView[] navIcons = new IconView[4];

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
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        } else {
            window.getDecorView().setSystemUiVisibility(0);
        }

        buildShell();
        setContentView(root);
        applyInsets();
        showPage(0);
    }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setClipChildren(false);
        root.setClipToPadding(false);

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        ));

        nav = new GlowCard(this, SURFACE, BLUE, 24f, 7f);
        nav.setPadding(dp(8), dp(7), dp(8), dp(7));

        LinearLayout navRow = row();
        navRow.setGravity(Gravity.CENTER);
        nav.addView(navRow, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        String[] names = {"Главная", "События", "Заявки", "Профиль"};
        int[] iconTypes = {
            IconView.HOME,
            IconView.CLOCK,
            IconView.CLIPBOARD,
            IconView.PERSON
        };

        for (int i = 0; i < names.length; i++) {
            final int index = i;

            LinearLayout item = col();
            item.setGravity(Gravity.CENTER);
            item.setClickable(true);
            item.setFocusable(true);
            item.setOnClickListener(v -> showPage(index));

            IconView icon = new IconView(this, iconTypes[i]);
            icon.setIconColor(MUTED);
            item.addView(icon, new LinearLayout.LayoutParams(dp(30), dp(30)));

            TextView label = text(names[i], 12, MUTED, false);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            labelLp.topMargin = dp(3);
            item.addView(label, labelLp);

            navRow.addView(item, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            navLabels[i] = label;
            navIcons[i] = icon;
        }

        LinearLayout.LayoutParams navLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(82)
        );
        navLp.setMargins(dp(14), dp(5), dp(14), dp(8));
        root.addView(nav, navLp);
    }

    private void applyInsets() {
        root.setOnApplyWindowInsetsListener((v, insets) -> {
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

            root.setPadding(0, top, 0, bottom);
            return insets;
        });

        root.requestApplyInsets();
    }

    private void showPage(int index) {
        content.removeAllViews();

        View next;
        if (index == 0) {
            next = homePage();
        } else if (index == 1) {
            next = eventsPage();
        } else if (index == 2) {
            next = requestsPage();
        } else {
            next = profilePage();
        }

        next.setAlpha(0f);
        content.addView(next, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        next.animate().alpha(1f).setDuration(120).start();

        for (int i = 0; i < navLabels.length; i++) {
            int color = i == index ? BLUE : MUTED;
            navLabels[i].setTextColor(color);
            navIcons[i].setIconColor(color);
        }
    }

    private View homePage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setClipToPadding(false);

        LinearLayout body = col();
        body.setPadding(dp(18), dp(12), dp(18), dp(18));
        scroll.addView(body);

        body.addView(homeHeader(), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(94)
        ));

        addSpace(body, 12);
        body.addView(heroCard());
        addSpace(body, 12);

        SwipeControl swipe = new SwipeControl(this);
        swipe.setOnOpened(() ->
            Toast.makeText(this, "Демо: команда открытия двери", Toast.LENGTH_SHORT).show()
        );
        body.addView(swipe, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(112)
        ));

        controllerOnline(body);

        body.addView(systemCard(), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(82)
        ));

        addSpace(body, 12);
        body.addView(recentEventsCard());

        return scroll;
    }

    private View homeHeader() {
        GlowCard card = new GlowCard(this, SURFACE, BLUE, 24f, 8f);
        card.setPadding(dp(20), dp(13), dp(14), dp(13));

        LinearLayout row = row();
        row.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(row, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout titleBlock = col();
        LinearLayout titleRow = row();
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = text("Мой дом", 31, TEXT, true);
        titleRow.addView(title);

        IconView down = new IconView(this, IconView.CHEVRON_DOWN);
        down.setIconColor(TEXT);
        LinearLayout.LayoutParams downLp = new LinearLayout.LayoutParams(dp(24), dp(24));
        downLp.leftMargin = dp(6);
        titleRow.addView(down, downLp);

        titleBlock.addView(titleRow);
        TextView address = text("Болградская, 46", 16, MUTED, false);
        LinearLayout.LayoutParams addressLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        addressLp.topMargin = dp(3);
        titleBlock.addView(address, addressLp);

        row.addView(titleBlock, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        GlowCard bellCard = new GlowCard(this, SURFACE_2, BLUE_SOFT, 18f, 4f);
        bellCard.setClickable(true);
        bellCard.setFocusable(true);
        bellCard.setOnClickListener(v ->
            Toast.makeText(this, "Уведомлений пока нет", Toast.LENGTH_SHORT).show()
        );

        IconView bell = new IconView(this, IconView.BELL);
        bell.setIconColor(TEXT);
        bellCard.addView(bell, new FrameLayout.LayoutParams(dp(32), dp(32), Gravity.CENTER));

        View dot = new View(this);
        dot.setBackground(circleDrawable(BLUE));
        FrameLayout.LayoutParams dotLp = new FrameLayout.LayoutParams(dp(10), dp(10), Gravity.TOP | Gravity.RIGHT);
        dotLp.topMargin = dp(9);
        dotLp.rightMargin = dp(9);
        bellCard.addView(dot, dotLp);

        row.addView(bellCard, new LinearLayout.LayoutParams(dp(62), dp(62)));

        return card;
    }

    private View heroCard() {
        RatioFrame frame = new RatioFrame(this, 793f / 367f);
        GradientDrawable clip = roundedGradient(
            new int[]{Color.rgb(8, 25, 45), Color.rgb(5, 18, 34)},
            24,
            BORDER,
            1
        );
        frame.setBackground(clip);
        frame.setClipToOutline(true);

        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.house46_target);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        frame.addView(image, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        return frame;
    }

    private void controllerOnline(LinearLayout body) {
        LinearLayout line = row();
        line.setGravity(Gravity.CENTER);
        line.setPadding(0, dp(8), 0, dp(8));

        View dot = new View(this);
        dot.setBackground(circleDrawable(GREEN));
        line.addView(dot, new LinearLayout.LayoutParams(dp(9), dp(9)));

        TextView label = text("Контроллер онлайн", 14, MUTED, false);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        labelLp.leftMargin = dp(8);
        line.addView(label, labelLp);

        body.addView(line, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(36)
        ));
    }

    private View systemCard() {
        LinearLayout card = row();
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(10), dp(14), dp(10));
        card.setBackground(cardBackground(SURFACE, BORDER, 22, 1));

        FrameLayout shieldBox = new FrameLayout(this);
        shieldBox.setBackground(cardBackground(Color.rgb(7, 48, 38), GREEN, 18, 1));

        IconView shield = new IconView(this, IconView.SHIELD_CHECK);
        shield.setIconColor(GREEN);
        shieldBox.addView(shield, new FrameLayout.LayoutParams(dp(38), dp(38), Gravity.CENTER));
        card.addView(shieldBox, new LinearLayout.LayoutParams(dp(56), dp(56)));

        LinearLayout textBlock = col();
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textLp.leftMargin = dp(14);

        TextView title = text("Система работает", 18, TEXT, false);
        title.setMaxLines(1);
        textBlock.addView(title);

        TextView subtitle = text("Все сервисы активны", 14, MUTED, false);
        subtitle.setMaxLines(1);
        LinearLayout.LayoutParams subtitleLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleLp.topMargin = dp(4);
        textBlock.addView(subtitle, subtitleLp);

        card.addView(textBlock, textLp);

        IconView right = new IconView(this, IconView.CHEVRON_RIGHT);
        right.setIconColor(MUTED);
        card.addView(right, new LinearLayout.LayoutParams(dp(26), dp(26)));

        return card;
    }

    private View recentEventsCard() {
        LinearLayout card = col();
        card.setPadding(dp(15), dp(13), dp(15), dp(6));
        card.setBackground(cardBackground(SURFACE, BORDER, 22, 1));

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = text("Последние события", 18, TEXT, false);
        title.setMaxLines(1);
        header.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView all = text("Все события", 14, BLUE, false);
        all.setClickable(true);
        all.setOnClickListener(v -> showPage(1));
        header.addView(all);

        IconView allArrow = new IconView(this, IconView.CHEVRON_RIGHT);
        allArrow.setIconColor(BLUE);
        allArrow.setOnClickListener(v -> showPage(1));
        LinearLayout.LayoutParams arrowLp = new LinearLayout.LayoutParams(dp(22), dp(22));
        arrowLp.leftMargin = dp(2);
        header.addView(allArrow, arrowLp);

        card.addView(header, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(34)
        ));

        card.addView(divider(0));
        card.addView(eventRow(IconView.DOOR, BLUE, "Дверь открыта", "Сегодня, 08:37"));
        card.addView(divider(62));
        card.addView(eventRow(IconView.PERSON, GREEN, "Вход по коду", "Сегодня, 08:31"));

        return card;
    }

    private View eventRow(int iconType, int color, String titleText, String subtitleText) {
        LinearLayout row = row();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(9), 0, dp(9));
        row.setMinimumHeight(dp(72));

        FrameLayout iconBox = new FrameLayout(this);
        iconBox.setBackground(cardBackground(alpha(color, 25), alpha(color, 70), 14, 1));

        IconView icon = new IconView(this, iconType);
        icon.setIconColor(color);
        iconBox.addView(icon, new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER));
        row.addView(iconBox, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout texts = col();
        LinearLayout.LayoutParams textsLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textsLp.leftMargin = dp(14);

        TextView title = text(titleText, 17, TEXT, false);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        texts.addView(title);

        TextView subtitle = text(subtitleText, 14, MUTED, false);
        subtitle.setSingleLine(true);
        subtitle.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams subtitleLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleLp.topMargin = dp(3);
        texts.addView(subtitle, subtitleLp);

        row.addView(texts, textsLp);

        IconView arrow = new IconView(this, IconView.CHEVRON_RIGHT);
        arrow.setIconColor(MUTED);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), dp(24)));

        return row;
    }

    private View eventsPage() {
        LinearLayout body = standardPage("События", "История дома");

        LinearLayout card = col();
        card.setPadding(dp(15), dp(6), dp(15), dp(6));
        card.setBackground(cardBackground(SURFACE, BORDER, 22, 1));

        card.addView(eventRow(IconView.DOOR, BLUE, "Дверь открыта", "Сегодня, 08:37"));
        card.addView(divider(62));
        card.addView(eventRow(IconView.PERSON, GREEN, "Вход по коду", "Сегодня, 08:31"));
        card.addView(divider(62));
        card.addView(eventRow(IconView.DOOR, BLUE, "Дверь закрыта", "Сегодня, 08:30"));
        card.addView(divider(62));
        card.addView(eventRow(IconView.GEAR, BLUE, "Плановое обслуживание", "Вчера, 16:42"));

        body.addView(card);
        return wrap(body);
    }

    private View requestsPage() {
        LinearLayout body = standardPage("Заявки", "Обращения в службу дома");

        TextView action = text("Оставить заявку", 16, Color.WHITE, true);
        action.setGravity(Gravity.CENTER);
        action.setBackground(cardBackground(BLUE, BLUE, 18, 0));
        action.setClickable(true);
        action.setOnClickListener(v ->
            Toast.makeText(this, "Форма заявки будет подключена позже", Toast.LENGTH_SHORT).show()
        );
        body.addView(action, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(52)
        ));

        addSpace(body, 12);

        LinearLayout card = col();
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(cardBackground(SURFACE, BORDER, 22, 1));
        card.addView(text("Последняя заявка", 13, MUTED, false));

        TextView issue = text("Не работает освещение у входа", 17, TEXT, false);
        issue.setMaxLines(2);
        LinearLayout.LayoutParams issueLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        issueLp.topMargin = dp(5);
        card.addView(issue, issueLp);

        TextView state = text("●  Принято в работу", 13, GREEN, false);
        LinearLayout.LayoutParams stateLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        stateLp.topMargin = dp(8);
        card.addView(state, stateLp);

        body.addView(card);
        return wrap(body);
    }

    private View profilePage() {
        LinearLayout body = standardPage("Профиль", "Настройки пользователя");

        LinearLayout card = col();
        card.setPadding(dp(15), dp(4), dp(15), dp(4));
        card.setBackground(cardBackground(SURFACE, BORDER, 22, 1));

        card.addView(settingRow("Уведомления"));
        card.addView(divider(0));
        card.addView(settingRow("Настройки приложения"));
        card.addView(divider(0));
        card.addView(settingRow("Безопасность"));

        body.addView(card);
        return wrap(body);
    }

    private LinearLayout standardPage(String title, String subtitle) {
        LinearLayout body = col();
        body.setPadding(dp(18), dp(12), dp(18), dp(18));
        body.addView(sectionHeader(title, subtitle), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(94)
        ));
        addSpace(body, 12);
        return body;
    }

    private View sectionHeader(String title, String subtitle) {
        GlowCard card = new GlowCard(this, SURFACE, BLUE, 24f, 7f);
        card.setPadding(dp(20), dp(13), dp(18), dp(13));

        LinearLayout block = col();
        block.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(block, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        block.addView(text(title, 28, TEXT, true));
        TextView sub = text(subtitle, 15, MUTED, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.topMargin = dp(4);
        block.addView(sub, lp);

        return card;
    }

    private View settingRow(String label) {
        LinearLayout row = row();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(11), 0, dp(11));
        row.setClickable(true);
        row.setOnClickListener(v ->
            Toast.makeText(this, label + ": раздел в разработке", Toast.LENGTH_SHORT).show()
        );

        TextView text = text(label, 16, TEXT, false);
        row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        IconView arrow = new IconView(this, IconView.CHEVRON_RIGHT);
        arrow.setIconColor(MUTED);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), dp(24)));

        return row;
    }

    private ScrollView wrap(LinearLayout body) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.addView(body);
        return scroll;
    }

    private View divider(int leftDp) {
        View line = new View(this);
        line.setBackgroundColor(Color.rgb(18, 45, 72));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(1)
        );
        lp.leftMargin = dp(leftDp);
        line.setLayoutParams(lp);
        return line;
    }

    private LinearLayout row() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private LinearLayout col() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView text(String value, float sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        view.setIncludeFontPadding(false);
        view.setTypeface(android.graphics.Typeface.create(
            "sans-serif",
            bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL
        ));
        return view;
    }

    private void addSpace(LinearLayout parent, int heightDp) {
        parent.addView(new View(this), new LinearLayout.LayoutParams(dp(1), dp(heightDp)));
    }

    private GradientDrawable cardBackground(int fill, int stroke, float radiusDp, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{fill, darken(fill, 0.94f)}
        );
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), stroke);
        }
        return drawable;
    }

    private GradientDrawable roundedGradient(int[] colors, float radiusDp, int stroke, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            colors
        );
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), stroke);
        }
        return drawable;
    }

    private GradientDrawable circleDrawable(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        return drawable;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static int alpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int darken(int color, float factor) {
        return Color.rgb(
            Math.round(Color.red(color) * factor),
            Math.round(Color.green(color) * factor),
            Math.round(Color.blue(color) * factor)
        );
    }

    private static final class RatioFrame extends FrameLayout {
        private final float ratio;

        RatioFrame(Context context, float ratio) {
            super(context);
            this.ratio = ratio;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = Math.round(width / ratio);
            int exactHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, exactHeight);
        }
    }

    private static class GlowCard extends FrameLayout {
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float density;
        private final float radius;
        private final float glow;

        GlowCard(Context context, int fill, int stroke, float radiusDp, float glowDp) {
            super(context);
            density = getResources().getDisplayMetrics().density;
            radius = radiusDp * density;
            glow = glowDp * density;

            setWillNotDraw(false);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setClipChildren(false);
            setClipToPadding(false);

            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(fill);
            fillPaint.setShadowLayer(glow, 0f, 0f, alpha(stroke, 125));

            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(Math.max(1f, density));
            strokePaint.setColor(stroke);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float inset = glow + density;
            RectF rect = new RectF(inset, inset, getWidth() - inset, getHeight() - inset);
            canvas.drawRoundRect(rect, radius, radius, fillPaint);
            canvas.drawRoundRect(rect, radius, radius, strokePaint);
            super.onDraw(canvas);
        }
    }

    private static final class SwipeControl extends GlowCard {
        private final float density;
        private final FrameLayout thumb;
        private final TextView title;
        private final TextView subtitle;
        private float drag;
        private float maxDrag;
        private boolean dragging;
        private boolean opened;
        private ValueAnimator animator;
        private Runnable openedCallback;

        SwipeControl(Context context) {
            super(context, SURFACE, BLUE, 26f, 7f);
            density = getResources().getDisplayMetrics().density;

            setPadding(dpLocal(14), dpLocal(12), dpLocal(18), dpLocal(12));
            setClickable(true);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dpLocal(92), 0, 0, 0);
            addView(row, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ));

            TextView arrows = new TextView(context);
            arrows.setText("›››");
            arrows.setTextColor(Color.rgb(0, 102, 224));
            arrows.setTextSize(TypedValue.COMPLEX_UNIT_SP, 39);
            arrows.setIncludeFontPadding(false);
            arrows.setGravity(Gravity.CENTER);
            row.addView(arrows, new LinearLayout.LayoutParams(dpLocal(70), ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout texts = new LinearLayout(context);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setGravity(Gravity.CENTER_VERTICAL);

            title = new TextView(context);
            title.setText("Проведите, чтобы\nоткрыть");
            title.setTextColor(TEXT);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            title.setIncludeFontPadding(false);
            title.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
            title.setMaxLines(2);
            texts.addView(title);

            subtitle = new TextView(context);
            subtitle.setText("Потяните вправо");
            subtitle.setTextColor(MUTED);
            subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            subtitle.setIncludeFontPadding(false);
            LinearLayout.LayoutParams subtitleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            subtitleLp.topMargin = dpLocal(4);
            texts.addView(subtitle, subtitleLp);

            row.addView(texts, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ));

            thumb = new FrameLayout(context);
            thumb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            thumb.setBackground(new ThumbDrawable(density));

            IconView door = new IconView(context, IconView.DOOR);
            door.setIconColor(Color.WHITE);
            thumb.addView(door, new FrameLayout.LayoutParams(dpLocal(40), dpLocal(40), Gravity.CENTER));

            FrameLayout.LayoutParams thumbLp = new FrameLayout.LayoutParams(dpLocal(82), dpLocal(82), Gravity.LEFT | Gravity.CENTER_VERTICAL);
            thumbLp.leftMargin = dpLocal(9);
            addView(thumb, thumbLp);

            setOnTouchListener((v, event) -> handleTouch(event));
        }

        void setOnOpened(Runnable callback) {
            openedCallback = callback;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            maxDrag = Math.max(0f, w - dpLocal(82) - dpLocal(30));
        }

        private boolean handleTouch(MotionEvent event) {
            float x = event.getX();
            float thumbLeft = dpLocal(9) + drag;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (x >= thumbLeft - dpLocal(8) && x <= thumbLeft + dpLocal(90)) {
                    dragging = true;
                    return true;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE && dragging) {
                drag = Math.max(0f, Math.min(maxDrag, x - dpLocal(50)));
                thumb.setTranslationX(drag);
                return true;
            }

            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && dragging) {
                dragging = false;
                boolean success = drag >= maxDrag * 0.70f;
                animateThumb(success ? maxDrag : 0f, success);
                return true;
            }

            return true;
        }

        private void animateThumb(float target, boolean success) {
            if (animator != null) {
                animator.cancel();
            }

            animator = ValueAnimator.ofFloat(drag, target);
            animator.setDuration(220);
            animator.addUpdateListener(animation -> {
                drag = (float) animation.getAnimatedValue();
                thumb.setTranslationX(drag);
            });
            animator.start();

            if (success && !opened) {
                opened = true;
                title.setText("Дверь открыта");
                title.setTextColor(GREEN);
                title.setMaxLines(1);
                subtitle.setText("Команда выполнена");

                if (openedCallback != null) {
                    openedCallback.run();
                }

                postDelayed(() -> {
                    opened = false;
                    title.setText("Проведите, чтобы\nоткрыть");
                    title.setTextColor(TEXT);
                    title.setMaxLines(2);
                    subtitle.setText("Потяните вправо");
                    animateThumb(0f, false);
                }, 1300);
            }
        }

        private int dpLocal(float value) {
            return Math.round(value * density);
        }

        private static final class ThumbDrawable extends android.graphics.drawable.Drawable {
            private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final float density;

            ThumbDrawable(float density) {
                this.density = density;
                fill.setColor(Color.rgb(4, 37, 74));
                fill.setShadowLayer(12f * density, 0f, 0f, Color.rgb(0, 119, 255));
                stroke.setStyle(Paint.Style.STROKE);
                stroke.setStrokeWidth(2f * density);
                stroke.setColor(Color.rgb(68, 155, 255));
            }

            @Override
            public void draw(Canvas canvas) {
                Rect bounds = getBounds();
                float cx = bounds.exactCenterX();
                float cy = bounds.exactCenterY();
                float r = Math.min(bounds.width(), bounds.height()) / 2f - 6f * density;
                canvas.drawCircle(cx, cy, r, fill);
                canvas.drawCircle(cx, cy, r, stroke);
            }

            @Override public void setAlpha(int alpha) { fill.setAlpha(alpha); stroke.setAlpha(alpha); }
            @Override public void setColorFilter(android.graphics.ColorFilter filter) { fill.setColorFilter(filter); stroke.setColorFilter(filter); }
            @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
        }
    }

    private static final class IconView extends View {
        static final int BELL = 1;
        static final int SHIELD_CHECK = 2;
        static final int CHEVRON_RIGHT = 3;
        static final int CHEVRON_DOWN = 4;
        static final int DOOR = 5;
        static final int HOME = 6;
        static final int CLOCK = 7;
        static final int CLIPBOARD = 8;
        static final int PERSON = 9;
        static final int GEAR = 10;

        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final int type;
        private int iconColor = MUTED;

        IconView(Context context, int type) {
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
            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float cy = h / 2f;
            float unit = Math.min(w, h) / 32f;

            stroke.setColor(iconColor);
            fill.setColor(iconColor);
            stroke.setStrokeWidth(Math.max(2f, unit * 2f));

            if (type == BELL) {
                RectF bell = new RectF(cx - 8 * unit, cy - 10 * unit, cx + 8 * unit, cy + 7 * unit);
                canvas.drawArc(bell, 195, 150, false, stroke);
                canvas.drawLine(cx - 8 * unit, cy + 1 * unit, cx - 8 * unit, cy + 6 * unit, stroke);
                canvas.drawLine(cx + 8 * unit, cy + 1 * unit, cx + 8 * unit, cy + 6 * unit, stroke);
                canvas.drawLine(cx - 8 * unit, cy + 6 * unit, cx + 8 * unit, cy + 6 * unit, stroke);
                canvas.drawCircle(cx, cy + 10 * unit, 1.8f * unit, fill);
            } else if (type == SHIELD_CHECK) {
                path.reset();
                path.moveTo(cx, cy - 12 * unit);
                path.lineTo(cx + 10 * unit, cy - 7 * unit);
                path.lineTo(cx + 8 * unit, cy + 7 * unit);
                path.lineTo(cx, cy + 13 * unit);
                path.lineTo(cx - 8 * unit, cy + 7 * unit);
                path.lineTo(cx - 10 * unit, cy - 7 * unit);
                path.close();
                canvas.drawPath(path, stroke);
                canvas.drawLine(cx - 5 * unit, cy, cx - 1 * unit, cy + 4 * unit, stroke);
                canvas.drawLine(cx - 1 * unit, cy + 4 * unit, cx + 6 * unit, cy - 5 * unit, stroke);
            } else if (type == CHEVRON_RIGHT) {
                canvas.drawLine(cx - 4 * unit, cy - 7 * unit, cx + 3 * unit, cy, stroke);
                canvas.drawLine(cx + 3 * unit, cy, cx - 4 * unit, cy + 7 * unit, stroke);
            } else if (type == CHEVRON_DOWN) {
                canvas.drawLine(cx - 7 * unit, cy - 3 * unit, cx, cy + 4 * unit, stroke);
                canvas.drawLine(cx, cy + 4 * unit, cx + 7 * unit, cy - 3 * unit, stroke);
            } else if (type == DOOR) {
                RectF door = new RectF(cx - 7 * unit, cy - 11 * unit, cx + 7 * unit, cy + 10 * unit);
                canvas.drawRect(door, stroke);
                canvas.drawCircle(cx + 3 * unit, cy, 1.4f * unit, fill);
                canvas.drawLine(cx - 11 * unit, cy + 11 * unit, cx + 11 * unit, cy + 11 * unit, stroke);
            } else if (type == HOME) {
                path.reset();
                path.moveTo(cx - 11 * unit, cy - 1 * unit);
                path.lineTo(cx, cy - 11 * unit);
                path.lineTo(cx + 11 * unit, cy - 1 * unit);
                path.lineTo(cx + 9 * unit, cy - 1 * unit);
                path.lineTo(cx + 9 * unit, cy + 10 * unit);
                path.lineTo(cx + 2 * unit, cy + 10 * unit);
                path.lineTo(cx + 2 * unit, cy + 3 * unit);
                path.lineTo(cx - 2 * unit, cy + 3 * unit);
                path.lineTo(cx - 2 * unit, cy + 10 * unit);
                path.lineTo(cx - 9 * unit, cy + 10 * unit);
                path.lineTo(cx - 9 * unit, cy - 1 * unit);
                canvas.drawPath(path, stroke);
            } else if (type == CLOCK) {
                canvas.drawCircle(cx, cy, 11 * unit, stroke);
                canvas.drawLine(cx, cy, cx, cy - 7 * unit, stroke);
                canvas.drawLine(cx, cy, cx + 6 * unit, cy + 4 * unit, stroke);
            } else if (type == CLIPBOARD) {
                RectF sheet = new RectF(cx - 9 * unit, cy - 9 * unit, cx + 9 * unit, cy + 11 * unit);
                canvas.drawRoundRect(sheet, 2 * unit, 2 * unit, stroke);
                RectF top = new RectF(cx - 4 * unit, cy - 13 * unit, cx + 4 * unit, cy - 8 * unit);
                canvas.drawRoundRect(top, 1.5f * unit, 1.5f * unit, stroke);
                canvas.drawLine(cx - 5 * unit, cy - 2 * unit, cx + 5 * unit, cy - 2 * unit, stroke);
                canvas.drawLine(cx - 5 * unit, cy + 4 * unit, cx + 5 * unit, cy + 4 * unit, stroke);
            } else if (type == PERSON) {
                canvas.drawCircle(cx, cy - 7 * unit, 5 * unit, stroke);
                RectF shoulders = new RectF(cx - 10 * unit, cy + 1 * unit, cx + 10 * unit, cy + 15 * unit);
                canvas.drawArc(shoulders, 180, 180, false, stroke);
            } else if (type == GEAR) {
                canvas.drawCircle(cx, cy, 7 * unit, stroke);
                canvas.drawCircle(cx, cy, 2.5f * unit, stroke);
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI * 2 * i / 8d;
                    float x1 = cx + (float) Math.cos(angle) * 9 * unit;
                    float y1 = cy + (float) Math.sin(angle) * 9 * unit;
                    float x2 = cx + (float) Math.cos(angle) * 12 * unit;
                    float y2 = cy + (float) Math.sin(angle) * 12 * unit;
                    canvas.drawLine(x1, y1, x2, y2, stroke);
                }
            }
        }
    }
}
