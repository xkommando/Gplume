package com.caibowen.gplume.context.bean;

import com.caibowen.gplume.context.IBeanAssembler;

/**
 * @author BowenCai
 * @since 10/12/2014.
 */
public interface AssemblerAwareBean  {

    void setAssembler(IBeanAssembler assembler);
}
