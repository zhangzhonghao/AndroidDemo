package com.example.androiddemo.tools;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androiddemo.R;
import java.util.HashMap;

public class UsbDebugActivity extends AppCompatActivity {

    private UsbManager usbManager;
    private TextView tvConnectionStatus;
    private TextView tvEmptyHint;
    private RecyclerView rvDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_usb_debug);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        tvEmptyHint = findViewById(R.id.tv_empty_hint);
        rvDevices = findViewById(R.id.rv_devices);

        rvDevices.setLayoutManager(new LinearLayoutManager(this));

        refreshDeviceList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDeviceList();
    }

    private void refreshDeviceList() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        if (deviceList.isEmpty()) {
            tvConnectionStatus.setText("未连接USB设备");
            tvConnectionStatus.setTextColor(0xFF757575);
            tvEmptyHint.setVisibility(TextView.VISIBLE);
            rvDevices.setVisibility(RecyclerView.GONE);
        } else {
            tvConnectionStatus.setText("已连接 " + deviceList.size() + " 个USB设备");
            tvConnectionStatus.setTextColor(0xFF4CAF50);
            tvEmptyHint.setVisibility(TextView.GONE);
            rvDevices.setVisibility(RecyclerView.VISIBLE);

            UsbDeviceAdapter adapter = new UsbDeviceAdapter(deviceList);
            rvDevices.setAdapter(adapter);
        }
    }

    private static class UsbDeviceAdapter extends RecyclerView.Adapter<UsbDeviceAdapter.ViewHolder> {

        private final HashMap<String, UsbDevice> deviceList;

        UsbDeviceAdapter(HashMap<String, UsbDevice> deviceList) {
            this.deviceList = deviceList;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));
            textView.setPadding(32, 24, 32, 24);
            textView.setTextSize(16);
            textView.setTextColor(0xFFFFFFFF);
            return new ViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            UsbDevice device = deviceList.get(deviceList.keySet().toArray()[position]);
            if (device != null) {
                holder.textView.setText(device.getDeviceName() + "\n"
                        + "Vendor: 0x" + Integer.toHexString(device.getVendorId()) + " | "
                        + "Product: 0x" + Integer.toHexString(device.getProductId()));
            }
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textView;
            ViewHolder(TextView textView) {
                super(textView);
                this.textView = textView;
            }
        }
    }
}