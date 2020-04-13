package com.salutron.lifetrakwatchapp;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.CheckBox;

import com.salutron.lifetrak.R;

/**
 * Created by rsarmiento on 3/27/15.
 */
public class ProfileSelectR450Activity extends BaseActivity {
    private CheckBox mDoNotShowAgain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_select);

        mDoNotShowAgain = (CheckBox) findViewById(R.id.chkDoNotShowAgain);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindBLEService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindBLEService();
    }

    public void onButtonSelectClick(View view) {
        Intent intent = new Intent();

        switch (view.getId()) {
            case R.id.btnUseApp:
                intent.putExtra(USE_SETTING, USE_APP);
                break;
            case R.id.btnUseWatch:
                intent.putExtra(USE_SETTING, USE_WATCH);
                break;
        }

        intent.putExtra(DO_NOT_SHOW_PROMPT_DIALOG, mDoNotShowAgain.isChecked());
        setResult(RESULT_OK, intent);
        finish();
    }
}
