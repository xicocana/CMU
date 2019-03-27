package pt.ulisboa.tecnico.p2photo.GoogleUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import pt.ulisboa.tecnico.p2photo.R;


/**
 * An activity to list all available demo activities.
 */
public class HomeActivity extends Activity {
    private final Class[] sActivities = new Class[]{GoogleCreateFolderActivity.class};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String[] titles = getResources().getStringArray(R.array.titles_array);
        ListView mListViewSamples = (ListView) findViewById(R.id.listViewSamples);
        mListViewSamples.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles));
        mListViewSamples.setOnItemClickListener((arg0, arg1, i, arg3) -> {
            Intent intent = new Intent(getBaseContext(), sActivities[i]);
            startActivity(intent);
        });
    }
}