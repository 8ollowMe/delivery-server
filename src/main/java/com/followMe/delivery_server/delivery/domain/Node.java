package com.followMe.delivery_server.delivery.domain;

import static lombok.AccessLevel.PROTECTED;

import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Node {
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NodeType nodeType;

  @Column(nullable = false, columnDefinition = "uuid")
  private UUID nodeId;

  @Column(nullable = false, length = 50)
  private String nodeName;
}
