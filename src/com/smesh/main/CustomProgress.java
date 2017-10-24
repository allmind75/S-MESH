package com.smesh.main;

import com.samsung.android.example.helloaccessoryprovider.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CustomProgress extends View {

	private int barLength = 100;
	private int barWidth = 50;
	private int rimWidth = 20;
	private int textSize = 20;
	private float contourSize = 0;

	private int paddingTop = 5;
	private int paddingBottom = 5;
	private int paddingLeft = 5;
	private int paddingRight = 5;

	private int barColor; // 안에 bar 색
	private int circleColor; // 가운데 원 색
	private int rimColor; // 색칠되지 않은 원 색
	private int textColor;

	private Paint barPaint = new Paint();
	private Paint circlePaint = new Paint();
	private Paint rimPaint = new Paint();
	private Paint textPaint = new Paint();
	private Paint contourPaint = new Paint();

	public static int battery_per = 0;

	@SuppressWarnings("unused")
	private RectF circleBounds = new RectF();

	int progress = 0;

	private String text = "";
	private String[] splitText = {};

	public CustomProgress(Context context, AttributeSet attrs) {
		super(context, attrs);

		parseAttributes(context.obtainStyledAttributes(attrs,
				R.styleable.ProgressWheel));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int size = 0;
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
		int heigthWithoutPadding = height - getPaddingTop()
				- getPaddingBottom();

		if (widthWithoutPadding > heigthWithoutPadding) {
			size = heigthWithoutPadding;
		} else {
			size = widthWithoutPadding;
		}

		setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size
				+ getPaddingTop() + getPaddingBottom());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setupBounds();
		invalidate();
	}

	private void setupPaints() { // 60퍼센트 까지 파란색
		if (battery_per == 1) {
			barPaint.setColor(barColor);

		} else if (battery_per == 2) {

			barPaint.setColor(Color.YELLOW);
		} else {

			barPaint.setColor(Color.RED);
		}

		barPaint.setAntiAlias(true);
		barPaint.setStyle(Style.STROKE);
		barPaint.setStrokeWidth(barWidth);

		rimPaint.setColor(rimColor);
		rimPaint.setAntiAlias(true);
		rimPaint.setStyle(Style.STROKE);
		rimPaint.setStrokeWidth(rimWidth);

		circlePaint.setColor(circleColor);
		circlePaint.setAntiAlias(true);
		circlePaint.setStyle(Style.FILL);

		textPaint.setColor(textColor);
		textPaint.setStyle(Style.FILL);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(textSize);

	}

	private void setupBounds() {

		paddingTop = this.getPaddingTop();
		paddingBottom = this.getPaddingBottom();
		paddingLeft = this.getPaddingLeft();
		paddingRight = this.getPaddingRight();

		int width = getWidth();
		int height = getHeight();

		circleBounds = new RectF(paddingLeft + barWidth, paddingTop + barWidth,
				width - paddingRight - barWidth, height - paddingBottom
						- barWidth);

	}

	private void parseAttributes(TypedArray a) {
		barWidth = (int) a.getDimension(R.styleable.ProgressWheel_barWidth,
				barWidth);

		rimWidth = (int) a.getDimension(R.styleable.ProgressWheel_rimWidth,
				rimWidth);

		barColor = a.getColor(R.styleable.ProgressWheel_barColor, barColor);

		barLength = (int) a.getDimension(R.styleable.ProgressWheel_barLength,
				barLength);

		textSize = (int) a.getDimension(R.styleable.ProgressWheel_textSize,
				textSize);

		textColor = (int) a.getColor(R.styleable.ProgressWheel_textColor,
				textColor);

		if (a.hasValue(R.styleable.ProgressWheel_text)) {
			setText(a.getString(R.styleable.ProgressWheel_text));
		}

		rimColor = (int) a.getColor(R.styleable.ProgressWheel_rimColor,
				rimColor);

		circleColor = (int) a.getColor(R.styleable.ProgressWheel_circleColor,
				circleColor);

		a.recycle();
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawArc(circleBounds, 360, 360, false, circlePaint);

		canvas.drawArc(circleBounds, 360, 360, false, rimPaint);

		canvas.drawArc(circleBounds, -90, progress, false, barPaint);

		float textHeight = textPaint.descent() - textPaint.ascent();
		float verticalTextOffset = (textHeight / 2) - textPaint.descent();

		for (String s : splitText) {
			float horizontalTextOffset = textPaint.measureText(s) / 2;
			canvas.drawText(s, this.getWidth() / 2 - horizontalTextOffset,
					this.getHeight() / 2 + verticalTextOffset, textPaint);
		}

	}

	public void setProgress(int i) {
		progress = i * 360 / 100;
		setText(i + " %");
		if (i > 60) {
			battery_per = 1;
			setupPaints();
		} else if (i <= 60 && i > 30) {
			battery_per = 2;
			setupPaints();
		} else if (i <= 30) {
			battery_per = 3;
			setupPaints();
		}
		postInvalidate();
	}

	public void setText(String text) {
		this.text = text;
		splitText = this.text.split("\n");
	}

}
