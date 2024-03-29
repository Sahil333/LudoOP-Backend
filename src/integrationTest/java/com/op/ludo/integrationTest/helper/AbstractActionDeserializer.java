package com.op.ludo.integrationTest.helper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import com.op.ludo.game.action.AbstractAction;
import com.op.ludo.game.action.Action;
import com.op.ludo.game.action.impl.DiceRoll;
import com.op.ludo.game.action.impl.DiceRollPending;
import com.op.ludo.game.action.impl.DiceRollReq;
import com.op.ludo.game.action.impl.GameStarted;
import com.op.ludo.game.action.impl.StoneMove;
import com.op.ludo.game.action.impl.StoneMovePending;
import java.io.IOException;
import java.util.Objects;

public class AbstractActionDeserializer extends StdDeserializer<AbstractAction> {

    public AbstractActionDeserializer() {
        super(AbstractAction.class);
    }

    @Override
    public AbstractAction deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        TreeNode node = jsonParser.readValueAsTree();
        if (Objects.equals(
                ((TextNode) node.get("action")).textValue(), Action.STARTED.toString())) {
            return jsonParser.getCodec().treeToValue(node, GameStarted.class);
        } else if (Objects.equals(
                ((TextNode) node.get("action")).textValue(), Action.DICEROLLPENDING.toString())) {
            return jsonParser.getCodec().treeToValue(node, DiceRollPending.class);
        } else if (Objects.equals(
                ((TextNode) node.get("action")).textValue(), Action.STONEMOVE.toString())) {
            return jsonParser.getCodec().treeToValue(node, StoneMove.class);
        } else if (Objects.equals(
                ((TextNode) node.get("action")).textValue(), Action.STONEMOVEPENDING.toString())) {
            return jsonParser.getCodec().treeToValue(node, StoneMovePending.class);
        } else if (Objects.equals(
                ((TextNode) node.get("action")).textValue(), Action.DICEROLLREQ.toString())) {
            return jsonParser.getCodec().treeToValue(node, DiceRollReq.class);
        } else if (Objects.equals(
                ((TextNode) node.get("action")).textValue(), Action.DICEROLL.toString())) {
            return jsonParser.getCodec().treeToValue(node, DiceRoll.class);
        } else {
            throw new JsonParseException(
                    jsonParser,
                    "no matching concrete class found for AbstractAction="
                            + ((TextNode) node.get("action")).textValue());
        }
    }
}
