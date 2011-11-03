package net {
  package liftmodules.jqplot {
  
  import java.text.SimpleDateFormat
  
  import scala.collection.mutable.Map
  import scala.xml.{Comment,NodeSeq}
  
  import liftweb.common.Loggable
  import liftweb.http.{ LiftRules, S,ResourceServer }
  import liftweb.http.js.JE.{JsRaw,JsVar}
  import liftweb.http.js.JsCmds._
  import liftweb.json.JsonAST._
  import liftweb.util.{Helpers,Props}
  import liftweb.util.Props.RunModes._
  
      class JqPlot(w:Int,h:Int,options:Option[Options],series:List[List[Any]]*) extends Loggable {
        
       val sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm")
        
        val id = Helpers.nextFuncName   
        
        val style = "height:%spx; width:%spx;".format(h,w)
        
        val version = Props.mode match {
            case Production => ".min.js" 
            case Pilot => ".min.js"
            case otherwise => ".js"
        }

       val plugins = options match {
         case  Some(op) =>
           		for { plugin <- op.plugins } yield <script type="text/javascript" src={"%s/%s/js/plugins/jqplot.%s%s".format(S.contextPath,LiftRules.resourceServerPath,plugin.name,version)}></script>
         case otherwise => Nil   
        }     
        
        def toCssTransformer:NodeSeq => NodeSeq = { _ => toHtml}
        
        def toHtml = {
            val jsonSeries = series.map { s => JArray( s.map { v => JArray( 
              v.map { 
              	case x:Int     => JInt(x)
              	case x:Long	   => JInt(x)	
              	case x:Double  => JDouble(x)
              	case x:String  => JString(x)              
              	case x:java.util.Date  => JString(sdf.format(x))             
              }
              )
              })}.toList
              val hack = JsRaw("""function _hackJsonFunctions(json) {
               //https://bitbucket.org/cleonello/jqplot/issue/139/allows-basic-json-to-work-with-jqplot
	            for (var k in json) {
	            if (typeof json[k] == 'function') {
	                continue;
	            }
	            if (typeof json[k] == 'object') {
	                json[k] = _hackJsonFunctions(json[k]);
	            }
	            if (k == 'renderer') {
	                json[k] = eval(json[k]);
	            }
	            }
	        	return json;
            }""")
              
            val onLoadJs = {
  
                val series = "series_%s".format(id)
                val opt = "options_%s".format(id)
                
                val y = new JsCrVar(series,JArray(jsonSeries))
                val z = Run("$.jqplot('%s',%s, _hackJsonFunctions(%s));".format(id,series,opt))
         
                options match {
                  case Some(o) =>  OnLoad( new JsCrVar(opt,o.toJson)&  hack & y & z )
                  case otherwise => OnLoad(new JsCrVar(opt,"") &  hack & y & z  ) 
                }
              }

             val ie =  Comment("""[if lt IE 9]><script language="javascript" type="text/javascript" src="%s/%s/js/excanvas%s"></script><![endif]""".format(S.contextPath,LiftRules.resourceServerPath,version)) 
        <span>
          <head_merge>
            {ie}
            <script type="text/javascript" src={S.contextPath + "/" + LiftRules.resourceServerPath + "/js/jquery.jqplot" + version}></script>
	       { plugins } 	
            <link rel="stylesheet" type="text/css" href={S.contextPath + "/" + LiftRules.resourceServerPath + "/css/jquery.jqplot.css"} />
            { Script(onLoadJs) }
          </head_merge>
          <div id={ id } style={ style }></div>
        </span>
        }
      }
      
      
      case class Options(title:Option[Title] = None,
    		  			 axisDefault:Option[Axis] = None,
    		  			 axes:Option[Axes] = None,
    		  			 seriesDefault:Option[Series] = None,
    		  			 series:Option[MultipleSeries] = None,
                         legend:Option[Legend] = None,
                         grid:Option[Grid] = None) {
        
        def title(t:String):Options = this.copy(title = Some(Title(t)))
        
        def legend(l:Legend):Options = this.copy(legend = Some(l))
        
        def grid(g:Grid):Options = this.copy(grid = Some(g))
        
        def axisDefault(a:Axis):Options = this.copy(axisDefault = Some(a))
        
        def axes(a:Axes):Options = this.copy(axes = Some(a))
        
        def seriesDefault(s:Series):Options = this.copy(seriesDefault = Some(s))
        
        def series(s:List[Series]):Options = this.copy(series = Some(MultipleSeries(Some(s))))        
        
        private def fields = List(title,axisDefault,axes,legend,grid,seriesDefault,series)
        
        private val renderers:List[Option[Renderable]] = List(axisDefault,axes,seriesDefault,series,grid)  
        
        def plugins = (for { b <- renderers; render <- b } yield render.renderers).flatten.distinct
        
        def toJson = JObject(for { b <- fields; t <- b } yield t.toJson) 
        
      }
 
      trait JSONable extends Loggable { 
        
        def toJson:JField  
        
        protected def toJValue(x:Any):JValue = x match {
          case s:String       => JString(s)
          case i:Int          => JInt(i)
          case l:Long         => JInt(l)          
          case d:Double       => JDouble(d)
          case l:Location     => JString(l.toString())
          case b:Boolean      => JBool(b)
          case r:Renderer     => JString(r.toString())
          case m:MarkerOption => m.toJObject
          case m:MarkerStyle  => JString(m.toString())
          case a:AxisName     => JString(a.toString())
          case t:TickOptions  => t.toJObject
          case j:JSONable 	  => j.toJson
          case otherwise      => logger.error("We didn't cater for %s, sorry.".format(otherwise))
            JNull
          
        } 
        
      }
      
      trait Renderable {
        
        val possible_renderers:List[Option[Any]]
        
        def renderers:List[Renderer] = {
          val r = for { b <- possible_renderers; render <- b} yield {  
             if (render.isInstanceOf[Renderable]){ render.asInstanceOf[Renderable].renderers}
             else if (render.isInstanceOf[Renderer]) {  List(render.asInstanceOf[Renderer]):List[Renderer] }
             else if (render.isInstanceOf[List[Any]]) {(for{ i <- render.asInstanceOf[List[Any]] if i.isInstanceOf[Renderable]} yield { i.asInstanceOf[Renderable].renderers}).flatten }
            else Nil:List[Renderer]
          }
          r.flatten
        } 
      }
      
      case class Title(name:String) extends JSONable  { override def toJson = JField("title",JString(name)) }

      case class Axes(xaxis:Option[Axis] = None,yaxis:Option[Axis] = None,x2axis:Option[Axis] = None,y2axis:Option[Axis] = None)  extends JSONable with Renderable{
        
        def xaxis(x:Axis):Axes =  this.copy(xaxis = Some(x))
        def x2axis(x:Axis):Axes =  this.copy(x2axis = Some(x))

        def yaxis(y:Axis):Axes =  this.copy(yaxis = Some(y))
        def y2axis(y:Axis):Axes =  this.copy(y2axis = Some(y))        
        
        
        private def fields:List[(String,Option[Axis])] = List(("xaxis",xaxis),
        													 ("x2axis",x2axis),
        													 ("yaxis",yaxis),
        													 ("y2axis",y2axis))
        
        override def toJson = {JField("axes",JObject(for { b <- fields; t <- b._2 } yield JField(b._1,t.toJObject)))}        
        
        override val possible_renderers:List[Option[Any]] = List(xaxis,yaxis,x2axis,y2axis)
        
      }
      
      sealed trait Renderer { 
    	
        def name  =  {
          this.getClass.getSimpleName match {
            case n @ "BezierCurveRenderer" => n
            case n @ "OHLCRenderer" => "ohlcRenderer"
            case n => n.replaceFirst(n.take(1),n.take(1).toLowerCase)
          }
          
        }
    	override def toString = "$.jqplot.%s".format(this.getClass.getSimpleName) 
    	  
      }
      
      case class DateAxisRenderer() extends Renderer   
      case class LinearAxisRenderer() extends Renderer 
      case class PieRenderer() extends Renderer
      case class BarRenderer() extends Renderer
      case class CategoryAxisRenderer() extends Renderer
      case class OHLCRenderer() extends Renderer
      case class BubbleRenderer() extends Renderer
      case class RenderOptions() extends Renderer
      
      case class TickOptions(formatString:Option[String] = None) extends JSONable {
        
        def formatString(s:String):TickOptions = this.copy(formatString = Some(s))
        
        private def fields:List[(String,Option[Any])] = List(("formatString", formatString))
        													 
        def toJObject = JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t)))
        
        def toJson = JField("tickOptions",JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t))))
        
      }
      
      sealed trait AxisName  { override def toString = this.getClass.getSimpleName }
      sealed trait XAxisName extends AxisName
      sealed trait YAxisName extends AxisName 
      case class xaxis() extends AxisName with XAxisName
      case class x2axis() extends AxisName with XAxisName
      case class yaxis() extends AxisName with YAxisName
      case class y2axis() extends AxisName with YAxisName
      
      
      //TODO: Add ticks
      case class Axis(label:Option[String] = None,
    		  		  min:Option[String] = None,
    		  		  max:Option[String] = None,
                      pad:Option[String] = None,
                      ticks:Option[List[Any]] = None,
                      numberOfTicks:Option[Int] = None,
                      renderer:Option[Renderer] = None,
                      rendererOptions:Option[RenderOptions] = None,
                      tickOptions:Option[TickOptions] = None,
                      showTicks:Option[Boolean] = None,
                      showTickMarks:Option[Boolean] = None) extends JSONable with Renderable {

        def label(l:String):Axis = this.copy(label = Some(l))
        
        def min(m:String):Axis = this.copy(min = Some(m))
        
        def max(m:String):Axis = this.copy(max = Some(m))
        
        def pad(p:String) = this.copy(pad = Some(p))

        def ticks(l:List[Any]):Axis =  this.copy(ticks = Option(l))
        
        def numberOfTicks(i:Int):Axis = this.copy(numberOfTicks = Some(i))
                
        def renderer(r:Renderer):Axis = this.copy(renderer = Some(r))
        
        def rendererOptions(r:RenderOptions):Axis = this.copy(rendererOptions = Some(r))
        
        def tickOptions(t:TickOptions):Axis = this.copy(tickOptions = Some(t))
        
        def showTicks(b:Boolean):Axis = this.copy(showTicks = Some(b))
        
        def showTickMarks(b:Boolean):Axis = this.copy(showTickMarks = Some(b))
        
        private def fields:List[(String,Option[Any])] = List(("label",label),
        													 ("min",min),
        													 ("max",max),
        													 ("pad",pad),
        													 ("ticks",ticks),
        													 ("numberTicks",numberOfTicks),
        													 ("renderer",renderer),
        													 ("rendererOptions",rendererOptions),
        													 ("tickOptions",tickOptions),
        													 ("showTicks",showTicks),
        													 ("showTickMarks",showTickMarks))
        													 
        													 
        
        def toJObject = {JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t)))}
        
        def toJson = {JField("axesDefaults",JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t))))}        													 
        													 
        override val possible_renderers = List(renderer)  
          
      }
      
       	
      sealed trait MarkerStyle { override def toString = this.getClass.getSimpleName.toLowerCase() }
      case class circle() extends MarkerStyle
      case class diamond() extends MarkerStyle
      case class square() extends MarkerStyle
      case class filledCircle() extends MarkerStyle
      case class filledDiamond() extends MarkerStyle
      case class filledSquare() extends MarkerStyle
      case class custom(marker:String) extends MarkerStyle { override def toString = marker}
      
      case class MarkerOption(show:Option[Boolean] = None,
           					  style:Option[MarkerStyle] = None,
          					  lineWidth:Option[Int] = None,
          					  size:Option[Int] = None,
          					  color:Option[String] = None,
          					  showShadow:Option[Boolean] = None,
          					  shadowAngle:Option[Int] = None,
          					  shadowOffset:Option[Double] = None,
          					  shadowDepth:Option[Int] = None,
          					  shadowAlpha:Option[Double] = None)  extends JSONable {

        def show(b:Boolean):MarkerOption = this.copy(show = Some(b))
        def style(ms:MarkerStyle):MarkerOption = this.copy(style = Some(ms))
        def lineWidth(w:Int):MarkerOption = this.copy(lineWidth = Some(w))
        def size(s:Int):MarkerOption = this.copy(size = Some(s))
        def color(c:String):MarkerOption = this.copy(color = Some(c))
        
        def showShadow(b:Boolean):MarkerOption = this.copy(showShadow =  Some(b))
        def shadowAngle(i:Int):MarkerOption = this.copy(shadowAngle =  Some(i))
        def shadowOffset(d:Double):MarkerOption = this.copy(shadowOffset =  Some(d))
        def shadowDepth(i:Int):MarkerOption = this.copy(shadowDepth =  Some(i))
        def shadowAlpha(d:Double):MarkerOption = this.copy(shadowAlpha =  Some(d))

        
        
       private def fields:List[(String,Option[Any])] = List(("show",show),
    		   											("style",style),
    		   											("lineWidth",lineWidth),
    		   											("size",size),
    		   											("color",color),
    		   											("shadow",showShadow),
    		   											("shadowAngle",shadowAngle),
    		   											("shadowOffset",shadowOffset),
    		   											("shadowDepth",shadowDepth),
    		   											("shadowAlpha",shadowAlpha))
        
        def toJObject:JObject = { JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t)))}
        
        
        override def toJson = { JField("markerOptions",JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t))))}

        
      }
      
      case class MultipleSeries(ss:Option[List[Series]] = None) extends JSONable with Renderable {
        
        def series(l:List[Series]):MultipleSeries = this.copy(ss =  Some(l))

        override val possible_renderers = List(ss)  
        
        override  def toJson = ss match {
          case Some(l) => JField("series",JArray(for {s <- l } yield s.toJObject	)) 
          case None => JField("series",JArray(List()))
        } 
        
      }
      
      case class Series(xaxis:Option[XAxisName] = None,
    		  		    yaxis:Option[YAxisName] = None,
    		  		    label:Option[String] = None,
    		  			lineWidth:Option[Int] = None,
    		  			displayLine:Option[Boolean] = None,
    		  			showShadow:Option[Boolean] = None,
                     	shadowAngle:Option[Int] = None,
                     	shadowOffset:Option[Double] = None,
                     	shadowWidth:Option[Int] = None,
                     	shadowDepth:Option[Int] = None,
                     	shadowAlpha:Option[Double] = None,
                     	fill:Option[Boolean] = None,
                     	fillAndStroke:Option[Boolean] = None,
                     	fillColor:Option[String] = None,
                     	fillAlpha:Option[Double] = None,                     	
    		  			renderer:Option[Renderer] = None,
    		  			rendererOptions:Option[RenderOptions] = None,
    		  			markerOptions:Option[MarkerOption] = None,
    		  			color:Option[String] = None) extends JSONable with Renderable{

        def xaxis(axis:XAxisName):Series =  this.copy(xaxis = Option(axis)) 
        def yaxis(axis:YAxisName):Series =  this.copy(yaxis = Option(axis))
        def lineWidth(w:Int):Series = this.copy(lineWidth = Some(w))
        def showLine:Series = this.copy(displayLine = Some(true))
        def hideLine:Series = this.copy(displayLine = Some(false))
        def showShadow(b:Boolean):Series = this.copy(showShadow =  Some(b))
        def shadowAngle(i:Int):Series = this.copy(shadowAngle =  Some(i))
        def shadowOffset(d:Double):Series = this.copy(shadowOffset =  Some(d))
        def shadowWidth(i:Int):Series = this.copy(shadowWidth =  Some(i))
        def shadowDepth(i:Int):Series = this.copy(shadowDepth =  Some(i))
        def shadowAlpha(d:Double):Series = this.copy(shadowAlpha =  Some(d))
        def fill(b:Boolean):Series = this.copy(fill = Some(b))
        def fillAndStroke(b:Boolean):Series = this.copy(fillAndStroke = Some(b))
        def fillColor(c:String):Series = this.copy(fillColor = Some(c))
        def fillAlpha(d:Double):Series = this.copy(fillAlpha = Some(d))
        def renderer(r:Renderer):Series = this.copy(renderer = Some(r))
        def rendererOptions(r:RenderOptions):Series = this.copy(rendererOptions = Some(r))
        def markerOptions(m:MarkerOption):Series = this.copy(markerOptions = Some(m))
        def color(c:String):Series = this.copy(color = Some(c))
        
        def fields:List[(String,Option[Any])] = List(("xaxis",xaxis),("yaxis",yaxis),
        											 ("lineWidth",lineWidth),
        											 ("showLine",displayLine),
        											 ("showShadow",showShadow),
        											 ("shadowAngle",shadowAngle),
        										     ("shadowOffset",shadowOffset),
        										     ("shadowWidth",shadowWidth),
        										     ("shadowDepth",shadowDepth),
        										     ("shadowAlpha",shadowAlpha),
        										     ("fill",fill),
        										     ("fillAndStroke",fillAndStroke),
        										     ("fillColor",fillColor),
        										     ("fillAlpha",fillAlpha),
        										     ("renderer",renderer),
        										     ("rendererOptions",rendererOptions),
        										     ("markerOptions",markerOptions),
        										     ("color",color))
        										        
        										 
        def toJObject = { JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t))) }
        
        override def toJson = { JField("seriesDefaults",JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t)))) }
        
        override val possible_renderers = List(markerOptions,renderer)
      
      }  
      
      sealed trait Location 
      case class NW() extends Location { override def toString = this.getClass.getSimpleName.toLowerCase() } 
      case class NO() extends Location { override def toString = "n" }
      case class NE() extends Location { override def toString = this.getClass.getSimpleName.toLowerCase() }
      case class EA() extends Location { override def toString = "e" }
      case class SE() extends Location { override def toString = this.getClass.getSimpleName.toLowerCase() }
      case class SO() extends Location { override def toString = "s" }
      case class SW() extends Location { override def toString = this.getClass.getSimpleName.toLowerCase() }
      case class WE() extends Location { override def toString = "w" }

      //Missing show. Assumption, if you don't want to show the legend, don't include it. 
      case class Legend(display:Option[Boolean] = None,location:Option[Location] = None,xoffset:Option[Int] = None,yoffset:Option[Int] = None)  extends JSONable{

        def show:Legend = this.copy(display = Some(true))
        def hide:Legend = this.copy(display = Some(false))
        def location(l:Location):Legend = this.copy(location = Some(l))
        def xoffset(x:Int):Legend = this.copy(xoffset = Some(x))
        def yoffset(y:Int):Legend = this.copy(yoffset = Some(y))
        
        private def fields:List[(String,Option[Any])] = List(("show",display),("location",location),("xoffset",xoffset),("yoffset",yoffset))
       
        override def toJson = { JField("legend",JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t)))) }        
        
      }
      case class Grid(drawGridLines:Option[Boolean] = None,
    		  		 gridLineColor:Option[String] = None,
    		  		 background:Option[String] = None,
                     borderColor:Option[String] = None,
                     borderWidth:Option[Double] = None,
                     renderer:Option[Renderer] = None,
                     rendererOptions:Option[RenderOptions] = None,
                     showShadow:Option[Boolean] = None,
                     shadowAngle:Option[Int] = None,
                     shadowOffset:Option[Double] = None,
                     shadowWidth:Option[Int] = None,
                     shadowDepth:Option[Int] = None,
                     shadowAlpha:Option[Double] = None) extends JSONable with Renderable {

        def drawGridLines(b:Boolean):Grid = this.copy(drawGridLines = Some(b))
        
        def gridLineColor(s:String):Grid = this.copy(gridLineColor = Some(s))
        
        def background(s:String):Grid = this.copy(background =  Some(s))
        
        def borderColor(s:String):Grid = this.copy(borderColor =  Some(s))
        
        def borderWidth(d:Double):Grid = this.copy(borderWidth =  Some(d))
        
        def renderer(r:Renderer):Grid = this.copy(renderer = Some(r))
        
        def rendererOptions(r:RenderOptions):Grid = this.copy(rendererOptions = Some(r))
        
        def showShadow(b:Boolean):Grid = this.copy(showShadow =  Some(b))
        
        def shadowAngle(i:Int):Grid = this.copy(shadowAngle =  Some(i))
        
        def shadowOffset(d:Double):Grid = this.copy(shadowOffset =  Some(d))
        
        def shadowWidth(i:Int):Grid = this.copy(shadowWidth =  Some(i))
        
        def shadowDepth(i:Int):Grid = this.copy(shadowDepth =  Some(i))
        
        def shadowAlpha(d:Double):Grid = this.copy(shadowAlpha =  Some(d))
        
        
       private val fields:List[(String,Option[Any])] = List(("drawGridLines",drawGridLines),
    		   											("gridLineColor",gridLineColor),
    		   											("background",background),
    		   											("borderWidth",borderWidth),
    		   											("shadow",showShadow),
    		   											("shadowAngle",shadowAngle),
    		   											("shadowOffset",shadowOffset),
    		   											("shadowDepth",shadowDepth),
    		   											("shadowWidth",shadowWidth),
    		   											("shadowAlpha",shadowAlpha),
    		   											("renderer",renderer),
    		   											("rendererOptions",rendererOptions))

        
        override def toJson = { JField("grid",JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t)))) }
        
        override val possible_renderers = List(renderer)
        
      }
 
  }
}