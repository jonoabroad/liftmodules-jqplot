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
package net.liftmodules.jqplot

import net.liftweb.common._
import net.liftweb.http.S

trait PlotSnippet {
  
	implicit def pair2ListAny(p:List[(Any,Any)]):  List[List[Any]] = p.map{x => List(x._1,x._2)}
	implicit def pair2ListAny(p:(Any,Any)):List[Any] = List(p._1,p._2)
	implicit def series2Array(s:Series): Array[Series] = Array(s)
	implicit def series2Array(o:Options): Box[Options] = Box !! o  

    val w  = S.attr("w").map(a => a.toInt ).openOr(512)
    val h  = S.attr("h").map(a => a.toInt ).openOr(512)
    
    val options:Box[Options] = Empty
    
    val series:Array[List[List[Any]]]
    
    def render = new JqPlot(w,h,options,series: _*).toCssTransformer      
      
    
}