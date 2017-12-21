package com.hardkernel.wiringpi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    TabHost mTabHost;
    private final static String TAG = "example-wiringPi";
    //GPIO {{{
    private ToggleButton mBtn_GPIO;
    private final int DATA_UPDATE_PERIOD = 100; // 100ms
    private final int PORT_ADC1 = 0;   // ADC.AIN0
    private ProgressBar mPB_ADC;

    private final static int INPUT = 0;
    private final static int OUTPUT = 1;

	/*
    private final int ledPorts[] = {
        214, // GPIOY.3
        234, // GPIOX.6
        219, // GPIOY.8
        228, // GPIOX.0
        230, // GPIOX.2
        232, // GPIOX.4
        235, // GPIOX.7
        237, // GPIOX.9
        239, // GPIOX.11
        247, // GPIOX.9
        249, // GPIOX.21
        238, // GPIOX.10
        236, // GPIOX.8
        233, // GPIOX.5
        231, // GPIOX.3
        229, // GPIOX.1
        224, // GPIOY.13
        225, // GPIOY.14
        218, // GPIOX.1
    };
	*/

    private final int ledPorts[] = {
        24, //214
        23, //234
        22, //219
        21, //228
        14, //230
        13, //232
        12, //235
        3,  //237
        2,  //239
        0,  //247
        7,  //249
        5,  //238
        4,  //236
        5,  //233
        6,  //231
        10, //229
        26, //224
        11, //225
        27, //218
    };


    private static final int[] CHECKBOX_IDS = {
        R.id.led01, R.id.led02, R.id.led03, R.id.led04, R.id.led05,
        R.id.led06, R.id.led07, R.id.led08, R.id.led09, R.id.led10,
        R.id.led11, R.id.led12, R.id.led13, R.id.led14, R.id.led15,
        R.id.led16, R.id.led17, R.id.led18, R.id.led19
    };

    private List<CheckBox>mLeds;
    private boolean mStopGPIO;
    private Process mProcess;

    private Handler handler = new Handler();
    Runnable mRunnableGPIO = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            updateGPIO();
        }
    };
    //GPIO }}}

    //PWM {{{
    private RadioButton mRB_PWM1;
    private RadioButton mRB_PWM2;
    private ToggleButton mBtn_PWM;
    private LinearLayout mLayout_PWM2;
    private CheckBox mCB_EnablePWM1;
    private CheckBox mCB_EnablePWM2;
    private TextView mTV_Duty1;
    private TextView mTV_Duty2;
    private SeekBar mSB_DutyPWM1;
    private SeekBar mSB_DutyPWM2;
    private int mPWMCount = 1;
    private final String PWM_PREFIX = "/sys/devices/pwm-ctrl.";
    private final String PWM_ENABLE = "/enable";
    private final String PWM_DUTY = "/duty";
    private final String PWM_FREQ = "/freq"; //added J.
    private String mPWMEnableNode;
    private String mPWMDutyNode;
    private String mPWMFreqNode; //added J.
    //PWM }}}

    //I2C {{{
    private boolean mStopWeather;
    private ToggleButton mBtn_Weather;
    //BME280 {{{
    private TextView mTV_Temperature;
    private TextView mTV_Humidity;
    private TextView mTV_Pressure;
    private TextView mTV_Altitude;
    private String mTemperature;
    private String mHumidity;
    private String mPressure;
    private String mAltitude;
    Runnable mRunnableWeather = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            updateWeather();
        }
    };
    //BME280 }}}

    //SI1132 {{{
    private TextView mTV_UV_index;
    private TextView mTV_Visible;
    private TextView mTV_IR;
    private String mUVindex;
    private String mVisible;
    private String mIR;
    //SI1132 }}}
    //I2C }}}

    //UART {{{
    private Spinner mSP_Baudradtes;
    private boolean mStopSerial;
    private Button mBtn_WriteSerial;
    private EditText mET_Write;
    private TextView mTV1_data;
    private TextView mTV2_data;
    private TextView mTV3_data;
    private String mData;
    private final static String TTYS = "/dev/ttyS";

    Runnable mRunnableSerial1 = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message msg = mHandler.obtainMessage();

            try {
                BufferedReader br = new BufferedReader(new FileReader(TTYS + "2"));
                    while((mData = br.readLine()) != null) {
                        msg.obj = (Object)mData;
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                        break;
                    }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable mRunnableSerial2 = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message msg = mHandler.obtainMessage();

            try {
                BufferedReader br = new BufferedReader(new FileReader(TTYS + "3"));
                    while((mData = br.readLine()) != null) {
                        msg.obj = (Object)mData;
                        msg.what = 2;
                        mHandler.sendMessage(msg);
                        break;
                    }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable mRunnableSerial3 = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message msg = mHandler.obtainMessage();

            try {
                BufferedReader br = new BufferedReader(new FileReader(TTYS + "1"));
                    while((mData = br.readLine()) != null) {
                        msg.obj = (Object)mData;
                        msg.what = 3;
                        mHandler.sendMessage(msg);
                        break;
                    }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == 0) {
                writeSerial((String)msg.obj, 1);
                mET_Write.setText("");
            } else if (msg.what == 1) {
                mTV1_data.setText((String)msg.obj);
                writeSerial((String)msg.obj, 2);
            } else if (msg.what == 2) {
                mTV2_data.setText((String)msg.obj);
                writeSerial((String)msg.obj, 3);
            } else if (msg.what == 3) {
                mTV3_data.setText((String)msg.obj);
                mStopSerial = true;
            }
        }
    };
    //UART }}}

    //1-Wire {{{
    private ToggleButton mBtn_1Wire;
    private final static String W1_BUS = "/sys/bus/w1/devices/";
    private List<String> mIDs;
    private Button mBtn_DS1820_1;
    private EditText mET_DS1820_Info_1;
    private LinearLayout mLO_DS1820_2;
    private Button mBtn_DS1820_2;
    private EditText mET_DS1820_Info_2;
    //1-Wire }}}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (TabHost) findViewById(R.id.tabhost);
        mTabHost.setup();

        TabSpec tab1 = mTabHost.newTabSpec("GPIO");
        TabSpec tab2 = mTabHost.newTabSpec("PWM");
        TabSpec tab3 = mTabHost.newTabSpec("I2C");
        TabSpec tab4 = mTabHost.newTabSpec("UART");
        TabSpec tab5 = mTabHost.newTabSpec("1-Wire");

        tab1.setIndicator("GPIO");
        tab1.setContent(R.id.tab1);
        tab2.setIndicator("PWM");
        tab2.setContent(R.id.tab2);
        tab3.setIndicator("I2C");
        tab3.setContent(R.id.tab3);
        tab4.setIndicator("UART");
        tab4.setContent(R.id.tab4);
        tab5.setIndicator("1-Wire");
        tab5.setContent(R.id.tab5);

        mTabHost.addTab(tab1);
        mTabHost.addTab(tab2);
        mTabHost.addTab(tab3);
        mTabHost.addTab(tab4);
        mTabHost.addTab(tab5);

        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                // TODO Auto-generated method stub
                mBtn_PWM.setChecked(false);
                mBtn_Weather.setChecked(false);
                mBtn_GPIO.setChecked(false);
                mBtn_1Wire.setChecked(false);
            }
        });

        //GPIO {{{
        mBtn_GPIO = (ToggleButton) findViewById(R.id.btn_gpio);
        mBtn_GPIO.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {

                    wiringPiSetup();

                    for (int i = 0; i < ledPorts.length; i++)
                        pinMode(ledPorts[i], OUTPUT);

                    mStopGPIO = false;
                    handler.postDelayed(mRunnableGPIO, 100);
                    for (CheckBox cb: mLeds)
                        cb.setEnabled(true);
                } else {
                    mStopGPIO = true;
                    for (CheckBox cb: mLeds) {
                        cb.setEnabled(false);
                    }
                    mPB_ADC.setEnabled(false);
                }
            }
        });

        mPB_ADC = (ProgressBar) findViewById(R.id.adc);

        mLeds = new ArrayList<CheckBox>();

        try {
            mProcess = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int id: CHECKBOX_IDS) {
            CheckBox cb = (CheckBox) findViewById(id);
            mLeds.add(cb);
        }
        //GPIO }}}

        //PWM {{{
        mTV_Duty1 = (TextView) findViewById(R.id.tv_duty1);
        mTV_Duty2 = (TextView) findViewById(R.id.tv_duty2);

        mCB_EnablePWM1 = (CheckBox) findViewById(R.id.cb_pwm1);
        mCB_EnablePWM1.setEnabled(false);
        mCB_EnablePWM1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                mSB_DutyPWM1.setEnabled(isChecked);
                mSB_DutyPWM1.setProgress(0);
                mTV_Duty1.setText("Duty : 0");
                setEnablePWM(0, isChecked);
            }
        });

        mCB_EnablePWM2 = (CheckBox) findViewById(R.id.cb_pwm2);
        mCB_EnablePWM2.setEnabled(false);
        mCB_EnablePWM2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                mSB_DutyPWM2.setEnabled(isChecked);
                mSB_DutyPWM2.setProgress(0);
                mTV_Duty2.setText("Duty : 0");
                setEnablePWM(1, isChecked);
            }
        });

        for (int i = 0; i < 100; i++) {
            File f = new File(PWM_PREFIX + i);
            if (f.isDirectory()) {
                mPWMEnableNode = PWM_PREFIX + i + PWM_ENABLE;
                Log.e(TAG, "pwm enable : " + mPWMEnableNode);
                mPWMDutyNode = PWM_PREFIX + i + PWM_DUTY;
                Log.e(TAG, "pwm duty : " + mPWMDutyNode);
                mPWMFreqNode = PWM_PREFIX + i + PWM_FREQ; //added J.
                break;
            }
        }

        mSB_DutyPWM1 = (SeekBar) findViewById(R.id.sb_duty1);
        mSB_DutyPWM1.setEnabled(false);
        mSB_DutyPWM1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                mTV_Duty1.setText("Duty : " + seekBar.getProgress());
                setDuty(0, seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // TODO Auto-generated method stub
            }
        });

        mSB_DutyPWM2 = (SeekBar) findViewById(R.id.sb_duty2);
        mSB_DutyPWM2.setEnabled(false);
        mSB_DutyPWM2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                mTV_Duty2.setText("Duty : " + seekBar.getProgress());
                setDuty(1, seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // TODO Auto-generated method stub
            }
        });

        mLayout_PWM2 = (LinearLayout) findViewById(R.id.lo_pwm2);
        mLayout_PWM2.setVisibility(View.GONE);
 
        mRB_PWM1 = (RadioButton) findViewById(R.id.radio_pwm1);
        mRB_PWM1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mPWMCount = 1;
                mCB_EnablePWM2.setEnabled(false);
                mLayout_PWM2.setVisibility(View.GONE);
            }
        });

        mRB_PWM2 = (RadioButton) findViewById(R.id.radio_pwm2);
        mRB_PWM2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mPWMCount = 2;
                mLayout_PWM2.setVisibility(View.VISIBLE);
            }
        });

        mTV_Duty1 = (TextView) findViewById(R.id.tv_duty1);
        mTV_Duty2 = (TextView) findViewById(R.id.tv_duty2);

        mBtn_PWM = (ToggleButton) findViewById(R.id.btn_pwm);
        mBtn_PWM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    insmodPWM();
                    mCB_EnablePWM1.setEnabled(true);
                    mCB_EnablePWM1.setChecked(false);
                    mCB_EnablePWM2.setEnabled(true);
                    mCB_EnablePWM2.setChecked(false);
                    mSB_DutyPWM1.setProgress(0);
                    mSB_DutyPWM2.setProgress(0);
                    mRB_PWM1.setEnabled(false);
                    mRB_PWM2.setEnabled(false);
                } else {
                    rmmodPWM();
                    mCB_EnablePWM1.setEnabled(false);
                    mCB_EnablePWM1.setChecked(false);
                    mCB_EnablePWM2.setEnabled(false);
                    mCB_EnablePWM2.setChecked(false);
                    mSB_DutyPWM1.setProgress(0);
                    mSB_DutyPWM2.setProgress(0);
                    mRB_PWM1.setEnabled(true);
                    mRB_PWM2.setEnabled(true);
                }
            }
        });
        mBtn_PWM.setChecked(false);
        //PWM }}}

        //I2C {{{
        //BME280 {{{
        mBtn_Weather = (ToggleButton) findViewById(R.id.tb_weather);
        mBtn_Weather.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    insmodI2C();
                    if (openWeatherBoard() == -1) {
                        Log.e(TAG, "filed");
                        return;
                    }
                    mStopWeather = false;
                    handler.postDelayed(mRunnableWeather, 300);
                } else {
                    rmmodI2C();
                    mStopWeather = true;
                    closeWeatherBoard();
                }
            }
        });

        mTV_Temperature = (TextView) findViewById(R.id.tv_temperature);
        mTV_Humidity = (TextView) findViewById(R.id.tv_humidity);
        mTV_Pressure = (TextView) findViewById(R.id.tv_pressure);
        mTV_Altitude = (TextView) findViewById(R.id.tv_altitude);
        //BME280 }}}

        //SI1173 {{{
        mTV_UV_index = (TextView) findViewById(R.id.tv_uv_index);
        mTV_Visible = (TextView) findViewById(R.id.tv_visible);
        mTV_IR = (TextView) findViewById(R.id.tv_ir);
        //SI1132 }}}
        //I2C }}}

        //UART {{{
        String baudrates[] = { "4800", "9600", "115200" };
        mSP_Baudradtes = (Spinner) findViewById(R.id.spinner_baudrates);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
            (this, android.R.layout.simple_spinner_item, baudrates);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSP_Baudradtes.setAdapter(spinnerArrayAdapter);
        mSP_Baudradtes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String baudrate = (String)mSP_Baudradtes.getSelectedItem();
                setBaudRate(Integer.parseInt(baudrate));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTV1_data = (TextView) findViewById(R.id.tv1_data);
        mTV2_data = (TextView) findViewById(R.id.tv2_data);
        mTV3_data = (TextView) findViewById(R.id.tv3_data);
        mET_Write = (EditText) findViewById(R.id.et_write);
        mET_Write.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                if (s.length() > 0)
                    mBtn_WriteSerial.setEnabled(true);
                else
                    mBtn_WriteSerial.setEnabled(false);
            }
        });

        mBtn_WriteSerial = (Button) findViewById(R.id.btn_write_serial);
        mBtn_WriteSerial.setEnabled(false);
        mBtn_WriteSerial.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mStopSerial = false;
                new Thread(mRunnableSerial1).start();
                new Thread(mRunnableSerial2).start();
                new Thread(mRunnableSerial3).start();

                // TODO Auto-generated method stub
                Message msg = mHandler.obtainMessage();
                msg.obj = (Object)mET_Write.getText().toString();
                msg.what = 0;
                mHandler.sendMessageDelayed(msg, 100);
            }
        });
        //UART }}}

        //1-Wire {{{
        mBtn_1Wire = (ToggleButton) findViewById(R.id.btn_1w);
        mBtn_1Wire.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    insmod1Wire();
                    mIDs = new ArrayList<String>();
                    getDS1820ID(mIDs);
                    if (mIDs.size() == 0)
                        return;

                    mBtn_DS1820_1.setText(mBtn_DS1820_1.getText() + "(" +
                            mIDs.get(0) + ")");
                    if (mIDs.size() > 1) {
                        mBtn_DS1820_2.setText(mBtn_DS1820_2.getText() + "(" +
                            mIDs.get(1) + ")");
                        mLO_DS1820_2.setVisibility(View.VISIBLE);
                    }
                } else
                    rmmod1Wire();
            }
        });

        mET_DS1820_Info_1 = (EditText) findViewById(R.id.et_ds1820_info_1);
        mBtn_DS1820_1 = (Button) findViewById(R.id.btn_ds1829_1);
        mBtn_DS1820_1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mIDs.size() == 0)
                    return;

                mET_DS1820_Info_1.setText(getDS1820Info(mIDs.get(0)));
            }
        });
        mLO_DS1820_2 = (LinearLayout) findViewById(R.id.lo_ds1820_2);
        mLO_DS1820_2.setVisibility(View.GONE);
        mET_DS1820_Info_2 = (EditText) findViewById(R.id.et_ds1820_info_2);
        mBtn_DS1820_2 = (Button) findViewById(R.id.btn_ds1820_2);
        mBtn_DS1820_2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mET_DS1820_Info_2.setText(getDS1820Info(mIDs.get(1)));
            }
        });
        //1-Wire }}}
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mStopSerial = true;

        //GPIO {{{
        mBtn_GPIO.setChecked(false);
        //GPIO }}}
        //PWM {{{
        mBtn_PWM.setChecked(false);
        //PWM }}}
        //I2C {{{
        mBtn_Weather.setChecked(false);
        //I2C }}}
        //1-Wire {{{
        mBtn_1Wire.setChecked(false);
        //1-Wire }}}
        //I2C }}}
    }

    //GPIO {{{
    public void updateGPIO() {
        int i = 0;
        int ledPos = 0;
        int adcValue = analogRead(PORT_ADC1);
        //Log.e(TAG, "updateGPIO adcValue = " + adcValue);

        //added J.
        //eleminated the hopping of checked checkboxes
        if (adcValue < 0) adcValue = 0;
        //if (adcValue > 0) {
        ledPos = adcValue * ledPorts.length / 1024;
            //ledPos = (ledPorts.length - (ledPos / 1000));
        mPB_ADC.setProgress(adcValue);
        //} else
        //    ledPos = 0;

        for (i = 0; i < ledPorts.length; i++) {
            digitalWrite (ledPorts[i], 0);
            mLeds.get(i).setChecked(false);
        }

        for (i = 0; i < ledPos; i++) {
            digitalWrite (ledPorts[i], 1);
            mLeds.get(i).setChecked(true);
        }

        if (!mStopGPIO)
            handler.postDelayed(mRunnableGPIO, 100);
    }
    //GPIO {{{

    //PWM {{{
    private void insmodPWM() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("insmod /system/lib/modules/pwm-meson.ko npwm=" + mPWMCount + "\n");
            os.writeBytes("insmod /system/lib/modules/pwm-ctrl.ko\n");
            os.flush();
            Thread.sleep(100);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void rmmodPWM() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("rmmod pwm_ctrl\n");
            os.writeBytes("rmmod pwm_meson\n");
            os.flush();
            Thread.sleep(100);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setEnablePWM(int index, boolean enable) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(mPWMEnableNode + index));
            if (enable)
                bw.write("1");
            else
                bw.write("0");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //added J.
        //need to set the frequency
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(mPWMFreqNode + index));
            bw.write("100000");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDuty(int index, int duty) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(mPWMDutyNode + index));
            bw.write(Integer.toString(duty));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //PWM }}}

    //I2C {{{
    private void insmodI2C() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("insmod /system/lib/modules/aml_i2c.ko\n");
            os.flush();
            Thread.sleep(100);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void rmmodI2C() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("rmmod aml_i2c\n");
            os.flush();
            Thread.sleep(100);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updateWeather() {
        if (mStopWeather == true)
            return;
        mTV_UV_index.setText("UV index : "
                + String.format("%.2f", (double)getUVindex() / 100.0));
        mTV_Visible.setText("Visible : "
                + String.format("%.0f", (double)getVisible()) + " Lux");
        mTV_IR.setText("IR : "
                + String.format("%.0f", (double)getIR()) + " Lux");
        readyData();
        mTV_Temperature.setText("Temperature : "
                + String.format("%.2f", getTemperature() / 100.0) + " °C");
        mTV_Humidity.setText("Humidity : "
                + String.format("%.2f", getHumidity() / 1024.0) + " %");
        mTV_Pressure.setText("Pressure : "
                + String.format("%.2f", getPressure() / 100.0) + " hPa");
        mTV_Altitude.setText("Altitude : " + getAltitude() + " m");

        if (!mStopWeather)
            handler.postDelayed(mRunnableWeather, 1000);
    }
    //I2C }}}

    //UART {{{
    private void setBaudRate(int baudrate) {
        Log.e(TAG, "setBaudRate " + baudrate);
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("stty -F /dev/ttyS1 raw " + baudrate + " cs8 -hupcl ignbrk -icrnl -ixon -opost -isig -icanon -iexten -echo > /dev/null 2>&1");
            os.flush();
            os.writeBytes("stty -F /dev/ttyS2 raw " + baudrate + " cs8 -hupcl ignbrk -icrnl -ixon -opost -isig -icanon -iexten -echo > /dev/null 2>&1");
            os.flush();
            os.writeBytes("stty -F /dev/ttyS3 raw " + baudrate + " cs8 -hupcl ignbrk -icrnl -ixon -opost -isig -icanon -iexten -echo > /dev/null 2>&1");
            os.flush();
            Thread.sleep(100);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void writeSerial(String data, int serial) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(TTYS + String.valueOf(serial)));
            Log.e(TAG, "write data : " + data + ", node : " + TTYS + String.valueOf(serial));
            bw.write(data + "\n");
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //UART }}}

    //1-Wire
    private void insmod1Wire() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("insmod /system/lib/modules/wire.ko\n");
            os.writeBytes("insmod /system/lib/modules/w1-gpio.ko\n");
            os.writeBytes("insmod /system/lib/modules/w1_therm.ko\n");
            os.flush();
            Thread.sleep(100);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void rmmod1Wire() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("rmmod w1_therm\n");
            os.writeBytes("rmmod w1_gpio\n");
            os.flush();
            Thread.sleep(100);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void getDS1820ID(List<String> ids) {
        File[] files = new File(W1_BUS).listFiles();
        for ( File aFile : files ) {
             if (aFile.isDirectory()) {
                 Log.e(TAG, aFile.getName());
                 if (!aFile.getName().toString().equals("w1_bus_master1"))
                     ids.add(aFile.getName());
             }
        }
    }

    private String getDS1820Info(String id) {
        String info = "";
        try {
            BufferedReader w1_slave_reader =
                new BufferedReader(new FileReader(W1_BUS + "/" + id + "/w1_slave"));

            String txt = "";

            while((txt = w1_slave_reader.readLine()) != null) {
                info += txt + "\n";
            }

            info = info.trim();

            w1_slave_reader.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return info;
    }
    //1-Wire

    public native int wiringPiSetup();
    public native int wiringPiSetupSys();
    public native int analogRead(int port);
    public native void digitalWrite(int port, int onoff);
    public native void pinMode(int port, int value);
    public native int openWeatherBoard();
    public native int closeWeatherBoard();
    public native void readyData();
    public native int getUVindex();
    public native float getVisible();
    public native float getIR();
    public native int getTemperature();
    public native int getPressure();
    public native int getHumidity();
    public native int getAltitude();

    static {
        System.loadLibrary("wpi_android");
    }
}
