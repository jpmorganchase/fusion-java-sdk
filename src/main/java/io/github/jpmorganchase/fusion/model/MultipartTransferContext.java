package io.github.jpmorganchase.fusion.model;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MultipartTransferContext {

    Operation operation;
    MultipartTransferStatus status;

    public static MultipartTransferContext started(Operation operation) {
        return new MultipartTransferContext(operation, MultipartTransferStatus.INITIATED);
    }

    public static MultipartTransferContext error() {
        return new MultipartTransferContext(null, MultipartTransferStatus.IN_ERROR);
    }

    public boolean canProceedToTransfer(){
        return MultipartTransferStatus.INITIATED.equals(status);
    }

    public enum MultipartTransferStatus {
        INITIATED, IN_PROGRESS, IN_ERROR, COMPLETED, ABORTED;
    }
}
