package com.utar.habit;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitTracker extends AppCompatActivity {

    private SQLiteAdapter mySQLiteAdapter;
    private String lastSelectedDate;
    int hour,minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String userID = "empty";
        if (extras != null) {
            userID = extras.getString("userID");
        }
        if(userID.equals("empty")){
            Toast.makeText(getApplicationContext(), "Something went wrong, please login again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(HabitTracker.this, Login.class));
            finish();
        }
        setContentView(R.layout.activity_habit_tracker);
        setOnChangeEvent(userID);
        setOnClickEvent(userID);
        setCurrentTime();
    }

    public void setOnClickEvent(String userID){
        //Click Event for Date Edit Text
        EditText date = findViewById(R.id.date_text);
        date.setOnClickListener(v -> showDatePicker());

        //Click Event for Calendar Icon
        ImageView img = findViewById(R.id.calendar);
        img.setOnClickListener(v -> showDatePicker());

        //Click Event for Insert Button
        Button insert_btn = findViewById(R.id.insert_btn);
        insert_btn.setOnClickListener(v -> showDialog(userID));

        //Click Event for Home Button
        Button home_btn = findViewById(R.id.home_btn);
        home_btn.setOnClickListener(v -> startActivity(new Intent(HabitTracker.this, Home.class)));
        //Click Event for Timer Button
        Button timer_btn = findViewById(R.id.timer_btn);
        timer_btn.setOnClickListener(v -> startActivity(new Intent(HabitTracker.this, Timer.class)));
        //Click Event for Todolist Button
        Button todolist_btn = findViewById(R.id.todolist_btn);
        todolist_btn.setOnClickListener(v -> startActivity(new Intent(HabitTracker.this, Todolist.class)));
    }

    public void setOnChangeEvent(String userID){
        EditText date_text = findViewById(R.id.date_text);

        date_text.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                setTableRecord(userID);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    public void setCurrentTime() {
        //Set current time
        EditText date = findViewById(R.id.date_text);
        String currDate = new SimpleDateFormat("EE, dd MMM yyyy", Locale.getDefault()).format(new Date());
        date.setText(currDate);
        String[] pickerDate = currDate.split(" ");
        lastSelectedDate = pickerDate[1];
    }

    @SuppressLint("SetTextI18n")
    public void setTotalAmount(List<List<String>> recordList){
        int total = 0;
        for (List<String> r : recordList) {
            total += Integer.parseInt(r.get(0));
        }
        TextView totalML = findViewById(R.id.total_water_ml);
        TextView totalCup = findViewById(R.id.total_water_cup);
        totalML.setText(total + "ml");
        totalCup.setText(String.valueOf(total/250));
    }

    public void setTableRecord(String userID){
        EditText date = findViewById(R.id.date_text);
        String[] pickerDate = date.getText().toString().split(" ");
        if(!pickerDate[1].equals(lastSelectedDate)) {
            lastSelectedDate = pickerDate[1];
            TableLayout tl = findViewById(R.id.table_data);
            tl.removeAllViews();
            mySQLiteAdapter = new SQLiteAdapter(this);
            mySQLiteAdapter.openToRead();
            List<String> records = mySQLiteAdapter.queueByIDANDDate(userID, pickerDate[1]);
            mySQLiteAdapter.close();
            List<List<String>> recordList = sortRecord(records);
            setTotalAmount(recordList);
            if(recordList.size() == 0) {
                TableRow tr = new TableRow(this);
                TextView empty = new TextView(this);
                empty.setTextColor(Color.parseColor("#FFFFFF"));
                empty.setBackgroundResource(R.drawable.table_border);
                empty.setTextSize(20);
                empty.setTypeface(null, Typeface.BOLD);
                empty.setGravity(1);
                empty.setWidth(350);
                empty.setText("You do not have any record today!\nDrink more water!");
                tr.addView(empty);
                tl.addView(tr);
            } else {
                for (List<String> r : recordList) {
                    TableRow tr = addRow(r);
                    tl.addView(tr);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private TableRow addRow(List<String> recordList) {
        TableRow tr = new TableRow(this);
        Dialog dialog = new Dialog(this,R.style.DialogStyle);
        dialog.setContentView(R.layout.popup_habit_edit);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tr.setOnClickListener(v -> {
            TableRow t = (TableRow) v;
            TextView time_tv = (TextView) t.getChildAt(0);
            TextView amt_tv = (TextView) t.getChildAt(1);
            TextView id_tv = (TextView) t.getChildAt(2);
            String time = time_tv.getText().toString();
            String amt = amt_tv.getText().toString();
            if(amt.contains("ml")){
                amt = amt.replace("ml", "");
            }
            String id = id_tv.getText().toString();
            showEditDialog(time,amt,id);
        });
        TextView amt = new TextView(this);
        TextView time = new TextView(this);
        TextView id = new TextView(this);
        id.setVisibility(View.GONE);
        time.setTextColor(Color.parseColor("#FFFFFF"));
        amt.setTextColor(Color.parseColor("#FFFFFF"));
        time.setBackgroundResource(R.drawable.table_border);
        amt.setBackgroundResource(R.drawable.table_border);
        time.setTextSize(20);
        amt.setTextSize(20);
        time.setTypeface(null, Typeface.BOLD);
        amt.setTypeface(null,Typeface.BOLD);
        time.setGravity(1);
        amt.setGravity(1);
        time.setWidth(175);
        amt.setWidth(175);

        String[] timeStr;
        String[] dateTime = recordList.get(1).split(" ");
        for (int i = 0; i < dateTime.length; i++) {
            timeStr = dateTime[1].split(":");
            if(Integer.parseInt(timeStr[0]) > 12){
                time.setText((Integer.parseInt(timeStr[0]) - 12) + " : " + timeStr[1] + " p.m.");
            } else if(Integer.parseInt(timeStr[0]) == 12){
                time.setText(timeStr[0] + " : " + timeStr[1] + " p.m.");
            } else {
                time.setText(timeStr[0] + " : " + timeStr[1] + " a.m.");
            }
        }
        amt.setText(recordList.get(0) + "ml");
        id.setText(recordList.get(2));

        tr.addView(time);
        tr.addView(amt);
        tr.addView(id);
        return tr;
    }

    public void showDialog(String userID){
        Dialog dialog = new Dialog(this,R.style.DialogStyle);
        dialog.setContentView(R.layout.popup_habit_insert);
        //Transform keyboard input to accept only numbers
        EditText water_amount = dialog.findViewById(R.id.water_amount);
        water_amount.setTransformationMethod(new NumericKeyBoardTransformationMethod());
        //Click Event for Confirm Button
        Button confirm_btn = dialog.findViewById(R.id.confirm_btn);
        confirm_btn.setOnClickListener(v -> {
            if(!water_amount.getText().toString().trim().equals("")) {
                String amt = water_amount.getText().toString();
                mySQLiteAdapter = new SQLiteAdapter(getApplicationContext());
                mySQLiteAdapter.openToWrite();
                mySQLiteAdapter.insert(userID, amt);
                mySQLiteAdapter.close();
                dialog.dismiss();
                finish();
                startActivity(getIntent());
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    public void showEditDialog(String time, String amt, String id){
        Dialog dialog = new Dialog(this,R.style.DialogStyle);
        dialog.setContentView(R.layout.popup_habit_edit);
        //Transform keyboard input to accept only numbers
        EditText water_amount = dialog.findViewById(R.id.water_amount);
        water_amount.setTransformationMethod(new NumericKeyBoardTransformationMethod());
        water_amount.setText(amt);
        //Click Event for time and drop down button
        TextView record_time = dialog.findViewById(R.id.record_time);
        record_time.setText(time);
        ImageView dropdown = dialog.findViewById(R.id.dropdown_btn);
        record_time.setOnClickListener(v->showTimePicker(record_time));
        dropdown.setOnClickListener(v->showTimePicker(record_time));

        //Click Event for Delete btn
        ImageView delete_btn = dialog.findViewById(R.id.delete_btn);
        delete_btn.setOnClickListener(v -> {
            mySQLiteAdapter = new SQLiteAdapter(getApplicationContext());
            mySQLiteAdapter.openToWrite();
            mySQLiteAdapter.deleteByID(id);
            mySQLiteAdapter.close();
            dialog.dismiss();
            finish();
            startActivity(getIntent());
        });
        //Click Event for Confirm Button
        Button confirm_btn = dialog.findViewById(R.id.confirm_btn);
        confirm_btn.setOnClickListener(v -> {
            if(!water_amount.getText().toString().trim().equals("")) {
                String amt1 = water_amount.getText().toString();
                mySQLiteAdapter = new SQLiteAdapter(getApplicationContext());
                mySQLiteAdapter.openToRead();
                String time_stamp = mySQLiteAdapter.queueByID(id);
                String[] timeWithSeconds = time_stamp.split(" ");
                mySQLiteAdapter.close();
                String selectedTime = record_time.getText().toString();
                if(selectedTime.contains("a.m.")) {
                    selectedTime = timeWithSeconds[0] + " " + selectedTime.replace("a.m.", "").replace(" ","");
                } else if(selectedTime.contains("p.m.")) {
                    selectedTime = timeWithSeconds[0] + " " + selectedTime.replace("p.m.", "").replace(" ", "");
                }
                mySQLiteAdapter.openToWrite();
                mySQLiteAdapter.update(amt1,id,selectedTime);
                mySQLiteAdapter.close();
                dialog.dismiss();
                finish();
                startActivity(getIntent());
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    private List<List<String>> sortRecord(List<String> records) {
        List<List<String>> recordList = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            String resStr = records.get(i);
            recordList.add(Arrays.asList(resStr.split(",")));
        }
        return recordList;
    }

    public void showDatePicker(){
        EditText date = findViewById(R.id.date_text);
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(HabitTracker.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdfDayOfWeek = new SimpleDateFormat("EE");
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdfMonthOfYear = new SimpleDateFormat("MMM");
                    Date day = new Date(year, monthOfYear, dayOfMonth-1);
                    String selectedDate = sdfDayOfWeek.format(day);
                    String selectedMonth = sdfMonthOfYear.format(day);
                    date.setText(selectedDate + ", " + dayOfMonth + " " + selectedMonth + " " + year);
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void showTimePicker(TextView record_time){
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, selectedHour, selectedMinute) -> {
            try {
                @SuppressLint("SimpleDateFormat") String timeStamp = new
                        SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                EditText selectedDateET = findViewById(R.id.date_text);
                String selectedDateStr = selectedDateET.getText().toString();
                String[] selectedDateArray = selectedDateStr.split(" ");
                selectedDateStr = selectedDateArray[1] + "-" + selectedDateArray[2] + "-" + selectedDateArray[3] + " "+
                        selectedHour + ":" + selectedMinute;
                Date currDate = sdf.parse(timeStamp);
                Date selectedDate = sdf2.parse(selectedDateStr);
                if(selectedDate.compareTo(currDate) <= 0) {
                    String period = "a.m.";
                    hour = selectedHour;
                    minute = selectedMinute;
                    String hourStr = String.valueOf(hour);
                    String minuteStr = String.valueOf(minute);
                    if (hour < 10) {
                        hourStr = "0" + hourStr;
                    }
                    if (minute < 10) {
                        minuteStr = "0" + minuteStr;
                    }
                    if (hour > 12) {
                        hour = hour - 12;
                        hourStr = String.valueOf(hour);
                        period = "p.m.";
                    } else if (hour == 12) {
                        period = "p.m.";
                    } else if (hour == 0) {
                        hour += 12;
                        hourStr = String.valueOf(hour);
                    }
                    record_time.setText(hourStr + " : " + minuteStr + " " + period);
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot record for the future.", Toast.LENGTH_LONG).show();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, false);
        timePickerDialog.show();
    }
}