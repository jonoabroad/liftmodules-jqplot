package net {
  package liftmodules {

    import org.specs2.mutable._
  import net.liftweb.common.Full
  import net.liftweb.json.JsonAST.JField
  import net.liftweb.json.JsonAST.JObject
  import net.liftweb.json.JsonAST.JString
  import net.liftweb.json.JsonAST.JInt

    class OptionsSpec extends Specification {

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

    


