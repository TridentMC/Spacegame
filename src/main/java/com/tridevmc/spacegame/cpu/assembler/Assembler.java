package com.tridevmc.spacegame.cpu.assembler;

import com.tridevmc.spacegame.util.Result;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
    private static final Pattern prg = Pattern.compile("([A-Z]{3})(?: +)([A-Za-z0-9_\\[\\]+]+)(?:(?: +)?,(?: +)?([A-Za-z0-9_\\[\\]+]+))?");
    private static final Pattern label = Pattern.compile(":([A-Za-z0-9_.]+)");
    private List<Label> _labels = new ArrayList<>();
    private List<UnsatsifiedLinkLocation> _awaiting = new ArrayList<>();
    private List<Character> _words = new ArrayList<>();
    private int _lineNo = 0;
    private char _loc = 0;

    public Assembler(File file) {
        BufferedReader r = null;
        try {
            //r  = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            r  = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(file.getPath())));

            while(r.ready()) {
                String line = r.readLine().trim();

                if(line.length() == 0 || line.charAt(0) == ';') continue;

                if(line.charAt(0) == ':') {
                    Matcher m = label.matcher(line);
                    if(!m.find()) {
                        Logger.error(_lineNo + ": Label definition '" + line + "' invalid");
                        System.exit(-1);
                    }
                    String n = m.group(1);
                    for(Label l : _labels) {
                        if(l.label.equals(n)) {
                            Logger.error(_lineNo + ": Duplicate label '" + n + "'");
                            System.exit(-1);
                        }
                    }
                    _labels.add(new Label(n, _loc));
                    continue;
                }

                Matcher matcher = prg.matcher(line);

                if(!matcher.find()) continue;

                if(matcher.groupCount() != 3) {
                    Logger.error(_lineNo + ": Incorrectly formatted arguments in '" + line + "'");
                    System.exit(-1);
                }

                Result<Integer> opo = opcodeToHex(matcher.group(1));

                if(opo.isError()) {
                    Logger.error(_lineNo + ": " + opo.getError());
                    System.exit(-1);
                }

                int op = opo.get();

                boolean unary = (op == 0x00);

                int b;
                int a;
                char word;
                if(unary) {
                    Result<Integer> bo = unaryToHex(matcher.group(1));
                    if(bo.isError()) {
                        Logger.error(_lineNo + ": " + opo.getError());
                        System.exit(-1);
                    }
                    b = bo.get();
                    a = valueToHex(matcher.group(2), true);
                    word = (char)((a << 10) + (b << 5));
                } else {
                    b = valueToHex(matcher.group(2), false);
                    a = valueToHex(matcher.group(3), true);
                    word = (char)((a << 10) + (b << 5) + op);
                }
                _words.add(word);
                _loc++;


                // TODO: Account for all next-word cases, including unsatisfied link locations.
                if(!unary && b == 0x1F) {
                    _words.add((char)parseNextWord(matcher.group(2)));
                    _loc++;
                }
                if(a == 0x1F) {
                    _words.add((char)parseNextWord(matcher.group(unary ? 2 : 3)));
                    _loc++;
                }

                if(unary) {
                    Logger.debug("Opcode: " + matcher.group(1) + " (read as " + String.format("0x%02X", b) +
                                 ") | A: " + matcher.group(2) + " (read as " + String.format("0x%02X", a) + ")");
                } else {
                    Logger.debug("Opcode: " + matcher.group(1) + " (read as " + String.format("0x%02X", op) +
                                 ") | B: " + matcher.group(2) + " (read as " + String.format("0x%02X", b) +
                                 ") | A: " + matcher.group(3) + " (read as " + String.format("0x%02X", a) + ")");
                }
                _lineNo++;
            }

            for(UnsatsifiedLinkLocation ul : _awaiting) {
                Logger.debug("Finding '" + ul.label + "' for wl [" + String.format("0x%04X", (int)ul.wordLocation) + "]...");
                boolean found = false;
                for(Label l : _labels) {
                    if(ul.label.equals(l.label)) {
                        Logger.debug("Found '" + l.label + "' at wl [" + String.format("0x%04X", (int)l.ref) + "]");
                        _words.set(ul.wordLocation, (char)(_words.get(ul.wordLocation) + l.ref));
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    Logger.error("Couldn't satisfy link location '" + ul.label + "'!");
                    System.exit(-1);
                }
            }

            for(int i =0;i < _words.size();i+=8) {
                System.out.print(String.format("[0x%04X] ", i));
                if(_words.size()-i < 8) {
                    for(int j = 0;j < _words.size()-i;j++) {
                        System.out.print(String.format("0x%04X ", (int)_words.get(i+j)));
                    }
                } else {
                    for (int j = 0; j < 8; j++) {
                        System.out.print(String.format("0x%04X ", (int) _words.get(i + j)));
                    }
                }
                System.out.println();
            }
        } catch (Exception e) {
            Logger.error(e);
            System.exit(-1);
        }
    }

    // TODO: Parse basic arithmetic.
    public int parseNextWord(String value) {
        try {
            if (value.startsWith("0x")) {
                return Integer.parseInt(value.substring(2), 16);
            }
            return Integer.parseInt(value, 10);
        } catch(NumberFormatException e) {
            for(Label l : _labels) {
                if(l.label.equals(value)) {
                    return l.ref;
                }
            }
            _awaiting.add(new UnsatsifiedLinkLocation(value, _loc));
            return 0x00;
        }
    }

    public int valueToHex(String value, boolean lng) throws NumberFormatException {
        if(value.startsWith("[")) return -1;
        if(value.startsWith("0x")) {
            int v = Integer.parseInt(value.substring(2), 16);
            if(v == 0xFFFF || (v >= 0x00 && v <= 0x1E)) {
                return v + 0x21;
            } else {
                return 0x1F;
            }
        }
        try {
            int v = Integer.parseInt(value, 10);
            if(v == 0xFFFF || (v >= 0x00 && v <= 0x1E)) {
                return v + 0x21;
            } else {
                return 0x1F;
            }
        } catch(NumberFormatException e) {
            // just continue;
        }

        switch(value.toUpperCase()) {
            case "A":
                return 0x00;
            case "B":
                return 0x01;
            case "C":
                return 0x02;
            case "X":
                return 0x03;
            case "Y":
                return 0x04;
            case "Z":
                return 0x05;
            case "I":
                return 0x06;
            case "J":
                return 0x07;
            case "PUSH":
                if(lng) {
                    Logger.error(_lineNo + ": PUSH in src position!");
                    return -1;
                }
                return 0x18;
            case "POP":
                if(!lng) {
                    Logger.error(_lineNo + ": POP in dst position!");
                    return -1;
                }
                return 0x18;
            case "SP":
                return 0x1B;
            case "PC":
                return 0x1C;
            case "EX":
                return 0x1D;
            default:
                return 0x1F; // let's hope it resolves I guess
        }
    }

    public Result<Integer> opcodeToHex(String opcode) {
        switch(opcode.toUpperCase()) {
            case "SET":
                return Result.ok(0x01);
            case "ADD":
                return Result.ok(0x02);
            case "SUB":
                return Result.ok(0x03);
            case "MUL":
                return Result.ok(0x04);
            case "MLI":
                return Result.ok(0x05);
            case "DIV":
                return Result.ok(0x06);
            case "DVI":
                return Result.ok(0x07);
            case "MOD":
                return Result.ok(0x08);
            case "MDI":
                return Result.ok(0x09);
            case "AND":
                return Result.ok(0x0A);
            case "BOR":
                return Result.ok(0x0B);
            case "XOR":
                return Result.ok(0x0C);
            case "SHR":
                return Result.ok(0x0D);
            case "ASR":
                return Result.ok(0x0E);
            case "SHL":
                return Result.ok(0x0F);
            case "IFB":
                return Result.ok(0x10);
            case "IFC":
                return Result.ok(0x11);
            case "IFE":
                return Result.ok(0x12);
            case "IFN":
                return Result.ok(0x13);
            case "IFG":
                return Result.ok(0x14);
            case "IFA":
                return Result.ok(0x15);
            case "IFU":
                return Result.ok(0x16);
            case "ADX":
                return Result.ok(0x1A);
            case "SBX":
                return Result.ok(0x1B);
            case "STI":
                return Result.ok(0x1E);
            case "STD":
                return Result.ok(0x1F);
            case "JSR":
            case "INT":
            case "IAG":
            case "IAS":
            case "RFI":
            case "IAQ":
            case "HWN":
            case "HWQ":
            case "HWI":
                return Result.ok(0x00);
            default:
                return Result.error("Unable to parse instruction '" + opcode + "'");
        }
    }

    public Result<Integer> unaryToHex(String unary) {
        switch(unary.toUpperCase()) {
            case "JSR":
                return Result.ok(0x01);
            case "INT":
                return Result.ok(0x08);
            case "IAG":
                return Result.ok(0x09);
            case "IAS":
                return Result.ok(0x0A);
            case "RFI":
                return Result.ok(0x0B);
            case "IAQ":
                return Result.ok(0x0C);
            case "HWN":
                return Result.ok(0x10);
            case "HWQ":
                return Result.ok(0x11);
            case "HWI":
                return Result.ok(0x12);
            default:
                return Result.error("Unable to parse unary instruction '" +unary + "'");
        }
    }

    public static void main(String args[]) {
        Assembler a = new Assembler(new File("/test.dasm"));
    }
}
