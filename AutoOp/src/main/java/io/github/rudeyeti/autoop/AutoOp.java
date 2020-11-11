package io.github.rudeyeti.autoop;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

public class AutoOp extends PluginBase {
    @Override
    public void onLoad() {
        this.getLogger().info(TextFormat.WHITE + "\n\n┏━━━━┓  ┏━┓ ┏━┓ ┏━━━━━┓ ┏━┓     ┏━━━━━━┓     ┏━━━━━━━┓ ┏━┓ ┏━┓ ┏━━━━━┓     ┏━━━━━┓ ┏━━━━━┓ ┏━━━━━┓ ┏━━━━━━━┓ ┏━┓ ┏━┓\n" +
                                                     "┃ ┏┓ ┃  ┃ ┃ ┃ ┃ ┗━┓ ┏━┛ ┃ ┃     ┗┓ ┏━┓ ┃     ┃ ┏┓ ┏┓ ┃ ┃ ┃ ┃ ┃ ┃ ┏━━━┛     ┃ ┏━━━┛ ┃ ┏━┓ ┃ ┃ ┏━┓ ┃ ┃ ┏┓ ┏┓ ┃ ┃ ┃ ┃ ┃\n" +
                                                     "┃ ┗┛ ┗┓ ┃ ┃ ┃ ┃   ┃ ┃   ┃ ┃      ┃ ┃ ┃ ┃     ┗━┛┃ ┃┗━┛ ┃ ┗━┛ ┃ ┃ ┗━━━┓     ┃ ┗━━━┓ ┃ ┃ ┃ ┃ ┃ ┗━┛ ┃ ┗━┛┃ ┃┗━┛ ┃ ┗━┛ ┃\n" +
                                                     "┃ ┏━┓ ┃ ┃ ┃ ┃ ┃   ┃ ┃   ┃ ┃ ┏━┓  ┃ ┃ ┃ ┃        ┃ ┃    ┃ ┏━┓ ┃ ┃ ┏━━━┛     ┃ ┏━━━┛ ┃ ┗━┛ ┃ ┃ ┏┓ ┏┛    ┃ ┃    ┃ ┏━┓ ┃\n" +
                                                     "┃ ┗━┛ ┃ ┃ ┗━┛ ┃ ┏━┛ ┗━┓ ┃ ┗━┛ ┃ ┏┛ ┗━┛ ┃        ┃ ┃    ┃ ┃ ┃ ┃ ┃ ┗━━━┓     ┃ ┗━━━┓ ┃ ┏━┓ ┃ ┃ ┃┃ ┗┓    ┃ ┃    ┃ ┃ ┃ ┃\n" +
                                                     "┗━━━━━┛ ┗━━━━━┛ ┗━━━━━┛ ┗━━━━━┛ ┗━━━━━━┛        ┗━┛    ┗━┛ ┗━┛ ┗━━━━━┛     ┗━━━━━┛ ┗━┛ ┗━┛ ┗━┛┗━━┛    ┗━┛    ┗━┛ ┗━┛\n");
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }
}