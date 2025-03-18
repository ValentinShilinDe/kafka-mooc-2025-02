trait Show[A] {
  def show(a: A): String
}

object  Show {
  def show[A](a: A)(implicit sh: Show[A]): String = sh.show(a)

  implicit val paramInt: Show[Int] =
    new Show[Int] {
      def show(a: Int): String = s"int $a"
    }

  implicit val paramString: Show[String] =
    new Show[String] {
      def show(a: String): String = s"string $a"
    }


  def main(args: Array[String]): Unit ={
    println(show(42))
    println(show("42"))
  }
}