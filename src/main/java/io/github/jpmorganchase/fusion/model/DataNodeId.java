package io.github.jpmorganchase.fusion.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class DataNodeId {

    String id;
    String name;
    String dataNodeType;
}
