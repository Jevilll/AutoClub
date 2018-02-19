package com.example.jevil.autoclub.Views;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.jevil.autoclub.Adapters.RequestGroupAdapter;
import com.example.jevil.autoclub.Models.GroupRequestModel;
import com.example.jevil.autoclub.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class GroupRequestActivity extends AppCompatActivity {

    String group;

    private RecyclerView recyclerViewRequest;
    public List<GroupRequestModel> resultRequest;
    private RequestGroupAdapter createGroupAdapter;


    // получаем нашу базу
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // ссылка на логин пользователя
    DatabaseReference usersRef = database.getReference("Users");

    // ссылка на группы с запросами
    DatabaseReference groupsForRequestRef = database.getReference("GroupsRequest");

//    // ссылка на группы
//    DatabaseReference groupsRef = database.getReference("Groups");

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_request);
        group = getIntent().getStringExtra("group");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(group);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        context = this;

        resultRequest = new ArrayList<>();
        recyclerViewRequest = findViewById(R.id.rvGroupRequest);
        recyclerViewRequest.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewRequest.setLayoutManager(llm);
        createGroupAdapter = new RequestGroupAdapter(resultRequest);
        recyclerViewRequest.setAdapter(createGroupAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createGroupAdapter.clear();
        resultRequest.clear();
        updateListRequest(group);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public int getItemIndex(String uid) {
        int index = -1;
        for (int i = 0; i < resultRequest.size(); i++) {
            if (resultRequest.get(i).getUid().equals(uid)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void updateListRequest(String currentGroup) {
        groupsForRequestRef.child(currentGroup).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                resultRequest.add(dataSnapshot.getValue(GroupRequestModel.class));
                createGroupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String uid = dataSnapshot.getKey();
                int index = getItemIndex(uid);
                try {
                    resultRequest.remove(index);
                } catch (Exception e) {
                    Log.d("myLog", e.getMessage());
                }
                createGroupAdapter.notifyItemRemoved(index);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
