package com.jkoh.util;

import java.util.*;
import static java.util.Calendar.*;

public class CountdownUtil {
	public static long getCountdown(Date nowTime, Date endTime) {
		return endTime.getTime() - nowTime.getTime();
	}
	
	public enum TimedType {
		MINUTES,
		HOURS,
		DAYS
	}
	public static long getTimedCountdown(Date nowTime, Date startTime, int duration, TimedType timedType) {
		Calendar end = Calendar.getInstance();
		end.setTime(startTime);
		switch(timedType) {
			case MINUTES:
				end.add(MINUTE, duration);
				break;
			case HOURS:
				end.add(HOUR, duration);
				break;
			case DAYS:
				end.add(DATE, duration);
				break;
		}
		return getCountdown(nowTime, end.getTime());
	}
	
	public enum RecurringType {
		HOURLY,
		DAILY,
		WEEKLY
	}
	public static long getRecurringCountdown(Date nowTime, TimeZone timeZone, Date recurringSlot, int offsetSeconds, RecurringType recurringType) {
		Calendar now = Calendar.getInstance(timeZone);
		now.setTime(nowTime);
		Calendar slot = Calendar.getInstance(timeZone);
		slot.setTime(recurringSlot);
		if(offsetSeconds != 0) {
			now.add(SECOND, offsetSeconds);
			slot.add(SECOND, offsetSeconds);
		}
		return getCountdown(now.getTime(), getNextRecurrence(slot.getTime(), timeZone, recurringType));
	}
	public static Date getNextRecurrence(Date recurringSlot, TimeZone timeZone, RecurringType recurringType) {
		Calendar next = Calendar.getInstance(timeZone);
		next.setTime(recurringSlot);
		
		// truncate
		switch(recurringType) {
			case WEEKLY:
				next.setFirstDayOfWeek(SUNDAY);
				next.set(DAY_OF_WEEK, next.getFirstDayOfWeek());
				// fallthrough
			case DAILY:
				next.set(HOUR_OF_DAY, 0);
				// fallthrough
			case HOURLY:
				next.set(MINUTE, 0);
				next.set(SECOND, 0);
				next.set(MILLISECOND, 0);
				next.getTime(); // force recalculate
				break;
		}
		
		// next recurrence
		switch(recurringType) {
			case HOURLY:
				next.add(HOUR, 1);
				break;
			case DAILY:
				next.add(DATE, 1);
				break;
			case WEEKLY:
				next.add(WEEK_OF_YEAR, 1);
				break;
		}
		return next.getTime();
	}
}