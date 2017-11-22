/*
 *  Copyright 2017 Budapest University of Technology and Economics
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hu.bme.mit.theta.formalism.sts.aiger;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import hu.bme.mit.theta.formalism.sts.aiger.elements.AigerNode;
import hu.bme.mit.theta.formalism.sts.aiger.elements.AigerSystem;
import hu.bme.mit.theta.formalism.sts.aiger.elements.AigerWire;

public final class AigerCoi {

	private AigerCoi() {
	}

	public static void apply(final AigerSystem system) {
		final Set<AigerNode> reachable = getReachableNodes(system);
		pruneUnreachableNodes(system, reachable);
	}

	private static Set<AigerNode> getReachableNodes(final AigerSystem system) {
		final Set<AigerNode> reached = new HashSet<>();
		final Queue<AigerNode> queue = new ArrayDeque<>();
		queue.add(system.getOutput());

		while (!queue.isEmpty()) {
			final AigerNode node = queue.remove();
			if (!reached.contains(node)) {
				reached.add(node);
				node.getInWires().forEach(w -> queue.add(w.getSource()));
			}
		}
		reached.remove(system.getOutput());
		return reached;
	}

	private static void pruneUnreachableNodes(final AigerSystem system, final Set<AigerNode> reachable) {
		system.getNodes().clear();
		system.getNodes().addAll(reachable);
		final Collection<AigerWire> wiresToRemove = new LinkedList<>();
		for (final AigerNode node : system.getNodes()) {
			for (final AigerWire wire : node.getOutWires()) {
				if (!reachable.contains(wire.getTarget())) {
					wiresToRemove.add(wire);
				}
			}
		}
		wiresToRemove.forEach(w -> w.getSource().removeOutWire(w));
	}
}