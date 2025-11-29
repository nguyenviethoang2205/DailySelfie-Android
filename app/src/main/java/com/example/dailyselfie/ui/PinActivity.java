package com.example.dailyselfie.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dailyselfie.R;
import com.example.dailyselfie.utils.SecurityPrefs;

public class PinActivity extends AppCompatActivity {

    private boolean isSetupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        TextView txtTitle = findViewById(R.id.txtPinTitle);
        EditText edtPin = findViewById(R.id.edtPin);
        Button btnConfirm = findViewById(R.id.btnConfirmPin);
        Button btnRemove = findViewById(R.id.btnRemovePin);

        isSetupMode = getIntent().getBooleanExtra("SETUP_MODE", false);

        if (isSetupMode) {
            txtTitle.setText("Tạo mã PIN mới");
            btnRemove.setVisibility(View.VISIBLE);
        } else {
            txtTitle.setText("Nhập mã PIN để mở");
            btnRemove.setVisibility(View.GONE);
        }

        btnConfirm.setOnClickListener(v -> {
            String pinInput = edtPin.getText().toString();

            if (pinInput.length() < 4) {
                Toast.makeText(this, "Vui lòng nhập đủ 4 số", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isSetupMode) {
                SecurityPrefs.setPin(this, pinInput);
                Toast.makeText(this, "Đã bật bảo mật!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String savedPin = SecurityPrefs.getPin(this);
                if (pinInput.equals(savedPin)) {
                    Intent intent = new Intent(PinActivity.this, MainActivity.class);
                    intent.putExtra("IS_UNLOCKED", true);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Sai mật khẩu!", Toast.LENGTH_SHORT).show();
                    edtPin.setText("");
                }
            }
        });

        btnRemove.setOnClickListener(v -> {
            SecurityPrefs.clearSecurity(this);
            Toast.makeText(this, "Đã tắt bảo mật", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (isSetupMode) {
            super.onBackPressed();
        } else {
            finishAffinity();
        }
    }
}