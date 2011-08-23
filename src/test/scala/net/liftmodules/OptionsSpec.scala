package net {
  package liftmodules {

  import org.specs2.mutable._
  import net.liftweb.json.JsonAST.{JArray,JBool,JField,JInt,JObject,JString}

    class OptionsSpec extends Specification {

      "Series " should {
        
        " produce correct JSON  " in {
          
          val s = Series().lineWidth(42).markerOptions(MarkerOption().style(circle()))

          s.toJObject must_==  JObject(List(
        		  					JField("lineWidth",JInt(42)),
        		  					JField("markerOptions",JObject(List(
        		  												JField("style",JString("circle")))						
        		  					))))
        		  					
          Series().yaxis(y2axis()).toJson must_== JField("seriesDefaults",JObject(List(JField("yaxis",JString("y2axis")))))     		  					
        }
      }
      
      
      
      "Axis " should {
        
        " compose correctly  " in {
        	val a =  Axis(xaxis()).min("42").max("forty two")
        	 
        	a.min must_== Some("42")
        	a.max must_== Some("forty two")
        	
        	val a1 = Axis(yaxis()).numberOfTicks(42).pad("padding").showTickMarks(true).showTicks(true)
        	
        	a1.numberOfTicks must_== Some(42)
        	a1.showTickMarks must_== Some(true)
        	a1.showTicks must_== Some(true)
        	a1.pad must_== Some("padding")
        
        }

        " produce correct JSON  " in {
        	val a =  Axis(xaxis()).min("42").max("forty two")
        	
        	a.toJson must_==  JField("xaxis",JObject(List(JField("min",JString("42")),JField("max",JString("forty two")))))
        	
        	val a1 = Axis(yaxis()).numberOfTicks(42).pad("padding").showTickMarks(true).showTicks(true)
        	
        	a1.toJson must_==  JField("yaxis",JObject(List(JField("pad",JString("padding")),JField("numberTicks",JInt(42)),JField("showTicks",JBool(true)),JField("showTickMarks",JBool(true)))))

        	val a2 = Axis(xaxis()).renderer(DateAxisRenderer())
        	
        	a2.toJson must_==  JField("xaxis",JObject(List(JField("renderer",JString("$.jqplot.DateAxisRenderer")))))

        	
        	
        }
      }
      
      "Axes " should {
        
        " compose correctly  " in {
        	
          val axes = Axes().xaxis(Axis(xaxis()).min("42").max("forty two"))
        	 
        	axes.xaxis must_!= None
        }

        " produce correct JSON  " in {
          val a1 = Axes().xaxis(Axis(xaxis()).min("42").max("forty two"))
        	
          a1.toJson must_==  JField("axes",JObject(List(JField("xaxis",JObject(List(JField("min",JString("42")),JField("max",JString("forty two"))))))))
        	
          val a2 = Axes().xaxis(Axis(xaxis()).min("42").max("forty two")).yaxis(Axis(yaxis()).min("24").max("two forty"))
        	
          a2.toJson must_==  JField("axes",JObject(List(JField("xaxis",JObject(List(JField("min",JString("42")),JField("max",JString("forty two"))))),JField("yaxis",JObject(List(JField("min",JString("24")),JField("max",JString("two forty"))))))))  
        	
        }
      }         
      
      "Legend " should {
        
        " compose correctly  " in {
        	val l =  Legend().location(SE()).xoffset(10).yoffset(12)
        	
        	l.location must_== Some(SE())
        	l.xoffset must_== Some(10)
        	l.yoffset must_== Some(12)
        }

        " produce correct JSON  " in {
        	val l =  Legend().location(SE()).xoffset(10).yoffset(12)
        	
        	l.toJson must_==  JField("legend",JObject(List(
        	    JField("location",JString("se")),
        	    JField("xoffset",JInt(10)),
        	    JField("yoffset",JInt(12)))))  
        }
      }
      
      "MarkerOption " should {
        
        " compose correctly  " in {
        	MarkerOption().style(circle()).style must_== Some(circle())
        	
        }

        " produce correct JSON  " in {
        	
        	MarkerOption().style(circle()).toJson must_==  JField("markerOptions",JObject(List(JField("style",JString("circle")))))  
        }
      }      
      
      "Options " should {
        
        " compose correctly  " in {
           val a = Axes().xaxis(Axis(xaxis()).min("42").max("forty two").showTickMarks(true))

          val o =  Options().title("example").axes(a)
        	
            o.axes must_==  Some(Axes(Some(Axis(xaxis()).min("42").max("forty two").showTickMarks(true))))
            o.title must_== Some(Title("example"))

        }

        " produce correct JSON  " in {
           val a = Axes().xaxis(Axis(xaxis()).min("42").max("forty two").showTickMarks(true))
          
           val o =  Options().title("example").axes(a)
        	
        	
        	
           o.toJson must_== JObject(List(JField("title",JString("example")),
        		   	    JField("axes",JObject(
        		   	        List(JField("xaxis",JObject(
        		   	        						List(JField("min",JString("42")),
        		   	        						     JField("max",JString("forty two")),
        		   	        						     JField("showTickMarks",JBool(true))))))))))
             
        }
        
        " produce correct plugin lists" in {
          
          Axes().xaxis(Axis(xaxis()).renderer(DateAxisRenderer())).renderers must_== List(DateAxisRenderer())
          
           val o1 =  Options().title("example").axes(Axes().xaxis(Axis(xaxis()).min("42").max("forty two").showTickMarks(true)))
        	
           o1.plugins must_== List()

           val o2 =  Options().title("Default Date axis").axes(Axes().xaxis(Axis(xaxis()).renderer(DateAxisRenderer()))).
           seriesDefault(Series().lineWidth(4).markerOptions(MarkerOption().style(square())))
        	
           o2.plugins must_== List(DateAxisRenderer())           

           val o3 =  Options().title("Pie chart example").seriesDefault(Series().renderer(PieRenderer()))
        	
           o3.plugins must_== List(PieRenderer())        		   	        						     

           
           val o4 =  Options().title("Bar chart example").seriesDefault(Series().renderer(BarRenderer())).axes(Axes().yaxis(Axis(yaxis()).renderer(CategoryAxisRenderer())))
        	
           o4.plugins must_== List(CategoryAxisRenderer(),BarRenderer())
           
           val o5 = Options().title("bubble chart").seriesDefault(Series().renderer(BubbleRenderer()))
           o5.plugins must_== List(BubbleRenderer())
           
           val o6 = Options().title("OHLC").
           axes(Axes().xaxis(Axis(xaxis()).renderer(DateAxisRenderer()))).
           series(List(Series().renderer(ohlcRenderer())))
           
           o6.plugins must_== List(DateAxisRenderer(),ohlcRenderer())	   	        						     
        }    
        
      }    
      
    }
  }
}

    


