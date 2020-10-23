package com.curiouslearning.referralnetwork;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.IntDef;

public class Event {
    @IntDef
    public @interface EventType {
        int UNKNOWN = 0;
        int SESSION_START = 1;
        int SESSION_END = 2;
        int PROGRESS_CHANGE = 3;
        int FIRST_USE = 4;          // TBD: First time app use after install
        int REFERRAL = 5;           // TBD: this app was referred from
    }

    private @EventType final int type;
    private final Date timestamp;

    Event(@EventType int type) {
        this.type = type;
        this.timestamp = Calendar.getInstance().getTime();
    }

    @Override
    public String toString() {
        String typeStr = "";
        if (type == EventType.UNKNOWN) {
            typeStr = "Unknown";
        } else if (type == EventType.SESSION_START) {
            typeStr = "Session Start";
        } else if (type == EventType.SESSION_END) {
            typeStr = "Session End";
        } else if (type == EventType.PROGRESS_CHANGE) {
            typeStr = "Progress Change";
        } else if (type == EventType.FIRST_USE) {
            typeStr = "First Use";
        } else if (type == EventType.REFERRAL) {
            typeStr = "Referral";
        }
        return String.format("%s @ %s", typeStr, DateFormat.getDateTimeInstance().format(timestamp));
    }
}
