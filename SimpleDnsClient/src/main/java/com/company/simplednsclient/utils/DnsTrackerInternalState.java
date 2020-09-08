package com.company.simplednsclient.utils;

public enum DnsTrackerInternalState {
    CHECK_RESPONSE,
    SEND_REQUEST,
    TIMED_OUT,
    SLEEPING,
    RESPONSE_RECEIVED
}
