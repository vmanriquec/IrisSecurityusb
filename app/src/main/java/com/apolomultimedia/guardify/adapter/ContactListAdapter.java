package com.apolomultimedia.guardify.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.model.ContactModel;
import com.apolomultimedia.guardify.util.RecyclerViewOnItemClickListener;

import java.util.ArrayList;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {

    private ArrayList<ContactModel> contactsList = new ArrayList<>();
    private RecyclerViewOnItemClickListener recyclerViewOnItemClickListener;

    public ContactListAdapter(ArrayList<ContactModel> contactsList,
                              RecyclerViewOnItemClickListener recyclerViewOnItemClickListener) {
        this.contactsList = contactsList;
        this.recyclerViewOnItemClickListener = recyclerViewOnItemClickListener;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        ContactModel model = contactsList.get(position);
        holder.getTv_name().setText(model.getName());

        String txt = "";
        String cod = model.getCod();
        if (cod.equals("1")) {
            txt = model.getPhone();
        } else {
            txt = model.getEmail();
        }
        holder.getTv_string().setText(txt);

        String selected = model.getSelected();
        if (selected != null) {
            if (selected.equals("1")) {
                holder.getCb_selected().setChecked(true);
            } else {
                holder.getCb_selected().setChecked(false);
            }
        } else {
            holder.getCb_selected().setChecked(false);
        }

    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView tv_name, tv_string;
        private CheckBox cb_selected;
        private RelativeLayout rl_main;

        public ContactViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_string = (TextView) itemView.findViewById(R.id.tv_string);
            cb_selected = (CheckBox) itemView.findViewById(R.id.cb_selected);
            cb_selected.setOnClickListener(this);
            rl_main = (RelativeLayout) itemView.findViewById(R.id.rl_main);
            rl_main.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    recyclerViewOnItemClickListener.onItemLongCLick(v, getAdapterPosition());
                    return true;
                }
            });
        }

        public CheckBox getCb_selected() {
            return cb_selected;
        }

        public TextView getTv_name() {
            return tv_name;
        }

        public TextView getTv_string() {
            return tv_string;
        }

        @Override
        public void onClick(View v) {
            recyclerViewOnItemClickListener.onClick(v, getAdapterPosition());
        }
    }

}
