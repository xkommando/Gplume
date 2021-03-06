package gplume.scala.context


import java.lang.reflect.{Field, Modifier}
import java.util.{List => JList, Map => JMap, Properties => JProperties}
import javax.annotation.{Nonnull, Nullable}

import com.caibowen.gplume.context.{BeanAssemblingException, IBeanBuilder, XMLTags, DefaultBeanBuilder => JBeanBuilder}
import com.caibowen.gplume.core.{BeanEditor, Converter}
import com.caibowen.gplume.misc.Str.Utils._
import com.caibowen.gplume.misc.{Assert, Klass}
import org.w3c.dom.{Element, Node}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.convert.wrapAsScala._

/**
* @author BowenCai
* @since  12/12/2014.
*/
class SBeanBuilder extends JBeanBuilder {

  /**
   * @param start
   * @return
   * @throws Exception
   */
  @Nonnull
  protected def
  scalaList(start: Node): List[AnyRef] = {
    val beanList = new ListBuffer[AnyRef]
    beanList.sizeHint(16)

    var iter = start
    while (iter != null && iter.getNodeType == Node.ELEMENT_NODE) {
      val elemBn = iter.asInstanceOf[Element]
      val tagType = elemBn.getNodeName
      tagType match {
        case XMLTags.BEAN =>
          beanList += buildBean(elemBn, "temp")

        case XMLTags.PROP_REF =>
          val _s = elemBn.getTextContent
          if (isBlank(_s)) throw new IllegalArgumentException("Empty Reference")
          beanList += getAssembler.getBean(getConfigCenter.replaceIfPresent(_s.trim))

        case XMLTags.PROP_VALUE =>
          val lit = getConfigCenter.replaceIfPresent(elemBn.getTextContent.trim)
          val _tgtType = elemBn.getAttribute(XMLTags.TYPE)
          if (notBlank(_tgtType)) {
            val k: Class[_] = Converter.getClass(getConfigCenter.replaceIfPresent(_tgtType.trim))
            beanList += Converter.slient.translateStr(lit, k)
          }
          else beanList += lit

        case XMLTags.PROP_LIST =>
          val _prop = elemBn.getFirstChild.getNextSibling.asInstanceOf[Element]
          val nls = this.scalaList(_prop)
          beanList += nls

        case XMLTags.PROP_MAP =>
          val _propM: Element = elemBn.getFirstChild.getNextSibling.asInstanceOf[Element]
          val m = scalaMap(_propM, "Constructor:")
          beanList += m

        case _ =>
          throw new IllegalArgumentException("Unknown property[" + iter.getNodeName + "]")
      }
      iter = iter.getNextSibling.getNextSibling
    }
    beanList.result()
  }

  protected def
  scalaMap(start: Node, propName: String): Map[String, AnyRef] = {
    var iter = start
    val builder = new mutable.HashMap[String, AnyRef]

    while (iter != null) {
      if (iter.getNodeType == Node.ELEMENT_NODE) {
        val elemBn = iter.asInstanceOf[Element]
        var mapK = elemBn.getTagName.trim
        if (XMLTags.PROP_MAP_KEY == mapK) {
          val _k = elemBn.getAttribute(XMLTags.PROP_NAME)
          if (notBlank(_k)) mapK = _k
        }
        mapK = getConfigCenter.replaceIfPresent(mapK)
        if (builder.contains(mapK))
          throw new IllegalArgumentException("duplicated map key[" + mapK + "] for property[" + propName + "]")

        val _v = elemBn.getTextContent
        Assert.hasText(_v)
        var mapV: AnyRef = getConfigCenter.replaceIfPresent(_v)
        val _tgtType = elemBn.getAttribute(XMLTags.TYPE)
        if (notBlank(_tgtType)) {
          val k = Converter.getClass(getConfigCenter.replaceIfPresent(_tgtType.trim))
          mapV = Converter.slient.translateStr(mapV.asInstanceOf[String], k)
        }
        builder += mapK -> mapV
        iter = iter.getNextSibling.getNextSibling
      }
    }
    builder.toMap
  }

  protected override def
  newInstance(@Nonnull klass: Class[_], @Nullable prop: Element): AnyRef = {
    if (prop == null) {
      val _sobj = findSingletonObject(klass)
      if (_sobj != null)
        return _sobj
      try {
        val empty = Array[Class[_]]()
        val ctor = klass.getDeclaredConstructor(empty:_*)
        if (!Modifier.isPublic(ctor.getModifiers))
          ctor.setAccessible(true)
        return ctor.newInstance().asInstanceOf[AnyRef]
      }catch {
        case e:Throwable =>
                  throw new BeanAssemblingException("Could not find default constructor:" + klass.getCanonicalName, e)
      }
    }
    // 2 try in tag
    var _tagVal: AnyRef = null
    try {
      _tagVal = super.inTag(prop, false, null)
    }
    catch {
      case e: NullPointerException =>
        val cs = Klass.findCtorParam(klass)
        if (cs.size != 0) {
          throw new IllegalStateException("Could not determine constructor parameter for [" + klass + "]")
        }
        _tagVal = inTag(prop, false, cs.get(0))
    }

    if (null != _tagVal) return BeanEditor.construct(klass, _tagVal)

    // 3 try parse xml
    val ls = this.scalaList(prop)
    BeanEditor.construct(klass, ls.toArray)
  }

