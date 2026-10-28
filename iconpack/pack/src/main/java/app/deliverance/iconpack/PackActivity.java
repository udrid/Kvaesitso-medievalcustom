package app.deliverance.iconpack;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shows how to apply the pack. Icon launchers read the pack from the manifest filters; this screen does nothing else. */
public class PackActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0A0907"));
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextColor(Color.parseColor("#C9A55A"));
        title.setTextSize(24);
        title.setTypeface(Typeface.SERIF, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView body = new TextView(this);
        body.setText(R.string.how_to_apply);
        body.setTextColor(Color.parseColor("#E6D9B8"));
        body.setTextSize(16);
        body.setGravity(Gravity.CENTER);
        body.setPadding(0, pad, 0, 0);
        root.addView(body);

        setContentView(root);
    }
}
