package ru.spbstu.ioffe.satellite;

import org.orekit.time.AbsoluteDate;

public class Periodis {
    // UTC time
    private AbsoluteDate from;
    private AbsoluteDate to;

    public AbsoluteDate getFrom () {
        return from;
    }

    public void setFrom(AbsoluteDate from) {
        this.from = from;
    }

    public AbsoluteDate getTo () {
        return to;
    }

    public void setTo(AbsoluteDate to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "Periodis{" +
                "from=" + from +
                ", to=" + to + " duration: "
                + to.durationFrom(from) +
                '}';
    }
}
