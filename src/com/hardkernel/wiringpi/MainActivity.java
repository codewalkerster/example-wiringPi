package com.hardkernel.wiringpi;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
    //Tinkering Kit {{{
    private ToggleButton mBtn_GPIO;
    private final int DATA_UPDATE_PERIOD = 100; // 100ms
    private final int PORT_ADC1 = 0;   // ADC.AIN0
    private ProgressBar mPB_ADC;
    private final int ledPorts[] = {
        97, // GPIOX.BIT0(#97)
        108, // GPIOX.BIT11(#108)
        100, // GPIOX.BIT3(#100)
        101, // GPIOX.BIT4(#101)
        105, // GPIOX.BIT8(#105)
        106, // GPIOX.BIT9(#106)
        107, // GPIOX.BIT10(#107)
        115,  // GPIOX.BIT18(#115)
        116,  // GPIOX.BIT19(#116)
        88,  // GPIOY.BIT8(#88)
        83,  // GPIOY.BIT3(#83)
        87,  // GPIOY.BIT7(#87)
        104,  // GPIOX.BIT7(#104)
        102,  // GPIOX.BIT5(#102)
        103,  // GPIOX.BIT6(#103)
        117, // GPIOX.BIT20(#117)
        99, // GPIOX.BIT2(#99)
        118, // GPIOX.BIT21(#118)
        98, // GPIOX.BIT1(#98)
    };

    private static final int[] CHECKBOX_IDS = {
        R.id.led01, R.id.led02, R.id.led03, R.id.led04, R.id.led05,
        R.id.led06, R.id.led07, R.id.led08, R.id.led09, R.id.led10,
        R.id.led11, R.id.led12, R.id.led13, R.id.led14, R.id.led15,
        R.id.led16, R.id.led17, R.id.led18, R.id.led19
    };

    private List<CheckBox>mLeds;
    private boolean mStop;
    private Process mProcess;

    private Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            update();
        }
    };
    //Tinkering Kit }}}

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (TabHost) findViewById(R.id.tabhost);
        mTabHost.setup();

        TabSpec tab1 = mTabHost.newTabSpec("GPIO");
        TabSpec tab2 = mTabHost.newTabSpec("PWM");
        TabSpec tab3 = mTabHost.newTabSpec("I2C");

        tab1.setIndicator("GPIO");
        tab1.setContent(R.id.tab1);
        tab2.setIndicator("PWM");
        tab2.setContent(R.id.tab2);
        tab3.setIndicator("GPIO");
        tab3.setContent(R.id.tab3);

        mTabHost.addTab(tab1);
        mTabHost.addTab(tab2);
        mTabHost.addTab(tab3);

        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            
            @Override
            public void onTabChanged(String tabId) {
                // TODO Auto-generated method stub
                if (mTabHost.getCurrentTabTag().equals("GPIO")) {
                    mBtn_PWM.setChecked(false);
                    Log.e(TAG, "GPIO");
                } else if (mTabHost.getCurrentTabTag().equals("PWM")){
                    mBtn_GPIO.setChecked(false);
                    Log.e(TAG, "PWM");
                } else if (mTabHost.getCurrentTabTag().equals("I2C")){

                }
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

                    mStop = false;
                    handler.postDelayed(runnable, 100);
                    for (CheckBox cb: mLeds)
                        cb.setEnabled(true);
                } else {
                    mStop = true;
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
        //Tinkering Kit }}}

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
                //mCB_EnablePWM1.setEnabled(true);
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
    }

    //Tinkering Kit {{{
    public void update() {
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

        if (!mStop)
            handler.postDelayed(runnable, 100);
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
    //Tinkering Kit {{{

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

    public native int wiringPiSetupSys();
    public native int analogRead(int port);
    public native void digitalWrite(int port, int onoff);

    static {
        System.loadLibrary("wpi_android");
    }
}