  /**
    * given
    * object scalaSingletonObject {}
    * returns
    * scalaSingletonObject$.MODULE$
    *
    *  check if it is sington object by:
    *  1. exists scalaSingtonObject$
    *  2. original class does not declare any fields
    *  3. original class only have public static methods
    *  4. the scalaSingtonObject$ has same typed non-null public static field named MODULE$;
    *
    * @param klass
    * @return
    */
  def findSingletonObject(@Nonnull klass: Class[_]): AnyRef = {
    if (klass.getDeclaredFields.isEmpty
      && (!klass.getDeclaredMethods.exists { m =>
      val modi = m.getModifiers
      !Modifier.isPublic(modi) || !Modifier.isStatic(modi)
    })) {
      val mdKls = try Class.forName(klass.getCanonicalName + "$")
      catch {
        case cn: ClassNotFoundException => null.asInstanceOf[Class[_]]
      }
      if (mdKls != null) {
        val fdSObj = try mdKls.getDeclaredField("MODULE$")
        catch {
          case nf: NoSuchFieldException => null.asInstanceOf[Field]
        }
        if (fdSObj != null && fdSObj.getType == mdKls) {
          val modi = fdSObj.getModifiers
          if (Modifier.isPublic(modi) && Modifier.isStatic(modi)) {
            val realObj = fdSObj.get()
            require(realObj != null)
            return realObj
          }
        }
      }
    }
    null.asInstanceOf[AnyRef]
  }

  @Nonnull@throws(classOf[Exception])
  override def
  buildBean(beanElem: Element, @Nullable beanID: String): AnyRef = {
    val bnClass = super.getClass(beanElem)
    val beanObj = super.construct(bnClass, beanElem)

    IBeanBuilder.LOG.debug("bean class[{}] created", bnClass.getName)
    super.beforeProcess(beanObj, beanID)

    val _propLs = beanElem.getElementsByTagName(XMLTags.BEAN_PROP)
    if (_propLs == null || _propLs.getLength == 0) {
      super.afterProcess(beanObj, null)
      return beanObj
    }

    // get top level properties only, skip properties in sub beans
    var next: Node = beanElem.getFirstChild
    while (next.getNextSibling != null) {
      next = next.getNextSibling

      if (next.getNodeType == Node.ELEMENT_NODE
        && XMLTags.BEAN_PROP.equals(next.getNodeName)) {

        val prop = next.asInstanceOf[Element]

        val _xname = prop.getAttribute(XMLTags.PROP_NAME)
        require(notBlank(_xname), s"empty property name in bean $beanID")
        val propName = getConfigCenter.replaceIfPresent(_xname.trim)

        val varList = prop.getChildNodes
        if (varList == null || varList.getLength == 0) {
          val _v = inTag(prop, true, Klass.findType(beanObj.getClass, propName))
          BeanEditor.setProperty(beanObj, propName, _v)

        } else {
          var iter = prop.getFirstChild.getNextSibling
          iter.getNodeName match {
            case XMLTags.PROP_LIST =>
              iter = iter.getFirstChild.getNextSibling
              val beanList1 = this.scalaList(iter)
              BeanEditor.setProperty(beanObj, propName, beanList1)

            case XMLTags.PROP_SET =>
              iter = iter.getFirstChild.getNextSibling
              val beanList2 = scalaList(iter)
              this.setSetProperty(beanObj, propName, beanList2)

            case XMLTags.PROP_MAP =>
              iter = iter.getFirstChild.getNextSibling
              val properties = this.scalaMap(iter, propName)
              BeanEditor.setProperty(beanObj, propName, properties)

            case _ =>
              val beanList3 = scalaList(iter)
              require(beanList3.size == 1,
                s"""Bean number miss match for property[$propName] " +
                  "in class [${bnClass.getName}]\r\n + needs 1 actual ${beanList3.size}
                  actual values : ${beanList3.toString}""")

              BeanEditor.setProperty(beanObj, propName, beanList3.head)

          } // match
        } //else
      }
    } // while
    beanObj
  }

  private def
  setSetProperty(@Nullable bean: AnyRef, @Nonnull propName: String, varList: List[AnyRef]): Unit = {
    val set = new mutable.HashSet
    set ++ varList
    BeanEditor.setProperty(bean, propName, set.toSet)
  }
}