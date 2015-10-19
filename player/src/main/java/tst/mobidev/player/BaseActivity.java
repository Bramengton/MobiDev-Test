package tst.mobidev.player;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * @author by Bramengton
 * @date 07.10.15.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private Toolbar mActionBarToolbar;

    public void setToolbar(@NonNull Toolbar toolbar, @NonNull final Boolean HomeAsUp) {
        this.mActionBarToolbar = toolbar;
        setSupportActionBar(this.mActionBarToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(12.0f);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(HomeAsUp);
        }
    }

    public Toolbar getToolbar() {
        return mActionBarToolbar;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setToolbar((Toolbar) findViewById(R.id.mainToolbar), false);
    }

}
