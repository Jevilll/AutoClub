package com.example.jevil.autoclub.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.jevil.autoclub.Models.GroupModel;
import com.example.jevil.autoclub.R;
import com.example.jevil.autoclub.Views.GroupMenageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupsViewHolder> {

    private List<GroupModel> list;
    private String currentGroup;
    private Context context;
    // получаем нашу базу
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    // ссылка на группы
    DatabaseReference groupsForRequestRef = database.getReference("GroupsRequest");

    // ссылка на логин пользователя
    DatabaseReference usersRef = database.getReference("Users");

    // ссылка на группы
    DatabaseReference groupsRef = database.getReference("Groups");

    // ссылка на чат
    private DatabaseReference chatRef = database.getReference("Chat");

    ValueEventListener lastValueEventListener = null;

    public GroupAdapter(List<GroupModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false));
    }

    @Override
    public void onBindViewHolder(GroupsViewHolder holder, final int position) {
        final GroupModel group = list.get(position);
        holder.tvName.setText(group.getName());
        holder.tvStatus.setText(group.getStatus());

        switch (group.getStatus()) {
            case "creator":
                holder.tvStatus.setText("Моя группа");
                holder.btnAction.setText("Управлять");
                break;
            case "request":
                holder.tvStatus.setText("Запрос");
                holder.btnAction.setText("Отменить");
                break;
            case "user":
                holder.tvStatus.setText("Участник");
                holder.btnAction.setText("Выйти");
                break;
        }

        holder.btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (group.getStatus()) {
                    case "creator":
                        Intent intent = new Intent(context, GroupMenageActivity.class);
                        intent.putExtra("group", group.getName());
                        context.startActivity(intent);
                        break;
                    case "request":
                        // удаляем запрос в группу
                        groupsForRequestRef.child(group.getName()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

                        // удаляем из моих групп
                        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("myGroups").child(group.getName()).removeValue();
                        break;
                    case "user":
                        // удаляем запись из группы
                        groupsRef.child(group.getName()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

                        // удаляем из моих групп
                        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("myGroups").child(group.getName()).removeValue();
                        break;
                }
            }
        });
    }

    public void clear() {
        list.clear();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class GroupsViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvStatus;
        private Button btnAction;

        GroupsViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvGroupName);
            tvStatus = itemView.findViewById(R.id.tvGroupStatus);
            btnAction = itemView.findViewById(R.id.btnAction);

        }
    }
}
