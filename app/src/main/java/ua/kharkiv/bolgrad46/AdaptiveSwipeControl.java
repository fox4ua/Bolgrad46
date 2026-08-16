package ua.kharkiv.bolgrad46;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

final class AdaptiveSwipeControl extends View {
    private static final int SURFACE = Color.rgb(6, 24, 45);
    private static final int BLUE = Color.rgb(0, 119, 255);
    private static final int BLUE_LIGHT = Color.rgb(77, 160, 255);
    private static final int TEXT = Color.rgb(247, 249, 253);
    private static final int MUTED = Color.rgb(134, 157, 197);
    private static final int GREEN = Color.rgb(43, 211, 119);

    private final float density;
    private final Paint cardFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cardStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mutedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path doorPath = new Path();

    private float drag;
    private float maxDrag;
    private float touchOffset;
    private boolean dragging;
    private boolean opened;
    private ValueAnimator animator;

    AdaptiveSwipeControl(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
        setFocusable(true);

        cardFill.setColor(SURFACE);
        cardFill.setShadowLayer(dp(7), 0f, 0f, Color.argb(120, 0, 119, 255));

        cardStroke.setStyle(Paint.Style.STROKE);
        cardStroke.setStrokeWidth(dp(1));
        cardStroke.setColor(BLUE);

        thumbFill.setColor(Color.rgb(4, 37, 74));
        thumbFill.setShadowLayer(dp(12), 0f, 0f, Color.argb(210, 0, 119, 255));

        thumbStroke.setStyle(Paint.Style.STROKE);
        thumbStroke.setStrokeWidth(dp(2));
        thumbStroke.setColor(BLUE_LIGHT);

        textPaint.setColor(TEXT);
        textPaint.setTextSize(sp(16));
        textPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        mutedPaint.setColor(MUTED);
        mutedPaint.setTextSize(sp(14));
        mutedPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        arrowPaint.setColor(Color.rgb(0, 102, 224));
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(dp(2.5f));
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);
        arrowPaint.setStrokeJoin(Paint.Join.ROUND);

        iconPaint.setColor(Color.WHITE);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(dp(2.2f));
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maxDrag = Math.max(0f, w - dp(100));
        drag = Math.min(drag, maxDrag);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float outer = dp(7);
        RectF card = new RectF(outer, outer, getWidth() - outer, getHeight() - outer);
        float radius = dp(26);
        canvas.drawRoundRect(card, radius, radius, cardFill);
        canvas.drawRoundRect(card, radius, radius, cardStroke);

        if (opened) {
            drawOpenedState(canvas);
            return;
        }

        float thumbRadius = dp(38);
        float thumbCenterX = dp(50) + drag;
        float thumbCenterY = getHeight() / 2f;

        drawInstruction(canvas, thumbCenterX);

