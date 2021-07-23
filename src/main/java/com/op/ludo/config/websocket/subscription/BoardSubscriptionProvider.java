package com.op.ludo.config.websocket.subscription;

import com.op.ludo.model.BoardState;
import com.op.ludo.service.LobbyService;
import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardSubscriptionProvider implements SubscriptionProvider {

  private static final String boardTopicPattern = "\\/topic\\/game\\/(?<boardId>[0-9]{8})";
  private final LobbyService lobbyService;

  public BoardSubscriptionProvider(LobbyService lobbyService) {
    this.lobbyService = lobbyService;
  }

  @Override
  public boolean handles(String destination) {
    return Pattern.matches(boardTopicPattern, destination);
  }

  @Override
  public boolean hasPermission(Principal principal, String destination) {
    if (principal == null) return false;
    if (handles(destination)) {
      Pattern pattern = Pattern.compile(boardTopicPattern);
      Matcher matcher = pattern.matcher(destination);

      if (matcher.matches() && matcher.group("boardId") != null) {
        Long boardId = Long.valueOf(matcher.group("boardId"));
        BoardState board = lobbyService.getCurrentActiveGame(principal.getName());
        return board != null && board.getBoardId().equals(boardId);
      }
    }
    return false;
  }
}
