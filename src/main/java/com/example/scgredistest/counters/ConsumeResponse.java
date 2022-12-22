package com.example.scgredistest.counters;


record ConsumeResponse(boolean isAllowed, long remainingRequests, long retryDelayMs) { }
