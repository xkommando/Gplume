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

import com.caibowen.gplume.webex.Result;

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
}