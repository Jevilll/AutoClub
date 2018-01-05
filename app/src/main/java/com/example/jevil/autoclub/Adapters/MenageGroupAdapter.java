package com.example.jevil.autoclub.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.jevil.autoclub.Models.GroupRequestModel;
import com.example.jevil.autoclub.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MenageGroupAdapter extends RecyclerView.Adapter<MenageGroupAdapter.GroupsViewHolder>{

    private List<GroupRequestModel> list;
    // получаем нашу базу
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    // ссылка на группы
    DatabaseReference groupsForRequestRef = database.getReference("GroupsRequest");

    // ссылка на логин пользователя
    DatabaseReference usersRef = database.getReference("Users");

    // ссылка на группы
    DatabaseReference groupsRef = database.getReference("Groups");

    public MenageGroupAdapter(List<GroupRequestModel> list) {
        this.list = list;
    }

    @Override
    public GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_for_menage, parent, false));
    }

    @Override
    public void onBindViewHolder(GroupsViewHolder holder, final int position) {
        final GroupRequestModel user = list.get(position);
        holder.tvEmail.setText(user.getEmail());
        holder.tvName.setText(user.getNickname());

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // удаляем запись из группы
                groupsRef.child(user.getGroup()).child(user.getUid()).removeValue();

                // удаляем из групп у пользователя
                usersRef.child(user.getUid()).child("myGroups").child(user.getGroup()).removeValue();

                list.remove(position);
                notifyItemRemoved(position);
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
        private TextView tvName, tvEmail;
        private Button btnRemove;
        GroupsViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvGroupForAddName);
            tvEmail = itemView.findViewById(R.id.tvGroupForAddEmail);
            btnRemove = itemView.findViewById(R.id.btnRemove);

        }
    }
}
