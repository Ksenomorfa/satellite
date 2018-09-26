package ru.spbstu.ioffe.satellite;

import org.orekit.time.AbsoluteDate;

import java.text.DecimalFormat;

public class PeriodOfPresence {
    // UTC time of satellite presence inside zone
    private AbsoluteDate presenceFrom;
    private AbsoluteDate presenceTo;

    public AbsoluteDate getPresenceFrom() {
        return presenceFrom;
    }

    public void setPresenceFrom(AbsoluteDate presenceFrom) {
        this.presenceFrom = presenceFrom;
    }

    public AbsoluteDate getPresenceTo() {
        return presenceTo;
    }

    public void setPresenceTo(AbsoluteDate presenceTo) {
        this.presenceTo = presenceTo;
    }

    public String getPeriodOfPresence() {
        return new DecimalFormat("0.00").format(presenceTo.durationFrom(presenceFrom));
    }

    @Override
    public String toString() {
        return "Period UTC " +
                "presenceFrom = " + presenceFrom +
                ", presenceTo = " + presenceTo
                + " duration = "
                + getPeriodOfPresence() + " seconds ";
    }
}
