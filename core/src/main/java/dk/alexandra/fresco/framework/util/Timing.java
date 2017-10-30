package dk.alexandra.fresco.framework.util;

public class Timing {

    private long startTime;
    private long currentTime;

    public Timing() {
        this.currentTime = 0;
    }

    public void start() {
        this.startTime = System.nanoTime();
    }

    /**
     * Returns time spend in nano seconds.
     * 
     * @return
     */
    public long stop() {
        long stopTime = System.nanoTime();
        long l = stopTime - this.startTime;
        this.currentTime += l;
        return l;

    }

    public String formatNanosAsMilliSeconds(double time) {
        return time / 1000000.0 + " ms";
    }

    public long getTimeInNanos() {
        return this.currentTime;
    }
}
