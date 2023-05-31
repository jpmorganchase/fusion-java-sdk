package io.github.jpmorganchase.fusion.api.context;

import com.google.gson.annotations.Expose;
import io.github.jpmorganchase.fusion.api.response.UploadedPart;
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
    int partNo;

    @Expose
    UploadedPart part;
}
