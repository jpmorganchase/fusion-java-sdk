package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.FusionException;
import io.github.jpmorganchase.fusion.api.APICallException;
import lombok.*;

import java.nio.ByteBuffer;
import java.util.*;

import static io.github.jpmorganchase.fusion.model.MultipartTransferContext.MultipartTransferStatus.COMPLETED;
import static io.github.jpmorganchase.fusion.model.MultipartTransferContext.MultipartTransferStatus.TRANSFERRED;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MultipartTransferContext {

    Operation operation;
    MultipartTransferStatus status;
    List<UploadedPartContext> parts;
    int chunkSize;
    int totalBytes;
    int totalPartsCount;


    public static MultipartTransferContext started(Operation operation) {
        return new MultipartTransferContext(operation, MultipartTransferStatus.INITIATED, new ArrayList<>(), 0,0, 0);
    }

    public MultipartTransferContext inProgress(){
        this.status = MultipartTransferStatus.IN_PROGRESS;
        return this;
    }

    public MultipartTransferContext transferred(int chunkSize, int totalBytes, int totalPartsCount){
        this.parts.sort(Comparator.comparingInt(UploadedPartContext::getPartCount));
        this.status = TRANSFERRED;
        this.chunkSize = chunkSize;
        this.totalBytes = totalBytes;
        this.totalPartsCount = totalPartsCount;
        return this;
    }

    public MultipartTransferContext competed(){
        this.status = MultipartTransferStatus.COMPLETED;
        return this;
    }

    public MultipartTransferContext aborted(){
        this.status = MultipartTransferStatus.ABORTED;
        return this;
    }

    public void partUploaded(UploadedPartContext partCtx){
        if (!MultipartTransferStatus.IN_PROGRESS.equals(this.status)) {
            inProgress();
        }
        this.parts.add(partCtx);
    }

    public List<ByteBuffer> digests(){

        if (!TRANSFERRED.equals(this.status) && !COMPLETED.equals(this.status)) {
            throw new FusionException("Unable to complete operation, transfer of parts not yet complete");
        }

        List<ByteBuffer> digests = new ArrayList<>();
        for (UploadedPartContext part : parts){
            digests.add(ByteBuffer.wrap(part.getDigest()));
        }

        return Collections.unmodifiableList(digests);
    }

    public boolean canProceedToComplete(){
        //TODO : knighto - dependant on how we implement this; threaded etc, we can perform additional checks
        return TRANSFERRED.equals(this.status) && parts.size() > 0;
    }

    public boolean canProceedToTransfer(){
        return MultipartTransferStatus.INITIATED.equals(status);
    }

    public UploadedParts uploadedParts() {

        if (!TRANSFERRED.equals(this.status) && !COMPLETED.equals(this.status)) {
            throw new FusionException("Unable to complete operation, transfer of parts not yet complete");
        }

        List<UploadedPart> uploadedParts = new ArrayList<>();
        for (UploadedPartContext ctx : parts) {
            uploadedParts.add(ctx.getPart());
        }
        return UploadedParts.builder()
                .parts(Collections.unmodifiableList(uploadedParts))
                .build();

    }

    public enum MultipartTransferStatus {
        INITIATED, IN_PROGRESS, TRANSFERRED, IN_ERROR, COMPLETED, ABORTED;
    }
}
