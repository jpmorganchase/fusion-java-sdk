package io.github.jpmorganchase.fusion.model;

import java.util.List;
import lombok.*;

@Builder
@Value
@EqualsAndHashCode()
@ToString()
public class Flow {

    String flowDirection;
    String flowType;
    String startTime;
    String endTime;
    String timeZone;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Application producerApplicationId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Singular("consumerApplicationId")
    List<Application> consumerApplicationId;
}
