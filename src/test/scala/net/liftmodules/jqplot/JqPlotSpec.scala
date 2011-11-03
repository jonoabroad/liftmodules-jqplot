/*
        Copyright 2011 Spiral Arm Ltd

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package bootstrap.liftmodules
*/
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


 

  