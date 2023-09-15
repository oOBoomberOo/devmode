package me.boomber.devmode.data;

import net.minecraft.resources.ResourceLocation;

public class ParsingError extends IllegalArgumentException {
    public ParsingFailure parsingFailure;
    public ResourceLocation resourceLocation;

    public ParsingError(IllegalArgumentException cause, ParsingFailure parsingFailure, ResourceLocation resourceLocation) {
        super(cause);
        this.parsingFailure = parsingFailure;
        this.resourceLocation = resourceLocation;
    }
}
