package net {
  package liftmodules {
  
  import java.text.SimpleDateFormat    
  
  import scala.collection.mutable.Map
  import scala.xml.NodeSeq
  
  import liftweb.common.{Loggable }
  import liftweb.http.{ LiftRules, S,ResourceServer }
  import liftweb.http.js.JE.{JsRaw,JsVar}
  import liftweb.http.js.JsCmds._
  import net.liftweb.json.JsonAST._
  import liftweb.util.{Helpers,Props}
  import liftweb.util.Props.RunModes._

      object JqPlot extends Loggable {
        
        def init: Unit = {
          
          logger.info("JqPlot.init")

          val coreLibraries = List("jquery.js","jquery.jqplot.js","excanvas.js","jquery.min.js","jquery.jqplot.min.js","excanvas.min.js")
          
          ResourceServer.allow({
            case "js" ::  lib :: Nil if coreLibraries.contains(lib) => true   //This should be supplied.
            case "js" :: "plugins" ::  plugin :: Nil => 
              logger.info("plugin %s %s %s".format(plugin,plugin.startsWith("jqplot"), plugin.endsWith(".js")))
              true
            case "css" :: "jquery.jqplot.css" :: Nil => true
          })

        }
        
      }
      
  
      class JqPlot(w:Int,h:Int,options:Option[Options],series:List[List[Any]]*) extends Loggable {
        
       val sdf =  new SimpleDateFormat("yyyy MM dd HH:mm")
        
        val id = Helpers.nextFuncName   
        
        val style = "height:%spx; width:%spx;".format(h,w)
        
        val version = Props.mode match {
            case Production => ".min.js" 
            case Pilot => ".min.js"
            case otherwise => ".js"
        }

       val plugins = options match {
         case  Some(op) =>
           		for { plugin <- op.plugins } yield <script type="text/javascript" src={"%s/%s/js/plugins/jqplot.%s%s".format(S.contextPath,LiftRules.resourceServerPath,plugin,version)}></script>
         case otherwise => Nil   
        }     
        
        logger.info("plugins %s".format(plugins))
        
        def toCssTransformer:NodeSeq => NodeSeq = { _ => toHtml}
        
        def toHtml = {
            val jsonSeries = series.map { s => JArray( s.map { v => JArray( 
              v.map { 
              	case x:Int    => JInt(x)
              	case x:Double  => JDouble(x)
              	case x:String  => JString(x)              
              	case x:java.util.Date  => JString(sdf.format(x))             
              }
              )
              })}.toList
              val hack = JsRaw("""function _hackJsonFunctions(json) {
            //https://bitbucket.org/cleonello/jqplot/issue/139/allows-basic-json-to-work-with-jqplot
	        //the json sent by the server CAN NOT contain functions because js functions are not
            //printable in the JSON standard !
            //so we need this little hack to be able to do an eval on the functions
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
  
                val y = new JsCrVar("series",JArray(jsonSeries))
                val z = Run("$.jqplot('%s',series, _hackJsonFunctions(options));".format(id))
         
                options match {
                  case Some(o) =>  OnLoad( new JsCrVar("options",o.toJson) & y & z & hack)
                  case otherwise => OnLoad(new JsCrVar("options","") &  y & z & hack) 
                }
              }
        <span>
          <head_merge>
            <!--[if lt IE 9]><script language="javascript" type="text/javascript" src="/js/excanvas.js"></script><![endif]-->
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
        
        private val pluginsList = List( "barRenderer","BezierCurveRenderer","blockRenderer",
            "bubbleRenderer","trendline","pointLabel","pieRenderer","ohlcRenderer",
            "meterGaugeRenderer","mekkoRenderer", "logAxisRenderer","json2",
            "highlighter","funnelRenderer","enhancedLegendRenderer","dragable",
            "donutRenderer","dateAxisRenderer","canvasAxisLabelRenderer",
            "canvasAxisTickRenderer","canvasOverlay","canvasTextRenderer",
            "categoryAxisRenderer","ciParser","cursor")
        
        
        def plugins:List[String] = {Nil}
        
        def fields = List(title,axes,legend,grid,seriesDefault,series)
        
        def toJson = JObject(for { b <- fields; t <- b } yield t.toJson) 
        
      }

      trait JSONable extends Loggable { 
        
        def toJson:JField  
        
        protected def toJValue(x:Any) = x match {
          case s:String => JString(s)
          case i:Int => JInt(i)
          case d:Double => JDouble(d)
          case l:Location => JString(l.toString())
          case b:Boolean => JBool(b)
          case r:Renderer => JString(r.toString())
          case m:MarkerOption => m.toJObject
          case m:MarkerStyle => JString(m.toString())
          case j:JSONable => j.toJson
          case otherwise => 
            logger.error("We didn't cater for %s, sorry.".format(otherwise))
            JNull
          
        } 
        
      }
      
      case class Title(name:String) extends JSONable  { override def toJson = JField("title",JString(name)) }

      case class Axes(xaxis:Option[Axis] = None,yaxis:Option[Axis] = None,x2axis:Option[Axis] = None,y2axis:Option[Axis] = None)  extends JSONable {
        
        private def fields:List[Option[JSONable]] = List(xaxis,yaxis,x2axis,y2axis)
        
        def xaxis(x:Axis):Axes =  this.copy(xaxis = Some(x))
        def x2axis(x:Axis):Axes =  this.copy(x2axis = Some(x))

        def yaxis(y:Axis):Axes =  this.copy(yaxis = Some(y))
        def y2axis(y:Axis):Axes =  this.copy(y2axis = Some(y))        
        
        override def toJson = { JField("axes",JObject(for { b <- fields; t <- b } yield t.toJson)) } 
        
      }
      
      sealed trait Renderer { override def toString = "$.jqplot.%s".format(this.getClass.getSimpleName) }
      case class DateAxisRenderer() extends Renderer   
      case class LinearAxisRenderer() extends Renderer 
      case class RenderOptions()  
      case class TickOptions()  
      
      sealed trait AxisName { override def toString = this.getClass.getSimpleName }
      sealed trait XAxisName;
      sealed trait YAxisName;
      case class xaxis() extends AxisName with XAxisName
      case class x2axis() extends AxisName with XAxisName
      case class yaxis() extends AxisName with YAxisName
      case class y2axis() extends AxisName with YAxisName
      
      
      //TODO: Add ticks
      case class Axis(name:AxisName,
    		  		  label:Option[String] = None,
    		  		  min:Option[String] = None,
    		  		  max:Option[String] = None,
                      pad:Option[String] = None,
                      ticks:Option[List[Any]] = None,
                      numberOfTicks:Option[Int] = None,
                      renderer:Option[Renderer] = None,
                      rendererOptions:Option[RenderOptions] = None,
                      tickOptions:Option[TickOptions] = None,
                      showTicks:Option[Boolean] = None,
                      showTickMarks:Option[Boolean] = None) extends JSONable {

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
        
        override def toJson = {JField(name.toString,JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t))))}
          
      }
      sealed trait MarkerStyle { override def toString = this.getClass.getSimpleName.toLowerCase() }
      case class circle() extends MarkerStyle
      case class diamond() extends MarkerStyle
      case class square() extends MarkerStyle
      case class filledCircle() extends MarkerStyle
      case class filledDiamond() extends MarkerStyle
      case class filledSquare() extends MarkerStyle      
      
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
      
      case class MultipleSeries(ss:Option[List[Series]] = None) extends JSONable {
        
        def series(l:List[Series]):MultipleSeries = this.copy(ss =  Some(l))
        
        override  def toJson = ss match {
          case Some(l) => JField("series",JArray(for {s <- l } yield s.toJObject	)) 
          case None => JField("series",JArray(List()))
        } 
        
      }
      
      case class Series(xaxis:Option[XAxisName] = None,
    		  		    yaxis:Option[YAxisName] = None,
    		  		    label:Option[String] = None,
    		  			lineWidth:Option[Int] = None,
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
    		  			markerOptions:Option[MarkerOption] = None    		  			
    		  			) extends JSONable {

        def xaxis(axis:XAxisName):Series =  this.copy(xaxis = Option(axis)) 
        def yaxis(axis:YAxisName):Series =  this.copy(yaxis = Option(axis))
        def lineWidth(w:Int):Series = this.copy(lineWidth = Some(w))
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
        
        def fields:List[(String,Option[Any])] = List(("xaxis",xaxis),("yaxis",yaxis),
        											 ("lineWidth",lineWidth),
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
        										     ("markerOptions",markerOptions))
        										        
        										 
        def toJObject = { JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t))) }
        
        override def toJson = { JField("seriesDefault",JObject(for { b <- fields; t <- b._2 } yield JField(b._1,toJValue(t)))) }
      
      }  
      
      sealed trait Location { override def toString = this.getClass.getSimpleName.toLowerCase() }
      case class NW() extends Location 
      case class NO() extends Location 
      case class NE() extends Location 
      case class EA() extends Location 
      case class SE() extends Location 
      case class SO() extends Location 
      case class SW() extends Location 
      case class WE() extends Location 

      //Missing show. Assumption, if you don't want to show the legend, don't include it. 
      case class Legend(location:Option[Location] = None,xoffset:Option[Int] = None,yoffset:Option[Int] = None)  extends JSONable{

        def location(l:Location):Legend = this.copy(location = Some(l))
        def xoffset(x:Int):Legend = this.copy(xoffset = Some(x))
        def yoffset(y:Int):Legend = this.copy(yoffset = Some(y))
        
        private def fields:List[(String,Option[Any])] = List(("location",location),("xoffset",xoffset),("yoffset",yoffset))
       
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
                     shadowAlpha:Option[Double] = None) extends JSONable{

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
        
      }
 
  }
}