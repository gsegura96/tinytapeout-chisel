package wrapper

import chisel3._
import chisel3.util._
import displaydriver._
import gcd._
class ChiselWrapper extends RawModule {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })
  val clk = io.in(0)
  val rst = 0.B

  val display = Module(new DisplayDriver)
  io.out := display.io.out
  display.io.dot := io.in(1)

  val gcd = withClockAndReset(clk.asClock, rst)(Module(new GCD(width = 4)))
  gcd.io.value1 := Cat(1.U(1.W), io.in(4, 2))
  gcd.io.value2 := Cat(0.U(1.W), io.in(7, 5))
  gcd.io.loadingValues := io.in(1)
  display.io.in := gcd.io.outputGCD
  display.io.dot := gcd.io.outputValid
}

class ChiselWrapperAux extends Module {
  val io = IO(new Bundle {
    val data1 = Input(UInt(3.W))
    val data2 = Input(UInt(3.W))
    val sw = Input(Bool())
    val out = Output(UInt(8.W))
  })
  val wrapper = Module(new  ChiselWrapper)
  wrapper.io.in := Cat(io.data2, io.data1,io.sw, this.clock.asBool)
  io.out := wrapper.io.out
}


object ChiselWrapper extends App {
  println("Generating the ChiselWrapper module Verilog")
  (new chisel3.stage.ChiselStage).emitVerilog(
    new ChiselWrapper,
    Array(
      "--target-dir",
      "generated"
    )
  )
}
