package com.lendico.api.service;

import org.springframework.stereotype.Service;

@Service
public class CalendarService {
    public int daysInMonth() {
        return 30;
    }
    public int getDaysInYear() {
        return 360;
    }
}
