package ca.jasonsim.rssapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DescriptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);
        TextView txtLink = (TextView) findViewById(R.id.txtLink);
        TextView txtDescription = (TextView) findViewById(R.id.txtDescription);

        Bundle extras = getIntent().getExtras();

        String itemLink = extras.getString("key");
        String itemDescription = extras.getString("description");

        txtLink.setText(itemLink);
        txtDescription.setText(itemDescription);

    }
}
