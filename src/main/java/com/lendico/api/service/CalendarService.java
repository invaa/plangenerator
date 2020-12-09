package com.lendico.api.service;

import org.springframework.stereotype.Service;

@Service
public class CalendarService {
    int daysInMonth() {
        return 30;
    }
    int getDaysInYear() {
        return 360;
    }
}
