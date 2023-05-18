package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode
@ToString
@Builder
public class UploadedPartContext {

    byte[] digest;
    int partCount;
    @Expose
    UploadedPart parts;

}
