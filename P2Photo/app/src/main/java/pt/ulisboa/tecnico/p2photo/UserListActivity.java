package pt.ulisboa.tecnico.p2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        final ArrayList<String> usersList = new ArrayList<String>();
        //cenas
        usersList.add("user1");
        usersList.add("user2");
        usersList.add("user3");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, usersList);
        final ListView usersViewList = (ListView) findViewById(R.id.user_list);
        usersViewList.setAdapter(arrayAdapter);


        Button addButton = (Button) findViewById(R.id.add_btn);
        addButton.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             ArrayList<String> checkedList = new ArrayList<>();
                                             int len = usersViewList.getCount();
                                             SparseBooleanArray checked = usersViewList.getCheckedItemPositions();
                                             for (int i = 0; i < len; i++)
                                                 if (checked.get(i)) {

                                                     checkedList.add(usersList.get(i));
                                                     Log.i("OnClickAddUsers", usersList.get(i));
                                                 }


                                         }
                                     }


        );

    }

}
