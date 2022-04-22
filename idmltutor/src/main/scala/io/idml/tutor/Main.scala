package io.idml.tutor

import cats.effect.{ExitCode, IO, IOApp}
import com.monovore.decline.{Command, Opts}
import fansi.Color.{Cyan, Green, Red}

object Main {
  import Colours._

  val banner =
    """
   "        #         ""#      m             m
 mmm     mmm#  mmmmm    #    mm#mm  m   m  mm#mm   mmm    m mm
   #    #" "#  # # #    #      #    #   #    #    #" "#   #"  "
   #    #   #  # # #    #      #    #   #    #    #   #   #
 mm#mm  "#m##  # # #    "mm    "mm  "mm"#    "mm  "#m#"   #
"""

  def execute(): Command[IO[ExitCode]] =
    Command("tutor", "run the IDML tutor") {
      Opts.apply(
        for {
          jline <- JLine[IO]("idmltutor")
          _     <- jline.printAbove(banner)
          _     <- jline.printAbove("""
        |Welcome to the IDML tutor, this is a utility for learning IDML
        |
        |Please select the option you'd like:
        |  start     - start at chapter 1
        |  quit      - quit
        |
      """.stripMargin)
          s     <- jline.readLine(cyan("# "))
          _     <- s match {
                     case "start" => Chapter1(new TutorialAlg[IO](jline))
                     case _       => IO.unit
                   }
        } yield ExitCode.Success
      )
    }

}
