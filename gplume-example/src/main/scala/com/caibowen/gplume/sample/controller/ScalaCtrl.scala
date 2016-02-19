package com.caibowen.gplume.sample.controller

import java.util.Date

import com.caibowen.gplume.web.RequestContext
import com.caibowen.gplume.web.annotation.{Handle, Controller}

/**
 * Created by Bowen Cai on 1/11/2015.
 */
@Controller("/scala")
class ScalaCtrl {

  @Handle(Array("/test1"))
  def t1(ctx: RequestContext): String = {
    ctx.putAttr("msg", "hallo scala!")
    ctx.putAttr("date", new Date())
    "happy"
  }

}
