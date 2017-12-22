package se.wetcat.ntpplayground;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Main entry for the app, also handles the ntp sync using Rx, shouldn't need any complicated
 * handling of network connections since Rx will (should) gracefully throw an exception that could
 * be caught.
 *
 * However, I choose to check if the SntpClient fails to get a timestamp and if it does fail it will
 * simply use the system millis.
 *
 * The subscription will be disposed when pausing the activity, and recreated when starting the
 * activity again.
 *
 * @author andreasgoransson0@gmail.com
 */
public class MainActivity extends ScreenHandlingActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add the fragment if it's not already added
        if (null == getScreen(SyncedClockScreen.class)) {
            setScreen(SyncedClockScreen.class);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mNtpObservable = Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                e.onNext(getTime());
                e.onComplete();
            }
        });

        mNtpObserver = new DisposableObserver<Long>() {

            @Override
            public void onNext(Long aLong) {
                Log.d(TAG, "onNext(" + aLong + ")");

                if (getScreen(SyncedClockScreen.class) != null) {
                    SyncedClockScreen screen = getScreen(SyncedClockScreen.class);
                    screen.newTime(aLong);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError(" + e.getMessage() + ")");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete()");
            }
        };

        // Start the subscription when activity comes to foreground
        mNtpObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Observable<Object> objectObservable) throws Exception {
                        return objectObservable.delay(1, TimeUnit.SECONDS);
                    }
                })
                .subscribe(mNtpObserver);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Dispose of the subscription when we're falling into the background
        if (mNtpObserver != null && !mNtpObserver.isDisposed()) {
            mNtpObserver.dispose();
        }
    }

    /*
        Create an observable that will handle creating timely updates.
     */
    private Observable<Long> mNtpObservable;

    /*
        Create an observer that will handle delivery of the timely updates to user interface.
     */
    private DisposableObserver<Long> mNtpObserver;

    /**
     * Get the NTP time from desired server, using Googles "hidden" SntpClient. This will fallback
     * to using system time if the client fails.
     *
     * @return The current time in millis
     */
    private long getTime() {
        SntpClient client = new SntpClient();

        long time = 0L;

        try {
            ensureDns();

            if (client.requestTime(getString(R.string.ntp_server), getResources().getInteger(R.integer.ntp_timeout))) {
                time = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
            } else {
                time = System.currentTimeMillis();
            }
        } catch (InterruptedException e) {
            time = System.currentTimeMillis();
        }

        return time;
    }

    /**
     * A method that should, theoretically, timeout the dns quicker than it normally would. It will
     * leave a thread as a short-term memory leak though, but for the user this will mean quicker
     * response.
     *
     * @throws InterruptedException
     */
    private void ensureDns() throws InterruptedException {
        DnsResolver dnsRes = new DnsResolver(getString(R.string.ntp_server));
        Thread t = new Thread(dnsRes);
        t.start();
        t.join(1000);
        InetAddress inetAddr = dnsRes.get();
    }

}
