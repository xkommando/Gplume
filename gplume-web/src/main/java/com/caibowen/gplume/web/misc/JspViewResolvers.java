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

package com.caibowen.gplume.web.misc;

import com.caibowen.gplume.web.RequestContext;
import com.caibowen.gplume.web.view.IStrViewResolver;

import java.io.Serializable;

/**
 * JSP view Resolvers
 *
 * @author BowenCai
 *
 */
public interface JspViewResolvers {

    public class CompletePathViewResolver implements IStrViewResolver {
        @Override
        public void resolve(RequestContext ctx, String ret) {
            ctx.render(ret);
        }
    }

    public class SuffixResolver implements IStrViewResolver, Serializable {

        private static final long serialVersionUID = 6433472763677545676L;

        public final String suffix;

        public SuffixResolver(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public void resolve(RequestContext ctx, String ret) {
            ctx.render(ret + suffix);
        }
    }

    public class PrefixResolver extends CompletePathViewResolver implements Serializable {

        private static final long serialVersionUID = -4770883523301450063L;

        public final String prefix;

        public PrefixResolver(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void resolve(RequestContext ctx, String ret) {
            ctx.render(prefix + ret);
        }

    }

    public class PrefixSuffixResolver extends SuffixResolver {

        private static final long serialVersionUID = 4685236015290815491L;

        public final String prefix;
        public PrefixSuffixResolver(String prefix, String suffix) {
            super(suffix);
            this.prefix = prefix;
        }

        @Override
        public void resolve(RequestContext ctx, String ret) {
            ctx.render(prefix + ret + suffix);
        }
    }

}
