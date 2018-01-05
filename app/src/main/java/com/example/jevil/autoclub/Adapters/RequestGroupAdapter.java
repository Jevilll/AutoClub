package com.example.jevil.autoclub.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.jevil.autoclub.Models.GroupModel;
import com.example.jevil.autoclub.Models.GroupRequestModel;
import com.example.jevil.autoclub.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestGroupAdapter extends RecyclerView.Adapter<RequestGroupAdapter.GroupsViewHolder>{

    private List<GroupRequestModel> list;
    // получаем нашу базу
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    // ссылка на группы
    DatabaseReference groupsForRequestRef = database.getReference("GroupsRequest");

    // ссылка на логин пользователя
    DatabaseReference usersRef = database.getReference("Users");

    // ссылка на группы
    DatabaseReference groupsRef = database.getReference("Groups");

    public RequestGroupAdapter(List<GroupRequestModel> list) {
        this.list = list;
    }

    @Override
    public GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_for_request, parent, false));
    }

    @Override
    public void onBindViewHolder(GroupsViewHolder holder, final int position) {
        final GroupRequestModel user = list.get(position);
        holder.tvEmail.setText(user.getEmail());
        holder.tvName.setText(user.getNickname());
        final GroupRequestModel groupRequest = list.get(position);
        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // вступаем в группу
                GroupRequestModel creator = new GroupRequestModel(user.getNickname(), user.getEmail(), user.getUid(), user.getGroup());
                Map<String, Object> groupValues = creator.toMap();
                Map<String, Object> group = new HashMap<>();
                group.put(user.getUid(), groupValues);
                groupsRef.child(user.getGroup()).updateChildren(group);

                // добавляем в мои группы
                GroupModel creatorMyGroups = new GroupModel(user.getGroup(), "user");
                Map<String, Object> myGroupValues = creatorMyGroups.toMap();
                Map<String, Object> myGroup = new HashMap<>();
                myGroup.put(user.getGroup(), myGroupValues);
                usersRef.child(user.getUid()).child("myGroups").updateChildren(myGroup);

                // удаляем запись из списка на добавление
                groupsForRequestRef.child(user.getGroup()).child(user.getUid()).removeValue();
                list.remove(position);
                notifyItemRemoved(position);

            }
        });
        holder.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("myLog", "reject " + groupRequest.getEmail());
                groupsForRequestRef.child(user.getGroup()).child(user.getUid()).removeValue();
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
        private Button btnAccept, btnReject;
        GroupsViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvGroupForAddName);
            tvEmail = itemView.findViewById(R.id.tvGroupForAddEmail);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);

        }
    }
}
