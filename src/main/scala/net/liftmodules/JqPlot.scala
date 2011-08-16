package net {
  package liftmodules {
      
    import scala.collection.mutable.Map
  import liftweb.common.{ Box, Empty, Failure,Full }
  import liftweb.http.{ LiftRules, S,ResourceServer }
  import liftweb.http.js.JE.JsRaw
  import liftweb.http.js.JsCmds._
  import liftweb.util.{Helpers,Props}
  import liftweb.util.Props.RunModes._
  import liftweb.common.Loggable
  import scala.xml.NodeSeq


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
      
      class JqPlot(w:Int,h:Int,series:String,options:String) extends Loggable {
        
        
//        def this(series:JValue,options:Value) = {
//          
//          
//        }
//        
//        def this(series:String)
//                
        val id = Helpers.nextFuncName   
        
        val style = "height:%spx; width:%spx;".format(h,w)
        
        val version = Props.mode match {
            case Production => ".min.js" 
            case Pilot => ".min.js"
            case otherwise => ".js"
        }
        
        val pluginsList = List( "barRenderer","BezierCurveRenderer","blockRenderer",
            "bubbleRenderer","trendline","pointLabel","pieRenderer","ohlcRenderer",
            "meterGaugeRenderer","mekkoRenderer", "logAxisRenderer","json2",
            "highlighter","funnelRenderer","enhancedLegendRenderer","dragable",
            "donutRenderer","dateAxisRenderer","canvasAxisLabelRenderer",
            "canvasAxisTickRenderer","canvasOverlay","canvasTextRenderer",
            "categoryAxisRenderer","ciParser","cursor")

       val plugins = {
          val lowerCaseOptions = options.toLowerCase()
          for { 
            plugin <- pluginsList
            if lowerCaseOptions.indexOf("jqplot." + plugin.toLowerCase()) > -1
          } yield <script type="text/javascript" src={"%s/%s/js/plugins/jqplot.%s%s".format(S.contextPath,LiftRules.resourceServerPath,plugin,version)}></script>
        }     
        
        logger.info("plugins %s".format(plugins))
        
        def toCssTransformer:NodeSeq => NodeSeq = { _ => toHtml}
        
        def toHtml = {
	        val onLoad = JsRaw(
"""$(document).ready(function(){
  var options = %s;
  var series  = %s; 

  $.jqplot('%s',series,options);
  
});""".format(options,series,id))
 
        <span>
          <head_merge>
            <!--[if lt IE 9]><script language="javascript" type="text/javascript" src="/js/excanvas.js"></script><![endif]-->
            <script type="text/javascript" src={S.contextPath + "/" + LiftRules.resourceServerPath + "/js/jquery.jqplot" + version}></script>
	       { plugins } 	
            <link rel="stylesheet" type="text/css" href={S.contextPath + "/" + LiftRules.resourceServerPath + "/css/jquery.jqplot.css"} />
            { Script(onLoad) }
          </head_merge>
          <div id={ id } style={ style }></div>
        </span>
          
        }
       
      } 

  }
}