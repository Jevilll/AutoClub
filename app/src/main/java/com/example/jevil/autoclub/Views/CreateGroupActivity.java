package com.example.jevil.autoclub.Views;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jevil.autoclub.Adapters.CreateGroupAdapter;
import com.example.jevil.autoclub.Models.GroupModel;
import com.example.jevil.autoclub.Models.GroupRequestModel;
import com.example.jevil.autoclub.R;
import com.example.jevil.autoclub.Models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateGroupActivity extends AppCompatActivity {

    // получаем нашу базу
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // ссылка на логин пользователя
    DatabaseReference usersRef = database.getReference("Users");

    // ссылка на группы с запросами
    DatabaseReference groupsForRequestRef = database.getReference("GroupsRequest");

    // ссылка на группы
    DatabaseReference groupsRef = database.getReference("Groups");

    Context context;

    @OnClick(R.id.btnCreateGroup)
    void createClick(Button btnCreateGroup) {
        createGroup(etGroupName.getText().toString());
    }

    @BindView(R.id.llGroupControl)
    LinearLayout llGroupControl;

    @BindView(R.id.tvCreateYourGroup)
    TextView tvCreateYourGroup;

    private RecyclerView recyclerView;
    public List<GroupModel> result;
    private CreateGroupAdapter createGroupAdapter;
    private List<String> myGroups;

    private UserModel currentUser;

    @BindView(R.id.etGroupName)
    EditText etGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Создание группы");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        context = this;

        // если это моя группа, добавляем
        myGroups = new ArrayList<>();
        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("myGroups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // проверяем уникальность названия группы
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    if (groupSnapshot.getValue(GroupModel.class).getStatus().equals("creator"))
                    myGroups.add(groupSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        result = new ArrayList<>();
        updateList();

        recyclerView = findViewById(R.id.rvGroup);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        createGroupAdapter = new CreateGroupAdapter(result, context);
        recyclerView.setAdapter(createGroupAdapter);


        // получаем данные текущего юзера
        getCurrentUser();

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

    public int getItemIndex(String groupName) {
        int index = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).getName().equals(groupName)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void updateList() {

        groupsForRequestRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                if (myGroups.contains(dataSnapshot.getKey())) {
                    result.add(new GroupModel(dataSnapshot.getKey(), dataSnapshot.getChildrenCount()));
                    createGroupAdapter.notifyDataSetChanged();
                    llGroupControl.setVisibility(View.VISIBLE);
                    tvCreateYourGroup.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String groupName = dataSnapshot.getKey();
                int index = getItemIndex(groupName);
                result.get(index).setCount(dataSnapshot.getChildrenCount());
                createGroupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String groupName = dataSnapshot.getKey();
                int index = getItemIndex(groupName);
                try {
                    result.remove(index);
                } catch (Exception e) {
                    Log.d("myLog", e.getMessage());
                }
                createGroupAdapter.notifyItemRemoved(index);
                if (result.size() == 0) {
                    llGroupControl.setVisibility(View.GONE);
                    tvCreateYourGroup.setVisibility(View.VISIBLE);
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


    public void createGroup(final String groupName) {
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean canAdd = true;
                // проверяем уникальность названия группы
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    if (groupSnapshot.getKey().equals(groupName)) {
                        canAdd = false;
                    }
                }
                if (canAdd) { // если группы с таким называнием нет
                    // добавляем группу к пользователю

                    GroupModel creatorMyGroups = new GroupModel(groupName, "creator");
                    Map<String, Object> myGroupValues = creatorMyGroups.toMap();
                    Map<String, Object> myGroup = new HashMap<>();
                    myGroup.put(groupName, myGroupValues);
                    usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("myGroups").updateChildren(myGroup);

                    // вступаем в созданную группу
                    GroupRequestModel creator = new GroupRequestModel(currentUser.getNickname(), currentUser.getEmail(), currentUser.getUid(), groupName);
                    Map<String, Object> groupValues = creator.toMap();
                    Map<String, Object> group = new HashMap<>();
                    group.put(currentUser.getUid(), groupValues);
                    groupsRef.child(groupName).updateChildren(group);

                    Snackbar.make(llGroupControl, "Группа \"" + groupName + " \" создана", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                } else
                    Snackbar.make(llGroupControl, "Группа \"" + groupName + "\" уже существует", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getCurrentUser() {
        // получаем данные текущего пользователя
        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(UserModel.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}




