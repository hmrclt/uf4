
import cats.{Applicative, Monoid, Monad}
import cats.implicits._
import ltbs.uniform._

import language.implicitConversions

package object programs {

  case class DataType1(i: String)
  case class DataType2(i: String)
  case class DataType3(i: String)
  case class DataType4(i: String)
  case class DataType5(i: String)
  case class DataType6(i: String)
  case class DataType7(i: String)
  case class DataType8(i: String)
  case class DataType9(i: String)
  case class DataType10(i: String)
  case class DataType11(i: String)
  case class DataType12(i: String)
  case class DataType13(i: String)
  case class DataType14(i: String)
  case class DataType15(i: String)
  case class DataType16(i: String)
  case class DataType17(i: String)
  case class DataType18(i: String)
  case class DataType19(i: String)
  case class DataType20(i: String)
  case class DataType21(i: String)
  case class DataType22(i: String)
  case class DataType23(i: String)
  case class DataType24(i: String)
  case class DataType25(i: String)
  case class DataType26(i: String)
  case class DataType27(i: String)
  case class DataType28(i: String)
  case class DataType29(i: String)
  case class DataType30(i: String)
  case class DataType31(i: String)
  case class DataType32(i: String)
  case class DataType33(i: String)
  case class DataType34(i: String)
  case class DataType35(i: String)
  case class DataType36(i: String)
  case class DataType37(i: String)
  case class DataType38(i: String)
  case class DataType39(i: String)
  case class DataType40(i: String)
  case class DataType41(i: String)
  case class DataType42(i: String)
  case class DataType43(i: String)
  case class DataType44(i: String)
  case class DataType45(i: String)
  case class DataType46(i: String)
  case class DataType47(i: String)
  case class DataType48(i: String)
  case class DataType49(i: String)
  case class DataType50(i: String)
  case class DataType51(i: String)
  case class DataType52(i: String)
  case class DataType53(i: String)
  case class DataType54(i: String)
  case class DataType55(i: String)
  case class DataType56(i: String)
  case class DataType57(i: String)
  case class DataType58(i: String)
  case class DataType59(i: String)
  case class DataType60(i: String)
  case class DataType61(i: String)
  case class DataType62(i: String)
  case class DataType63(i: String)
  case class DataType64(i: String)
  case class DataType65(i: String)
  case class DataType66(i: String)
  case class DataType67(i: String)
  case class DataType68(i: String)
  case class DataType69(i: String)
  case class DataType70(i: String)
  case class DataType71(i: String)
  case class DataType72(i: String)
  case class DataType73(i: String)
  case class DataType74(i: String)
  case class DataType75(i: String)
  case class DataType76(i: String)
  case class DataType77(i: String)
  case class DataType78(i: String)
  case class DataType79(i: String)
  case class DataType80(i: String)
  case class DataType81(i: String)
  case class DataType82(i: String)
  case class DataType83(i: String)
  case class DataType84(i: String)
  case class DataType85(i: String)
  case class DataType86(i: String)
  case class DataType87(i: String)
  case class DataType88(i: String)
  case class DataType89(i: String)
  case class DataType90(i: String)
  case class DataType91(i: String)
  case class DataType92(i: String)
  case class DataType93(i: String)
  case class DataType94(i: String)
  case class DataType95(i: String)
  case class DataType96(i: String)
  case class DataType97(i: String)
  case class DataType98(i: String)
  case class DataType99(i: String)
  case class DataType100(i: String)

  type TellTypes = NilTypes
  type AskTypes = (Int, Int) :: Int :: Boolean :: NilTypes

  def greasy[F[_] : Monad](
    interpreter: Language[F, TellTypes, AskTypes]
  ): F[Int] = {
    import interpreter._
    for {
      age           <- ask[Int]("age")
      h2           <- ask[(Int,Int)]("height")
      height3           <- ask[Int]("height3")                  
      food          <- ask[Boolean]("wantFood")
      tea           <- ask[Boolean]("wantTea")
      baconCost     <- ask[Int]("bacon").map(_ * 12) emptyUnless food
      eggsCost      <- ask[Int]("eggs").map(_ * 24) emptyUnless food
      foodCost      = baconCost + eggsCost
      teaCost       <- ask[Int]("sugar").map(_ * 10 + 50) emptyUnless tea
      youngDiscount = if (age < 16) teaCost / 10 else 0
      oldDiscount   = if (age > 60) (teaCost + foodCost) * (Math.min(age - 60,25) / 100) else 0
    } yield (foodCost + teaCost + youngDiscount + oldDiscount)
  }

  type TellTypes2 = NilTypes
  type AskTypes2 = DataType1 :: DataType2 :: DataType3 :: DataType4 :: DataType5 :: DataType6 :: DataType7 :: DataType8 :: DataType9 :: DataType10 :: DataType11 :: DataType12 :: DataType13 :: DataType14 :: DataType15 :: DataType16 :: DataType17 :: DataType18 :: DataType19 :: DataType20 :: DataType21 :: DataType22 :: DataType23 :: DataType24 :: DataType25 :: DataType26 :: DataType27 :: DataType28 :: DataType29 :: DataType30 :: DataType31 :: DataType32 :: DataType33 :: DataType34 :: DataType35 :: DataType36 :: DataType37 :: DataType38 :: DataType39 :: DataType40 :: DataType41 :: DataType42 :: DataType43 :: DataType44 :: DataType45 :: DataType46 :: DataType47 :: DataType48 :: DataType49 :: DataType50 :: DataType51 :: DataType52 :: DataType53 :: DataType54 :: DataType55 :: DataType56 :: DataType57 :: DataType58 :: DataType59 :: DataType60 :: DataType61 :: DataType62 :: DataType63 :: DataType64 :: DataType65 :: DataType66 :: DataType67 :: DataType68 :: DataType69 :: DataType70 :: DataType71 :: DataType72 :: DataType73 :: DataType74 :: DataType75 :: DataType76 :: DataType77 :: DataType78 :: DataType79 :: DataType80 :: DataType81 :: DataType82 :: DataType83 :: DataType84 :: DataType85 :: DataType86 :: DataType87 :: DataType88 :: DataType89 :: DataType90 :: DataType91 :: DataType92 :: DataType93 :: DataType94 :: DataType95 :: DataType96 :: DataType97 :: DataType98 :: DataType99 :: DataType100 :: NilTypes

  def greasy2[F[_]: Monad](
    interpreter: Language[F, TellTypes2, AskTypes2]
  ): F[Unit] = {
    import interpreter._
    for {
      _   <- ask[DataType1]("one")
    } yield (())
  }

}
