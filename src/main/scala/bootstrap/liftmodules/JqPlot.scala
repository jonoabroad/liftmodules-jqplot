package net {
  package liftmodules {
    package jqplot {
      
    import scala.collection.mutable.Map
    
    
    import net.liftweb.common.{ Box, Empty, Failure,Full }
    import net.liftweb.http.{ LiftRules, ResourceServer }
    import net.liftweb.http.js.JE.JsRaw
    import net.liftweb.http.js.JsCmds._
    import net.liftweb.http.rest.RestHelper
    import net.liftweb.json.JsonAST.JValue
    import net.liftweb.util.Helpers

      object JqPlot extends RestHelper {

        val m = Map[String,JqPlot]();
        
        def init: Unit = {

          ResourceServer.allow({
            case "js" :: "jquery.js" :: Nil => true
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
            case "js" :: "plugins" :: "jqplot.trenline.js" :: Nil => true
            case "css" :: "jquery.jqplot.css" :: Nil => true
          })

          serve {
            case JsonGet("jqplot" :: id :: Nil, _) => m.get(id) match {
              case Some(plot) => JValue("yay")
              case otherwise => Failure("Not yet implemented", Empty, Empty)
             }
          }
          
          LiftRules.dispatch.append(JqPlot)

        }

        def data(id:String,plot:JqPlot) = {m.put(id,plot)}
          
        
      }
      
      
      class JqPlot(data: () => Box[String] , options: () => Box[String]) {

        val id = Helpers.nextFuncName

        val onLoad = JsRaw(
          """$(document).ready(function(){
  var json_url = "/plot/%s"
  
  var payload = (function () {
        var ajaxResponse = '';
        $.ajax({url:json_url, async:false, type:"post", success: function (data) {ajaxResponse = data;}, dataType:"json"}); 
        return ajaxResponse;
  }());

  $.jqplot('%s', payload.data,payload.options);
  
});""".format(id, id))

        <span>
          <head>
            <!--[if lt IE 9]><script language="javascript" type="text/javascript" src="/js/excanvas.js"></script><![endif]-->
            <script type="text/javascript" src="/js/jquery.jqplot.min.js"></script>
            <script type="text/javascript" src="/js/plugins/jqplot.dateAxisRenderer.min.js"></script>
            <script type="text/javascript" src="/js/plugins/jqplot.canvasTextRenderer.min.js"></script>
            <script type="text/javascript" src="/js/plugins/jqplot.canvasAxisLabelRenderer.min.js"></script>
            <script type="text/javascript" src="/js/plugins/jqplot.trendline.min.js"></script>
            <link rel="stylesheet" type="text/css" href="/css/jquery.jqplot.min.css"/>
            { Script(onLoad) }
          </head>
          <div id={ id } style="height:384px; width:512px;"></div>
        </span>

      }

    }
  }
}
