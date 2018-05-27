package de.berlios.vch.download;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SpeedometerInputStream extends FilterInputStream {

    private int bytesRead;
    private long lastTime;
    private float speed;
    private int interval = 1;
    float[] history = new float[10];
    int historyIndex = 0;
    int historyCount = 0;

    public SpeedometerInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        bytesRead++;
        calculateSpeed();
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        bytesRead += read;
        calculateSpeed();
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = super.read(b);
        // dont do speed calculation here, because
        // this method calls read(byte[] b, int off, int len)
        // calculation will be done there
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = super.skip(n);
        bytesRead += skipped;
        calculateSpeed();
        return skipped;
    }

    private void calculateSpeed() {
        long now = System.nanoTime();
        if(lastTime == 0) {
            speed = -1;
            lastTime = now;
            return;
        }

        long diff = now - lastTime;
        if(diff > (interval * 10000000l)) {
            double bytesPerNanoSec = (double)bytesRead / (double)diff;
            double bytesPerSec = bytesPerNanoSec * 1000000000l;
            float kilobytesPerSec = (float)bytesPerSec / 1024f;
            history[historyIndex] = kilobytesPerSec;
            historyIndex = ++historyIndex % history.length;
            historyCount = Math.min(++historyCount, history.length);

            if(historyCount > 3) {
                float sum = 0;
                for (int i = 0; i < historyCount; i++) {
                    sum += history[i];
                }
                float avg = sum / historyCount;

                float stdDeviation = 0;
                for (int i = 0; i < historyCount; i++) {
                    stdDeviation += Math.abs(history[i] - avg);
                }
                stdDeviation = stdDeviation / historyCount;

                sum = 0;
                float avg2 = 0;
                int counted = 0;
                for (int i = 0; i < historyCount; i++) {
                    float value = history[i];
                    float deviation = Math.abs(avg - value);
                    if(avg2 == 0 || deviation < stdDeviation) {
                        sum += value;
                        avg2 = sum / ++counted;
                    }
                }
                speed = avg2;
            }

            // reset values
            bytesRead = 0;
            lastTime = now;
        }
    }

    /**
     *
     * @return the speed in KiB/s for the last {@link #interval} or -1, if no
     *         speed is available currently
     */
    public float getSpeed() {
        return speed;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
