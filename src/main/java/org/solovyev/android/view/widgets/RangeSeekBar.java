package org.solovyev.android.view.widgets;

/**
 * User: serso
 * Date: 9/19/11
 * Time: 3:30 PM
 */

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.view.MotionEvent;
import android.widget.ImageView;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.calculator.R;

import java.math.BigDecimal;

/**
 * Widget that lets users select a minimum and maximum value on a given numerical range.
 * The range value types can be one of Long, Double, Integer, Float, Short, Byte or BigDecimal.
 *
 * @param <T> The Number type of the range values. One of Long, Double, Integer, Float, Short, Byte or BigDecimal.
 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
 */
public class RangeSeekBar<T extends Number> extends ImageView {

	@NotNull
	private final Paint paint = new Paint();

	@NotNull
	private final Bitmap thumbImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal);

	@NotNull
	private final Bitmap thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed);

	private final float thumbWidth = thumbImage.getWidth();

	private final float thumbHalfWidth = 0.5f * thumbWidth;

	private final float thumbHalfHeight = 0.5f * thumbImage.getHeight();

	private final float lineHeight = 0.3f * thumbHalfHeight;

	private final float padding = thumbHalfWidth;

	@NotNull
	private final T minValue, maxValue;

	@NotNull
	private final NumberType numberType;

	private final double dMinValue, dMaxValue;

	private double normalizedMinValue = 0d;
	private double normalizedMaxValue = 1d;
	private Thumb pressedThumb = null;
	private boolean notifyWhileDragging = false;
	private OnRangeSeekBarChangeListener<T> listener;

	/**
	 * Creates a new RangeSeekBar.
	 *
	 * @param minValue The minimum value of the selectable range.
	 * @param maxValue The maximum value of the selectable range.
	 * @param context
	 * @throws IllegalArgumentException Will be thrown if min/max value types are not one of Long, Double, Integer, Float, Short, Byte or BigDecimal.
	 */
	public RangeSeekBar(@NotNull T minValue, @NotNull T maxValue, Context context) throws IllegalArgumentException {
		super(context);

		this.minValue = minValue;
		this.maxValue = maxValue;

		dMinValue = minValue.doubleValue();
		dMaxValue = maxValue.doubleValue();

		numberType = NumberType.fromNumber(minValue);
	}

	public boolean isNotifyWhileDragging() {
		return notifyWhileDragging;
	}

	/**
	 * Should the widget notify the listener callback while the user is still dragging a thumb? Default is false.
	 *
	 * @param flag
	 */
	public void setNotifyWhileDragging(boolean flag) {
		this.notifyWhileDragging = flag;
	}

	/**
	 * Returns the absolute minimum value of the range that has been set at construction time.
	 *
	 * @return The absolute minimum value of the range.
	 */
	@NotNull
	public T getMinValue() {
		return minValue;
	}

	/**
	 * Returns the absolute maximum value of the range that has been set at construction time.
	 *
	 * @return The absolute maximum value of the range.
	 */
	@NotNull
	public T getMaxValue() {
		return maxValue;
	}

	/**
	 * Returns the currently selected min value.
	 *
	 * @return The currently selected min value.
	 */
	public T getSelectedMinValue() {
		return normalizedToValue(normalizedMinValue);
	}

	/**
	 * Sets the currently selected minimum value. The widget will be invalidated and redrawn.
	 *
	 * @param value The Number value to set the minimum value to. Will be clamped to given absolute minimum/maximum range.
	 */
	public void setSelectedMinValue(T value) {
		// in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
		if (0 == (dMaxValue - dMinValue)) {
			setNormalizedMinValue(0d);
		} else {
			setNormalizedMinValue(normalizeValue(value));
		}
	}

	/**
	 * Returns the currently selected max value.
	 *
	 * @return The currently selected max value.
	 */
	public T getSelectedMaxValue() {
		return normalizedToValue(normalizedMaxValue);
	}

	/**
	 * Sets the currently selected maximum value. The widget will be invalidated and redrawn.
	 *
	 * @param value The Number value to set the maximum value to. Will be clamped to given absolute minimum/maximum range.
	 */
	public void setSelectedMaxValue(T value) {
		// in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
		if (0 == (dMaxValue - dMinValue)) {
			setNormalizedMaxValue(1d);
		} else {
			setNormalizedMaxValue(normalizeValue(value));
		}
	}

	/**
	 * Registers given listener callback to notify about changed selected values.
	 *
	 * @param listener The listener to notify about changed selected values.
	 */
	public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener<T> listener) {
		this.listener = listener;
	}

	/**
	 * Handles thumb selection and movement. Notifies listener callback on certain events.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				pressedThumb = evalPressedThumb(event.getX());
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				if (pressedThumb != null) {
					if (Thumb.MIN.equals(pressedThumb)) {
						setNormalizedMinValue(convertToNormalizedValue(event.getX()));
					} else if (Thumb.MAX.equals(pressedThumb)) {
						setNormalizedMaxValue(convertToNormalizedValue(event.getX()));
					}
					if (notifyWhileDragging && listener != null) {
						listener.rangeSeekBarValuesChanged(getSelectedMinValue(), getSelectedMaxValue());
					}
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				pressedThumb = null;
				invalidate();
				if (listener != null) {
					listener.rangeSeekBarValuesChanged(getSelectedMinValue(), getSelectedMaxValue());
				}
				break;
		}
		return true;
	}

	/**
	 * Ensures correct size of the widget.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = 200;
		if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
			width = MeasureSpec.getSize(widthMeasureSpec);
		}
		int height = thumbImage.getHeight();
		if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
			height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
		}
		setMeasuredDimension(width, height);
	}

	/**
	 * Draws the widget on the given canvas.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// draw seek bar background line
		final RectF rect = new RectF(padding, 0.5f * (getHeight() - lineHeight), getWidth() - padding, 0.5f * (getHeight() + lineHeight));
		paint.setStyle(Style.FILL);
		paint.setColor(Color.GRAY);
		canvas.drawRect(rect, paint);
		// draw seek bar active range line
		rect.left = convertToScreenValue(normalizedMinValue);
		rect.right = convertToScreenValue(normalizedMaxValue);
		// orange color
		paint.setColor(Color.rgb(255, 165, 0));
		canvas.drawRect(rect, paint);

		// draw minimum thumb
		drawThumb(convertToScreenValue(normalizedMinValue), Thumb.MIN == pressedThumb, canvas);

		// draw maximum thumb
		drawThumb(convertToScreenValue(normalizedMaxValue), Thumb.MAX == pressedThumb, canvas);
	}

	/**
	 * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
	 *
	 * @param normalizedToScreenValue The x-coordinate in screen space where to draw the image.
	 * @param pressed	 Is the thumb currently in "pressed" state?
	 * @param canvas	  The canvas to draw upon.
	 */
	private void drawThumb(float normalizedToScreenValue, boolean pressed, Canvas canvas) {
		canvas.drawBitmap(pressed ? thumbPressedImage : thumbImage, normalizedToScreenValue - thumbHalfWidth, (float) ((0.5f * getHeight()) - thumbHalfHeight), paint);
	}

	/**
	 * Decides which (if any) thumb is touched by the given x-coordinate.
	 *
	 * @param touchX The x-coordinate of a touch event in screen space.
	 * @return The pressed thumb or null if none has been touched.
	 */
	private Thumb evalPressedThumb(float touchX) {
		Thumb result = null;
		boolean minThumbPressed = isInThumbRange(touchX, normalizedMinValue);
		boolean maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue);
		if (minThumbPressed && maxThumbPressed) {
			// if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a corner, not being able to drag them apart anymore.
			result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
		} else if (minThumbPressed) {
			result = Thumb.MIN;
		} else if (maxThumbPressed) {
			result = Thumb.MAX;
		}
		return result;
	}

	/**
	 * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
	 *
	 * @param touchX			   The x-coordinate in screen space to check.
	 * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
	 * @return true if x-coordinate is in thumb range, false otherwise.
	 */
	private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
		return Math.abs(touchX - convertToScreenValue(normalizedThumbValue)) <= thumbHalfWidth;
	}

	/**
	 * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1.
	 * The View will get invalidated when calling this method.
	 *
	 * @param value The new normalized min value to set.
	 */
	private void setNormalizedMinValue(double value) {
		normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValue)));
		invalidate();
	}

	/**
	 * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1.
	 * The View will get invalidated when calling this method.
	 *
	 * @param value The new normalized max value to set.
	 */
	private void setNormalizedMaxValue(double value) {
		normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValue)));
		invalidate();
	}

	/**
	 * Converts a normalized value to a Number object in the value space between absolute minimum and maximum.
	 *
	 * @param normalized
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private T normalizedToValue(double normalized) {
		return (T) numberType.toNumber(dMinValue + normalized * (dMaxValue - dMinValue));
	}

	/**
	 * Converts the given Number value to a normalized double.
	 *
	 * @param value The Number value to normalize.
	 * @return The normalized double.
	 */
	private double normalizeValue(T value) {
		assert 0 != dMaxValue - dMinValue;
		return (value.doubleValue() - dMinValue) / (dMaxValue - dMinValue);
	}

	/**
	 * Converts a normalized value into screen space.
	 *
	 * @param normalizedValue The normalized value to convert.
	 * @return The converted value in screen space.
	 */
	private float convertToScreenValue(double normalizedValue) {
		return (float) (padding + normalizedValue * (getWidth() - 2 * padding));
	}

	/**
	 * Converts screen space x-coordinates into normalized values.
	 *
	 * @param screenValue The x-coordinate in screen space to convert.
	 * @return The normalized value.
	 */
	private double convertToNormalizedValue(float screenValue) {
		int width = getWidth();
		if (width <= 2 * padding) {
			// prevent division by zero, simply return 0.
			return 0d;
		} else {
			double result = (screenValue - padding) / (width - 2 * padding);
			return Math.min(1d, Math.max(0d, result));
		}
	}

	/**
	 * Callback listener interface to notify about changed range values.
	 *
	 * @param <T> The Number type the RangeSeekBar has been declared with.
	 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
	 */
	public interface OnRangeSeekBarChangeListener<T> {
		void rangeSeekBarValuesChanged(T minValue, T maxValue);
	}

	/**
	 * Thumb constants (min and max).
	 *
	 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
	 */
	private static enum Thumb {
		MIN, MAX
	}

	/**
	 * Utility enumaration used to convert between Numbers and doubles.
	 *
	 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
	 */
	private static enum NumberType {

		LONG(Long.class),
		DOUBLE(Double.class),
		INTEGER(Integer.class),
		FLOAT(Float.class),
		SHORT(Short.class),
		BYTE(Byte.class),
		BIG_DECIMAL(BigDecimal.class);

		@NotNull
		private final Class underlyingClass;

		private NumberType(@NotNull Class underlyingClass) {
			this.underlyingClass = underlyingClass;
		}

		@NotNull
		public static <E extends Number> NumberType fromNumber(E value) throws IllegalArgumentException {

			for (NumberType numberType : NumberType.values()) {
				if ( numberType.underlyingClass.isInstance(value) ) {
					return numberType;
				}
			}

			throw new IllegalArgumentException("Number class '" + value.getClass().getName() + "' is not supported");
		}

		public Number toNumber(double value) {

			switch (this) {
				case LONG:
					return (long) value;
				case DOUBLE:
					return value;
				case INTEGER:
					return (int)value;
				case FLOAT:
					return (float)value;
				case SHORT:
					return (short)value;
				case BYTE:
					return (byte)value;
				case BIG_DECIMAL:
					return new BigDecimal(value);
			}

			throw new InstantiationError("can't convert " + this + " to a Number object");
		}
	}
}
