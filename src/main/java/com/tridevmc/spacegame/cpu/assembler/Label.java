package com.tridevmc.spacegame.cpu.assembler;

public class Label {
    public String label;
    public char ref;

    public Label(String label, char ref) {
        this.label = label;
        this.ref = ref;
    }
}
