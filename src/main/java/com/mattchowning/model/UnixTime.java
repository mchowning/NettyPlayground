package com.mattchowning.model;

import com.mattchowning.utils.TimeUtil;

import java.util.Date;

public class UnixTime {

    private final long value;

    public UnixTime() {
        this(TimeUtil.getCurrentTimeSince1900());
    }

    public UnixTime(long value) {
        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public String toString() {
        Date d = new Date(TimeUtil.unixFromTimeSince1900(value()));
        return d.toString();
    }
}
