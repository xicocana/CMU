package pt.ulisboa.tecnico.p2photo.wifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.p2photo.R;

public class SearchUsersActivityWifi extends AppCompatActivity {

    List<String> usersList = new ArrayList<>();
    private static final String TAG = "UserListActivityWifi";
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users_wifi);

        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        name = pref.getString("username", null);

        getWifiUsers();
    }

    private void getWifiUsers() {
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, usersList);
        final ListView usersViewList = (ListView) findViewById(R.id.user_list);
        usersViewList.setAdapter(arrayAdapter);
    }
}
