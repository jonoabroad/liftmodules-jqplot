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

