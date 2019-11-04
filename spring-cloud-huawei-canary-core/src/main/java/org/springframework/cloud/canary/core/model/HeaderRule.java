/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.canary.core.model;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class HeaderRule {
    //正则
    private String regex;

    //是否区分大小写 false区分 true不区分
    private Boolean caseInsensitive = false;

    //精准匹配
    private String exact;

    public HeaderRule() {
    }

    public boolean match(String str) {
        if (!caseInsensitive) {
            str = str.toLowerCase();
            exact = exact == null ? null : exact.toLowerCase();
            regex = regex == null ? null : regex.toLowerCase();
        }
        if (exact != null && !str.equals(exact)) {
            return false;
        }
        return regex == null || str.matches(regex);
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public Boolean getCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public String getExact() {
        return exact;
    }

    public void setExact(String exact) {
        this.exact = exact;
    }

    @Override
    public String toString() {
        return "HeaderRule{" +
            "regex='" + regex + '\'' +
            ", caseInsensitive=" + caseInsensitive +
            ", exact='" + exact + '\'' +
            '}';
    }
}
