package com.fasterxml.jackson.datatype.joda;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Configure Jackson to do ISO-8601 conversions on DateTimes.
 */
public class JodaThroughJacksonTest {
    @SuppressWarnings("WeakerAccess")
    static class Beanie {
        public final DateTime jodaDateTime;
        public final Date javaUtilDate;
        @JsonCreator
        public Beanie(@JsonProperty("jodaDateTime") DateTime jodaDateTime,
               @JsonProperty("javaUtilDate") Date javaUtilDate) {
            this.jodaDateTime = jodaDateTime;
            this.javaUtilDate = javaUtilDate;
        }
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JodaModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    /**
     * https://github.com/FasterXML/jackson-datatype-joda/issues/50
     */
    @Test
    public void iso8601ThroughJoda() throws IOException, ParseException {
        String source = "2000-01-01 00:00:00 UTC";

        ObjectMapper mapper = getObjectMapper();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
        Date targetDate = df.parse(source);
        Beanie expectedBean = new Beanie(new DateTime(targetDate.getTime(), DateTimeZone.UTC), targetDate);

        String expectedJSON =
                "{\"jodaDateTime\":\"2000-01-01T00:00:00.000Z\","
                        + "\"javaUtilDate\":\"2000-01-01T00:00:00.000+0000\"}";

        Beanie actualBean = mapper.readValue(expectedJSON, Beanie.class);
        assertEquals(expectedBean.javaUtilDate, actualBean.javaUtilDate);
        assertEquals(expectedBean.jodaDateTime.getMillis(), actualBean.jodaDateTime.getMillis());

        assertEquals(expectedBean.jodaDateTime, actualBean.jodaDateTime);
    }

    /**
     * https://github.com/FasterXML/jackson-datatype-joda/issues/50
     */
    @Test
    public void iso8601ThroughJodaCEST() throws IOException, ParseException {
        String source = "2000-01-01 01:00:00 CET";

        ObjectMapper mapper = getObjectMapper();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
        Date targetDate = df.parse(source);
        Beanie expectedBean = new Beanie(new DateTime(targetDate.getTime(), DateTimeZone.forID("CET")), targetDate);

        String expectedJSON =
                "{\"jodaDateTime\":\"2000-01-01T01:00:00.000+01:00[CET]\","
                        + "\"javaUtilDate\":\"2000-01-01T00:00:00.000+0000\"}";

        Beanie actualBean = mapper.readValue(expectedJSON, Beanie.class);
        assertEquals(expectedBean.javaUtilDate, actualBean.javaUtilDate);
        assertEquals(expectedBean.jodaDateTime.getMillis(), actualBean.jodaDateTime.getMillis());

    /*
     * Why doesn't the JodaModule deserialize a DateTime that is equal to the one it serialized in
     * the first place? I expected these two to be equal but they aren't.
     */
        assertEquals(expectedBean.jodaDateTime, actualBean.jodaDateTime);
    }

    /**
     * https://github.com/FasterXML/jackson-datatype-joda/issues/50
     */
    @Test
    public void iso8601ThroughJodaTZplus3() throws IOException, ParseException {
        String source = "2000-01-01 04:00:00 +0300";

        ObjectMapper mapper = getObjectMapper();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
        Date targetDate = df.parse(source);
        Beanie expectedBean = new Beanie(new DateTime(targetDate.getTime(), DateTimeZone.forOffsetHours(3)), targetDate);

        String expectedJSON =
                "{\"jodaDateTime\":\"2000-01-01T04:00:00.000+03:00\","
                        + "\"javaUtilDate\":\"2000-01-01T01:00:00.000+0000\"}";

        Beanie actualBean = mapper.readValue(expectedJSON, Beanie.class);
        assertEquals(expectedBean.javaUtilDate, actualBean.javaUtilDate);
        assertEquals(expectedBean.jodaDateTime.getMillis(), actualBean.jodaDateTime.getMillis());

    /*
     * Why doesn't the JodaModule deserialize a DateTime that is equal to the one it serialized in
     * the first place? I expected these two to be equal but they aren't.
     */
        assertEquals(expectedBean.jodaDateTime, actualBean.jodaDateTime);
    }

    /**
     * https://github.com/FasterXML/jackson-datatype-joda/issues/43
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void iso8601ThroughJodaTZminus5() throws IOException, ParseException {
        String source = "2001-03-11 19:00:00 EST";

        ObjectMapper mapper = getObjectMapper();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
        Date targetDate = df.parse(source);
        Beanie expectedBean = new Beanie(new DateTime(targetDate.getTime(), DateTimeZone.forID("EST")), targetDate);

        String expectedJSON =
                "{\"jodaDateTime\":\"2001-03-11T19:00:00.000-05:00\","
                        + "\"javaUtilDate\":\"2001-03-12T00:00:00.000+0000\"}";
        String actualJSON = mapper.writeValueAsString(expectedBean);
        assertEquals(expectedJSON, actualJSON );
    }

    /**
     * https://github.com/FasterXML/jackson-datatype-joda/issues/44
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void iso8601ThroughJodaTZminus5WithTZID() throws IOException, ParseException {
        String source = "2001-03-11 19:00:00 EST";

        ObjectMapper mapper = getObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
        Date targetDate = df.parse(source);
        Beanie expectedBean = new Beanie(new DateTime(targetDate.getTime(), DateTimeZone.forID("EST")), targetDate);

        String expectedJSON =
                "{\"jodaDateTime\":\"2001-03-11T19:00:00.000-05:00[EST]\","
                        + "\"javaUtilDate\":\"2001-03-12T00:00:00.000+0000\"}";
        String actualJSON = mapper.writeValueAsString(expectedBean);
        assertEquals(expectedJSON, actualJSON );
    }
}