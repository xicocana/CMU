package pt.ulisboa.tecnico.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    ArrayList<String> usersList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        getUserList();
    }

    public void getUserList() {
        ClientServerComms clientServerComms = new ClientServerComms(this.getApplicationContext());
        boolean state = clientServerComms.sendGetUsers();
        proceedAccordingToState(clientServerComms, state);
    }

    private void proceedAccordingToState(ClientServerComms clientServerComms, boolean state) {
        if(state) {
            //get session key
            this.usersList = (ArrayList<String>) clientServerComms.getContent();

            for(int i = 0; i<this.usersList.size(); i++) {
                System.out.println(usersList.get(i));
            }
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, this.usersList);
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
                         //TODO por isto a adicionar os utilizadores a drive e tambem no servidor
                         if (checked.get(i)) {
                             checkedList.add(usersList.get(i));
                             Log.i("OnClickAddUsers", usersList.get(i));
                         }
                    }
                }
            );
        }
        else {}
    }
}
