/*******************************************************************************
 * Copyright (c) 2017 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ibm.ws.lars.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Existing clients will send URLs containing the '|' character which isn't permitted in URLs
 * <p>
 * This filter escapes that character before it reaches other parts of the system which will
 * complain.
 */
@WebFilter("/ma/v1/*")
public class FixUrlFilter implements Filter {

    private static final String ESCAPED_BAR = "%7C";

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest) {
            req = new HttpServletRequestWrapper((HttpServletRequest) req) {

                @Override
                public String getPathInfo() {
                    return escapeBar(super.getPathInfo());
                }

                @Override
                public String getQueryString() {
                    return escapeBar(super.getQueryString());
                }

                @Override
                public String getRequestURI() {
                    return escapeBar(super.getRequestURI());
                }

                @Override
                public StringBuffer getRequestURL() {
                    return new StringBuffer(escapeBar(super.getRequestURL().toString()));
                }
            };
        }

        chain.doFilter(req, resp);
    }

    private static String escapeBar(String string) {
        if (string == null) {
            return null;
        } else {
            return string.replace("|", ESCAPED_BAR);
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        // Nothing to do
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

}
