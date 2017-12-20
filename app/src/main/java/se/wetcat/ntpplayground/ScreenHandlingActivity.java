package se.wetcat.ntpplayground;

import android.support.v4.app.Fragment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Handling screen changes (fragments), as a way to clean up the code in main activity.
 *
 * @author andreasgoransson0@gmail.com
 */
public abstract class ScreenHandlingActivity extends PermissionsHandlingActivity {

    public static abstract class Screen extends Fragment {
        // Just a way to separate full-screen fragments from part-screen fragments and dialogs.
    }

    protected <T extends Screen> T getScreen(Class<? extends Screen> fragmentClass) {
        return (T) getSupportFragmentManager().findFragmentByTag(fragmentClass.getSimpleName());
    }

    protected void setScreen(Class<? extends Screen> fragmentClass) {
        try {
            Method method = fragmentClass.getMethod("newInstance");
            Screen screen = (Screen) method.invoke(null);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, screen, fragmentClass.getSimpleName())
                    .commit();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
