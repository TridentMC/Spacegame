package com.tridevmc.spacegame.cpu.assembler;

public class UnsatsifiedLinkLocation {
    public String label;
    public char wordLocation;

    public UnsatsifiedLinkLocation(String label, char wordLocation) {
        this.label = label;
        this.wordLocation = wordLocation;
    }
}
