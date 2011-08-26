package net {
  package liftmodules.jqplot {

    import liftweb.common.{Empty,Full}
    import org.specs2.mutable._
    
    class JqPlotSpec extends Specification {

      "JqPlot" should {
        " correctly caculate plugins to add as Nil. " in {
          val jqp = new JqPlot(0, 0, Empty,List())
          
          jqp.plugins  must_== Nil
        }
        
        " correctly adds known plugins " in {
           val options = Options().title("OHLC").
           axes(Axes().xaxis(Axis().renderer(DateAxisRenderer()))).
           series(List(Series().renderer(OHLCRenderer())))
          
          val jqp = new JqPlot(0, 0, Full(options),List())
          jqp.plugins  must_== List(<script type="text/javascript" src="/classpath/js/plugins/jqplot.dateAxisRenderer.js"></script>, <script type="text/javascript" src="/classpath/js/plugins/jqplot.ohlcRenderer.js"></script>) //List(<script type="text/javascript" src="/classpath/js/jqplot.pieRenderer.js"></script>)
        }        

      }
    }
  }
}


 

  