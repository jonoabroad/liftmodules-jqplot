package net {
  package liftmodules {

    import org.specs2.mutable._
  import net.liftweb.common.Full
  import net.liftweb.json.JsonAST.JField
  import net.liftweb.json.JsonAST.JObject
  import net.liftweb.json.JsonAST.JString
  import net.liftweb.json.JsonAST.JInt
  import net.liftweb.common.Empty

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
        	 
        	a.min must_== Full("42")
        	a.max must_== Full("forty two")
        }

        " produce correct JSON  " in {
        	val a =  Axis(xaxis()).min("42").max("forty two")
        	
        	a.toJson must_==  JField("xaxis",JObject(List(JField("min",JString("42")),JField("max",JString("forty two")))))  
        }
      }
      
      "Axes " should {
        
        " compose correctly  " in {
        	
          val axes = Axes().xaxis(Axis(xaxis()).min("42").max("forty two"))
        	 
        	axes.xaxis must_!= Empty
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
        	
        	l.location must_== Full(SE())
        	l.xoffset must_== Full(10)
        	l.yoffset must_== Full(12)
        }

        " produce correct JSON  " in {
        	val l =  Legend().location(SE()).xoffset(10).yoffset(12)
        	
        	l.toJson must_==  JField("legend",JObject(List(
        	    JField("location",JString("se")),
        	    JField("xoffset",JInt(10)),
        	    JField("yoffset",JInt(12)))))  
        }
      }
    }
  }
}

    