        canvas.drawCircle(thumbCenterX, thumbCenterY, thumbRadius, thumbFill);
        canvas.drawCircle(thumbCenterX, thumbCenterY, thumbRadius, thumbStroke);
        drawDoorIcon(canvas, thumbCenterX, thumbCenterY);
    }

    private void drawInstruction(Canvas canvas, float thumbCenterX) {
        float fraction = maxDrag <= 0f ? 0f : drag / maxDrag;
        boolean textOnLeft = fraction >= 0.5f;

        float titleY = getHeight() / 2f - dp(7);
        float subtitleY = getHeight() / 2f + dp(18);
        float edgePadding = dp(24);
        float gapFromThumb = dp(22);
        float arrowWidth = dp(42);

        if (!textOnLeft) {
            float groupLeft = thumbCenterX + dp(52);
            drawRightArrows(canvas, groupLeft, getHeight() / 2f);
            float textX = groupLeft + arrowWidth + dp(10);
            float maxTextRight = getWidth() - edgePadding;
            drawInstructionText(canvas, textX, maxTextRight, titleY, subtitleY, Paint.Align.LEFT);
        } else {
            float groupRight = thumbCenterX - gapFromThumb;
            float arrowLeft = groupRight - arrowWidth;
            drawRightArrows(canvas, arrowLeft, getHeight() / 2f);
            float textRight = arrowLeft - dp(12);
            drawInstructionText(canvas, edgePadding, textRight, titleY, subtitleY, Paint.Align.RIGHT);
        }
    }

    private void drawInstructionText(
        Canvas canvas,
        float left,
        float right,
        float titleY,
        float subtitleY,
        Paint.Align align
    ) {
        float available = Math.max(0f, right - left);
        if (available < dp(72)) {
            return;
        }

        String title = "Проведите, чтобы открыть";
        String subtitle = "Потяните вправо";

        float titleSize = sp(16);
        textPaint.setTextSize(titleSize);
        while (textPaint.measureText(title) > available && titleSize > sp(13)) {
            titleSize -= 1f;
            textPaint.setTextSize(titleSize);
        }

        float subtitleSize = sp(14);
        mutedPaint.setTextSize(subtitleSize);
        while (mutedPaint.measureText(subtitle) > available && subtitleSize > sp(12)) {
            subtitleSize -= 1f;
            mutedPaint.setTextSize(subtitleSize);
        }

        float x = align == Paint.Align.RIGHT ? right : left;
        textPaint.setTextAlign(align);
        mutedPaint.setTextAlign(align);
        canvas.drawText(title, x, titleY, textPaint);
        canvas.drawText(subtitle, x, subtitleY, mutedPaint);
    }

    private void drawRightArrows(Canvas canvas, float left, float centerY) {
        float step = dp(12);
        for (int i = 0; i < 3; i++) {
            float x = left + i * step;
            canvas.drawLine(x, centerY - dp(8), x + dp(8), centerY, arrowPaint);
            canvas.drawLine(x + dp(8), centerY, x, centerY + dp(8), arrowPaint);
        }
    }

    private void drawOpenedState(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        mutedPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(sp(19));
        mutedPaint.setTextSize(sp(14));
        textPaint.setColor(GREEN);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        canvas.drawText("Дверь открыта", centerX, centerY - dp(3), textPaint);
        canvas.drawText("Команда выполнена", centerX, centerY + dp(22), mutedPaint);

        textPaint.setColor(TEXT);
    }

    private void drawDoorIcon(Canvas canvas, float cx, float cy) {
        float halfW = dp(11);
        float top = cy - dp(17);
        float bottom = cy + dp(14);

        doorPath.reset();
        doorPath.moveTo(cx - halfW, bottom);
        doorPath.lineTo(cx - halfW, top);
        doorPath.lineTo(cx + halfW, top);
        doorPath.lineTo(cx + halfW, bottom);
        canvas.drawPath(doorPath, iconPaint);
        canvas.drawCircle(cx + dp(5), cy, dp(1.7f), iconPaint);
        canvas.drawLine(cx - dp(16), bottom + dp(2), cx + dp(16), bottom + dp(2), iconPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (opened) {
            return true;
        }

        float thumbCenterX = dp(50) + drag;
        float hitRadius = dp(48);

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (Math.abs(event.getX() - thumbCenterX) <= hitRadius) {
                dragging = true;
                touchOffset = event.getX() - drag;
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            return true;
        }

        if (event.getActionMasked() == MotionEvent.ACTION_MOVE && dragging) {
            drag = Math.max(0f, Math.min(maxDrag, event.getX() - touchOffset));
            invalidate();
            return true;
        }

        if ((event.getActionMasked() == MotionEvent.ACTION_UP
            || event.getActionMasked() == MotionEvent.ACTION_CANCEL) && dragging) {
            dragging = false;
            getParent().requestDisallowInterceptTouchEvent(false);

            boolean success = drag >= maxDrag * 0.72f;
            if (success) {
                animateTo(maxDrag, true);
            } else {
                animateTo(0f, false);
            }
            return true;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void animateTo(float target, boolean success) {
        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(drag, target);
        animator.setDuration(180);
        animator.addUpdateListener(animation -> {
            drag = (float) animation.getAnimatedValue();
            invalidate();
        });

        if (success) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    opened = true;
                    invalidate();
                    postDelayed(() -> {
                        opened = false;
                        drag = maxDrag;
                        invalidate();
                        animateTo(0f, false);
                    }, 1500);
                }
            });
        }

        animator.start();
    }

    private float dp(float value) {
        return value * density;
    }

    private float sp(float value) {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            getResources().getDisplayMetrics()
        );
    }
}
