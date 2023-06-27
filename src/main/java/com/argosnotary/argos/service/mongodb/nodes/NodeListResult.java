package com.argosnotary.argos.service.mongodb.nodes;

import java.util.List;

import com.argosnotary.argos.domain.nodes.Node;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NodeListResult {
	
	private List<Node> nodelist;

}
