package org.gtagency.autotetris.bot;

import org.gtagency.autotetris.field.Field;
import org.gtagency.autotetris.bot.BotStarter.Node;
import org.gtagency.autotetris.bot.BotState;

public interface Utility {
    double value(Field field, Node firstMove, Node secondMove, BotState state, int par);
}