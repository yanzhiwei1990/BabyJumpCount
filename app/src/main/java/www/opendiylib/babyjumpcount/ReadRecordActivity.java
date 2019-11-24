package www.opendiylib.babyjumpcount;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ReadRecordActivity extends Activity {
    private static final String TAG = ReadRecordActivity.class.getSimpleName();
    private ListView mListView = null;
    private ArrayAdapter<String> adapter = null;
    private PowerManager.WakeLock mWakeLock = null;

    private static final int CMD_LOAD = 1;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            Log.d(TAG, "handleMessage " + msg);
            switch (msg.what) {
                case CMD_LOAD:
                    initView();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_record);
        mHandler.sendEmptyMessage(CMD_LOAD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        acquireWakeLock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseWakeLock();
    }

    private void initView() {
        String[] data = null;
        data = getRecords();
        if (data == null || data.length == 0) {
            data = new String[]{"无记录"};
        }
        adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, data);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(adapter);
    }

    private String[] getRecords() {
        String[] result = null;
        List<JSONObject> list = MyDatabaseManager.getDatas(this, MyDatabaseManager.BABY_ACTION_URI);
        String time = null;
        String total = null;
        String period = null;
        String count = null;
        String action = null;
        String status = null;
        if (list != null && list.size() > 0) {
            result = new String[list.size()];
            int count_item = 0;
            for (JSONObject obj : list) {
                try {
                    time = obj.getString(MyProvider.TIME);
                    total = obj.getString(MyProvider.TOTAL);
                    period = obj.getString(MyProvider.PERIOD);
                    count = String.valueOf(obj.getInt(MyProvider.COUNT));
                    status = obj.getString(MyProvider.STATUS);
                    action = obj.getString(MyProvider.ACTION);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //result[count_item] = String.format("%20s  %10s  %10s %s", time, count, status, action);
                result[count_item] = String.format("总时间%6s 倒计时%3s秒 计数%3s次", total , period, count);
                count_item++;
            }
        }
        return result;
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
