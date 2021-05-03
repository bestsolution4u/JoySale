package com.hitasoft.app.external.timessquare;

import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hitasoft.app.joysale.R;

import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DefaultDayViewAdapter implements DayViewAdapter {
  @Override public void makeCellView(CalendarCellView parent) {
    TextView subTitleTextView = new TextView(
            new ContextThemeWrapper(parent.getContext(), R.style.CalendarCell_SubTitle));
    subTitleTextView.setDuplicateParentStateEnabled(true);
    subTitleTextView.setGravity(Gravity.BOTTOM);
    parent.addView(subTitleTextView);

    TextView textView = new TextView(
            new ContextThemeWrapper(parent.getContext(), R.style.CalendarCell_CalendarDate));
    textView.setDuplicateParentStateEnabled(true);
    parent.addView(textView);

    parent.setDayOfMonthTextView(textView);
    parent.setSubTitleTextView(subTitleTextView);
  }
}
