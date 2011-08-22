package net {
  package liftmodules {

  import org.specs2.mutable._
  import net.liftweb.json.JsonAST.{JArray,JBool,JField,JInt,JObject,JString}

    class OptionsSpec extends Specification {



      "Series " should {
        
        " compose correctly  " in {
        	val s =  Series().lineWidth(42).xaxis(xaxis())  	 

        	Nil must_== Nil
        }

        " produce correct JSON  " in {
        	val a =  Axis(xaxis()).min("42").max("forty two")
        	
        	a.toJson must_==  JField("xaxis",JObject(List(JField("min",JString("42")),JField("max",JString("forty two")))))  
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
      }      
    }
  }
}

    


