package net {
  package liftmodules {
    package jqplot {
      
    import scala.collection.mutable.Map
    import net.liftweb.common.{ Box, Empty, Failure,Full }
    import net.liftweb.http.{ LiftRules, S,ResourceServer }
    import net.liftweb.http.js.JE.JsRaw
    import net.liftweb.http.js.JsCmds._
    import net.liftweb.util.{Helpers,Props}
    import net.liftweb.util.Props.RunModes._
    import net.liftweb.common.Loggable


      object JqPlot extends Loggable {
        
        def init: Unit = {
          
          logger.info("JqPlot.init")

          val coreLibraries = List("jquery.js","jquery.jqplot.js","excanvas.js","jquery.min.js","jquery.jqplot.min.js","excanvas.min.js")
          
          ResourceServer.allow({
            case "js" ::  lib :: Nil if coreLibraries.contains(lib) => true   //This should be supplied.
            case "js" :: "plugins" ::  plugin :: Nil  if plugin.startsWith("jqplot")  && plugin.endsWith(".js") => true
            case "css" :: "jquery.jqplot.css" :: Nil => true
          })

        }
        
      }
      
      class JqPlot(series:String, options:String) {
                
        val id = Helpers.nextFuncName   
        
        val version = Props.mode match {
            case Production => ".js" 
            case Pilot => ".js"
            case otherwise => ".min.js"
          }
        
        def toHtml = {
	        val onLoad = JsRaw(
"""$(document).ready(function(){
  var options = %s;
  var series  = %s; 

  $.jqplot('%s',series,options);
  
});""".format(options,series,id))
 
        <span>
          <head>
            <!--[if lt IE 9]><script language="javascript" type="text/javascript" src="/js/excanvas.js"></script><![endif]-->
            <script type="text/javascript" src={S.contextPath + "/" + LiftRules.resourceServerPath + "/js/jquery.jqplot" + version}></script>
            <link rel="stylesheet" type="text/css" href={S.contextPath + "/" + LiftRules.resourceServerPath + "/css/jquery.jqplot.css"} />
            { Script(onLoad) }
          </head>
          <div id={ id } style="height:384px; width:512px;"></div>
        </span>
          
        }
       
      } 

    }
  }
}
//			possible plugins 
//          "jqplot.barRenderer.js"
//          "jqplot.BezierCurveRenderer.js"
//          "jqplot.blockRenderer.js"
//          "jqplot.bubbleRenderer.js"
//          "jqplot.trendline.js" 
//          "jqplot.pointLabel.js"
//          "jqplot.pieRenderer.js"
//          "jqplot.ohlcRenderer.js"
//          "jqplot.meterGaugeRenderer.js"
//          "jqplot.mekkoRenderer.js" 
//          "jqplot.logAxisRenderer.js"
//          "jqplot.json2.js"
//          "jqplot.highlighter.js"
//          "jqplot.funnelRenderer.js"
//          "jqplot.enhancedLegendRenderer.js"
//          "jqplot.dragable.js"
//          "jqplot.donutRenderer.js" 
//          "jqplot.dateAxisRenderer.js"
//          "jqplot.canvasAxisLabelRenderer.js"
//          "jqplot.canvasAxisTickRenderer.js"
//          "jqplot.canvasOverlay.js"
//          "jqplot.canvasTextRenderer.js"
//          "jqplot.categoryAxisRenderer.js"
//          "jqplot.ciParser.js"
//          "jqplot.cursor.js"
//    
