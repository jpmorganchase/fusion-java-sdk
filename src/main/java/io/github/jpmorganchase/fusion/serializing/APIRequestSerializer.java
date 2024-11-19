package io.github.jpmorganchase.fusion.serializing;

import io.github.jpmorganchase.fusion.model.Dataset;

public interface APIRequestSerializer {

    String serializeDatasetRequest(Dataset dataset);

}
