package amazon.framework.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateUtils<pubic> {

    public final static String DATE_FORMAT = "MMM d, yyyy";

    public static List<Date> convertStringToDate(final List<String> listByString) {
        List<Date> listByDate = new ArrayList<>(listByString.size());
        for(String s : listByString) {
            try {
                Date dateObj = new SimpleDateFormat(DATE_FORMAT).parse(s);
                listByDate.add(dateObj);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return listByDate;
    }
}
