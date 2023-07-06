package ftn.app.util;

import ftn.app.model.enums.EventType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static Date getDateWithoutTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            LoggingUtil.LogEvent("Internal error.", EventType.ERROR, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
