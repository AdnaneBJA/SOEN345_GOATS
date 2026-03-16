package com.example.ticketreservationapp.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(event.getDate());
        holder.tvLocation.setText(event.getLocation());
        holder.tvPrice.setText(String.format(Locale.US, "$%.2f", event.getPrice()));
        holder.tvCategory.setText(event.getCategory());

        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvDate;
        final TextView tvLocation;
        final TextView tvPrice;
        final TextView tvCategory;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvDate = itemView.findViewById(R.id.tv_event_date);
            tvLocation = itemView.findViewById(R.id.tv_event_location);
            tvPrice = itemView.findViewById(R.id.tv_event_price);
            tvCategory = itemView.findViewById(R.id.tv_event_category);
        }
    }
}
