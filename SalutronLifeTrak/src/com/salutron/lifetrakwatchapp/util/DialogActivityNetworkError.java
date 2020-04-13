package com.salutron.lifetrakwatchapp.util;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.salutron.lifetrak.R;

/**
 * Created by janwelcris on 8/17/2015.
 */
public class DialogActivityNetworkError extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_network_error);

        this.setFinishOnTouchOutside(false);
        ((Button)findViewById(R.id.buttonClose)).setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonClose:
                finish();
                break;
        }
    }
}
