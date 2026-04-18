package com.example.androiddemo.tools;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class FraudAppAdapter extends RecyclerView.Adapter<FraudAppAdapter.FraudAppViewHolder> {

    private final List<FraudApp> fraudAppList = new ArrayList<>();
    private OnUninstallClickListener listener;

    public interface OnUninstallClickListener {
        void onUninstallClick(FraudApp fraudApp);
    }

    public void setOnUninstallClickListener(OnUninstallClickListener listener) {
        this.listener = listener;
    }

    public void setFraudAppList(List<FraudApp> list) {
        fraudAppList.clear();
        fraudAppList.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FraudAppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fraud_app, parent, false);
        return new FraudAppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FraudAppViewHolder holder, int position) {
        holder.bind(fraudAppList.get(position));
    }

    @Override
    public int getItemCount() {
        return fraudAppList.size();
    }

    class FraudAppViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivIcon;
        private final TextView tvAppName;
        private final TextView tvPackageName;
        private final TextView tvRiskType;
        private final TextView tvDescription;
        private final Button btnUninstall;

        FraudAppViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_app_icon);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            tvPackageName = itemView.findViewById(R.id.tv_package_name);
            tvRiskType = itemView.findViewById(R.id.tv_risk_type);
            tvDescription = itemView.findViewById(R.id.tv_description);
            btnUninstall = itemView.findViewById(R.id.btn_uninstall);
        }

        void bind(FraudApp fraudApp) {
            if (fraudApp.getIcon() != null) {
                ivIcon.setImageDrawable(fraudApp.getIcon());
            } else {
                ivIcon.setImageResource(R.drawable.ic_image_placeholder);
            }
            tvAppName.setText(fraudApp.getAppName());
            tvPackageName.setText(fraudApp.getPackageName());
            tvRiskType.setText(fraudApp.getRiskType());
            tvDescription.setText(fraudApp.getDescription());

            btnUninstall.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUninstallClick(fraudApp);
                }
            });
        }
    }
}