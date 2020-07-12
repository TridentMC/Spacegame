package com.tridevmc.spacegame.cpu;

import com.tridevmc.spacegame.cpu.hardware.HardwareList;
import com.tridevmc.spacegame.cpu.hardware.IHardware;
import com.tridevmc.spacegame.util.CharQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DCPU {
    public char a, b, c, x, y, z, i, j;
    public char pc, sp, ex, ia;
    public final char[] ram = new char[0x10000];
    public int lastCycles = 0;
    public int cycles = 0;
    private final CharQueue _interruptQueue = new CharQueue();
    private boolean _queueing = false;
    private final List<BiConsumer<Integer, Integer>> _ops = new ArrayList<>(0x20);
    private final List<Consumer<Integer>> _unary = new ArrayList<>(0x20);
    public final HardwareList hardwareList = new HardwareList();

    public DCPU() {
        for(int i =0x00;i < 0x20;i++) {
            _ops.add(null);
        }

        _ops.set(0x00, (a, b) -> _unary.get(b).accept(a)); // special
        _ops.set(0x01, (a, b) -> { // SET
            cycles += 1;
            char val = read(getAddress(a, false));
            write(getAddress(b, true), val);
        });
        _ops.set(0x02, (a, b) -> { // ADD
            cycles += 2;
            int val = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            val += read(baddr);
            write(baddr, (char) val);
            ex = (val > 0xFFFF) ? (char) 0x0001 : (char) 0x0000;
        });
        _ops.set(0x03, (a, b) -> { // SUB
            cycles += 2;
            int val = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            val = read(baddr) - val;
            write(baddr, (char) val);
            ex = (val < 0x0000) ? (char) 0xFFFF : (char) 0x0000;
        });
        _ops.set(0x04, (a, b) -> { // MUL
            cycles += 2;
            char av = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            char bv = read(baddr);
            write(baddr, (char)(bv*av));
            ex = (char)(((bv*av)>>16)&0xffff);
        });
        _ops.set(0x05, (a, b) -> { // MLI
            cycles += 2;
            int av = signedFromChar(read(getAddress(a, false)));
            int baddr = getAddress(b, true);
            int bv = signedFromChar(read(baddr));
            write(baddr, signedToChar(bv*av));
            ex = (char)(((bv*av)>>16)&0xffff);
        });
        _ops.set(0x06, (a, b) -> { // DIV
            cycles += 3;
            char av = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            char bv = read(baddr);
            if(av == 0) {
                write(baddr, (char)0);
                ex = 0;
            } else {
                write(baddr, (char)(bv/av));
                ex = (char)(((bv<<16)/av)&0xffff);
            }
        });
        _ops.set(0x07, (a, b) -> { // DVI
            cycles += 3;
            int av = signedFromChar(read(getAddress(a, false)));
            int baddr = getAddress(b, true);
            int bv = signedFromChar(read(baddr));
            if(av == 0) {
                write(baddr, (char)0);
                ex = 0;
            } else {
                write(baddr, signedToChar(round((double)bv/(double)av)));
                ex = (char)((round((double)(bv<<16)/(double)av))&0xffff);
            }
        });
        _ops.set(0x08, (a, b) -> { // MOD
            cycles += 3;
            char av = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            char bv = read(baddr);
            if(av == 0) {
                write(baddr, (char)0);
            } else {
                write(baddr, (char)(bv%av));
            }
        });
        _ops.set(0x09, (a, b) -> { // MDI
            cycles += 3;
            int av = signedFromChar(read(getAddress(a, false)));
            int baddr = getAddress(b, true);
            int bv = signedFromChar(read(baddr));
            if(av == 0) {
                write(baddr, (char)0);
            } else {
                write(baddr, signedToChar(bv%av));
            }
        });
        _ops.set(0x0A, (a, b) -> { // AND
            cycles += 1;
            char val = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            val = (char)(read(baddr) & val);
            write(baddr, val);
        });
        _ops.set(0x0B, (a, b) -> { // BOR
            cycles += 1;
            char val = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            val = (char)(read(baddr) | val);
            write(baddr, val);
        });
        _ops.set(0x0C, (a, b) -> { // XOR
            cycles += 1;
            char val = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            val = (char)(read(baddr) ^ val);
            write(baddr, val);
        });
        _ops.set(0x0D, (a, b) -> { // SHR
            cycles += 1;
            char av = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            char bv = read(baddr);
            write(baddr, (char)(bv>>>av));
            ex = (char)(((bv<<16)>>av)&0xffff);
        });
        _ops.set(0x0E, (a, b) -> { // ASR
            cycles += 1;
            int av = signedFromChar(read(getAddress(a, false)));
            int baddr = getAddress(b, true);
            int bv = signedFromChar(read(baddr));
            write(baddr, signedToChar(bv >> av));
            ex = signedToChar(((bv<<16)>>av)&0xffff);
        });
        _ops.set(0x0F, (a, b) -> { // SHL
            cycles += 1;
            char av = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            char bv = read(baddr);
            write(baddr, (char)(bv<<av));
            ex = (char)(((b<<a)>>16)&0xffff);
        });
        _ops.set(0x10, (a, b) -> { // IFB
            cycles += 2;
            char av = read(getAddress(a, false));
            char bv = read(getAddress(b, true));
            if(((bv&av) == 0)) skipNext();
        });
        _ops.set(0x11, (a, b) -> { // IFC
            cycles += 2;
            char av = read(getAddress(a, false));
            char bv = read(getAddress(b, true));
            if((bv&av) != 0) skipNext();
        });
        _ops.set(0x12, (a, b) -> { // IFE
            cycles += 2;
            char av = read(getAddress(a, false));
            char bv = read(getAddress(b, true));
            if(bv != av) skipNext();
        });
        _ops.set(0x13, (a, b) -> { // IFN
            cycles += 2;
            char av = read(getAddress(a, false));
            char bv = read(getAddress(b, true));
            if(bv == av) skipNext();
        });
        _ops.set(0x14, (a, b) -> { // IFG
            cycles += 2;
            char av = read(getAddress(a, false));
            char bv = read(getAddress(b, true));
            if(bv < av) skipNext();
        });
        _ops.set(0x15, (a, b) -> { // IFA
            cycles += 2;
            int av = signedFromChar(read(getAddress(a, false)));
            int bv = signedFromChar(read(getAddress(b, true)));
            if(bv < av) skipNext();
        });
        _ops.set(0x16, (a, b) -> { // IFL
            cycles += 2;
            char av = read(getAddress(a, false));
            char bv = read(getAddress(b, true));
            if(bv > av) skipNext();
        });
        _ops.set(0x17, (a, b) -> { // IFU
            cycles += 2;
            int av = signedFromChar(read(getAddress(a, false)));
            int bv = signedFromChar(read(getAddress(b, true)));
            if(bv > av) skipNext();
        });
        _ops.set(0x18, (a, b) -> { // undefined
            getAddress(a, false, false);
            getAddress(b, true, true);
        });
        _ops.set(0x19, (a, b) -> { // undefined
            getAddress(a, false, false);
            getAddress(b, true, true);
        });
        _ops.set(0x1A, (a, b) -> { // ADX
            cycles += 3;
            int val = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            val += read(baddr);
            val += ex;
            write(baddr, (char) val);
            ex = (val > 0xFFFF) ? (char) 0x0001 : (char) 0x0000;
        });
        _ops.set(0x1B, (a, b) -> { // SBX
            cycles += 3;
            int val = read(getAddress(a, false));
            int baddr = getAddress(b, true);
            val = read(baddr) - val;
            val += ex;
            write(baddr, (char) val);
            ex = (val < 0x0000) ? (char) 0xFFFF : (char) 0x0000;
        });
        _ops.set(0x1C, (a, b) -> { // undefined
            getAddress(a, false, false);
            getAddress(b, true, true);
        });
        _ops.set(0x1D, (a, b) -> { // undefined
            getAddress(a, false, false);
            getAddress(b, true, true);
        });
        _ops.set(0x1E, (a, b) -> { // STI
            cycles += 2;
            char val = read(getAddress(a, false));
            write(getAddress(b, true), val);
            i++;
            j++;
        });
        _ops.set(0x1F, (a, b) -> { // STD
            cycles += 2;
            char val = read(getAddress(a, false));
            write(getAddress(b, true), val);
            i--;
            j--;
        });

        for(int i =0;i < 0x20;i++) {
            _unary.add(null);
        }

        _unary.set(0x00, (a) -> { // reserved
            getAddress(a, false, false);
        });
        _unary.set(0x01, (a) -> { // JSR
            cycles += 3;
            char val = read(getAddress(a, false));
            ram[--sp] = pc;
            pc = val;
        });
        _unary.set(0x02, (a) -> { // undefined
            getAddress(a, false, false);
        });
        _unary.set(0x03, (a) -> { // undefined
            getAddress(a, false, false);
        });
        _unary.set(0x04, (a) -> { // undefined
            getAddress(a, false, false);
        });
        _unary.set(0x05, (a) -> { // undefined
            getAddress(a, false, false);
        });
        _unary.set(0x06, (a) -> { // undefined
            getAddress(a, false, false);
        });
        _unary.set(0x07, (a) -> { // undefined
            getAddress(a, false, false);
        });
        _unary.set(0x08, (a) -> { // INT
            cycles += 4;
            _interruptQueue.enqueue(read(getAddress(a, false)));
        });
        _unary.set(0x09, (a) -> { // IAG
            cycles += 1;
            write(getAddress(a, false), ia);
        });
        _unary.set(0x0A, (a) -> { // IAS
            cycles += 1;
            ia = read(getAddress(a, false));
        });
        _unary.set(0x0B, (a) -> { // RFI
            cycles += 3;
            _queueing = false;
            read(getAddress(a, false)); // discard?
            this.a = ram[sp++];
            this.pc = ram[sp++];
        });
        _unary.set(0x0C, (a) -> { // IAQ
            cycles += 2;
            _queueing = read(getAddress(a, false)) != 0;
        });
        _unary.set(0x0D, (a) -> { // undefined
            getAddress(a, false, false);
        });
        _unary.set(0x0E, (a) -> { // undefined
            getAddress(a, false, false);
        });
        _unary.set(0x0F, (a) -> { // undefined
            getAddress(a, false, false);
        });
        _unary.set(0x10, (a) -> { // HWN
            cycles += 2;
            write(getAddress(a, false), hardwareList.size());
        });
        _unary.set(0x11, (a) -> { // HWQ
            cycles += 4;
            char id = read(getAddress(a, false));
            IHardware hw = hardwareList.get(id);
            if(hw != null) {
                int hid = hw.id();
                this.a = (char)(hid & 0xFFFF);
                this.b = (char)((hid >> 16) & 0xFFFF);
                this.c = hw.version();
                int hman = hw.manufacturer();
                this.x = (char)(hman & 0xFFFF);
                this.y = (char)((hman >> 16) & 0xFFFF);
            }

        });
        _unary.set(0x12, (a) -> { // HWI
            cycles += 4;
            char id = read(getAddress(a, false));
            hardwareList.interrupt(id);
        });
        for(int i =0x13;i < 0x20;i++) {
            _unary.set(i, (a) -> { // undefined
                getAddress(a, false, false);
            });
        }
    }

    public void connect(IHardware hw) {
        hardwareList.connect(this, hw);
    }

    public char signedToChar(int signed) {
        if(signed < 0) {
            return (char) ((signed & 0x7fff) | 0x8000);
        }
        return (char)signed;
    }

    public int signedFromChar(char val) {
        if((val & 0x8000) > 0) {
            return (((~val) + 1) & 0xffff) * -1;
        }
        return val;
    }

    public int round(double res) {
        if(res < 0) {
            return (int)Math.ceil(res);
        }
        return (int)Math.floor(res);
    }

    public char makeOpcode(int opcode, int a, int b) {
        return (char)((opcode & 0b11111)
                 + ((b & 0b11111) << 5)
                 + ((a & 0b111111) << 10));
    }

    public void skipNext() {
        cycles++;
        char word = ram[pc++];
        int opcode = (word & 0b0000000000011111);
        int a =      (word & 0b1111110000000000) >> 10;
        int b =      (word & 0b0000001111100000) >> 5;
        getAddress(a, false, false);
        if(opcode != 0x00)
            getAddress(b, true, false);
        if(opcode >= 0x10 && opcode <= 0x17) {
            skipNext();
        }
    }

    public void interrupt(char msg) {
        _interruptQueue.enqueue(msg);
    }

    private void processInterrupt(char msg) {
        if (ia != 0) {
            _queueing = true;
            ram[--sp] = pc;
            ram[--sp] = a;
            pc = ia;
            a = msg;
        }
    }

    public void eval(int cycles) {
        while(this.cycles < cycles) {
            char word = ram[pc++];
            int opcode = (word & 0b0000000000011111);
            int a = (word & 0b1111110000000000) >> 10;
            int b = (word & 0b0000001111100000) >> 5;

            if (opcode >= _ops.size()) {
                throw new RuntimeException("Bad opcode '" + opcode + "'!");
            }

            _ops.get(opcode).accept(a, b);

            if (!_queueing && _interruptQueue.size() != 0) {
                processInterrupt(_interruptQueue.dequeue());
            }
        }
        this.cycles = 0;
    }

    public char read(int loc) {
        if(loc < 0x10000) {
            return ram[loc];
        }
        if(loc >= 0x10020 && loc <= 0x1003F) {
            return (char) (loc - 0x10000 - 0x21);
        }
        switch(loc) {
            case 0x10000:
                return a;
            case 0x10001:
                return b;
            case 0x10002:
                return c;
            case 0x10003:
                return x;
            case 0x10004:
                return y;
            case 0x10005:
                return z;
            case 0x10006:
                return i;
            case 0x10007:
                return j;
            case 0x10008:
                return sp;
            case 0x10009:
                return pc;
            case 0x1000A:
                return ex;
            case 0x1000B:
                return read(pc - 1);
        }
        throw new RuntimeException("read addr '" + loc + "' invalid!!!");
    }

    public void write(int loc, char val) {
        if(loc < 0x10000) {
            ram[loc] = val;
            return;
        }
        if(loc >= 0x10020 && loc <= 0x1003F) {
            return; // silent fail
        }
        switch(loc) {
            case 0x10000:
                a = val;
                return;
            case 0x10001:
                b = val;
                return;
            case 0x10002:
                c = val;
                return;
            case 0x10003:
                x = val;
                return;
            case 0x10004:
                y = val;
                return;
            case 0x10005:
                z = val;
                return;
            case 0x10006:
                i = val;
                return;
            case 0x10007:
                j = val;
                return;
            case 0x10008:
                sp = val;
                return;
            case 0x10009:
                pc = val;
                return;
            case 0x1000A:
                ex = val;
                return;
            case 0x1000B:
                return; // silent fail
        }
        throw new RuntimeException("write addr '" + loc + "' invalid!!!");
    }

    public int getAddress(int loc, boolean rb) {
        return getAddress(loc, rb, true);
    }

    public int getAddress(int loc, boolean rb, boolean sideEffects) {
        if(loc >= 0x20 && loc <= 0x3F) {
            return 0x10000 + loc;
        }
        switch(loc) {
            case 0x00:
                return 0x10000; // a
            case 0x01:
                return 0x10001; // b
            case 0x02:
                return 0x10002; // c
            case 0x03:
                return 0x10003; // x
            case 0x04:
                return 0x10004; // y
            case 0x05:
                return 0x10005; // z
            case 0x06:
                return 0x10006; // i
            case 0x07:
                return 0x10007; // j
            case 0x08:
                return a;
            case 0x09:
                return b;
            case 0x0A:
                return c;
            case 0x0B:
                return x;
            case 0x0C:
                return y;
            case 0x0D:
                return z;
            case 0x0E:
                return i;
            case 0x0F:
                return j;
            case 0x10:
                cycles++;
                return a + read(pc++);
            case 0x11:
                cycles++;
                return b + read(pc++);
            case 0x12:
                cycles++;
                return c + read(pc++);
            case 0x13:
                cycles++;
                return x + read(pc++);
            case 0x14:
                cycles++;
                return y + read(pc++);
            case 0x15:
                cycles++;
                return z + read(pc++);
            case 0x16:
                cycles++;
                return i + read(pc++);
            case 0x17:
                cycles++;
                return j + read(pc++);
            case 0x18:
                if(!sideEffects) return sp;
                return rb ? --sp : sp++;
            case 0x19:
                return sp;
            case 0x1A:
                cycles++;
                return sp + read(pc++);
            case 0x1B:
                return 0x10008; // sp
            case 0x1C:
                return 0x10009; // pc
            case 0x1D:
                return 0x1000A; // ex
            case 0x1E:
                cycles++;
                return read(pc++); // [[PC++]]
            case 0x1F:
                cycles++;
                pc++;
                return 0x1000B; // [PC++]
        }
        throw new RuntimeException("address id '" + loc + "' invalid!!!");
    }

    public String dump() {
       return
               "A: " + String.format("0x%04X", (int) this.a) +
                       "\nB: " + String.format("0x%04X", (int) this.b) +
                       "\nC: " + String.format("0x%04X", (int) this.c) +
                       "\nX: " + String.format("0x%04X", (int) this.x) +
                       "\nY: " + String.format("0x%04X", (int) this.y) +
                       "\nZ: " + String.format("0x%04X", (int) this.z) +
                       "\nI: " + String.format("0x%04X", (int) this.i) +
                       "\nJ: " + String.format("0x%04X", (int) this.j) +
                       "\nSP: " + String.format("0x%04X", (int) this.sp) +
                       "\nPC: " + String.format("0x%04X", (int) this.pc) +
                       "\nEX: " + String.format("0x%04X", (int) this.ex) +
                       "\nIA: " + String.format("0x%04X", (int) this.ia);
    }


}
