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
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

final class HoldToOpenControl extends View {
    private static final int SURFACE = Color.rgb(6, 24, 45);
    private static final int BLUE = Color.rgb(0, 119, 255);
    private static final int BLUE_LIGHT = Color.rgb(77, 160, 255);
    private static final int TEXT = Color.rgb(247, 249, 253);
    private static final int MUTED = Color.rgb(134, 157, 197);
    private static final int GREEN = Color.rgb(43, 211, 119);

    private static final long HOLD_DURATION_MS = 850L;
    private static final long OPENED_STATE_MS = 1600L;

    private final float density;
    private final Paint cardFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cardStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressTrack = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint doorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path doorPath = new Path();
    private final Path checkPath = new Path();

    private ValueAnimator holdAnimator;
    private ValueAnimator resetAnimator;
    private float progress;
    private boolean pressing;
    private boolean opened;

    HoldToOpenControl(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
        setFocusable(true);
        setContentDescription("Открыть дверь. Удерживайте для открытия");

        cardFill.setColor(SURFACE);
        cardFill.setShadowLayer(dp(7), 0f, 0f, Color.argb(105, 0, 119, 255));

        cardStroke.setStyle(Paint.Style.STROKE);
        cardStroke.setStrokeWidth(dp(1));
        cardStroke.setColor(Color.rgb(25, 79, 132));

        iconFill.setColor(Color.rgb(4, 37, 74));

        iconStroke.setStyle(Paint.Style.STROKE);
        iconStroke.setStrokeWidth(dp(1.7f));
        iconStroke.setColor(BLUE_LIGHT);

        progressTrack.setStyle(Paint.Style.STROKE);
        progressTrack.setStrokeWidth(dp(3.5f));
        progressTrack.setStrokeCap(Paint.Cap.ROUND);
        progressTrack.setColor(Color.argb(95, 77, 160, 255));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(dp(4));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(BLUE_LIGHT);

        titlePaint.setColor(TEXT);
        titlePaint.setTextSize(sp(18));
        titlePaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        subtitlePaint.setColor(MUTED);
        subtitlePaint.setTextSize(sp(14));
        subtitlePaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        doorPaint.setColor(Color.WHITE);
        doorPaint.setStyle(Paint.Style.STROKE);
        doorPaint.setStrokeWidth(dp(2.2f));
        doorPaint.setStrokeCap(Paint.Cap.ROUND);
        doorPaint.setStrokeJoin(Paint.Join.ROUND);

        checkPaint.setColor(Color.WHITE);
        checkPaint.setStyle(Paint.Style.STROKE);
        checkPaint.setStrokeWidth(dp(3));
        checkPaint.setStrokeCap(Paint.Cap.ROUND);
        checkPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float outer = dp(7);
        RectF card = new RectF(outer, outer, getWidth() - outer, getHeight() - outer);
        float radius = dp(26);
        canvas.drawRoundRect(card, radius, radius, cardFill);
        canvas.drawRoundRect(card, radius, radius, cardStroke);

        float centerY = getHeight() / 2f;
        float iconCenterX = dp(58);
        float iconRadius = dp(37);

        if (opened) {
            iconFill.setColor(Color.rgb(12, 72, 53));
            iconStroke.setColor(GREEN);
            progressPaint.setColor(GREEN);
        } else {
            iconFill.setColor(Color.rgb(4, 37, 74));
            iconStroke.setColor(BLUE_LIGHT);
            progressPaint.setColor(BLUE_LIGHT);
        }

        canvas.drawCircle(iconCenterX, centerY, iconRadius, iconFill);
        canvas.drawCircle(iconCenterX, centerY, iconRadius, iconStroke);

        RectF progressRect = new RectF(
            iconCenterX - iconRadius - dp(5),
            centerY - iconRadius - dp(5),
            iconCenterX + iconRadius + dp(5),
            centerY + iconRadius + dp(5)
        );

        canvas.drawArc(progressRect, -90f, 360f, false, progressTrack);
        if (progress > 0f) {
            canvas.drawArc(progressRect, -90f, 360f * progress, false, progressPaint);
        }

        if (opened) {
            drawCheck(canvas, iconCenterX, centerY);
        } else {
            drawDoor(canvas, iconCenterX, centerY);
        }

        float textX = dp(122);
        float titleY = centerY - dp(5);
        float subtitleY = centerY + dp(22);

        titlePaint.setColor(opened ? GREEN : TEXT);
        subtitlePaint.setColor(MUTED);
        titlePaint.setTextAlign(Paint.Align.LEFT);
        subtitlePaint.setTextAlign(Paint.Align.LEFT);

        if (opened) {
            canvas.drawText("Дверь открыта", textX, titleY, titlePaint);
            canvas.drawText("Команда выполнена", textX, subtitleY, subtitlePaint);
        } else if (pressing) {
            canvas.drawText("Удерживайте…", textX, titleY, titlePaint);
            canvas.drawText("Не отпускайте", textX, subtitleY, subtitlePaint);
        } else {
            canvas.drawText("Открыть дверь", textX, titleY, titlePaint);
            canvas.drawText("Удерживайте для открытия", textX, subtitleY, subtitlePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (opened) {
            return true;
        }

        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            pressing = true;
            getParent().requestDisallowInterceptTouchEvent(true);
            startHolding();
            invalidate();
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE && pressing) {
            float tolerance = dp(18);
            boolean inside = event.getX() >= -tolerance
                && event.getX() <= getWidth() + tolerance
                && event.getY() >= -tolerance
                && event.getY() <= getHeight() + tolerance;

            if (!inside) {
                cancelHolding();
            }
            return true;
        }

        if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) && pressing) {
            cancelHolding();
            return true;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void startHolding() {
        if (holdAnimator != null) {
            holdAnimator.cancel();
        }
        if (resetAnimator != null) {
            resetAnimator.cancel();
        }

        holdAnimator = ValueAnimator.ofFloat(progress, 1f);
        holdAnimator.setDuration(Math.round(HOLD_DURATION_MS * (1f - progress)));
        holdAnimator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
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
                if (!cancelled && pressing && progress >= 0.999f) {
                    completeOpen();
                }
            }
        });
        holdAnimator.start();
    }

    private void cancelHolding() {
        pressing = false;
        getParent().requestDisallowInterceptTouchEvent(false);

        if (holdAnimator != null) {
            holdAnimator.cancel();
        }

        if (!opened) {
            resetProgress();
        }

        invalidate();
    }

    private void resetProgress() {
        if (resetAnimator != null) {
            resetAnimator.cancel();
        }

        resetAnimator = ValueAnimator.ofFloat(progress, 0f);
        resetAnimator.setDuration(160L);
        resetAnimator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        resetAnimator.start();
    }

    private void completeOpen() {
        pressing = false;
        opened = true;
        progress = 1f;
        getParent().requestDisallowInterceptTouchEvent(false);
        performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        performClick();
        setContentDescription("Дверь открыта");
        invalidate();

        postDelayed(() -> {
            opened = false;
            progress = 0f;
            setContentDescription("Открыть дверь. Удерживайте для открытия");
            invalidate();
        }, OPENED_STATE_MS);
    }

    private void drawDoor(Canvas canvas, float cx, float cy) {
        float halfW = dp(11);
        float top = cy - dp(17);
        float bottom = cy + dp(14);

        doorPath.reset();
        doorPath.moveTo(cx - halfW, bottom);
        doorPath.lineTo(cx - halfW, top);
        doorPath.lineTo(cx + halfW, top);
        doorPath.lineTo(cx + halfW, bottom);
        canvas.drawPath(doorPath, doorPaint);
        canvas.drawCircle(cx + dp(5), cy, dp(1.7f), doorPaint);
        canvas.drawLine(cx - dp(16), bottom + dp(2), cx + dp(16), bottom + dp(2), doorPaint);
    }

    private void drawCheck(Canvas canvas, float cx, float cy) {
        checkPath.reset();
        checkPath.moveTo(cx - dp(12), cy);
        checkPath.lineTo(cx - dp(3), cy + dp(9));
        checkPath.lineTo(cx + dp(14), cy - dp(11));
        canvas.drawPath(checkPath, checkPaint);
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
