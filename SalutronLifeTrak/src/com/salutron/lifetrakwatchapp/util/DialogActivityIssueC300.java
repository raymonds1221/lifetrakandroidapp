package com.salutron.lifetrakwatchapp.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.salutron.lifetrak.R;

/**
 * Created by janwelcris on 8/17/2015.
 */
public class DialogActivityIssueC300 extends Activity implements View.OnClickListener, SalutronLifeTrakUtility {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alert_dialog_c300_c410_issue);

        this.setFinishOnTouchOutside(false);
        ((Button)findViewById(R.id.issue_dialog_no)).setOnClickListener(this);
        ((Button)findViewById(R.id.issue_dialog_yes)).setOnClickListener(this);

        ((CheckBox)findViewById(R.id.issue_checkbox_remember_choice)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceWrapper.getInstance(DialogActivityIssueC300.this).setPreferenceBooleanValue(IS_REMEMBER_ME_ISSUE, isChecked).synchronize();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.issue_dialog_no:
                finish();
                break;
            case R.id.issue_dialog_yes:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(API_LOLLIPOP_ISSUE_URL));
                startActivity(intent);
                finish();
                break;

        }
    }
}
