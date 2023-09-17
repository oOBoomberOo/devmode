package me.boomber.devmode.text;

import lombok.experimental.UtilityClass;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class Text {
    public MutableComponent literal(String content, ChatFormatting ...formatting) {
        return Component.literal(content).withStyle(formatting);
    }

    public MutableComponent join(List<? extends Component> children, Component separator) {
        var result = Component.empty();

        for (int i = 0; i < children.size(); i++) {
            var child = children.get(i);
            result.append(child);

            if (i < children.size() - 1) {
                result.append(separator);
            }
        }

        return result;
    }

    public MutableComponent append(Component ...children) {
        return Arrays.stream(children).map(Component::copy).reduce(Component.empty(), MutableComponent::append);
    }

    public MutableComponent format(String fmt, Component ...arguments) {
        return Component.translatableWithFallback("", fmt, (Object[]) arguments);
    }
}
