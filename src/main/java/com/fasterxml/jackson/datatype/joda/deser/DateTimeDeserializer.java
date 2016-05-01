package com.fasterxml.jackson.datatype.joda.deser;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableInstant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.joda.cfg.FormatConfig;
import com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat;

/**
 * Basic deserializer for {@link ReadableDateTime} and its subtypes.
 * Accepts JSON String and Number values and passes those to single-argument constructor.
 * Does not (yet?) support JSON object; support can be added if desired.
 */
public class DateTimeDeserializer
    extends JodaDateDeserializerBase<ReadableInstant>
{
    private static final long serialVersionUID = 1L;

    public DateTimeDeserializer(Class<?> cls, JacksonJodaDateFormat format) {
        super(cls, format);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ReadableInstant> JsonDeserializer<T> forType(Class<T> cls)
    {
        return (JsonDeserializer<T>) new DateTimeDeserializer(cls,
                FormatConfig.DEFAULT_DATETIME_PARSER);
    }

    @Override
    public JodaDateDeserializerBase<?> withFormat(JacksonJodaDateFormat format) {
        return new DateTimeDeserializer(_valueClass, format);
    }

    @Override
    public ReadableDateTime deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException
    {
        JsonToken t = p.getCurrentToken();

        if (t == JsonToken.VALUE_NUMBER_INT) {
            DateTimeZone tz = _format.isTimezoneExplicit() ? _format.getTimeZone() : DateTimeZone.forTimeZone(ctxt.getTimeZone());
            return new DateTime(p.getLongValue(), tz);
        }
        if (t == JsonToken.VALUE_STRING) {
            String str = p.getText().trim();
            if (str.length() == 0) {
                return null;
            }
            // 08-Jul-2015, tatu: as per [datatype-joda#44], optional TimeZone inclusion
            // NOTE: on/off feature only for serialization; on deser should accept both
            int ix = str.indexOf('[');
            if (ix > 0) {
                int ix2 = str.lastIndexOf(']');
                String tzId = (ix2 < ix)
                        ? str.substring(ix+1)
                        : str.substring(ix+1, ix2);
                DateTimeZone tz;
                try {
                    tz = DateTimeZone.forID(tzId);
                } catch (IllegalArgumentException e) {
                    throw ctxt.mappingException(String.format("Unknown DateTimeZone id '%s'", tzId));
                }
                str = str.substring(0, ix);

                /* 12-Jul-2015, tatu: Initially planned to support "timestamp[zone-id]"
                 *    format as well as textual, but since JSR-310 datatype (Java 8 datetime)
                 *    does not support it, was left out of 2.6.
                 */
                /*
                // One more thing; do we have plain timestamp?
                if (_allDigits(str)) {
                    return new DateTime(Long.parseLong(str), tz);
                }
                */
                return _format.createParser(ctxt)
                        .withOffsetParsed()
                        .parseDateTime(str)
                        .withZone(tz);
            }
            return _format.createParser(ctxt)
                    .withOffsetParsed()
                    .parseDateTime(str);
        }
        throw ctxt.mappingException(handledType());
    }
}
