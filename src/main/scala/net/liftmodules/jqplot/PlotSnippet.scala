package net.liftmodules.jqplot

import net.liftweb.common._
import net.liftweb.http.S

trait PlotSnippet {

    val w  = S.attr("w").map(a => a.toInt ).openOr(512)
    val h  = S.attr("h").map(a => a.toInt ).openOr(512)
    
    val options:Box[Options] = Empty
    
    val series:Array[List[List[Any]]]
    
    def render = new JqPlot(w,h,options,series: _*).toCssTransformer      
      
    
}