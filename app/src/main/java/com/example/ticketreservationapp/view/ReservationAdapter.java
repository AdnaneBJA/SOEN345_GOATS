package com.example.ticketreservationapp.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.model.Reservation;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.VH> {

    public interface OnCancelClick {
        void onCancel(Reservation reservation);
    }

    private List<Reservation> reservations = new ArrayList<>();
    private final OnCancelClick onCancelClick;

    public ReservationAdapter(OnCancelClick onCancelClick) {
        this.onCancelClick = onCancelClick;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations == null ? new ArrayList<>() : reservations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Reservation r = reservations.get(position);
        holder.title.setText(r.getEventTitle());
        holder.date.setText(r.getEventDate());
        holder.location.setText(r.getEventLocation());
        holder.tickets.setText(holder.itemView.getContext()
                .getString(R.string.tickets_count, r.getNumberOfTickets()));
        holder.total.setText(String.format(Locale.US, "$%.2f", r.getTotalPrice()));
        holder.code.setText(holder.itemView.getContext()
                .getString(R.string.confirmation_code_label, r.getConfirmationCode()));
        holder.btnCancel.setOnClickListener(v -> {
            if (onCancelClick != null) onCancelClick.onCancel(r);
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, date, location, tickets, total, code;
        MaterialButton btnCancel;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_res_title);
            date = itemView.findViewById(R.id.tv_res_date);
            location = itemView.findViewById(R.id.tv_res_location);
            tickets = itemView.findViewById(R.id.tv_res_tickets);
            total = itemView.findViewById(R.id.tv_res_total);
            code = itemView.findViewById(R.id.tv_res_code);
            btnCancel = itemView.findViewById(R.id.btn_cancel_reservation);
        }
    }
}
