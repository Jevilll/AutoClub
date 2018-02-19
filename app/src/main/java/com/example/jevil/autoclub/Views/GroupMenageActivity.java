package com.example.jevil.autoclub.Views;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.jevil.autoclub.Adapters.UsersGroupAdapter;
import com.example.jevil.autoclub.Adapters.RequestGroupAdapter;
import com.example.jevil.autoclub.Models.GroupRequestModel;
import com.example.jevil.autoclub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupMenageActivity extends AppCompatActivity {

    String group;

    // запросы
    private RecyclerView recyclerViewRequest;
    public List<GroupRequestModel> resultRequest;
    private RequestGroupAdapter createGroupAdapter;

    // пользователи
    private RecyclerView recyclerView;
    public List<GroupRequestModel> result;
    private UsersGroupAdapter menageGroupAdapter;


    // получаем нашу базу
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // ссылка на запроссы к группе
    DatabaseReference groupsForRequestRef = database.getReference("GroupsRequest");

    // ссылка на логин пользователя
    DatabaseReference usersRef = database.getReference("Users");

    // ссылка на чат
    private DatabaseReference chatRef = database.getReference("Chat");

    // ссылка на группы
    DatabaseReference groupsRef = database.getReference("Groups");

    Context context;

    TextView tvRequest, tvUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_menage);
        group = getIntent().getStringExtra("group");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(group);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        context = this;

        tvRequest = findViewById(R.id.tvRequest);
        tvUsers = findViewById(R.id.tvUsers);

        // запросы
        resultRequest = new ArrayList<>();
        recyclerViewRequest = findViewById(R.id.rvGroupRequest);
        recyclerViewRequest.setHasFixedSize(true);
        LinearLayoutManager llmRequest = new LinearLayoutManager(context);
        llmRequest.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewRequest.setLayoutManager(llmRequest);
        createGroupAdapter = new RequestGroupAdapter(resultRequest);
        recyclerViewRequest.setAdapter(createGroupAdapter);

        // пользователи
        result = new ArrayList<>();
        recyclerView = findViewById(R.id.rvGroup);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        menageGroupAdapter = new UsersGroupAdapter(result);
        recyclerView.setAdapter(menageGroupAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // запросы
        createGroupAdapter.clear();
        resultRequest.clear();
        updateListRequest(group);

        // пользователи
        menageGroupAdapter.clear();
        result.clear();
        updateListUsers(group);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menage_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            // удаляем группу
            groupsRef.child(group).removeValue();

            // удаляем запросы к группе
            groupsForRequestRef.child(group).removeValue();

            // удаляем чат группы
            chatRef.child(group).removeValue();

            // удаляем группу у пользователей
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot groupsSnapshot : userSnapshot.child("myGroups").getChildren()) {
                            if (groupsSnapshot.getKey().equals(group)) {
                                groupsSnapshot.getRef().removeValue();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public int getItemIndex(String uid, List<GroupRequestModel> list) {
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUid().equals(uid)) {
                index = i;
                break;
            }
        }
        return index;
    }

    // пользователи
    public void updateListUsers(String currentGroup) {
        groupsRef.child(currentGroup).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // исключаем себя из списка на удаление
                if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(dataSnapshot.getValue(GroupRequestModel.class).getUid())) {
                    result.add(dataSnapshot.getValue(GroupRequestModel.class));
                    menageGroupAdapter.notifyDataSetChanged();
//                    if (result.size() != 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        tvUsers.setText("Пользователи");
                        tvUsers.setGravity(Gravity.END);
//                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String uid = dataSnapshot.getKey();
                int index = getItemIndex(uid, result);
                try {
                    result.remove(index);
                    if (result.size() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        tvUsers.setText("Пользователей нет");
                        tvUsers.setGravity(Gravity.CENTER);
                    }
                } catch (Exception e) {
                    Log.d("myLog", e.getMessage());
                }
                menageGroupAdapter.notifyItemRemoved(index);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // запросы
    public void updateListRequest(String currentGroup) {
        groupsForRequestRef.child(currentGroup).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                resultRequest.add(dataSnapshot.getValue(GroupRequestModel.class));
                createGroupAdapter.notifyDataSetChanged();
//                if (resultRequest.size() != 0) {
                    recyclerViewRequest.setVisibility(View.VISIBLE);
                    tvRequest.setText("Заявки");
                tvRequest.setGravity(Gravity.END);
//                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String uid = dataSnapshot.getKey();
                int index = getItemIndex(uid, resultRequest);
                try {
                    resultRequest.remove(index); // тут инденс -1
                    createGroupAdapter.notifyItemRemoved(index);
                    if (resultRequest.size() == 0) {
                        recyclerViewRequest.setVisibility(View.GONE);
                        tvRequest.setText("Запросов нет");
                        tvRequest.setGravity(Gravity.CENTER);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("myLog", e.getMessage());
                }

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
