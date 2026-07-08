/**
* This script step makes node labels unique within the requisition.
* Nodes are kept as-is (the foreign ID stays the unique key); when two or more
* nodes share the same label, the first keeps it and each subsequent duplicate
* gets a numeric suffix: label, label_1, label_2, ...
* This is useful when a source (for example xls/csv) produces distinct nodes
* that share a node label because they differ only by their foreign ID.
*/

import org.opennms.pris.model.Requisition
import org.opennms.pris.model.RequisitionNode

logger.info("starting deduplicateNodeLabels.groovy")

// how many times we have already emitted each label
Map<String, Integer> seen = new HashMap<>()

for (RequisitionNode node : requisition.getNodes()) {
    String label = node.getNodeLabel()
    Integer count = seen.get(label)

    if (count == null) {
        // first time we see this label - leave it untouched
        seen.put(label, 1)
    } else {
        // duplicate - append _<n>, skipping any suffix that would itself collide
        String candidate
        do {
            candidate = label + "_" + count
            count++
        } while (seen.containsKey(candidate))

        logger.info("renaming duplicate label '{}' (foreign ID '{}') to '{}'",
                    label, node.getForeignId(), candidate)
        node.setNodeLabel(candidate)

        seen.put(label, count)
        seen.put(candidate, 1)
    }
}

logger.info("done with deduplicateNodeLabels.groovy")
return requisition
