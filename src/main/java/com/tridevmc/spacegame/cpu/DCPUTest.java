package com.tridevmc.spacegame.cpu;

public class DCPUTest {
    public static int loadRom(DCPU cpu, int ...args) {
        int index = 0x0000;
        for(int c : args) {
            cpu.ram[index++] = (char)c;
        }
        return index;
    }
    public static void main(String args[]) {
        DCPU cpu = new DCPU();
        int max = loadRom(cpu, 0x8401,0x8821,0xac41,0x8452,0x7f81,0x000d,0x0061,0x0462,0x0401,0x0c21,0x8843,0x7f81,0x0003);
        while(cpu.pc < max) {
            cpu.eval(cpu.ram[cpu.pc]);
        }
        System.out.println("A: " + String.format("0x%04X", (int)cpu.a)
                + "\nB: " + String.format("0x%04X", (int)cpu.b)
                + "\nC: " + String.format("0x%04X", (int)cpu.c)
                + "\nX: " + String.format("0x%04X", (int)cpu.x)
                + "\nY: " + String.format("0x%04X", (int)cpu.y)
                + "\nZ: " + String.format("0x%04X", (int)cpu.z)
                + "\nI: " + String.format("0x%04X", (int)cpu.i)
                + "\nJ: " + String.format("0x%04X", (int)cpu.j)
                + "\nSP: " + String.format("0x%04X", (int)cpu.sp)
                + "\nPC: " + String.format("0x%04X", (int)cpu.pc)
                + "\nEX: " + String.format("0x%04X", (int)cpu.ex)
                + "\nIA: " + String.format("0x%04X", (int)cpu.ia));
        System.out.println("Cycles elapsed: " + cpu.cycles);
        System.out.println("RAM[0x0000]: " + String.format("0x%04X", (int)cpu.ram[0x0000]));
        System.out.println("RAM[0xFFFF]: " + String.format("0x%04X", (int)cpu.ram[0xFFFF]));

    }
}
