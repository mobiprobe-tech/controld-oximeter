package com.android.oxymeter.ble;

import com.android.oxymeter.utilities.CommonUtils;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by ZXX on 2016/1/8.
 */
public class DataParser {

    //Const
    public String TAG = this.getClass().getSimpleName();

    //Buffer queue
    private LinkedBlockingQueue<Integer> bufferQueue = new LinkedBlockingQueue<>(256);

    private boolean isStop = true;

    private onPackageReceivedListener mPackageReceivedListener;
    private OxiParams mOxiParams = new OxiParams();


    /**
     * interface for parameters changed.
     */
    public interface onPackageReceivedListener {
        void onOxiParamsChanged(OxiParams params);

        void onPlethWaveReceived(int amp);
    }

    //Constructor
    public DataParser(onPackageReceivedListener listener) {
        this.mPackageReceivedListener = listener;

    }

    public void start() {
        //Parse Runnable
        ParseRunnable mParseRunnable = new ParseRunnable();
        new Thread(mParseRunnable).start();
    }

    public void stop() {
        isStop = true;
    }

    /**
     * ParseRunnable
     */
    class ParseRunnable implements Runnable {
        int dat;
        int[] packageData;

        @Override
        public void run() {
            while (isStop) {
                dat = getData();
                packageData = new int[5];
                if ((dat & 0x80) > 0) //search package head
                {
                    packageData[0] = dat;
                    for (int i = 1; i < packageData.length; i++) {
                        dat = getData();
                        if ((dat & 0x80) == 0) {
                            packageData[i] = dat;
                        } else {
                            continue;
                        }
                    }


                    int spo2 = packageData[4];
                    int pulseRate = packageData[3] | ((packageData[2] & 0x40) << 1);
                    double pi = packageData[0] & 0x0f;

                    if (spo2 != mOxiParams.spo2 || pulseRate != mOxiParams.pulseRate || pi != mOxiParams.pi) {
                        mOxiParams.update(spo2, pulseRate, pi);
                        mPackageReceivedListener.onOxiParamsChanged(mOxiParams);
                    }
                    mPackageReceivedListener.onPlethWaveReceived(packageData[1]);
                }
            }
        }
    }

    /**
     * Add the data received from USB or Bluetooth
     *
     * @param dat data
     */
    public void add(byte[] dat) {
        for (byte b : dat) {
            try {
                bufferQueue.put(toUnsignedInt(b));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get Dat from Queue
     *
     * @return data
     */
    private int getData() {
        int dat = 0;
        try {
            dat = bufferQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return dat;
    }


    private int toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }

    /**
     * a small collection of Oximeter parameters.
     * you can add more parameters as the manual.
     * <p>
     * spo2          Pulse Oxygen Saturation
     * pulseRate     pulse rate
     * pi_data            perfusion index
     */
    public class OxiParams {
        private int spo2;
        private int pulseRate;
        private double pi;             //perfusion index

        private void update(int spo2, int pulseRate, double pi) {
            this.spo2 = spo2;
            this.pulseRate = pulseRate;
            this.pi = pi;
        }

        public int getSpo2() {
            return spo2;
        }

        public int getPulseRate() {
            return pulseRate;
        }

        public double getPi() {
            return pi;
        }
    }

}
