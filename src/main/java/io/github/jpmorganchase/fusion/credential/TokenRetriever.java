package io.github.jpmorganchase.fusion.credential;

public interface TokenRetriever {
    BearerToken retrieve(Credentials credentials);
}
