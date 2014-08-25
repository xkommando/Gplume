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

package com.caibowen.gplume.webex.json;

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
 * json java bean 互转工具类
 *
 * @author Yanxiaoxu
 */
public final class Jsons {

    private final static ObjectMapper mapper = createMapper();

    public static ObjectMapper createMapper() {
        return createMapper(Include.NON_NULL);
    }

    public static ObjectMapper createMapper4Spring() {
        final ObjectMapper mapper = createMapper(Include.NON_NULL);
        setDateToLong(mapper);
        return mapper;
    }

    public static ObjectMapper createMapper(Include inclusion) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(inclusion);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);//忽略map value为空
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Nullable
    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        if (Str.Utils.isBlank(jsonString))
            return null;
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T fromJson(String jsonString, TypeReference typeReference) {
        if (Str.Utils.isBlank(jsonString)) return null;

        try {
            return mapper.readValue(jsonString, typeReference);
        } catch (Exception e) {
            return null;
        }
    }

    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            return null;
        }
    }


    public static void setDateFormat(String pattern) {
        if (Str.Utils.notBlank(pattern)) {
            DateFormat df = new SimpleDateFormat(pattern);
            mapper.getSerializationConfig().with(df);
            mapper.getDeserializationConfig().with(df);
        }
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }


    public static JsonNode getValue(String json, String key) {
        try {
            final JsonNode node = mapper.readTree(json);
            if (node == null) return null;
            return node.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setDateToLong(ObjectMapper mapper) {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new Date2LongSerializer());
        mapper.registerModule(module);
    }

    private static class Date2LongSerializer extends JsonSerializer<Date> {

        @Override
        public void serialize(Date value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            if (value == null) return;
            generator.writeNumber(value.getTime());
        }
    }
}