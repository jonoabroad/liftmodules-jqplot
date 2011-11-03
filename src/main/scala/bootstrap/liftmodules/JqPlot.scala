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
package bootstrap.liftmodules {
  
  import net.liftweb.common.Loggable
  import net.liftweb.http.{ LiftRules, S,ResourceServer }

  object JqPlot extends Loggable {
        
        def init: Unit = {
          
          logger.info("JqPlot.init")

          val coreLibraries = List("jquery.js","jquery.jqplot.js","excanvas.js","jquery.min.js","jquery.jqplot.min.js","excanvas.min.js")
          
          ResourceServer.allow({
            case "js" ::  lib :: Nil if coreLibraries.contains(lib) => true
            case "js" :: "plugins" ::  plugin :: Nil => true
            case "css" :: "jquery.jqplot.css" :: Nil => true
          })
  
        }
        
      }  
  
}

