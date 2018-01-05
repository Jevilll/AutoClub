package com.example.jevil.autoclub.Views;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jevil.autoclub.Adapters.GroupAdapter;
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

public class GroupsActivity extends AppCompatActivity {

    // получаем нашу базу
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // ссылка на логин пользователя
    DatabaseReference usersRef = database.getReference("Users");

    // ссылка на группы с запросами
    DatabaseReference groupsForRequestRef = database.getReference("GroupsRequest");

    // ссылка на группы
    DatabaseReference groupsRef = database.getReference("Groups");

    Context context;
    private UserModel currentUser;
    public List<GroupModel> result;

    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;

    @BindView(R.id.etGroupName)
    EditText etGroupName;

    @BindView(R.id.llGroupControl)
    LinearLayout llGroupControl;

    @BindView(R.id.tvMessage)
    TextView tvMessage;

    @OnClick(R.id.btnJoinGroup)
    void joinClick(Button btnJoin) {
        joinGroup(etGroupName.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Мои групы");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0);
        ButterKnife.bind(this);
        context = this;
        getCurrentUser();

        result = new ArrayList<>();
        recyclerView = findViewById(R.id.rvGroup);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        groupAdapter = new GroupAdapter(result, this);
        recyclerView.setAdapter(groupAdapter);

        updateList();
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

    public void joinGroup(final String groupName) {
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean canJoin = false;
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    if (groupSnapshot.getKey().equals(groupName)) {
                        canJoin = true;
                    }
                }
                if (canJoin) { // если группа с таким названием есть
                    // добавляем запрос к группе к пользователю
                    GroupModel creatorMyGroups = new GroupModel(groupName, "request");
                    Map<String, Object> myGroupValues = creatorMyGroups.toMap();
                    Map<String, Object> myGroup = new HashMap<>();
                    myGroup.put(groupName, myGroupValues);
                    usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("myGroups").updateChildren(myGroup);

                    // подаем заявку
                    GroupRequestModel request = new GroupRequestModel(currentUser.getNickname(), currentUser.getEmail(), currentUser.getUid(), groupName);
                    Map<String, Object> groupValues = request.toMap();
                    Map<String, Object> group = new HashMap<>();
                    group.put(currentUser.getUid(), groupValues);
                    groupsForRequestRef.child(groupName).updateChildren(group);
                    Snackbar.make(llGroupControl, "Заявка на вступление в гуппу: \"" + groupName + "\" подана", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else
                    Snackbar.make(llGroupControl, "Группы с названием \"" + groupName + "\" не существует", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public int getItemIndex(GroupModel groupName) {
        int index = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).getName().equals(groupName.getName())) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void updateList() {
        groupAdapter.clear();
        result.clear();
        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("myGroups").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                result.add(dataSnapshot.getValue(GroupModel.class));
                groupAdapter.notifyDataSetChanged();
                llGroupControl.setVisibility(View.VISIBLE);
                tvMessage.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                GroupModel model = dataSnapshot.getValue(GroupModel.class);
                int index = getItemIndex(model);
                result.get(index).setStatus(model.getStatus());
                groupAdapter.notifyItemChanged(index);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                GroupModel model = dataSnapshot.getValue(GroupModel.class);
                int index = getItemIndex(model);
                result.remove(index);
                groupAdapter.notifyItemRemoved(index);

                if (result.size() == 0) {
                    llGroupControl.setVisibility(View.GONE);
                    tvMessage.setVisibility(View.VISIBLE);
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


