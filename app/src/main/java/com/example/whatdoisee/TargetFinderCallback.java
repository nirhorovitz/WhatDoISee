package com.example.whatdoisee;

public interface TargetFinderCallback {
    void TargetFoundCallback(Target target, double height);
    void failureCallback();
    void NotHereCallback(Target target);
}
