package com.example.wuziqidemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView tv_mode;
    private Wuziqipanel id_gomoku;
    private Button retract_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        tv_mode.setText(R.string.AI_mode);
        retract_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id_gomoku != null) {
                    id_gomoku.regretLastStep();
                }
            }
        });
    }

    private void bindViews() {
        tv_mode = findViewById(R.id.tv_mode);
        id_gomoku = findViewById(R.id.id_gomoku);
        retract_btn = findViewById(R.id.retract_btn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_AI) {
            id_gomoku.start(true);
            tv_mode.setText(R.string.AI_mode);
            return true;
        } else if (id == R.id.action_People) {
            id_gomoku.start(false);
            tv_mode.setText(R.string.People_mode);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
