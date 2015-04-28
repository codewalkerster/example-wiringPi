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
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
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
    private final int ledPorts[] = {
        97,  // GPIOX.BIT0(#97)
        108, // GPIOX.BIT11(#108)
        100, // GPIOX.BIT3(#100)
        101, // GPIOX.BIT4(#101)
        105, // GPIOX.BIT8(#105)
        106, // GPIOX.BIT9(#106)
        107, // GPIOX.BIT10(#107)
        115, // GPIOX.BIT18(#115)
        116, // GPIOX.BIT19(#116)
        88,  // GPIOY.BIT8(#88)
        83,  // GPIOY.BIT3(#83)
        87,  // GPIOY.BIT7(#87)
        104, // GPIOX.BIT7(#104)
        102, // GPIOX.BIT5(#102)
        103, // GPIOX.BIT6(#103)
        117, // GPIOX.BIT20(#117)
        99,  // GPIOX.BIT2(#99)
        118, // GPIOX.BIT21(#118)
        98,  // GPIOX.BIT1(#98)
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
    private final String PWM_ENABLE = "/sys/devices/platform/pwm-ctrl/enable";
    private final String PWM_DUTY = "/sys/devices/platform/pwm-ctrl/duty";
    //PWM }}}

    //I2C {{{
    //BMP085 {{{
    private boolean mStopBMP085;
    private ToggleButton mBtn_BMP085;
    private TextView mTV_Temperature;
    private TextView mTV_Pressure;
    private String mTemperature;
    private String mPressure;
    Runnable mRunnableBMP085 = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            updateBMP085();
        }
    };
    private final static String TEMP_INPUT = "/sys/bus/i2c/drivers/bmp085/1-0077/temp0_input";
    private final static String PRESSURE_INPUT = "/sys/bus/i2c/drivers/bmp085/1-0077/pressure0_input";
    //BMP085 }}}

    //SI1132 {{{
    private boolean mStopSI1132;
    private ToggleButton mBtn_SI1132;
    private TextView mTV_Visible;
    private TextView mTV_LUX;
    private TextView mTV_UV;
    private String mVisibleLux;
    private String mLux;
    private String mUV;
    Runnable mRunnableSI1132 = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            updateSI1132();
        }
    };
    private final static String VISIBLE_INDEX = "/sys/devices/i2c-1/1-0060/visible_index";
    private final static String IR_INDEX = "/sys/devices/i2c-1/1-0060/ir_index";
    private final static String UV_INDEX = "/sys/devices/i2c-1/1-0060/uv_index";
    //SI1132 }}}

    //SI702x {{{
    private boolean mStopSI702x;
    private ToggleButton mBtn_SI702x;
    private TextView mTV_Temperature2;
    private TextView mTV_Humidity;
    private String mTemperature2;
    private String mHumidity;
    Runnable mRunnableSI702x = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            updateSI702x();
        }
    };
    private final static String TEMPERATURE = "/sys/bus/i2c/drivers/si702x/1-0040/temperature";
    private final static String HUMIDITY = "/sys/bus/i2c/drivers/si702x/1-0040/humidity";
    //SI702x }}}

    private int mLCDHandle = -1;
    private final static int LCD_ROW = 2;   // 16 Char
    private final static int LCD_COL = 16;  // 2 Line
    private final static int LCD_BUS = 4;   // Interface 4 Bit mode
    private final static int LCD_UPDATE_PERIOD = 300; // 300ms

    private final static int PORT_LCD_RS = 7;   // GPIOY.BIT3(#83)
    private final static int PORT_LCD_E = 0;   // GPIOY.BIT8(#88)
    private final static int PORT_LCD_D4 = 2;   // GPIOX.BIT19(#116)
    private final static int PORT_LCD_D5 = 3;   // GPIOX.BIT18(#115)
    private final static int PORT_LCD_D6 = 1;   // GPIOY.BIT7(#87)
    private final static int PORT_LCD_D7 = 4;   // GPIOX.BIT7(#104)
    //I2C }}}

    //UART {{{
    private boolean mStopSerial;
    private Button mBtn_WriteSerial;
    private EditText mET_Write;
    private ToggleButton mBtn_ReadSerial;
    private EditText mET_Read;
    private String mLine;
    Runnable mRunnableSerial = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            try {
                BufferedReader br = new BufferedReader(new FileReader(TTYS2));
                while (!mStopSerial) {
                    while((mLine = br.readLine()) != null) {
                        Log.e(TAG, mLine);
                        mHandler.sendEmptyMessage(0);
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private final static String TTYS2 = "/dev/ttyS2";
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            mET_Read.setText(mLine);
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
                mBtn_BMP085.setChecked(false);
                mBtn_SI1132.setChecked(false);
                mBtn_SI702x.setChecked(false);
                mBtn_GPIO.setChecked(false);
                mBtn_ReadSerial.setChecked(false);
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

                    exportGPIO();

                    wiringPiSetupSys();

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
                    unexportGPIO();
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
                setEnalbePWM(0, isChecked);
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
                setEnalbePWM(1, isChecked);
            }
        });

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
        //BMP085 {{{
        mBtn_BMP085 = (ToggleButton) findViewById(R.id.btn_bmp085);
        mBtn_BMP085.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    /*
                    wiringPiSetupSys();
                    mLCDHandle = lcdInit(LCD_ROW, LCD_COL, LCD_BUS,
                            PORT_LCD_RS, PORT_LCD_E, PORT_LCD_D4, PORT_LCD_D5, 
                            PORT_LCD_D6, PORT_LCD_D7, 0, 0, 0, 0);
                    Log.e(TAG, "mLCDHandler = " + mLCDHandle);
                    if (mLCDHandle < 0)
                        finish();
                    */
                    mBtn_SI1132.setChecked(false);
                    mBtn_SI702x.setChecked(false);
                    insmodBMP085();
                    mStopBMP085 = false;
                    handler.postDelayed(mRunnableBMP085, 300);
                } else {
                    mLCDHandle = -1;
                    mStopBMP085 = true;
                    rmmodBMP085();
                }
            }
        });

        mTV_Temperature = (TextView) findViewById(R.id.tv_temperature);
        mTV_Pressure = (TextView) findViewById(R.id.tv_pressure);
        //BMP085 }}}

        //SI1173 {{{
        mBtn_SI1132 = (ToggleButton) findViewById(R.id.btn_si1132);
        mBtn_SI1132.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    mBtn_BMP085.setChecked(false);
                    mBtn_SI702x.setChecked(false);
                    insmodSI1132();
                    mStopSI1132 = false;
                    handler.postDelayed(mRunnableSI1132, 300);
                } else {
                    mLCDHandle = -1;
                    mStopSI1132 = true;
                    rmmodSI1132();
                }
            }
        });

        mTV_Visible = (TextView) findViewById(R.id.tv_visible_index);
        mTV_LUX = (TextView) findViewById(R.id.tv_lux);
        mTV_UV = (TextView) findViewById(R.id.tv_uv);
        //SI1132 }}}

        //SI702x {{{
        mBtn_SI702x = (ToggleButton) findViewById(R.id.btn_si702x);
        mBtn_SI702x.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    mBtn_BMP085.setChecked(false);
                    mBtn_SI1132.setChecked(false);
                    insmodSI702x();
                    mStopSI702x = false;
                    handler.postDelayed(mRunnableSI702x, 300);
                } else {
                    mLCDHandle = -1;
                    mStopSI702x = true;
                    rmmodSI702x();
                }
            }
        });

        mTV_Temperature2 = (TextView) findViewById(R.id.tv_temperature2);
        mTV_Humidity = (TextView) findViewById(R.id.tv_humidity);
        //SI1132 }}}
        //I2C }}}

        //UART {{{
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
                // TODO Auto-generated method stub
                writeSerial(mET_Write.getText().toString());
                mET_Write.setText("");
            }
        });

        mET_Read = (EditText) findViewById(R.id.et_read);
        mBtn_ReadSerial = (ToggleButton) findViewById(R.id.btn_read_serial);
        mBtn_ReadSerial.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    setBaudRate();
                    mStopSerial = false;
                    new Thread(mRunnableSerial).start();
                } else
                    mStopSerial = true;
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
        
        //GPIO {{{
        mBtn_GPIO.setChecked(false);
        //GPIO }}}
        //PWM {{{
        mBtn_PWM.setChecked(false);
        //PWM }}}
        //I2C {{{
        mBtn_BMP085.setChecked(false);
        mBtn_SI1132.setChecked(false);
        mBtn_SI702x.setChecked(false);
        //I2C }}}
        //UARD {{{
        mBtn_ReadSerial.setChecked(false);
        //UART }}}
        //1-Wire {{{
        mBtn_1Wire.setChecked(false);
        //1-Wire }}}
        //I2C }}}
    }

    //GPIO {{{
    public void updateGPIO() {
        int i = 0;
        int adcValue = 0;
        int ledPos = 0;
        if ((adcValue = analogRead (PORT_ADC1)) > 0) {
            ledPos = (adcValue * ledPorts.length * 1000) / 1024;
            ledPos = (ledPorts.length - (ledPos / 1000));
            mPB_ADC.setProgress(adcValue);
        } else
            ledPos = 0;

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

    boolean exportGPIO() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            for (int port: ledPorts) {
                os.writeBytes("echo " + port + " > /sys/class/gpio/export\n");
                os.writeBytes("chmod 666 /sys/class/gpio/gpio" + port + "/direction\n");
                os.writeBytes("echo out > /sys/class/gpio/gpio" + port + "/direction\n");
                os.writeBytes("chmod 666 /sys/class/gpio/gpio" + port + "/value\n");
            }
            os.flush();
            Thread.sleep(1000);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    boolean unexportGPIO() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            for (int port : ledPorts) {
                os.writeBytes("echo " + port + " > /sys/class/gpio/unexport\n");
            }
            os.flush();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        }

        return true;
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

    private void setEnalbePWM(int index, boolean enable) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(PWM_ENABLE + index));
            if (enable)
                bw.write("1");
            else
                bw.write("0");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDuty(int index, int duty) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(PWM_DUTY + index));
            bw.write(Integer.toString(duty));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //PWM }}}

    //I2C {{{
    //BMP085 {{{
    private void insmodBMP085() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("insmod /system/lib/modules/i2c-algo-bit.ko\n");
            os.writeBytes("insmod /system/lib/modules/aml_i2c.ko\n");
            os.writeBytes("insmod /system/lib/modules/bmp085-i2c.ko\n");
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

    private void rmmodBMP085() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("rmmod bmp085_i2c\n");
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

    public void updateBMP085() {
        try {
            BufferedReader temp_reader = 
                new BufferedReader(new FileReader(TEMP_INPUT));

            String txt = "";

            while((txt = temp_reader.readLine()) != null) {
                float temp = Float.parseFloat(txt) / 10;
                mTemperature = String.format("%.1f", temp);
                mTemperature += " *C";
                mTV_Temperature.setText(mTemperature);
            }

            temp_reader.close();

            BufferedReader pressure_reader =
                new BufferedReader(new FileReader(PRESSURE_INPUT));

            while((txt = pressure_reader.readLine()) != null) {
                float temp = Float.parseFloat(txt) / 100;
                mPressure = String.format("%.2f", temp);
                mPressure += " hPa";
                mTV_Pressure.setText(mPressure);
            }
 
            pressure_reader.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if (!mStopBMP085)
            handler.postDelayed(mRunnableBMP085, 100);
    }
    //BMP085 }}}

    //SI1132 {{{
    private void insmodSI1132() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("insmod /system/lib/modules/i2c-algo-bit.ko\n");
            os.writeBytes("insmod /system/lib/modules/aml_i2c.ko\n");
            os.writeBytes("insmod /system/lib/modules/si1132.ko\n");
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

    private void rmmodSI1132() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("rmmod si1132\n");
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

    public void updateSI1132() {
        try {
            BufferedReader visible_index_reader =
                new BufferedReader(new FileReader(VISIBLE_INDEX));

            String txt = "";

            while((txt = visible_index_reader.readLine()) != null) {
                mVisibleLux = txt;
                mVisibleLux += " Lux(Visible index)";
                mTV_Visible.setText(mVisibleLux);
            }

            visible_index_reader.close();

            BufferedReader ir_reader =
                new BufferedReader(new FileReader(IR_INDEX));

            while((txt = ir_reader.readLine()) != null) {
                mLux = txt;
                mLux += " Lux";
                mTV_LUX.setText(mLux);
            }

            ir_reader.close();

            BufferedReader uv_reader =
                new BufferedReader(new FileReader(UV_INDEX));

            while((txt = uv_reader.readLine()) != null) {
                float temp = Float.parseFloat(txt) / 100;
                mUV = String.format("%.1f", temp);
                mUV += " index";
                mTV_UV.setText(mUV);
            }

            uv_reader.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

        if (!mStopSI1132)
            handler.postDelayed(mRunnableSI1132, 100);
    }
    //SI1132 }}}

    //SI702x {{{
    private void insmodSI702x() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("insmod /system/lib/modules/i2c-algo-bit.ko\n");
            os.writeBytes("insmod /system/lib/modules/aml_i2c.ko\n");
            os.writeBytes("insmod /system/lib/modules/si702x.ko\n");
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

    private void rmmodSI702x() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("rmmod si702x\n");
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

    public void updateSI702x() {
        try {
            BufferedReader temperature_reader =
                new BufferedReader(new FileReader(TEMPERATURE));

            String txt = "";

            while((txt = temperature_reader.readLine()) != null) {
                float temp = Float.parseFloat(txt);
                mTemperature2 = String.format("%.1f", temp);
                mTemperature2 += " *C";
                mTV_Temperature2.setText(mTemperature2);
            }

            temperature_reader.close();

            BufferedReader humidity_reader =
                new BufferedReader(new FileReader(HUMIDITY));

            while((txt = humidity_reader.readLine()) != null) {
                float temp = Float.parseFloat(txt);
                mHumidity = String.format("%.2f", temp);
                mHumidity += " %";
                mTV_Humidity.setText(mHumidity);
            }

            humidity_reader.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if (!mStopSI702x)
            handler.postDelayed(mRunnableSI702x, 100);
    }
    //SI702x }}}
    //I2C }}}

    //UART {{{
    private void setBaudRate() {
        try {
            DataOutputStream os = new DataOutputStream(mProcess.getOutputStream());
            os.writeBytes("stty -F /dev/ttyS2 1152200\n");
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

    private void writeSerial(String data) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(TTYS2));
            bw.write(data);
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

    private void updateLCDDisplay() {
        if (mLCDHandle == -1)
            return;

        // first line
        lcdPosition(mLCDHandle, 0, 0);
        for (int i = 0; i < LCD_COL ; i++) {
            if ( i < mTemperature.length() - 1)
                lcdPutchar(mLCDHandle, mTemperature.charAt(i));
            else
                lcdPutchar(mLCDHandle, ' ');
        }

        // second line
        lcdPosition(mLCDHandle, 0, 1);
        for (int i = 0; i < LCD_COL; i++) {
            if (i < mPressure.length() - 1)
                lcdPutchar(mLCDHandle, mPressure.charAt(i));
            else
                lcdPutchar(mLCDHandle, ' ');
        }
    }

    public native int wiringPiSetupSys();
    public native int analogRead(int port);
    public native void digitalWrite(int port, int onoff);
    public native int lcdInit(int rows, int cols, int bits,
            int rs, int strb, 
            int d0, int d1, int d2, int d3, int d4,
            int d5, int d6, int d7);
    public native void lcdPosition(int fd, int x, int y);
    public native void lcdPutchar(int fd, char c);

    static {
        System.loadLibrary("wpi_android");
    }
}
