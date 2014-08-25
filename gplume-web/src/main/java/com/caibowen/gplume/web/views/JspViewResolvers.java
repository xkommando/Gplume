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

package com.caibowen.gplume.web.views;

import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.IViewResolver;
import com.caibowen.gplume.web.RequestContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 *
 * resolve jsp by given name, the name is the complete path to the jsp file
 *
 * @author BowenCai
*/
public class JspViewResolvers {

    /**
     * case 1 : viewPrefix="a"   -> PrefixResolver
     * case 2 : viewSuffix="b"   -> SuffixResolver
     * case 3 : prefix="a" and viewSuffix="b" -> PrefixSuffixResolver
     * otherwise : CompletePathViewResolver
     *
     * @return IViewResolver
     */
    public static final IViewResolver
    get(@Nullable String prefix, @Nullable String suffix) {
        int resolverType = 0;
        if (Str.Utils.notBlank(prefix)) {
            resolverType++;
        }
        if (Str.Utils.notBlank(suffix)) {
            resolverType += 3;
        }

        switch (resolverType) {
            case 1:
                return new JspPrefixResolver(prefix);
            case 3:
                return new JspSuffixResolver(suffix);
            case 4:
                return new JspPrefixSuffixResolver(prefix, suffix);
            default:
                return new JspCompletePathViewResolver();
            }
    }

    /**
     * add a prefix to the jsp name to make a complete path
     *
     * @author BowenCai
     *
     */
    public static class JspPrefixResolver implements IViewResolver, Serializable {

        private static final long serialVersionUID = -4770883523301450063L;

        public final String prefix;

        public JspPrefixResolver(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public int fitness(Class val) {
            return val == String.class ? 1 : -1;
        }

        @Override
        public void resolve(RequestContext ctx, Object ret) throws Exception {
            ctx.render(prefix + (String)ret);
        }

    }

    /**
     *  add a suffix to the jsp name to make a complete path
     *
     * @author BowenCai
     *
     */
    public static class JspSuffixResolver implements IViewResolver, Serializable {

        private static final long serialVersionUID = 6433472763677545676L;

        public final String suffix;

        public JspSuffixResolver(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public int fitness(Class val) {
            return val == String.class ? 1 : -1;
        }

        @Override
        public void resolve(RequestContext ctx, Object ret) throws Exception {
            ctx.render((String)ret + suffix);
        }
    }



    /**
     *
     * add a prefix and a suffix to the jsp name to make a complete path
     *
     * @author BowenCai
     *
     */
    public static class JspPrefixSuffixResolver extends JspSuffixResolver {

        private static final long serialVersionUID = 4685236015290815491L;

        public final String prefix;

        public JspPrefixSuffixResolver(String prefix, String suffix) {
            super(suffix);
            this.prefix = prefix;
        }

        @Override
        public void resolve(RequestContext ctx, Object ret) throws Exception {
            ctx.render(prefix + ret + suffix);
        }
    }


    public static class JspCompletePathViewResolver implements IViewResolver {

        @Override
        public int fitness(Class val) {
            return val == String.class ? 1 : -1;
        }

        @Override
        public void resolve(RequestContext ctx, Object ret) throws Exception {
            ctx.render((String)ret);
        }
    }

}
