package net {
  package liftmodules {
    package jqplot {
      
    import scala.collection.mutable.Map
    import net.liftweb.common.{ Box, Empty, Failure,Full }
    import net.liftweb.http.{ LiftRules, ResourceServer }
    import net.liftweb.http.js.JE.JsRaw
    import net.liftweb.http.js.JsCmds._
    import net.liftweb.http.rest.RestHelper
    import net.liftweb.json.JsonAST.{JField,JObject,JString,JValue}
    import net.liftweb.util.Helpers
    import net.liftweb.http.S
    import net.liftweb.common.Loggable

      object JqPlot extends Loggable {
        
        def init: Unit = {
          
          logger.info("JqPlot.init")

          ResourceServer.allow({
            case "js" :: "jquery.js" :: Nil => true   //This should be supplied.
            case "js" :: "jquery.jqplot.js" :: Nil => true
            case "js" :: "excanvas.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.barRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.BezierCurveRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.blockRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.bubbleRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.canvasAxisLabelRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.canvasAxisTickRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.canvasOverlay.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.canvasTextRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.categoryAxisRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.ciParser.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.cursor.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.dateAxisRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.donutRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.dragable.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.enhancedLegendRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.funnelRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.highlighter.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.json2.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.logAxisRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.mekkoRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.meterGaugeRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.ohlcRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.pieRenderer.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.pointLabel.js" :: Nil => true
            case "js" :: "plugins" :: "jqplot.trendline.js" :: Nil => true
            case "css" :: "jquery.jqplot.css" :: Nil => true
          })

        }
        
      }
      
      class JqPlot(series:String, options:String) {
                
        val id = Helpers.nextFuncName        
        
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
            <script type="text/javascript" src={S.contextPath + "/" + LiftRules.resourceServerPath + "/js/jquery.jqplot.js"}></script>
            <script type="text/javascript" src={S.contextPath + "/" + LiftRules.resourceServerPath + "/js/plugins/jqplot.dateAxisRenderer.js"}></script>
            <script type="text/javascript" src={S.contextPath + "/" + LiftRules.resourceServerPath + "/js/plugins/jqplot.canvasTextRenderer.js"}></script>
            <script type="text/javascript" src={S.contextPath + "/" + LiftRules.resourceServerPath + "/js/plugins/jqplot.canvasAxisLabelRenderer.js"}></script>
            <script type="text/javascript" src={S.contextPath + "/" + LiftRules.resourceServerPath + "/js/plugins/jqplot.trendline.js"}></script>
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
