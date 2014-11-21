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

package com.caibowen.gplume.misc.test.json;

import com.caibowen.gplume.misc.test.Result;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bowen.cbw
 * @since 8/21/2014.
 */
public class JsonResult<T> implements Result<T> {

    protected int code;
    protected String msg;
    protected T data;

    public JsonResult() {
        this.code = 200;
    }

    public JsonResult(final T data) {
        this.code = 200;
        this.data = data;
    }

    public JsonResult(final int code, final T data) {
        this.code = code;
        this.data = data;
    }

    public JsonResult(final int code, final String msg) {
        this.code = code;
        this.msg = msg;
    }

    public JsonResult(final int code, final T data, final String msg) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public JsonResult(String key, Object value) {
        this.code = 200;
        final Map<String, Object> data = new HashMap<String, Object>(32);
        data.put(key, value);
        this.data = (T) data;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public T data() {
        return data;
    }

    @Override
    public String msg() {
        return msg;
    }

    public boolean ok() {
        return code == 200;
    }

    public JsonResult put(String key, Object value) {
        if (this.data == null) this.data = (T) new HashMap<String, Object>(32        );
        final Map<String, Object> data = (Map<String, Object>) this.data;
        data.put(key, value);
        return this;
    }

    public JsonResult putId(long data) {
        return put("id", data);
    }

    public JsonResult putList(@Nonnull Object data) {
        return put("list", data);
    }

    public JsonResult putSuccess(boolean data) {
        return put("success", data);
    }

    public JsonResult putTimestamp(long data) {
        return put("timestamp", data);
    }

    public static JsonResult list(@Nonnull Object data) {
        return new JsonResult("list", data);
    }

    public static JsonResult<Boolean> success(boolean data) {
        return new JsonResult("success", data);
    }

    public static JsonResult<Long> id(long data) {
        return new JsonResult("id", data);
    }

    public static JsonResult<Long> timestamp(long data) {
        return new JsonResult("timestamp", data);
    }
}
