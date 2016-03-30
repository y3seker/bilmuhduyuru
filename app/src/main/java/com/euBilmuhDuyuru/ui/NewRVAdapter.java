package com.euBilmuhDuyuru.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.euBilmuhDuyuru.models.Annc;
import com.euBilmuhDuyuru.R;

import java.util.ArrayList;
import java.util.List;


public class NewRVAdapter extends RecyclerView.Adapter<NewRVAdapter.NewAnncHolder>
        implements View.OnClickListener, View.OnLongClickListener {


    public interface OnItemClickListener {
        void onItemClick(View v, Annc annc, int pos);

        void onItemLongClick(View v, Annc annc, int pos);
    }

    private Context mContext;
    private List<Annc> liste, filtered, origin;
    private int layoutID;
    private OnItemClickListener listener;

    public NewRVAdapter(Context context, int layoutID) {
        mContext = context;
        liste = new ArrayList<>();
        filtered = new ArrayList<>();
        this.layoutID = layoutID;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void clearList() {
        liste.clear();
        notifyDataSetChanged();
    }

    public void addList(List<Annc> list) {
        liste.addAll(0, list);
        notifyItemRangeInserted(0, list.size());
    }

    public void changeList(List<Annc> list) {
        liste.clear();
        liste.addAll(list);
        origin = list;
        notifyDataSetChanged();
    }

    public void addItem(Annc annc) {
        liste.add(annc);
        notifyItemInserted(liste.size() - 1);
    }

    @Override
    public NewAnncHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(layoutID, viewGroup, false);
        NewAnncHolder holder = new NewAnncHolder(v);
        holder.cardView.setOnClickListener(this);
        holder.cardView.setOnLongClickListener(this);
        holder.cardView.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(NewAnncHolder anncHolder, int i) {
        anncHolder.title.setText(liste.get(i).getTitle());
        anncHolder.date.setText(liste.get(i).getDate());
    }

    @Override
    public void onClick(View v) {
        NewAnncHolder holder = (NewAnncHolder) v.getTag();
        int pos = holder.getAdapterPosition();

        if (listener != null)
            listener.onItemClick(v, liste.get(pos), pos);
    }

    @Override
    public boolean onLongClick(View v) {
        NewAnncHolder holder = (NewAnncHolder) v.getTag();
        int pos = holder.getAdapterPosition();

        if (listener != null)
            listener.onItemLongClick(v, liste.get(pos), pos);

        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemCount() {
        if (liste == null)
            return 0;
        return liste.size();
    }


    public static class NewAnncHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView date;
        public CardView cardView;

        public NewAnncHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.row_title);
            date = (TextView) v.findViewById(R.id.row_date);
            cardView = (CardView) v.findViewById(R.id.row_card);
        }
    }
}