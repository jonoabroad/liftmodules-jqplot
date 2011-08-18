package net {
  package liftmodules {

    import org.specs2.mutable._

    class OptionsSpec extends Specification {

      "Legend " should {
        " compose correctly  " in {
        	val l =  Legend()
        	l.location(SE()).xoffset(10).yoffset(12)
        	
        	l.location must_== SE()
        	l.xoffset must_== 10
        	l.yoffset must_== 12
        	
        	Nil must_== Nil
        }

        " produce correct JSON  " in {
        	val l =  Legend()
        	l.location(SE()).xoffset(10).yoffset(12)
        	
        	l.toJson must_== ""
        }
      }
    }
  }
}


