/*
 * *****************************************************************************
 *  Copyright 2014 Bowen Cai
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * *****************************************************************************
 */

package com.caibowen.gplume.webex;

import com.caibowen.gplume.misc.Str;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.annotation.Nullable;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 *
 * @author BowenCai
 */
public final class Jsons {

    public static final ObjectMapper MAPPER = createMapper();

    public static void setDateToLong(ObjectMapper mapper) {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new Date2LongSerializer());
        mapper.registerModule(module);
    }


    public static ObjectMapper createMapper() {
        return createMapper(Include.NON_NULL);
    }

    public static ObjectMapper createMapper(Include inclusion) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(inclusion);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);//����map valueΪ��
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public static <T> T deserial(String jsonString, Class<T> clazz) {
        if (Str.Utils.isBlank(jsonString))
            return null;
        try {
            return MAPPER.readValue(jsonString, clazz);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }

    public static <T> T deserial(String jsonString, TypeReference typeReference) {
        if (Str.Utils.isBlank(jsonString)) return null;

        try {
            return MAPPER.readValue(jsonString, typeReference);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }

    public static String serial(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }


    public static void setDateFormat(String pattern) {
        if (Str.Utils.notBlank(pattern)) {
            DateFormat df = new SimpleDateFormat(pattern);
            MAPPER.getSerializationConfig().with(df);
            MAPPER.getDeserializationConfig().with(df);
        }
    }


    @Nullable
    public static JsonNode getVal(String json, String key) {
        try {
            final JsonNode node = MAPPER.readTree(json);
            if (node == null) return null;
            return node.get(key);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }


    private static class Date2LongSerializer extends JsonSerializer<Date> {

        @Override
        public void serialize(Date value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            if (value == null) return;
            generator.writeNumber(value.getTime());
        }
    }
}