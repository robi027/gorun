package com.robi027.gorun.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robi027.gorun.R;
import com.robi027.gorun.model.RunData;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by robi on 4/30/2019.
 */

public class RunAdapter extends RecyclerView.Adapter<RunAdapter.RunViewHolder> {
    private Context context;
    private ArrayList<RunData> runData;

    public RunAdapter(Context context, ArrayList<RunData> runData) {
        this.context = context;
        this.runData = runData;
    }

    public class RunViewHolder extends RecyclerView.ViewHolder {
        TextView tvTgl, tvJarak, tvDurasi, tvKalori;
        public RunViewHolder(View itemView) {
            super(itemView);

            tvTgl = itemView.findViewById(R.id.tvTgl);
            tvJarak = itemView.findViewById(R.id.tvJarak);
            tvDurasi = itemView.findViewById(R.id.tvDurasi);
            tvKalori = itemView.findViewById(R.id.tvKalori);

        }
    }


    @NonNull
    @Override
    public RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_running, parent, false);
        RunViewHolder runViewHolder = new RunViewHolder(v);
        return runViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RunViewHolder holder, int position) {
        holder.tvTgl.setText(runData.get(position).getTgl());
        holder.tvJarak.setText(runData.get(position).getJarak());
        holder.tvDurasi.setText(runData.get(position).getTimer());
        holder.tvKalori.setText(runData.get(position).getKalori());
    }

    @Override
    public int getItemCount() {
        return runData.size();
    }
}
