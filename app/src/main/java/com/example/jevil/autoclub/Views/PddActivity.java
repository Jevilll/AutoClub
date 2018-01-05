package com.example.jevil.autoclub.Views;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.jevil.autoclub.R;

public class PddActivity extends AppCompatActivity {

    TextView tvPdd;
    int[] pddArray = {R.string.pdd1, R.string.pdd2, R.string.pdd3, R.string.pdd4, R.string.pdd5, R.string.pdd6, R.string.pdd7, R.string.pdd8, R.string.pdd9, R.string.pdd10,
            R.string.pdd11, R.string.pdd12, R.string.pdd13, R.string.pdd14, R.string.pdd15, R.string.pdd16, R.string.pdd17, R.string.pdd18, R.string.pdd19, R.string.pdd20,
            R.string.pdd21, R.string.pdd22, R.string.pdd23, R.string.pdd24, R.string.pdd25};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdd);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("ПДД");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0);

        // Получаем экземпляр элемента Spinner
        final Spinner spinner = findViewById(R.id.spinnerPdd);
        tvPdd = findViewById(R.id.tvPdd);
//        tvPdd.setMovementMethod(new ScrollingMovementMethod());
        // Настраиваем адаптер
        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(this, R.array.pdd, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Вызываем адаптер
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                Resources res = getResources();
                String text = res.getString(pddArray[selectedItemPosition]);

                tvPdd.setText(text);

            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
