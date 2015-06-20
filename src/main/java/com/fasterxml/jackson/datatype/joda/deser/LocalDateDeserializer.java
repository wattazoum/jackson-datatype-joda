package com.fasterxml.jackson.datatype.joda.deser;

import java.io.IOException;

import org.joda.time.LocalDate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.joda.cfg.FormatConfig;
import com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat;

public class LocalDateDeserializer
    extends JodaDateDeserializerBase<LocalDate>
{
    private static final long serialVersionUID = 1L;

    public LocalDateDeserializer() {
        this(FormatConfig.DEFAULT_LOCAL_DATEONLY_FORMAT);
    }
    
    public LocalDateDeserializer(JacksonJodaDateFormat format) {
        super(LocalDate.class, format);
    }

    @Override
    public JodaDateDeserializerBase<?> withFormat(JacksonJodaDateFormat format) {
        return new LocalDateDeserializer(format);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
            String str = p.getText().trim();
            return (str.length() == 0) ? null
                    : _format.createParser(ctxt).parseLocalDate(str);
        }
        if (p.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
            return new LocalDate(p.getLongValue());            
        }
        
        // [yyyy,mm,dd] or ["yyyy","mm","dd"]
        if (p.isExpectedStartArrayToken()) {
            int year = p.nextIntValue(-1); // fast speculative case
            if (year == -1) { // either -1, or not an integral number; slow path
                year = _parseIntPrimitive(p, ctxt);
            }
            int month = p.nextIntValue(-1);
            if (month == -1) {
                month = _parseIntPrimitive(p, ctxt);
            }
            int day = p.nextIntValue(-1);
            if (day == -1) {
                day = _parseIntPrimitive(p, ctxt);
            }
            if (p.nextToken() != JsonToken.END_ARRAY) {
                throw ctxt.wrongTokenException(p, JsonToken.END_ARRAY, "after LocalDate ints");
            }
            return new LocalDate(year, month, day);
        }
        throw ctxt.wrongTokenException(p, JsonToken.START_ARRAY, "expected String, Number or JSON Array");
    }
}