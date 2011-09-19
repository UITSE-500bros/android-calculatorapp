package org.solovyev.android.view.prefs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.view.widgets.NumberPicker;
import org.solovyev.android.view.widgets.RangeSeekBar;

/**
 * User: serso
 * Date: 9/19/11
 * Time: 12:27 PM
 */
public abstract class RangeSeekPreference<T extends Number> extends AbstractDialogPreference implements RangeSeekBar.OnRangeSeekBarChangeListener<T> {

	@NotNull
	private final RangeSeekBar<T> rangeSeekBar;

	public RangeSeekPreference(@NotNull Context context, AttributeSet attrs) {
		super(context, attrs);
		this.rangeSeekBar = new RangeSeekBar<T>(getMinValue(), getMaxValue(), context);
		rangeSeekBar.setOnRangeSeekBarChangeListener(this);
	}

	@NotNull
	abstract T getMinValue();

	@NotNull
	abstract T getMaxValue();

	@Override
	protected LinearLayout onCreateDialogView() {
		final LinearLayout result = super.onCreateDialogView();

		result.addView(rangeSeekBar);

		return result;
	}

	@Override
	public void rangeSeekBarValuesChanged(T minValue, T maxValue) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
