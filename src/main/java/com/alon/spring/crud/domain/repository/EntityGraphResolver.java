package com.alon.spring.crud.domain.repository;

import com.alon.spring.crud.domain.model.BaseEntity;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EntityGraphResolver {

    private EntityGraphResolver() {}

    public static EntityGraph resolveExpand(EntityManager entityManager,
                      Class<? extends BaseEntity> entityType, List<String> expand) {

        EntityGraph graph = entityManager.createEntityGraph(entityType);

        expand.stream()
                .filter(property -> !property.contains("."))
                .forEach(property -> graph.addAttributeNodes(property));

        expand.stream()
                .filter(property -> property.contains("."))
                .map(property -> property.split("\\."))
                .map(Stream::of)
                .map(stream -> stream.collect(Collectors.toList()))
                .forEach(expandComposition -> resolveCompoundExpand(graph, expandComposition));

        return graph;
    }

    private static void resolveCompoundExpand(EntityGraph graph, List<String> expandComposition) {
        Subgraph subgraph = graph.addSubgraph(expandComposition.remove(0));
        resolveExpandSubProperty(subgraph, expandComposition);
    }

    private static void resolveExpandSubProperty(Subgraph parentGraph, List<String> expandComposition) {
        if (expandComposition.size() > 1) {
            Subgraph subgraph = parentGraph.addSubgraph(expandComposition.remove(0));
            resolveExpandSubProperty(subgraph, expandComposition);
        } else {
            parentGraph.addAttributeNodes(expandComposition.get(0));
        }
    }
}
