package io.github.jpmorganchase.fusion.api.context;

import static io.github.jpmorganchase.fusion.api.context.MultipartTransferContext.MultipartTransferStatus.INITIATED;
import static io.github.jpmorganchase.fusion.api.context.MultipartTransferContext.MultipartTransferStatus.TRANSFERRED;

import io.github.jpmorganchase.fusion.api.response.UploadedPart;
import io.github.jpmorganchase.fusion.api.response.UploadedParts;
import io.github.jpmorganchase.fusion.model.Operation;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MultipartTransferContext {

    Operation operation;
    MultipartTransferStatus status;
    final List<UploadedPartContext> parts;
    int chunkSize;
    long totalBytes;
    int totalPartsCount;

    public static MultipartTransferContext started(Operation operation) {
        return new MultipartTransferContext(operation, MultipartTransferStatus.INITIATED, new ArrayList<>(), 0, 0, 0);
    }

    public MultipartTransferContext transferred(int chunkSize, long totalBytes, int totalPartsCount) {
        this.parts.sort(Comparator.comparingInt(UploadedPartContext::getPartNo));
        this.status = TRANSFERRED;
        this.chunkSize = chunkSize;
        this.totalBytes = totalBytes;
        this.totalPartsCount = totalPartsCount;
        return this;
    }

    public MultipartTransferContext completed() {
        this.status = MultipartTransferStatus.COMPLETED;
        return this;
    }

    public MultipartTransferContext aborted() {
        this.status = MultipartTransferStatus.ABORTED;
        return this;
    }

    public void partUploaded(UploadedPartContext partCtx) {
        if (!MultipartTransferStatus.IN_PROGRESS.equals(this.status)) {
            this.status = MultipartTransferStatus.IN_PROGRESS;
        }
        synchronized (this.parts) {
            this.parts.add(partCtx);
        }
    }

    public List<ByteBuffer> digests() {

        List<ByteBuffer> digests = new ArrayList<>();
        for (UploadedPartContext part : parts) {
            digests.add(ByteBuffer.wrap(part.getDigest()));
        }

        return Collections.unmodifiableList(digests);
    }

    public boolean canProceedToComplete() {
        return (this.status == TRANSFERRED) && (parts.size() > 0);
    }

    public boolean canProceedToTransfer() {
        return this.status == INITIATED;
    }

    public UploadedParts uploadedParts() {

        List<UploadedPart> uploadedParts = new ArrayList<>();
        for (UploadedPartContext ctx : parts) {
            uploadedParts.add(ctx.getPart());
        }

        return UploadedParts.builder()
                .parts(Collections.unmodifiableList(uploadedParts))
                .build();
    }

    public enum MultipartTransferStatus {
        INITIATED,
        IN_PROGRESS,
        TRANSFERRED,
        COMPLETED,
        ABORTED;
    }
}
