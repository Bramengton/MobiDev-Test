package tst.mobidev.player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

/**
 * @author by Bramengton
 * @date 07.10.15.
 */
public class Main extends BaseActivity
{
    static Player mPlayer =  new Player();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_play);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = mPlayer;
        if(!fragment.isAdded()){
            fragmentTransaction.add(R.id.fragment_content, fragment);
            fragmentTransaction.commit();
        }
    }
}
