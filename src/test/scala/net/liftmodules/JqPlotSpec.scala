package net {
  package liftmodules {

    import org.specs2.mutable._

    class JqPlotSpec extends Specification {

      "JqPlot" should {
        " correctly caculate plugins to add as Nil. " in {
          val jqp = new JqPlot(0, 0, "series", "options")
          
          jqp.plugins  must_== Nil
        }
        
        " correctly adds known plugins " in {
          val jqp = new JqPlot(0, 0, "series",  """{seriesDefaults: {renderer: jQuery.jqplot.PieRenderer,rendererOptions: {showDataLabels: true}},legend: { show:true, location: 'e' }}""")
          
          jqp.plugins  must_== List(<script type="text/javascript" src="/classpath/js/jqplot.pieRenderer.js"></script>)
        }        

      }
    }
  }
}


 

  