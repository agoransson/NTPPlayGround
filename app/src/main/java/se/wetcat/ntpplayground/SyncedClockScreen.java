package se.wetcat.ntpplayground;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import java.util.Calendar;

import static se.wetcat.ntpplayground.ScreenHandlingActivity.Screen;

/**
 * @author andreasgoransson0@gmail.com
 */
public class SyncedClockScreen extends Screen {

    private static final String TAG = SyncedClockScreen.class.getSimpleName();

    private TimePicker mClock;

    public static SyncedClockScreen newInstance() {
        Bundle args = new Bundle();
        SyncedClockScreen screen = new SyncedClockScreen();
        screen.setArguments(args);
        return screen;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_syncedclock, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mClock = view.findViewById(R.id.clock);

        // TODO: Base the format on TimeZone instead
        mClock.setIs24HourView(true);
        mClock.setEnabled(false);
    }

    public void newTime(long newTime) {
        if (isVisible()) {
            // TODO: Handle TimeZone properly in the calendar
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(newTime);

            mClock.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            mClock.setMinute(calendar.get(Calendar.MINUTE));
        }
    }
}
