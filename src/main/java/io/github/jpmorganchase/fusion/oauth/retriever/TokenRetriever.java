package io.github.jpmorganchase.fusion.oauth.retriever;

import io.github.jpmorganchase.fusion.oauth.credential.Credentials;
import io.github.jpmorganchase.fusion.oauth.model.BearerToken;

public interface TokenRetriever {
    BearerToken retrieve(Credentials credentials);
}
