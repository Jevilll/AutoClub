package com.example.jevil.autoclub.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jevil.autoclub.Views.GroupRequestActivity;
import com.example.jevil.autoclub.Models.GroupModel;
import com.example.jevil.autoclub.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CreateGroupAdapter extends RecyclerView.Adapter<CreateGroupAdapter.GroupsViewHolder>{

    private List<GroupModel> list;
    private Context context;
    // получаем нашу базу
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    // ссылка на группы
    DatabaseReference groupsForRequestRef = database.getReference("GroupsRequest");

    // ссылка на логин пользователя
    DatabaseReference usersRef = database.getReference("Users");

    // ссылка на группы
    DatabaseReference groupsRef = database.getReference("Groups");

    public CreateGroupAdapter(List<GroupModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_for_add, parent, false));
    }

    @Override
    public void onBindViewHolder(GroupsViewHolder holder, final int position) {
        final GroupModel group = list.get(position);
        holder.tvName.setText(group.getName());
        String count;
        if (group.getCount() != 0) {
            count = "Запросов: " + group.getCount();
        } else {
            count = "Запросов нет";
        }

        holder.tvCount.setText(count);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupRequestActivity.class);
                intent.putExtra("group", group.getName());
                context.startActivity(intent);

//                Log.d("myLog", group.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class GroupsViewHolder extends RecyclerView.ViewHolder{
        private TextView tvName, tvCount;
        GroupsViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCount = itemView.findViewById(R.id.tvCount);

        }


    }
}
