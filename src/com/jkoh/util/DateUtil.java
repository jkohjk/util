package com.jkoh.util;

import java.util.*;
import static java.util.Calendar.*;

public class DateUtil {
	public static int getDaysAgo(Date previousDate) {
		return getDaysAgo(previousDate, new Date(), TimeZone.getDefault());
	}
	public static int getDaysAgo(Date previousDate, Date nowDate, TimeZone timeZone) {
		Calendar previous = Calendar.getInstance(timeZone);
		previous.setTime(previousDate);
		Calendar now = Calendar.getInstance(timeZone);
		now.setTime(nowDate);
		
		int daysAgo = now.get(DAY_OF_YEAR) - previous.get(DAY_OF_YEAR);
		if(previousDate.before(nowDate)) {
			while(previous.get(YEAR) < now.get(YEAR)) {
				daysAgo += previous.getActualMaximum(DAY_OF_YEAR);
				previous.add(YEAR, 1);
			}
		} else {
			while(now.get(YEAR) < previous.get(YEAR)) {
				daysAgo -= now.getActualMaximum(DAY_OF_YEAR);
				now.add(YEAR, 1);
			}
		}
		
		return daysAgo;
	}
}