package www.opendiylib.babyjumpcount;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button mStart;
    private Button mRecord;
    private Button mRead;
    private Button mDelete;
    private EditText mTimeCount;
    private EditText mTimeOutCount;
    private TextView mTotalStatus;
    private TextView mPeriodStatus;
    private boolean mIsStarted = false;
    private int mTotalTime = 60 * 60;//60 minuteS
    private int mPeriodTimeout = 60;//60 secondS
    private int mBabyActionCount = 0;
    private PowerManager.WakeLock mWakeLock = null;

    private static final int TIME_TOTAL = 60 * 60;//60 minuteS
    private static final int TIME_PERIOD = 60;//60 secondS
    private static final int CMD_STATUS = 1;
    private static final int CMD_PERIOD_COUNT = 2;
    private static final int CMD_TIME_COUNT = 3;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            Log.d(TAG, "handleMessage " + msg);
            switch (msg.what) {
                case CMD_STATUS:
                    ;
                    break;
                case CMD_PERIOD_COUNT:
                    mPeriodTimeout--;
                    mPeriodStatus.setText("下次胎动记录 " + mPeriodTimeout + " 秒后有效");
                    if (mPeriodTimeout >= 0) {
                        mHandler.sendEmptyMessageDelayed(CMD_PERIOD_COUNT, 1000);
                    } else {
                        Log.d(TAG, "CMD_PERIOD_COUNT time is up");
                        mPeriodStatus.setText("距离上次胎动已超过 " + getValueFromEdit(mTimeOutCount)/*TIME_PERIOD*/ + " 秒, 点击记录有效！");
                        //mPeriodTimeout = getValueFromEdit(mTimeOutCount);//TIME_PERIOD;
                    }
                    break;
                case CMD_TIME_COUNT:
                    mTotalTime--;
                    mTotalStatus.setText("总时间还剩 " + (mTotalTime / 60) + " 分 " + (mTotalTime % 60) + " 秒, 有效胎动次数为 " + mBabyActionCount + " 次" );
                    if (mTotalTime >= 0) {
                        mHandler.sendEmptyMessageDelayed(CMD_TIME_COUNT, 1000);
                    } else {
                        Log.d(TAG, "CMD_TIME_COUNT time is up");
                        mTotalStatus.setText("时间到，有效胎动次数为 " + mBabyActionCount + " 次");
                        //mTotalTime = getValueFromEdit(mTimeCount);//TIME_TOTAL;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        acquireWakeLock();
        reset();
        writeRecord("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reset();
        writeRecord("onPause");
        releaseWakeLock();
    }

    private void initView() {
        mStart = (Button)findViewById(R.id.total_start);
        mRecord = (Button)findViewById(R.id.record);
        mRead = (Button)findViewById(R.id.read);
        mDelete = (Button)findViewById(R.id.delete);
        mTimeCount = (EditText) findViewById(R.id.time_count_edit);
        mTimeOutCount = (EditText)findViewById(R.id.timeout_count_edit);
        mTotalStatus = (TextView)findViewById(R.id.status_total_text);
        mPeriodStatus = (TextView)findViewById(R.id.status_period_text);
    }

    private void initListener() {
        mStart.setOnClickListener(mOnClickListener);
        mRecord.setOnClickListener(mOnClickListener);
        mRead.setOnClickListener(mOnClickListener);
        mDelete.setOnClickListener(mOnClickListener);
        boolean result = mStart.requestFocus();
        Log.d(TAG, "initListener requestFocus = " + result);
    }

    private void reset() {
        mHandler.removeCallbacksAndMessages(null);
        mStart.setText("开始");
        mPeriodTimeout = TIME_PERIOD;
        mTotalTime = TIME_TOTAL;
        mBabyActionCount = 0;
        mTotalStatus.setText("");
        mPeriodStatus.setText("");
        mIsStarted = false;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.total_start:
                    if (!mIsStarted) {
                        mHandler.sendEmptyMessageDelayed(CMD_TIME_COUNT, 0);
                        mStart.setText("停止");
                        mIsStarted = true;
                    } else {
                        reset();
                        /*mHandler.removeCallbacksAndMessages(null);
                        mStart.setText("开始");
                        mPeriodTimeout = TIME_PERIOD;
                        mTotalTime = TIME_TOTAL;
                        mBabyActionCount = 0;
                        mTotalStatus.setText("");
                        mPeriodStatus.setText("");
                        mIsStarted = false;*/
                    }
                    writeRecord("start");
                    break;
                case R.id.record:
                    if (!mIsStarted) {
                        Log.d(TAG, "onClick not started");
                        return;
                    }
                    if (mBabyActionCount == 0 || mPeriodTimeout < 0) {
                        mBabyActionCount++;
                    } else {
                        Log.d(TAG, "onClick record need wait for last record timeout");
                    }
                    mHandler.removeMessages(CMD_PERIOD_COUNT);
                    mPeriodTimeout = getValueFromEdit(mTimeOutCount);//TIME_PERIOD;
                    mHandler.sendEmptyMessageDelayed(CMD_PERIOD_COUNT, 0);
                    writeRecord("record");
                    break;
                case R.id.read:
                    Intent intent = new Intent();
                    intent.setClassName("www.opendiylib.babyjumpcount", "www.opendiylib.babyjumpcount.ReadRecordActivity");
                    startActivity(intent);
                    break;
                case R.id.delete:
                    MyDatabaseManager.deleteAllData(getApplicationContext(), MyDatabaseManager.BABY_ACTION_URI);
                    break;
                default:
                    break;
            }
        }
    };

    private int getValueFromEdit(EditText editText) {
        int result = 0;
        if (editText.getId() == R.id.time_count_edit) {
            result = TIME_TOTAL;
        } else if (editText.getId() == R.id.timeout_count_edit) {
            result = TIME_PERIOD;
        }
        if (editText != null) {
            Editable editable = editText.getText();
            if (editable != null/* && editable.length() > 0*/) {
                String value = editable.toString();
                if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
                    result = Integer.valueOf(value);
                }
            }
        }
        if (editText.getId() == R.id.time_count_edit) {
            result = result * 60;
        }
        Log.d(TAG, "getValueFromEdit " + result);
        return  result;
    }

    private void writeRecord(String step) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(date);
        ContentValues values = new ContentValues();
        values.put(MyDatabaseManager.PROJECTION[1], mBabyActionCount);
        values.put(MyDatabaseManager.PROJECTION[2], "total rest = " + mTotalTime + ", period rest = " + mPeriodTimeout + ", step = " + step);
        values.put(MyDatabaseManager.PROJECTION[3], time);
        values.put(MyDatabaseManager.PROJECTION[4], String.format("%2s", (mTotalTime / 60)) + "分" + String.format("%2s", (mTotalTime % 60)) + "秒");
        values.put(MyDatabaseManager.PROJECTION[5], mPeriodTimeout);
        values.put(MyDatabaseManager.PROJECTION[6], mIsStarted ? "started" : "stopped");
        MyDatabaseManager.insertData(getApplicationContext(), MyDatabaseManager.BABY_ACTION_URI, values);
    }

    private void acquireWakeLock()
    {
        if (null == mWakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getPackageName());
            if (null != mWakeLock)
            {
                mWakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock()
    {
        if (null != mWakeLock)
        {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
