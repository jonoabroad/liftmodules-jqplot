package net {
  package liftmodules {
  
  import java.text.SimpleDateFormat    
  
  import scala.collection.mutable.Map
  import scala.xml.NodeSeq
  
  import liftweb.common.{ Box, Empty, Failure,Full,Loggable }
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
      
  
      class JqPlot(w:Int,h:Int,options:Box[Options],series:List[List[Any]]*) extends Loggable {
        
       val sdf =  new SimpleDateFormat("yyyy MM dd HH:mm")
        
        val id = Helpers.nextFuncName   
        
        val style = "height:%spx; width:%spx;".format(h,w)
        
        val version = Props.mode match {
            case Production => ".min.js" 
            case Pilot => ".min.js"
            case otherwise => ".js"
        }

       val plugins = options match {
         case  Full(op) =>
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
              
            val onLoadJs = {
  
                val y = new JsCrVar("series",JArray(jsonSeries))
                val z = Run("$.jqplot('%s',series,options);".format(id))
         
                options match {
                  case Full(o) =>  OnLoad( new JsCrVar("options",o.toJson) & y & z )
                  case otherwise => OnLoad(new JsCrVar("options","") &  y & z ) 
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
      
      
      case class Options(title:Box[Title] = Empty,axes:Box[Axes] = Empty,series:Box[List[Series]] = Empty,legend:Box[Legend] = Empty,grid:Box[Grid] = Empty) {
        
                
        private val pluginsList = List( "barRenderer","BezierCurveRenderer","blockRenderer",
            "bubbleRenderer","trendline","pointLabel","pieRenderer","ohlcRenderer",
            "meterGaugeRenderer","mekkoRenderer", "logAxisRenderer","json2",
            "highlighter","funnelRenderer","enhancedLegendRenderer","dragable",
            "donutRenderer","dateAxisRenderer","canvasAxisLabelRenderer",
            "canvasAxisTickRenderer","canvasOverlay","canvasTextRenderer",
            "categoryAxisRenderer","ciParser","cursor")
        
        
        def plugins:List[String] = {Nil}
        
        def fields = List(title,axes,legend,grid)
        
        def toJson = { JObject(for { b <- fields; t <- b } yield t.toJson) } 
       
        def title(t:String):Options = this.copy(title = Full(Title(t)))
        
        def legend(l:Legend):Options = this.copy(legend = Full(l))
        
        def grid(g:Grid):Options = this.copy(grid = Full(g))
        
      }

      trait JSONable extends Loggable { 
      
        def fields:List[Box[JSONable]] = Nil
        
        def toJson:JField  
        
        protected def toJValue(x:Any) = x match {
          case s:String => JString(s)
          case i:Int => JInt(i)
          case d:Double => JDouble(d)
          case l:Location => JString(l.toString())
          case otherwise => 
            logger.error("We didn't cater for %s, sorry.".format(otherwise))
            JNull
          
        } 
        
      }
      
      case class Title(name:String) extends JSONable  { override def toJson = JField("title",JString(name)) }
      case class Axes(xaxis:Box[Axis] = Empty,yaxis:Box[Axis] = Empty,x2axis:Box[Axis] = Empty,y2axis:Box[Axis] = Empty)  extends JSONable {
        
        override def fields:List[Box[JSONable]] = List(xaxis,yaxis,x2axis,y2axis)
        
        def xaxis(x:Axis):Axes =  this.copy(xaxis = Full(x))
        def x2axis(x:Axis):Axes =  this.copy(x2axis = Full(x))

        def yaxis(y:Axis):Axes =  this.copy(yaxis = Full(y))
        def y2axis(y:Axis):Axes =  this.copy(y2axis = Full(y))        
        
        def toJson = {JField("axes",JObject(for { b <- fields; t <- b } yield t.toJson))} 
      }
      sealed trait Renderer
      case class LinearAxisRenderer extends Renderer { override def toString = "$.jqplot.LinearAxisRenderer" }
      case class RenderOptions()  
      case class TickOptions()  
      case class Axis(min:Box[String] = Empty,max:Box[String] = Empty,pad:Box[String] = Empty,ticks:List[String] = Nil,numberOfTicks:Box[Int],renderer:Renderer,rendererOptions:Box[RenderOptions] = Empty,
          tickOptions:Box[TickOptions] = Empty,showTicks:Boolean = true,showTickMarks:Boolean = true) extends JSONable {

        def min(m:String):Axis = this.copy(min = Full(m))
        
        def max(m:String):Axis = this.copy(max = Full(m))
        
        def pad(p:String) = this.copy(pad = Full(p))
        
        def tick(t:String):Axis =  {
          val l:List[String] = this.ticks
          this.copy(ticks = t :: l)
        }

        def ticks(l:List[String]):Axis =  this.copy(ticks = l)
        
        def numberOfTicks(i:Int):Axis = this.copy(numberOfTicks = Full(i))
                
        override def fields:List[Box[JSONable]] = Nil//List(min,max,pad,ticks,numberTicks,renderer,renderOptions,tickOptions,showTicks,showTickMarks)
        
        override def toJson = {JField("axes",JObject(for { b <- fields; t <- b } yield t.toJson))}
          
      }
      case class MarkerOption()  extends JSONable {

        override def fields:List[Box[JSONable]] = Nil
        
        override def toJson = {JField("axes",JObject(for { b <- fields; t <- b } yield t.toJson))}        
        
      }
      case class Series(lineWidth:Box[Int] = Empty,markerOptions:Box[MarkerOption] = Empty) extends JSONable {

        def lineWidth(w:Int):Series = this.copy(lineWidth = Full(w))
        
        def markerOptions(m:MarkerOption):Series = this.copy(markerOptions = Full(m))
        
        override def fields:List[Box[JSONable]] = Nil
        
        override def toJson = { JField("axes",JObject(for { b <- fields; t <- b } yield t.toJson)) }        
        
      }  
      
      sealed trait Location
      case class NW() extends Location { override def toString = "nw" }
      case class NO() extends Location  { override def toString = "n" }
      case class NE() extends Location { override def toString = "ne" }
      case class EA() extends Location  { override def toString = "e" }
      case class SE() extends Location { override def toString = "se" }
      case class SO() extends Location  { override def toString = "s" }
      case class SW() extends Location { override def toString = "sw" }
      case class WE() extends Location  { override def toString = "w" }

      //Missing show. Assumption, if you don't want to show the legend, don't include it. 
      case class Legend(location:Box[Location] = Empty,xoffset:Box[Int] = Empty,yoffset:Box[Int] = Empty)  extends JSONable{

        def location(l:Location):Legend = this.copy(location = Full(l))
        def xoffset(x:Int):Legend = this.copy(xoffset = Full(x))
        def yoffset(y:Int):Legend = this.copy(yoffset = Full(y))
        
        private def fieldz:List[(String,Box[Any])] = List(("location",location),("xoffset",xoffset),("yoffset",yoffset))
       
        override def toJson = { JField("legend",JObject(for { b <- fieldz; t <- b._2 } yield JField(b._1,toJValue(t)))) }        
        
      }
      case class Grid(drawGridLines:Box[Boolean] = Empty,gridLineColor:Box[String] = Empty,background:Box[String] = Empty,
                      borderColor:Box[String] = Empty,borderWidth:Box[Double] = Empty,shadow:Box[Boolean] = Empty,
                      shadowAngle:Box[Int] = Empty,shadowOffset:Box[Double] = Empty,shadowWidth:Box[Int] = Empty,
                      shadowDepth:Box[Int] = Empty,shadowAlpha:Box[Double] = Empty,renderer:Box[Renderer] = Empty,
                      rendererOptions:Box[RenderOptions] = Empty)  extends JSONable{

        def drawGridLines(b:Boolean):Grid = this.copy(drawGridLines = Full(b))
        
        def gridLineColor(s:String):Grid = this.copy(gridLineColor = Full(s))
        
        def background(s:String):Grid = this.copy(background =  Full(s))
        
        def borderColor(s:String):Grid = this.copy(borderColor =  Full(s))
        
        def borderWidth(d:Double):Grid = this.copy(borderWidth =  Full(d))

        def shadow(b:Boolean):Grid = this.copy(shadow =  Full(b))
        
        def shadowAngle(i:Int):Grid = this.copy(shadowAngle =  Full(i))
        
        def shadowOffset(d:Double):Grid = this.copy(shadowOffset =  Full(d))
        
        def shadowWidth(i:Int):Grid = this.copy(shadowWidth =  Full(i))
        
        def shadowDepth(i:Int):Grid = this.copy(shadowDepth =  Full(i))
        
        def shadowAlpha(d:Double):Grid = this.copy(shadowAlpha =  Full(d))
        
        def renderer(r:Renderer):Grid = this.copy(renderer = Full(r))
        
        def rendererOptions(r:RenderOptions):Grid = this.copy(rendererOptions = Full(r))
        
        
        
        
        override def fields:List[Box[JSONable]] = Nil
        
        override def toJson = { JField("axes",JObject(for { b <- fields; t <- b } yield t.toJson)) }        
        
      }
      
 
  }
}